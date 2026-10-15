package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderEx;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.CancellablePromise;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class PsiDocCache {

    public static final class CachedResult {
        public final String value;

        /**
         * true = the compute ended on a nested {@link PsiDocCache#getCache} that had not finished yet:
         * this null means "not computed yet", not "this element has no doc".
         * Such a result must never be read back as a final empty value, see getCache().
         */
        public final boolean notReady;

        public CachedResult(@Nullable String value) {
            this(value, false);
        }

        public CachedResult(@Nullable String value, boolean notReady) {
            this.value = value;
            this.notReady = notReady;
        }
    }

    public enum Kind {REF, RESOLVE}

    private static final Key<SettingsInfo> INFO_KEY = new Key<>(PsiDocCache.class.getName() + "-INFO");

    /**
     * Set by {@link #getCache} on the calling thread whenever it returns null because something
     * is not ready yet (first miss, stale value, a compute already scheduled) instead of a real
     * empty value. Read by {@link DocProvider#compute} after its compute function returned, on
     * the same thread, to tell "not computed" from "computed and there is no doc".
     */
    private static final ThreadLocal<Boolean> NESTED_MISS = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private static void markNestedMiss() {
        NESTED_MISS.set(Boolean.TRUE);
    }

    /**
     * Read and clear the "not ready" flag {@link #getCache} set on the calling thread.
     * <p>
     * The render caches ({@link RenderDocCache} / {@link LineEndDocCache}) call this right after
     * their compute supplier returned on the same pooled thread: a null result with this flag true
     * means "a nested compute was still running", not "this element has no doc", and one delayed
     * repaint is then worth scheduling so the view shows the doc right away instead of waiting for
     * the next external repaint (opening a file, moving the mouse over the tree, ...).
     */
    public static boolean consumeNotReadyFlag() {
        boolean notReady = Boolean.TRUE.equals(NESTED_MISS.get());
        NESTED_MISS.remove();
        return notReady;
    }

    private static final class DocProvider implements CachedValueProvider<CachedResult> {
        private final PsiElement element;
        private final BiFunction<SettingsInfo, PsiElement, String> compute;

        DocProvider(@NotNull PsiElement element,
                    @NotNull BiFunction<SettingsInfo, PsiElement, String> compute) {
            this.element = element;
            this.compute = compute;
        }

        @Override
        public @Nullable Result<CachedResult> compute() {
            SettingsInfo currentInfo = element.getUserData(INFO_KEY);
            if (currentInfo == null) {
                return null;
            }
            List<Object> deps = new ArrayList<>(5);
            deps.add(PsiModificationTracker.MODIFICATION_COUNT);
            deps.add(currentInfo.appSettings);
            deps.add(currentInfo.globalSettings);
            deps.add(currentInfo.projectSettings);
            if (!element.isPhysical()) {
                // PsiModificationTracker does not track non physical elements, so depend on the file
                // like CachedValuesManager.getCachedValue(PsiElement, ...) used to do for them
                PsiFile file = element.getContainingFile();
                if (file != null) {
                    deps.add(file);
                }
            }
            // stale flag from a previous user of this pooled thread
            NESTED_MISS.remove();
            try {
                String value = compute.apply(currentInfo, element);
                // refCache nests resolveCache: this compute called getCache() again for the
                // resolved element, which schedules and returns null the first time. Caching that
                // null as a real value would hide the doc forever (the real value is stored under
                // the resolved element, this one is never asked again), so mark it not ready.
                boolean notReady = value == null && Boolean.TRUE.equals(NESTED_MISS.get());
                return Result.create(new CachedResult(value, notReady), deps.toArray());
            } finally {
                NESTED_MISS.remove();
            }
        }
    }

    /**
     * Key.create() makes a new instance on every call, and Key equality is instance identity,
     * so a key created inside getCache() could never find the value stored by a previous call.
     * The key must be created once per name and reused, otherwise the cache always misses,
     * and every call spawns a new background read action doing the full resolve again.
     */
    private static final ConcurrentMap<String, Key<CachedValue<CachedResult>>> KEYS = new ConcurrentHashMap<>();

    public static @Nullable String getCache(
            @NotNull SettingsInfo info,
            @NotNull Kind kind,
            @NotNull PsiElement element,
            @NotNull BiFunction<SettingsInfo, PsiElement, String> compute) {

        String keyStr = PsiDocCache.class.getName() + "-" + kind.name() + "-" + info.funcEnum.name();
        Key<CachedValue<CachedResult>> cacheKey = KEYS.computeIfAbsent(keyStr, Key::create);

        CachedValue<CachedResult> cachedValue = element.getUserData(cacheKey);
        if (cachedValue != null) {
            // getUpToDateOrNull() does not recompute: a stale value must not be recomputed on the
            // caller thread, which may be EDT while painting. Stale falls through to the compute below.
            Getter<CachedResult> upToDate = cachedValue.getUpToDateOrNull();
            if (upToDate != null) {
                CachedResult result = upToDate.get();
                if (result != null && result.notReady) {
                    // the previous compute ended on a getCache() that had not finished yet, so this
                    // null is "not ready" and not "no doc". Dropping the whole CachedValue (not just
                    // its value) makes getCachedValue() below build a fresh one and run the compute
                    // again, otherwise this empty value would be returned on every paint forever.
                    element.putUserData(cacheKey, null);
                } else {
                    // a real value, including a real "this element has no doc"
                    return result == null ? null : result.value;
                }
            }
        }

        if (!markPending(element, keyStr)) {
            // compute already scheduled for this element and key
            markNestedMiss();
            return null;
        }
        try {
            element.putUserData(INFO_KEY, info);
            if (canComputeHere(info.project)) {
                // Already inside a background read action in smart mode (the pooled computes of
                // LineEndDocCache / RenderDocCache / the line end cache task). Compute here instead
                // of submitting a nested NonBlockingReadAction: one submission per painted line or
                // tree node was exactly what made the platform log
                // "Too many non blocking read actions submitted at once".
                @Nullable CachedResult here = computeHere(info.project, element, cacheKey, compute);
                clearPending(element, keyStr);
                if (here == null || here.notReady) {
                    // nested getCache inside this compute was not ready: same contract as below
                    markNestedMiss();
                    return null;
                }
                return here.value;
            }
            schedule(info.project, element, cacheKey, keyStr, compute);
        } catch (ProcessCanceledException e) {
            // a write action or indexing started: abort so the caller retries, never swallow it
            clearPending(element, keyStr);
            throw e;
        } catch (Throwable e) {
            clearPending(element, keyStr);
        }
        // null here always means not ready yet: scheduled, stale or already pending
        markNestedMiss();
        return null;
    }

    /**
     * true when a full compute can run right here on this thread: already inside a read action in
     * smart mode on a pooled thread.
     * <p>
     * Never on EDT (or on a thread without read access): a first resolve can initialize whole
     * subsystems and freeze the IDE for seconds, such callers must get the async schedule below.
     */
    private static boolean canComputeHere(@NotNull Project project) {
        @Nullable Application app = ApplicationManager.getApplication();
        if (app == null || app.isDispatchThread() || !app.isReadAccessAllowed()) {
            return false;
        }
        if (project.isDisposed()) {
            return false;
        }
        // DumbService.isDumb instead of isInSmartMode: exists on every IDE version since 201.1
        return !DumbService.isDumb(project);
    }

    private static @Nullable CachedResult computeHere(
            @NotNull Project project,
            @NotNull PsiElement element,
            @NotNull Key<CachedValue<CachedResult>> cacheKey,
            @NotNull BiFunction<SettingsInfo, PsiElement, String> compute) {
        if (!element.isValid()) {
            return null;
        }
        // The static getCachedValue(PsiElement, Key, provider) stopped storing a CachedValue under the
        // key: since 2024.x it stores a ParameterizedCachedValue, which only extends CachedValueBase
        // and is not a CachedValue, so getCache() above could never read it back and threw
        // ClassCastException. This overload stores a real CachedValue under the key on every IDE
        // version, which is exactly what getCache() reads.
        return CachedValuesManager.getManager(project)
                .getCachedValue(element, cacheKey, new DocProvider(element, compute), false);
    }

    /**
     * element -> keyStr set of computes already scheduled, so one repaint cannot start a compute storm.
     */
    private static final Key<Set<String>> PENDING_KEY = Key.create(PsiDocCache.class.getName() + "-PENDING");

    private static boolean markPending(@NotNull PsiElement element, @NotNull String keyStr) {
        Set<String> pending = element.getUserData(PENDING_KEY);
        if (pending == null) {
            Set<String> fresh = ConcurrentHashMap.newKeySet();
            pending = element instanceof UserDataHolderEx
                    ? ((UserDataHolderEx) element).putUserDataIfAbsent(PENDING_KEY, fresh)
                    : putPending(element, fresh);
        }
        return pending.add(keyStr);
    }

    private static @NotNull Set<String> putPending(@NotNull PsiElement element, @NotNull Set<String> fresh) {
        Set<String> old = element.getUserData(PENDING_KEY);
        if (old != null) {
            return old;
        }
        element.putUserData(PENDING_KEY, fresh);
        return fresh;
    }

    private static void clearPending(@NotNull PsiElement element, @NotNull String keyStr) {
        try {
            Set<String> pending = element.getUserData(PENDING_KEY);
            if (pending != null) {
                pending.remove(keyStr);
            }
        } catch (Throwable ignored) {
            // element may be invalid already, pending goes away with the element anyway
        }
    }

    /**
     * A resolve can block for a long time inside the read action, for example a JS/TS module resolve
     * blocks until the config graph is built. Give up after this time instead of pinning the read lock,
     * the next call retries.
     */
    private static final long TIMEOUT_MS = 10_000L;

    /**
     * Cap on how many non blocking read actions this plugin keeps submitted at once.
     * <p>
     * The platform logs {@code "Too many non blocking read actions submitted at once"} once too
     * many similar submissions are active, so the number of outstanding submissions is limited
     * here, not only the number of running threads. Callers get null (doc not ready) and the next
     * paint retries. 6 is far below the platform limit, and the common painters never reach this
     * path at all because they compute inline, see {@link #canComputeHere}.
     */
    private static final Semaphore SUBMISSIONS = new Semaphore(6, true);

    /**
     * NonBlockingReadAction instead of a bare pooled thread + runReadActionInSmartMode:
     * it is cancelled when a write action starts or the project is disposed, so it cannot
     * pin the read lock for long and freeze the EDT.
     */
    private static void schedule(@NotNull Project project,
                                 @NotNull PsiElement element,
                                 @NotNull Key<CachedValue<CachedResult>> cacheKey,
                                 @NotNull String keyStr,
                                 @NotNull BiFunction<SettingsInfo, PsiElement, String> compute) {
        if (!SUBMISSIONS.tryAcquire()) {
            // too many resolves already in flight: give up now, the next paint schedules again
            clearPending(element, keyStr);
            return;
        }
        boolean submitted = false;
        try {
            CancellablePromise<CachedResult> promise = ReadAction.nonBlocking(() ->
                            computeHere(project, element, cacheKey, compute))
                    .inSmartMode(project)
                    .expireWhen(project::isDisposed)
                    .submit(AppExecutorUtil.getAppExecutorService());
            submitted = true;

            // cancels e.g. a wait inside the JS config graph init, the read lock is released;
            // BaseLangDoc.refDoc detects the slow resolve itself and skips the next ones
            ScheduledFuture<?> watchdog = AppExecutorUtil.getAppScheduledExecutorService().schedule(
                    () -> {
                        if (!promise.isDone()) {
                            promise.cancel();
                        }
                    }, TIMEOUT_MS, TimeUnit.MILLISECONDS);

            promise.onProcessed(ignored -> {
                watchdog.cancel(false);
                clearPending(element, keyStr);
                SUBMISSIONS.release();
            });
        } finally {
            if (!submitted) {
                SUBMISSIONS.release();
                clearPending(element, keyStr);
            }
        }
    }
}
