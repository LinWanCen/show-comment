package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            return Result.create(
                    new CachedResult(compute.apply(currentInfo, element)),
                    PsiModificationTracker.MODIFICATION_COUNT,
                    currentInfo.appSettings,
                    currentInfo.globalSettings,
                    currentInfo.projectSettings
            );
        }
    }

    public static @Nullable String getCache(
            @NotNull SettingsInfo info,
            @NotNull Kind kind,
            @NotNull PsiElement element,
            @NotNull BiFunction<SettingsInfo, PsiElement, String> compute) {

        String keyStr = PsiDocCache.class.getName() + "-" + kind.name() + "-" + info.funcEnum.name();
        Key<CachedValue<CachedResult>> cacheKey = Key.create(keyStr);

        CachedValue<CachedResult> cachedValue = element.getUserData(cacheKey);

        if (cachedValue != null) {
            CachedResult result = cachedValue.getValue();
            return result.value;
        }

        element.putUserData(INFO_KEY, info);

        ApplicationManager.getApplication().executeOnPooledThread(() ->
                DumbService.getInstance(info.project).runReadActionInSmartMode(() -> {
                    if (!element.isValid()) return;
                    CachedValuesManager.getCachedValue(element, cacheKey, new DocProvider(element, compute));
                })
        );

        return null;
    }
}
