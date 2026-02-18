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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

public class PsiClassUtils {

    private PsiClassUtils() {}

    private static final Pattern SPLIT_PATTERN = Pattern.compile("[^\\w.]");

    /**
     * only xxx-ClassName.xxx
     */
    @NotNull
    public static PsiClass[] jsonFileToClasses(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        @NotNull String fileName = virtualFile.getNameWithoutExtension();
        String[] split = SPLIT_PATTERN.split(fileName);
        if (split.length < 2) {
            return camelNameToClass(project, fileName);
        }
        return camelNameToClass(project, split[split.length - 1]);
    }

    /**
     * ClassName-...-ClassName.json/xml
     */
    @NotNull
    public static PsiClass[] fileToClasses(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        @NotNull String fileName = virtualFile.getNameWithoutExtension();
        @Nullable String ext = virtualFile.getExtension();
        if (ext == null || (!ext.startsWith("json") && !ext.equals("xml"))) {
            return PsiClass.EMPTY_ARRAY;
        }
        String[] split = SPLIT_PATTERN.split(fileName);
        @NotNull PsiClass[] startClass = camelNameToClass(project, split[0]);
        if (split.length < 2) {
            return startClass;
        }
        @NotNull PsiClass[] endClass = camelNameToClass(project, split[split.length - 1]);
        @NotNull LinkedHashSet<PsiClass> psiClasses = new LinkedHashSet<>(Arrays.asList(startClass));
        psiClasses.addAll(Arrays.asList(endClass));
        return psiClasses.toArray(PsiClass.EMPTY_ARRAY);
    }

    @NotNull
    public static PsiClass[] camelNameToClass(@NotNull Project project, @Nullable String className) {
        if (className == null) {
            return PsiClass.EMPTY_ARRAY;
        }
        @NotNull PsiClass[] psiClasses = nameToClass(className, project);
        if (psiClasses.length != 0) {
            return psiClasses;
        }
        @NotNull char[] chars = className.toCharArray();
        if (chars.length < 1 || chars[0] < 97 || 122 < chars[0]) {
            return PsiClass.EMPTY_ARRAY;
        }
        // Upper Case
        chars[0] -= 32;
        @NotNull String name = String.valueOf(chars);
        return simpleNameToClass(name, project);
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
        try {
            return namesCache.getClassesByName(className, GlobalSearchScope.allScope(project));
        } catch (Throwable ignored) {
            return PsiClass.EMPTY_ARRAY;
        }
    }

    @NotNull
    public static PsiClass[] fullNameToClass(@NotNull String className, @NotNull Project project) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        try {
            return javaPsiFacade.findClasses(className, GlobalSearchScope.allScope(project));
        } catch (Throwable ignored) {
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
