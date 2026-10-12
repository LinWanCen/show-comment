package io.github.linwancen.plugin.show.tree;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.settings.AbstractSettingsState;
import io.github.linwancen.plugin.show.settings.GlobalSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RelFileDoc {
    private RelFileDoc() {}

    @Nullable
    public static String relFileDoc(@NotNull PsiElement element, @NotNull SettingsInfo info) {
        @NotNull ProjectSettingsState projectSettings = info.projectSettings;
        @NotNull GlobalSettingsState globalSettings = info.globalSettings;
        if (projectSettings.projectFilterEffective) {
            @Nullable String doc = relDoc(element, projectSettings);
            if (StringUtils.isNotBlank(doc)) {
                return doc;
            }
        }
        if (projectSettings.globalFilterEffective) {
            return relDoc(element, globalSettings);
        }
        return null;
    }

    @Nullable
    private static String relDoc(@NotNull PsiElement element, @NotNull AbstractSettingsState settings) {
        if (element instanceof PsiFile) {
            return FileDoc.fileDoc((PsiFile) element, settings);
        } else if (element instanceof PsiDirectory) {
            return DirDoc.dirDoc((PsiDirectory) element, settings);
        }
        return null;
    }
}
