package io.github.linwancen.plugin.show.java.line;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.lang.base.DocSkip;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

class SkipUtils {

    private SkipUtils() {}

    static boolean skipSign(PsiElement psiElement, AppSettingsState appSettings, ProjectSettingsState projectSettings) {
        String text = psiName(psiElement, appSettings);
        if (text == null) {
            return true;
        }
        return DocSkip.skipSign(appSettings, projectSettings, text);
    }

    private static @Nullable String psiName(@Nullable PsiElement psiElement, AppSettingsState appSettings) {
        if (psiElement instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) psiElement;
            if (appSettings.skipAnnotation && psiClass.isAnnotationType()) {
                return null;
            }
            return psiClass.getQualifiedName();
        } else if (psiElement instanceof PsiMember) {
            PsiMember psiMember = (PsiMember) psiElement;
            StringBuilder sb = new StringBuilder();
            PsiClass psiClass = psiMember.getContainingClass();
            if (psiClass != null) {
                String className = psiClass.getQualifiedName();
                if (className != null) {
                    sb.append(className);
                }
            }
            sb.append("#");
            String name = psiMember.getName();
            if (name != null) {
                sb.append(name);
            }
            return sb.toString();
        }
        return null;
    }

    static PsiDocComment skipDoc(PsiDocComment doc, AppSettingsState appSettings, ProjectSettingsState projectSettings) {
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

    private static boolean isBlank(PsiDocComment doc) {
        PsiElement[] elements = doc.getDescriptionElements();
        for (PsiElement element : elements) {
            String text = element.getText();
            if (StringUtils.isNotBlank(text)) {
                return false;
            }
        }
        return true;
    }
}
