package io.github.linwancen.plugin.show.java.doc;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PsiClassUtils {

    private PsiClassUtils() {}

    private static final Pattern JSON_PATTERN = Pattern.compile("[\\w.]*+$");

    @NotNull
    public static PsiClass[] encClass(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        @NotNull String fileName = virtualFile.getNameWithoutExtension();
        @NotNull Matcher matcher = JSON_PATTERN.matcher(fileName);
        if (!matcher.find()) {
            return PsiClass.EMPTY_ARRAY;
        }
        String className = matcher.group();
        if (className == null) {
            return PsiClass.EMPTY_ARRAY;
        }
        @NotNull PsiClass[] psiClasses = nameToClass(className, project);
        if (psiClasses.length != 0) {
            return psiClasses;
        }
        // issue #23
        if (virtualFile.getExtension() == null || className.length() != fileName.length()) {
            return PsiClass.EMPTY_ARRAY;
        }
        @NotNull char[] chars = fileName.toCharArray();
        if (chars.length < 1 || chars[0] < 97 || 122 < chars[0]) {
            return PsiClass.EMPTY_ARRAY;
        }
        // Upper Case
        chars[0] -= 32;
        @NotNull String name = String.valueOf(chars);
        return nameToClass(name, project);
    }

    @NotNull
    public static PsiClass[] nameToClass(@Nullable String className, @NotNull Project project) {
        if (className == null) {
            return PsiClass.EMPTY_ARRAY;
        }
        int i = className.indexOf('.');
        return i > 0
                ? fullNameToClass(className, project)
                : simpleNameToClass(className, project);
    }

    @NotNull
    private static PsiClass[] simpleNameToClass(@NotNull String className, @NotNull Project project) {
        PsiShortNamesCache namesCache = PsiShortNamesCache.getInstance(project);
        return namesCache.getClassesByName(className, GlobalSearchScope.allScope(project));
    }

    @NotNull
    public static PsiClass[] fullNameToClass(@NotNull String className, @NotNull Project project) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        try {
            return javaPsiFacade.findClasses(className, GlobalSearchScope.allScope(project));
        } catch (Throwable e) {
            return PsiClass.EMPTY_ARRAY;
        }
    }

    @NotNull
    public static String toClassFullName(@NotNull PsiField psiField) {
        // Array
        // use replace simpler than getDeepComponentType()
        @NotNull String typeName = psiField.getType().getCanonicalText().replace("[]", "");
        // List
        // use substring() because clsFieldImpl.getInnermostComponentReferenceElement() == null
        int index = typeName.indexOf("<");
        if (index >= 0) {
            return typeName.substring(index + 1, typeName.length() - 1);
        }
        return typeName;
    }
}
