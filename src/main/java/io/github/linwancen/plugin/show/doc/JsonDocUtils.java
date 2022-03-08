package io.github.linwancen.plugin.show.doc;

import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JsonDocUtils {

    private JsonDocUtils() {}

    @Nullable
    public static PsiDocComment jsonDoc(PsiElement element, int startOffset, int endOffset) {
        JsonProperty jsonProp = PsiTreeUtil.getParentOfType(element, JsonProperty.class, true, startOffset);
        if (jsonProp == null || jsonProp.getNameElement().getTextRange().getEndOffset() > endOffset) {
            return null;
        }
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        PsiClass[] psiClasses = PsiClassUtils.encClass(virtualFile, element.getProject());
        List<String> jsonPath = jsonPath(jsonProp);
        return doc(psiClasses, element.getProject(), jsonPath, jsonPath.size() - 1);
    }

    @NotNull
    public static List<String> jsonPath(JsonProperty jsonProp) {
        ArrayList<String> jsonPath = new ArrayList<>();
        do {
            jsonPath.add(jsonProp.getName());
        } while ((jsonProp = PsiTreeUtil.getParentOfType(jsonProp, JsonProperty.class)) != null);
        return jsonPath;
    }

    @Nullable
    private static PsiDocComment doc(PsiClass[] psiClasses, Project project, List<String> jsonPath, int level) {
        String name = jsonPath.get(level);
        for (PsiClass psiClass : psiClasses) {
            PsiField psiField = psiClass.findFieldByName(name, true);
            if (psiField == null) {
                return null;
            }
            if (level == 0) {
                return DocUtils.srcOrByteCodeDoc(psiField);
            }
            String classFullName = toClassFullName(psiField);
            PsiClass[] classes = PsiClassUtils.fullNameToClass(classFullName, project);
            PsiDocComment docComment = doc(classes, project, jsonPath, level - 1);
            if (docComment != null) {
                return docComment;
            }
        }
        return null;
    }

    @NotNull
    private static String toClassFullName(PsiField psiField) {
        PsiTypeElement typeElement = psiField.getTypeElement();
        if (typeElement != null) {
            PsiJavaCodeReferenceElement code = typeElement.getInnermostComponentReferenceElement();
            if (code != null) {
                PsiType[] types = code.getTypeParameters();
                if (types.length > 0) {
                    // List
                    return types[types.length - 1].getCanonicalText();
                }
            }
        }
        // Array
        return psiField.getType().getDeepComponentType().getCanonicalText();
    }
}
