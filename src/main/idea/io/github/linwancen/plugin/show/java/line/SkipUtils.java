package io.github.linwancen.plugin.show.java.line;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.lang.base.DocSkip;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class SkipUtils {

    private SkipUtils() {}

    static boolean skipSign(PsiElement psiElement, @NotNull AppSettingsState appSettings,
                            @NotNull ProjectSettingsState projectSettings) {
        @Nullable String text = psiName(psiElement, appSettings);
        if (text == null) {
            return true;
        }
        return DocSkip.skipSign(appSettings, projectSettings, text);
    }

    private static @Nullable String psiName(@Nullable PsiElement psiElement, @NotNull AppSettingsState appSettings) {
        if (psiElement instanceof PsiClass) {
            @NotNull PsiClass psiClass = (PsiClass) psiElement;
            if (appSettings.skipAnnotation && psiClass.isAnnotationType()) {
                return null;
            }
            return psiClass.getQualifiedName();
        } else if (psiElement instanceof PsiMember) {
            @NotNull PsiMember psiMember = (PsiMember) psiElement;
            @NotNull StringBuilder sb = new StringBuilder();
            @Nullable PsiClass psiClass = psiMember.getContainingClass();
            if (psiClass != null) {
                @Nullable String className = psiClass.getQualifiedName();
                if (className != null) {
                    sb.append(className);
                }
            }
            sb.append("#");
            @Nullable String name = psiMember.getName();
            if (name != null) {
                sb.append(name);
            }
            return sb.toString();
        }
        return null;
    }

    static PsiDocComment skipDoc(@Nullable PsiDocComment doc, @NotNull AppSettingsState appSettings,
                                 @NotNull ProjectSettingsState projectSettings) {
        if (doc == null) {
            return null;
        }
        if (appSettings.skipBlank && isBlank(doc)) {
            return null;
        }
        String text = doc.getText();
        boolean skip = DocSkip.skipDoc(appSettings, projectSettings, text);
        return skip ? null : doc;
    }

    private static boolean isBlank(@NotNull PsiDocComment doc) {
        @NotNull PsiElement[] elements = doc.getDescriptionElements();
        for (@NotNull PsiElement element : elements) {
            String text = element.getText();
            if (StringUtils.isNotBlank(text)) {
                return false;
            }
        }
        return true;
    }
}
