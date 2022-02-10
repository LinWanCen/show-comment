package io.github.linwancen.plugin.comment.utils;

import com.intellij.psi.PsiClass;

public class SkipUtils {

    private SkipUtils() {}

    public static boolean skip(PsiClass psiClass) {
        if (psiClass == null) {
            return true;
        }
        String name = psiClass.getQualifiedName();
        return name == null || name.startsWith("java");
    }
}
