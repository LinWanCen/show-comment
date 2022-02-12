package io.github.linwancen.plugin.comment.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import io.github.linwancen.plugin.comment.settings.AppSettingsState;
import io.github.linwancen.plugin.comment.settings.ProjectSettingsState;

public class SkipUtils {

    private SkipUtils() {}

    public static boolean skip(PsiClass psiClass, Project project) {
        if (psiClass == null) {
            return true;
        }
        String name = psiClass.getQualifiedName();
        if (name == null) {
            return true;
        }
        ProjectSettingsState projectSettings = ProjectSettingsState.getInstance(project);
        AppSettingsState appSettings = AppSettingsState.getInstance();
        if (projectSettings.globalFilterEffective
                && skipName(name, appSettings.lineEndIncludeArray, appSettings.lineEndExcludeArray)) {
            return true;
        }
        if (projectSettings.projectFilterEffective) {
            return skipName(name, projectSettings.lineEndIncludeArray, projectSettings.lineEndExcludeArray);
        }
        return false;
    }

    protected static boolean skipName(String name, String[] includeArray, String[] excludeArray) {
        if (exclude(name, excludeArray)) {
            return true;
        }
        return !include(name, includeArray);
    }

    protected static boolean include(String name, String[] lineEndIncludeArray) {
        if (lineEndIncludeArray.length == 0) {
            return true;
        }
        return exclude(name, lineEndIncludeArray);
    }

    protected static boolean exclude(String name, String[] projectLineEndExcludeArray) {
        for (String s : projectLineEndExcludeArray) {
            if (name.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
