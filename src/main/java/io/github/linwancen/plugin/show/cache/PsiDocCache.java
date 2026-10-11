package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.application.ReadAction;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class PsiDocCache {

    public static final class CachedResult {
        public final String value;

        public CachedResult(@Nullable String value) {
            this.value = value;
        }
    }

    public enum Kind {REF, RESOLVE}

    private static final Key<SettingsInfo> INFO_KEY = new Key<>(PsiDocCache.class.getName() + "-INFO");

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
            return Result.create(new CachedResult(compute.apply(currentInfo, element)), deps.toArray());
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
            // caller thread, which may be EDT while painting. Stale falls through to the schedule below.
            Getter<CachedResult> upToDate = cachedValue.getUpToDateOrNull();
            if (upToDate != null) {
                CachedResult result = upToDate.get();
                return result == null ? null : result.value;
            }
        }

        if (!markPending(element, keyStr)) {
            // compute already scheduled for this element and key
            return null;
        }
        try {
            element.putUserData(INFO_KEY, info);
            schedule(info.project, element, cacheKey, keyStr, compute);
        } catch (Throwable e) {
            clearPending(element, keyStr);
        }
        return null;
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
     * NonBlockingReadAction instead of a bare pooled thread + runReadActionInSmartMode:
     * it is cancelled when a write action starts or the project is disposed, so it cannot
     * pin the read lock for long and freeze the EDT.
     */
    private static void schedule(@NotNull Project project,
                                 @NotNull PsiElement element,
                                 @NotNull Key<CachedValue<CachedResult>> cacheKey,
                                 @NotNull String keyStr,
                                 @NotNull BiFunction<SettingsInfo, PsiElement, String> compute) {
        Callable<CachedResult> task = () -> {
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
        };
        CancellablePromise<CachedResult> promise = ReadAction.nonBlocking(task)
                .inSmartMode(project)
                .expireWhen(project::isDisposed)
                .submit(AppExecutorUtil.getAppExecutorService());

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
        });
    }
}
