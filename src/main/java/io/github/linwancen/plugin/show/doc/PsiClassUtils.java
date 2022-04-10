package io.github.linwancen.plugin.show.doc;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
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
        String fileName = virtualFile.getNameWithoutExtension();
        Matcher matcher = JSON_PATTERN.matcher(fileName);
        if (!matcher.find()) {
            return new PsiClass[0];
        }
        String className = matcher.group();
        return nameToClass(className, project);
    }

    @NotNull
    public static PsiClass[] nameToClass(@Nullable String className, @NotNull Project project) {
        if (className == null) {
            return new PsiClass[0];
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
        return javaPsiFacade.findClasses(className, GlobalSearchScope.allScope(project));
    }

    @NotNull
    public static String toClassFullName(PsiField psiField) {
        // Array
        // use replace simpler than getDeepComponentType()
        String typeName = psiField.getType().getCanonicalText().replace("[]", "");
        // List
        // use substring() because clsFieldImpl.getInnermostComponentReferenceElement() == null
        int index = typeName.indexOf("<");
        if (index >= 0) {
            return typeName.substring(index + 1, typeName.length() - 1);
        }
        return typeName;
    }
}
