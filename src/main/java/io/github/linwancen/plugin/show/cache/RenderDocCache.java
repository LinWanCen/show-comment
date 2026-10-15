package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Doc cache for render paths (TreeCellRenderer, ProjectViewNodeDecorator).
 * <p>
 * Lifecycle: registered as a project service (plugin.xml), so there is exactly one instance per
 * project. The platform creates it lazily on the first {@link #doc} call and disposes it
 * ({@link #dispose()}) when the project closes, so entries can never outlive the project.
 * Never construct it manually: a manual instance would never be disposed and would mix keys of
 * several projects in one map.
 * <p>
 * Those run on EDT while painting/laying out (some of them hold the AWTTreeLock),
 * so they must NEVER block: no runReadAction, no runReadActionInSmartMode, no waitForSmartMode.
 * A blocking call there can deadlock against a background write action that is waiting for the EDT
 * (for example a VFS refresh in RefreshSessionImpl.invokeOnEdt) and freeze the whole IDE.
 * <p>
 * This cache only reads memory on EDT and computes on a pooled thread in smart mode,
 * then repaints on EDT when the doc is ready.
 * <p>
 * Call {@link #doc} from EDT only.
 */
public final class RenderDocCache implements Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(RenderDocCache.class);

    /**
     * One-shot log for a missing plugin.xml registration, to avoid spamming on every paint.
     */
    private static final AtomicBoolean SERVICE_MISSING_LOGGED = new AtomicBoolean();

    /**
     * A doc found (or a doc proved absent) is reused for a while to avoid recompute on every paint.
     */
    private static final long TTL_HAS_DOC_MS = 5_000L;
    private static final long TTL_EMPTY_DOC_MS = 1_000L;
    /**
     * A pending compute should finish in time, otherwise allow a retry
     * (the task may be dropped on project dispose or by a ProcessCanceledException).
     */
    private static final long PENDING_TIMEOUT_MS = 30_000L;

    /**
     * How often {@link #sweep} may actually run (throttle, EDT only).
     */
    private static final long SWEEP_INTERVAL_MS = 10_000L;
    /**
     * Hard cap on how long a doc entry may stay in {@link #docs}.
     * <p>
     * {@link #ttl} only controls reuse, not eviction: an expired entry stays until its weak key
     * is collected, so a long-lived key would keep its value forever. {@link #finish} re-stamps
     * the entry on every recompute, so a node that is actually painted never reaches this age;
     * only entries nobody painted for this long are dropped (the next paint then behaves like a
     * normal cache miss and recomputes).
     */
    private static final long ENTRY_HARD_TTL_MS = 60_000L;
    /**
     * Hard cap on how long a marker may stay in {@link #pending}.
     * <p>
     * A marker is removed on every code path, but if one is ever missed (a dropped invokeLater,
     * a future refactor) the strong key reference would be held until the project closes. This
     * sweep is the safety net: a marker this old is either stuck or unreachable, and dropping it
     * only allows a duplicate compute.
     */
    private static final long PENDING_HARD_TTL_MS = 60_000L;

    /**
     * How long to wait before re-running {@code onComputed} once a compute ended on a nested doc
     * compute that was still running (see {@link PsiDocCache#consumeNotReadyFlag}).
     */
    private static final long NOT_READY_RETRY_DELAY_MS = 300L;

    /**
     * Cache value. See the contract on {@link #docs}: it may hold the doc text and its
     * timestamp, nothing that can reach back to the key (no PsiElement, node or lambda).
     */
    private static final class Entry {
        @Nullable
        final String doc;
        /**
         * true = the compute ended on a nested doc compute that had not finished yet, so this null
         * is "not computed yet" and not "no doc". Such an entry is recomputed on the next paint
         * instead of being served as a final empty value.
         */
        final boolean notReady;
        /**
         * true = the one delayed {@code onComputed} retry of this entry was already requested,
         * so a permanently not ready compute can never turn into a refresh loop.
         */
        final boolean retried;
        final long time;

        Entry(@Nullable String doc, boolean notReady, boolean retried, long time) {
            this.doc = doc;
            this.notReady = notReady;
            this.retried = retried;
            this.time = time;
        }
    }

    /**
     * Weak keys: tree nodes are dropped by GC after a tree rebuild, no leak.
     * Reads/writes happen on EDT (render + finish), {@link #dispose()} may run on the closing
     * thread, so every access is guarded by synchronized for safety.
     * <p>
     * Contract: a value ({@link Entry}) must never reference its key. If it did, the key would be
     * strongly reachable through the value and the weak key would never be collected, turning this
     * into a leak. Do not store PsiElement / node / lambda into {@link Entry}.
     */
    private final Map<Object, Entry> docs = new WeakHashMap<>();
    /**
     * key -> compute start time, avoid duplicate computes.
     * <p>
     * Keys are strong (unlike docs): every remove below is conditional
     * ({@code remove(key, token)}) so a late task cannot drop a newer marker, and {@link #sweep}
     * plus {@link #dispose()} guarantee the references are released even if a path is missed.
     */
    private final Map<Object, Long> pending = new ConcurrentHashMap<>();
    /**
     * Next time {@link #sweep} may run; EDT only ({@link #doc} is EDT only).
     */
    private long nextSweep;

    /**
     * Never blocks. Entry point for every render path: hides the per-project service instance,
     * so callers never hold a cache and never clean one up.
     *
     * @param onComputed called on EDT after the doc changed; never invoked when the project
     *                   is disposed (common guard lives here), so impls need not check again
     * @return cached doc (maybe null or stale while the first/refresh compute is running)
     */
    public static @Nullable String doc(@NotNull Project project, @NotNull Object key,
                                       @NotNull Supplier<String> compute, @NotNull Runnable onComputed) {
        if (project.isDisposed()) {
            return null;
        }
        @Nullable RenderDocCache cache = project.getService(RenderDocCache.class);
        if (cache == null) {
            // only possible when plugin.xml does not register the projectService
            if (SERVICE_MISSING_LOGGED.compareAndSet(false, true)) {
                LOG.warn("RenderDocCache is not registered as a projectService in plugin.xml.");
            }
            return null;
        }
        return cache.render(project, key, compute, onComputed);
    }

    private @Nullable String render(@NotNull Project project, @NotNull Object key,
                                    @NotNull Supplier<String> compute, @NotNull Runnable onComputed) {
        long now = System.currentTimeMillis();
        sweep(now);
        @Nullable Entry entry;
        synchronized (this) {
            entry = docs.get(key);
        }
        if (entry != null && !entry.notReady && now - entry.time < ttl(entry.doc)) {
            return entry.doc;
        }
        // a notReady entry is always recomputed: its null means "not computed yet", never "no doc"
        schedule(project, key, compute, onComputed, now);
        // stale value while refreshing, to avoid flicker
        return entry == null ? null : entry.doc;
    }

    /**
     * Called by the platform when the project closes: release everything at once, including the
     * strong keys in {@link #pending} ({@link #docs} uses weak keys, but no need to wait for GC).
     * <p>
     * A compute still in flight may put one entry afterwards: it holds the last reference to this
     * instance, so the whole instance is collected together with the project once the task ends.
     * Its conditional pending.remove then targets a cleared map and is a no-op.
     */
    @Override
    public void dispose() {
        pending.clear();
        synchronized (this) {
            docs.clear();
        }
    }

    private static long ttl(@Nullable String doc) {
        return doc == null || doc.isEmpty() ? TTL_EMPTY_DOC_MS : TTL_HAS_DOC_MS;
    }

    /**
     * Safety net for both maps: evict doc entries and pending markers that outlived their hard
     * TTL, so nothing can stay around forever (missed remove branch, long-lived key, stuck
     * compute). Throttled by {@link #SWEEP_INTERVAL_MS}; call from EDT only.
     * <p>
     * Cheap: only entries older than the hard TTL are touched, and a painted node is re-stamped
     * by {@link #finish} far more often than that, so this never drops what is on screen.
     */
    private void sweep(long now) {
        if (now < nextSweep) {
            return;
        }
        nextSweep = now + SWEEP_INTERVAL_MS;
        synchronized (this) {
            docs.entrySet().removeIf(entry -> now - entry.getValue().time > ENTRY_HARD_TTL_MS);
        }
        // ConcurrentHashMap: weakly consistent iteration, safe against pooled thread removes
        pending.entrySet().removeIf(since -> now - since.getValue() > PENDING_HARD_TTL_MS);
    }

    private void schedule(@NotNull Project project, @NotNull Object key,
                          @NotNull Supplier<String> compute, @NotNull Runnable onComputed, long now) {
        @Nullable Long since = pending.get(key);
        if (since != null && now - since < PENDING_TIMEOUT_MS) {
            return;
        }
        pending.put(key, now);
        try {
            ApplicationManager.getApplication().executeOnPooledThread(() ->
                    doCompute(project, key, compute, onComputed, now));
        } catch (Throwable e) {
            // conditional: do not drop a marker that a newer schedule has just put
            pending.remove(key, now);
            LOG.info("RenderDocCache schedule catch Throwable but log to record.", e);
        }
    }

    /**
     * Pooled thread: safe to wait for smart mode here, it never blocks the EDT.
     *
     * @param token the value this schedule put into {@link #pending}; removes are conditional on
     *              it so a late task cannot clear a marker of a newer schedule
     */
    private void doCompute(@NotNull Project project, @NotNull Object key,
                           @NotNull Supplier<String> compute, @NotNull Runnable onComputed, long token) {
        try {
            if (project.isDisposed()) {
                pending.remove(key, token);
                return;
            }
            DumbService.getInstance(project).runReadActionInSmartMode(() -> {
                // a pooled thread may still carry the flag of the task that used it before
                PsiDocCache.consumeNotReadyFlag();
                @Nullable String result = computeSafe(project, compute);
                // true = the compute ended on a nested doc compute that had not finished yet:
                // this null is "not computed yet" and not "no doc"
                boolean notReady = PsiDocCache.consumeNotReadyFlag();
                ApplicationManager.getApplication().invokeLater(() ->
                        finish(project, key, result, notReady, onComputed, token));
            });
        } catch (ProcessCanceledException e) {
            // aborted (PSI changed or indexing started), retry on next paint
            pending.remove(key, token);
        } catch (Throwable e) {
            pending.remove(key, token);
            LOG.info("RenderDocCache compute catch Throwable but log to record.", e);
        }
    }

    /**
     * The common part of every compute supplier: guards project disposed / dumb mode,
     * logs and returns null instead of throwing. Only PCE is rethrown to abort and retry.
     */
    private static String computeSafe(@NotNull Project project, @NotNull Supplier<String> compute) {
        if (project.isDisposed() || DumbService.isDumb(project)) {
            return null;
        }
        try {
            return compute.get();
        } catch (ProcessCanceledException ignore) {
            throw ignore;
        } catch (Throwable e) {
            LOG.info("RenderDocCache compute catch Throwable but log to record.", e);
            return null;
        }
    }

    /**
     * EDT: stores the result and re-stamps the entry, so a node that keeps being painted always
     * has a fresh timestamp and is never hit by {@link #sweep}.
     * <p>
     * A null result with {@code notReady} true does NOT mean "no doc": a nested doc compute
     * (usually the resolve cache under this doc) was still running. Without a follow up refresh the
     * view kept showing nothing until the next external repaint (opening a file, moving the mouse
     * over the tree ...), so the very first compute of a node asks for one more refresh.
     *
     * @param token the value this schedule put into {@link #pending}; the remove is conditional
     *              so a late task cannot clear a marker of a newer schedule
     */
    private void finish(@NotNull Project project, @NotNull Object key, @Nullable String result,
                        boolean notReady, @NotNull Runnable onComputed, long token) {
        boolean retry;
        boolean changed;
        synchronized (this) {
            @Nullable Entry old = docs.get(key);
            changed = !Objects.equals(old == null ? null : old.doc, result);
            // one delayed refresh per notReady episode: repeated not ready must not loop forever
            boolean retriedBefore = old != null && old.retried;
            retry = result == null && notReady && !retriedBefore;
            docs.put(key, new Entry(result, notReady, retry || (notReady && retriedBefore),
                    System.currentTimeMillis()));
        }
        pending.remove(key, token);
        if (!changed && !retry) {
            // stable doc: avoid repaint/refresh churn on every recompute
            return;
        }
        if (project.isDisposed()) {
            // the common guard of every onComputed, callbacks need not check again
            return;
        }
        if (retry) {
            // the nested compute has very likely finished by then: refresh once more so the node
            // shows its doc right away instead of waiting for the next repaint
            AppExecutorUtil.getAppScheduledExecutorService().schedule(() ->
                    ApplicationManager.getApplication().invokeLater(() -> runOnEdt(project, onComputed)),
                    NOT_READY_RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
            return;
        }
        runOnEdt(project, onComputed);
    }

    /**
     * EDT: the common guard and try/catch of every onComputed (refresh / repaint) call.
     */
    private static void runOnEdt(@NotNull Project project, @NotNull Runnable onComputed) {
        if (project.isDisposed()) {
            return;
        }
        try {
            onComputed.run();
        } catch (Throwable e) {
            LOG.info("RenderDocCache onComputed catch Throwable but log to record.", e);
        }
    }
}
