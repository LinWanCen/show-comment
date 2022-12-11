package io.github.linwancen.plugin.show.java.line;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.bean.FuncEnum;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.DocSkip;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class SkipUtils {

    private SkipUtils() {}

    static <T extends SettingsInfo> boolean skipSign(@NotNull T settingsInfo, PsiElement psiElement) {
        @Nullable String text = psiName(settingsInfo, psiElement);
        if (text == null) {
            return true;
        }
        return DocSkip.skipSign(settingsInfo, text);
    }

    @Nullable
    private static <T extends SettingsInfo> String psiName(@NotNull T settingsInfo, @Nullable PsiElement psiElement) {
        if (psiElement instanceof PsiClass) {
            @NotNull PsiClass psiClass = (PsiClass) psiElement;
            if (settingsInfo.funcEnum == FuncEnum.LINE
                    && settingsInfo.appSettings.skipAnnotation && psiClass.isAnnotationType()) {
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

    @Nullable
    static <T extends SettingsInfo> PsiDocComment skipDoc(@NotNull T settingsInfo, @Nullable PsiDocComment doc) {
        if (doc == null) {
            return null;
        }
        if (settingsInfo.appSettings.skipBlank && isBlank(doc)) {
            return null;
        }
        String text = doc.getText();
        boolean skip = DocSkip.skipDoc(settingsInfo, text);
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
