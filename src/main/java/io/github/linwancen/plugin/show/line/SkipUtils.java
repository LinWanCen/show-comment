package io.github.linwancen.plugin.show.line;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

class SkipUtils {

    private SkipUtils() {}

    static boolean skipSign(PsiElement psiElement, AppSettingsState appSettings, ProjectSettingsState projectSettings) {
        if (psiElement == null) {
            return true;
        }
        String text;
        if (psiElement instanceof PsiClass) {
            text = ((PsiClass) psiElement).getQualifiedName();
        } else if (psiElement instanceof PsiMember) {
            PsiMember psiMember = (PsiMember) psiElement;
            text = psiMember.getContainingClass() + "." + psiMember.getName();
        } else {
            return true;
        }
        return skipText(text,
                projectSettings.globalFilterEffective, appSettings.lineInclude, appSettings.lineExclude,
                projectSettings.projectFilterEffective, projectSettings.lineInclude, projectSettings.lineExclude);
    }

    public static final Pattern NOT_ASCII_PATTERN = Pattern.compile("[^\u0000-\u007f]");

    static PsiDocComment skipDoc(PsiDocComment doc, AppSettingsState appSettings, ProjectSettingsState projectSettings) {
        if (doc == null) {
            return null;
        }
        if (appSettings.skipBlank && isBlank(doc)) {
            return null;
        }
        String text = doc.getText();
        if (appSettings.skipAscii && !NOT_ASCII_PATTERN.matcher(text).find()) {
            return null;
        }
        boolean skip = skipText(text,
                projectSettings.globalFilterEffective, appSettings.docInclude, appSettings.docExclude,
                projectSettings.projectFilterEffective, projectSettings.docInclude, projectSettings.docExclude);
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

    private static boolean skipText(String text,
                                    boolean appFilterEffective, Pattern appDocInclude, Pattern appDocExclude,
                                    boolean projectFilterEffective, Pattern projectDocInclude, Pattern projectDocExclude
    ) {
        if (text == null) {
            return true;
        }
        if (appFilterEffective
                && skipText(text, appDocInclude, appDocExclude)) {
            return true;
        }
        if (projectFilterEffective) {
            return skipText(text, projectDocInclude, projectDocExclude);
        }
        return false;
    }

    static boolean skipText(String text, Pattern include, Pattern exclude) {
        if (exclude(text, exclude)) {
            return true;
        }
        return !include(text, include);
    }

    static boolean include(String text, Pattern include) {
        if (include.pattern().length() == 0) {
            return true;
        }
        return include.matcher(text).find();
    }

    static boolean exclude(String text, Pattern exclude) {
        if (exclude.pattern().length() == 0) {
            return false;
        }
        return exclude.matcher(text).find();
    }
}
