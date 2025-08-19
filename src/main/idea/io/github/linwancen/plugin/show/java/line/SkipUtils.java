package io.github.linwancen.plugin.show.java.line;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.bean.FuncEnum;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.DocSkip;
import io.github.linwancen.plugin.show.lang.base.PsiUnSaveUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkipUtils {

    private SkipUtils() {}

    public static <T extends SettingsInfo> boolean skipSign(@NotNull T info, PsiElement psiElement) {
        @Nullable String text = psiName(info, psiElement);
        if (text == null) {
            return false;
        }
        return DocSkip.skipSign(info, text);
    }

    @Nullable
    private static <T extends SettingsInfo> String psiName(@NotNull T info, @Nullable PsiElement psiElement) {
        if (psiElement instanceof PsiClass) {
            @NotNull PsiClass psiClass = (PsiClass) psiElement;
            if (info.funcEnum == FuncEnum.LINE
                    && info.appSettings.skipAnnotation && psiClass.isAnnotationType()) {
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
    static <T extends SettingsInfo> PsiDocComment skipDoc(@NotNull T info, @Nullable PsiDocComment doc) {
        if (doc == null) {
            return null;
        }
        if (info.appSettings.skipBlank && isBlank(doc)) {
            return null;
        }
        String text = PsiUnSaveUtils.getText(doc);
        boolean skip = DocSkip.skipDoc(info, text);
        return skip ? null : doc;
    }

    private static boolean isBlank(@NotNull PsiDocComment doc) {
        @NotNull PsiElement[] elements = doc.getDescriptionElements();
        for (@NotNull PsiElement element : elements) {
            String text = PsiUnSaveUtils.getText(element);
            if (StringUtils.isNotBlank(text)) {
                return false;
            }
        }
        return true;
    }
}
