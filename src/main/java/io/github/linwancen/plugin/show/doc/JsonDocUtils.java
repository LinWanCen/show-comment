package io.github.linwancen.plugin.show.doc;

import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonDocUtils {
    private static final Pattern JSON_PATTERN = Pattern.compile("[\\w.]*+$");

    private JsonDocUtils() {}

    @Nullable
    public static PsiDocComment jsonDoc(PsiElement element, int startOffset, int endOffset) {
        JsonProperty jsonProp = PsiTreeUtil.getParentOfType(element, JsonProperty.class, true, startOffset);
        if (jsonProp == null || jsonProp.getNameElement().getTextRange().getEndOffset() > endOffset) {
            return null;
        }
        String fileName = element.getContainingFile().getVirtualFile().getNameWithoutExtension();
        Matcher matcher = JSON_PATTERN.matcher(fileName);
        if (!matcher.find()) {
            return null;
        }
        String className = matcher.group();
        PsiClass[] psiClasses = classByName(className, element.getProject());
        PsiField psiField = psiField(psiClasses, element.getProject(), jsonProp);
        if (psiField != null) {
            return DocUtils.srcOrByteCodeDoc(psiField);
        }
        return null;
    }

    @NotNull
    private static PsiClass[] classByName(String className, @NotNull Project project) {
        int i = className.indexOf('.');
        if (i > 0) {
            return classByFullName(className, project);
        }
        PsiShortNamesCache namesCache = PsiShortNamesCache.getInstance(project);
        return namesCache.getClassesByName(className, GlobalSearchScope.allScope(project));
    }

    @NotNull
    private static PsiClass[] classByFullName(String className, @NotNull Project project) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        return javaPsiFacade.findClasses(className, GlobalSearchScope.allScope(project));
    }

    @Nullable
    private static PsiField psiField(PsiClass[] rootClasses, Project project, JsonProperty jsonProp) {
        JsonProperty parentJsonProp = PsiTreeUtil.getParentOfType(jsonProp, JsonProperty.class);
        if (parentJsonProp == null) {
            for (PsiClass c : rootClasses) {
                PsiField field = c.findFieldByName(jsonProp.getName(), true);
                if (field != null) {
                    return field;
                }
            }
            return null;
        }
        PsiField psiField = psiField(rootClasses, project, parentJsonProp);
        if (psiField == null) {
            return null;
        }
        String classFullName = psiField.getType().getCanonicalText();
        @NotNull PsiClass[] psiClasses = classByFullName(classFullName, project);
        for (PsiClass c : psiClasses) {
            PsiField field = c.findFieldByName(jsonProp.getName(), true);
            if (field != null) {
                return field;
            }
        }
        return null;
    }
}
