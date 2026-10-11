package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.LineEnd;
import io.github.linwancen.plugin.show.bean.LineInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Doc cache for the line-end painter when the lineEndCache option is off.
 * <p>
 * {@link com.intellij.openapi.editor.EditorLinePainter#getLineExtensions} runs on EDT while
 * painting the editor, and a first resolve there can initialize whole subsystems
 * (PSI resolve, injected language infrastructure, IntelliLang services) and freeze the IDE
 * for seconds. So it must NEVER resolve PSI on EDT, cached option or not.
 * <p>
 * Same contract as {@link RenderDocCache}: this cache only reads memory on EDT, computes on a
 * pooled thread in smart mode, then repaints the editors on EDT when the doc is ready
 * (placeholder: a miss paints nothing for a moment, then the repaint shows the doc).
 * <p>
 * Call {@link #getLineExtensions} from EDT only. Registered as a projectService in plugin.xml,
 * so entries can never outlive the project. Never construct it manually.
 */
public final class LineEndDocCache implements Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(LineEndDocCache.class);

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
     * Unlike {@link RenderDocCache} the keys here are plain strings (file path + line + text),
     * not weak tree nodes, so nothing collects them: editing produces new keys and the old ones
     * must be dropped by this sweep.
     */
    private static final long ENTRY_HARD_TTL_MS = 60_000L;
    /**
     * Hard cap on how long a marker may stay in {@link #pending} (safety net, same idea as
     * {@link RenderDocCache}: a missed remove must not hold a strong key until project close).
     */
    private static final long PENDING_HARD_TTL_MS = 60_000L;

    /**
     * Cache value. Contract: must never reference the key (the key is a plain string, and the
     * value is only a LineExtensionInfo: text + attributes from settings).
     */
    private static final class Entry {
        /**
         * null = computed absent (this line has no doc), not the same as "not computed yet".
         */
        @Nullable
        final LineExtensionInfo ext;
        final long time;

        Entry(@Nullable LineExtensionInfo ext, long time) {
            this.ext = ext;
            this.time = time;
        }
    }

    /**
     * file path + line number + line text -> doc. The line text is part of the key so an edit
     * naturally misses and recomputes; stale keys are dropped by {@link #sweep}.
     */
    private final Map<String, Entry> docs = new ConcurrentHashMap<>();
    /**
     * key -> compute start time, avoid duplicate computes (the "placeholder": a second paint of
     * the same miss does not start a second compute).
     */
    private final Map<String, Long> pending = new ConcurrentHashMap<>();
    /**
     * Next time {@link #sweep} may run; EDT only ({@link #getLineExtensions} is EDT only).
     */
    private long nextSweep;

    /**
     * Never blocks. Entry point for the line-end painter: hides the per-project service instance.
     *
     * @return cached extension (maybe null or stale while the first/refresh compute is running)
     */
    public static @Nullable Collection<LineExtensionInfo> getLineExtensions(@NotNull LineInfo info) {
        @NotNull Project project = info.project;
        if (project.isDisposed() || !info.file.isValid()) {
            return null;
        }
        @Nullable LineEndDocCache cache = project.getService(LineEndDocCache.class);
        if (cache == null) {
            // only possible when plugin.xml does not register the projectService
            if (SERVICE_MISSING_LOGGED.compareAndSet(false, true)) {
                LOG.warn("LineEndDocCache is not registered as a projectService in plugin.xml.");
            }
            return null;
        }
        return cache.render(info);
    }

    private @Nullable Collection<LineExtensionInfo> render(@NotNull LineInfo info) {
        long now = System.currentTimeMillis();
        sweep(now);
        @NotNull String key = key(info);
        @Nullable Entry entry = docs.get(key);
        if (entry != null && now - entry.time < ttl(entry.ext)) {
            return entry.ext == null ? null : Collections.singleton(entry.ext);
        }
        schedule(info, key, now);
        // stale value while refreshing, to avoid flicker
        return entry == null || entry.ext == null ? null : Collections.singleton(entry.ext);
    }

    private static @NotNull String key(@NotNull LineInfo info) {
        return info.file.getPath() + '\u0000' + info.lineNumber + '\u0000' + info.text;
    }

    private static long ttl(@Nullable LineExtensionInfo ext) {
        return ext == null ? TTL_EMPTY_DOC_MS : TTL_HAS_DOC_MS;
    }

    private void schedule(@NotNull LineInfo info, @NotNull String key, long now) {
        @Nullable Long since = pending.get(key);
        if (since != null && now - since < PENDING_TIMEOUT_MS) {
            return;
        }
        pending.put(key, now);
        try {
            ApplicationManager.getApplication().executeOnPooledThread(() ->
                    doCompute(info, key, now));
        } catch (Throwable e) {
            // conditional: do not drop a marker that a newer schedule has just put
            pending.remove(key, now);
            LOG.info("LineEndDocCache schedule catch Throwable but log to record.", e);
        }
    }

    /**
     * Pooled thread: safe to wait for smart mode here, it never blocks the EDT.
     *
     * @param token the value this schedule put into {@link #pending}; removes are conditional on
     *              it so a late task cannot clear a marker of a newer schedule
     */
    private void doCompute(@NotNull LineInfo info, @NotNull String key, long token) {
        @NotNull Project project = info.project;
        try {
            if (project.isDisposed()) {
                pending.remove(key, token);
                return;
            }
            DumbService.getInstance(project).runReadActionInSmartMode(() -> {
                @Nullable LineExtensionInfo result = computeSafe(info);
                ApplicationManager.getApplication().invokeLater(() ->
                        finish(project, info.file, key, result, token));
            });
        } catch (ProcessCanceledException e) {
            // aborted (PSI changed or indexing started), retry on next paint
            pending.remove(key, token);
        } catch (Throwable e) {
            pending.remove(key, token);
            LOG.info("LineEndDocCache compute catch Throwable but log to record.", e);
        }
    }

    /**
     * The whole doc resolve ({@link LineEnd#lineExt}) happens here, inside the read action on a
     * pooled thread. Only PCE is rethrown to abort and retry; anything else logs and counts as
     * absent so a broken line cannot start a compute storm on every paint.
     */
    private static @Nullable LineExtensionInfo computeSafe(@NotNull LineInfo info) {
        @NotNull Project project = info.project;
        if (project.isDisposed() || !info.file.isValid() || DumbService.isDumb(project)) {
            return null;
        }
        try {
            return LineEnd.lineExt(info);
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (Throwable e) {
            LOG.info("LineEndDocCache lineExt catch Throwable but log to record.", e);
            return null;
        }
    }

    /**
     * EDT: stores the result and re-stamps the entry, so a line that keeps being painted always
     * has a fresh timestamp and is never hit by {@link #sweep}.
     *
     * @param token the value this schedule put into {@link #pending}; the remove is conditional
     *              so a late task cannot clear a marker of a newer schedule
     */
    private void finish(@NotNull Project project, @NotNull VirtualFile file, @NotNull String key,
                        @Nullable LineExtensionInfo result, long token) {
        @Nullable Entry old = docs.put(key, new Entry(result, System.currentTimeMillis()));
        pending.remove(key, token);
        boolean changed = !Objects.equals(old == null ? null : old.ext, result);
        if (!changed) {
            // stable doc: avoid repaint churn on every recompute
            return;
        }
        if (project.isDisposed()) {
            // the common guard of every callback, callers need not check again
            return;
        }
        repaintEditors(project, file);
    }

    /**
     * The doc is ready but the editor already painted the placeholder: ask the editors showing
     * this file to paint again, the next paint is a cache hit.
     */
    private static void repaintEditors(@NotNull Project project, @NotNull VirtualFile file) {
        try {
            // getEditors(VirtualFile, Project) does not exist in the 2020.1 API
            @Nullable Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                return;
            }
            for (Editor editor : EditorFactory.getInstance().getEditors(document, project)) {
                editor.getContentComponent().repaint();
            }
        } catch (Throwable e) {
            LOG.info("LineEndDocCache repaintEditors catch Throwable but log to record.", e);
        }
    }

    /**
     * Safety net for both maps: drop doc entries and pending markers that outlived their hard
     * TTL, so nothing can stay around forever (stale key after an edit, missed remove branch,
     * stuck compute). Throttled by {@link #SWEEP_INTERVAL_MS}; call from EDT only.
     * <p>
     * Cheap: only entries older than the hard TTL are touched, and a painted line is re-stamped
     * by {@link #finish} far more often than that, so this never drops what is on screen.
     */
    private void sweep(long now) {
        if (now < nextSweep) {
            return;
        }
        nextSweep = now + SWEEP_INTERVAL_MS;
        docs.entrySet().removeIf(entry -> now - entry.getValue().time > ENTRY_HARD_TTL_MS);
        pending.entrySet().removeIf(since -> now - since.getValue() > PENDING_HARD_TTL_MS);
    }

    /**
     * Called by the platform when the project closes: release everything at once.
     * <p>
     * A compute still in flight may put one entry afterwards: it holds the last reference to
     * this instance, so the whole instance is collected together with the project once the task
     * ends. Its conditional pending.remove then targets a cleared map and is a no-op.
     */
    @Override
    public void dispose() {
        docs.clear();
        pending.clear();
    }
}
