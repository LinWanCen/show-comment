package io.github.linwancen.plugin.show.java.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParamDoc {

    private ParamDoc() {}

    @Nullable
    public static String paramDoc(@NotNull PsiParameter psiParameter) {
        @Nullable PsiMethod method = PsiTreeUtil.getParentOfType(psiParameter, PsiMethod.class);
        if (method == null) {
            return null;
        }
        @Nullable PsiDocComment psiDocComment = PsiMethodToPsiDoc.methodSupperNewPropDoc(method);
        if (psiDocComment == null) {
            return null;
        }
        @NotNull String name = psiParameter.getName();
        @NotNull PsiDocTag[] params = psiDocComment.findTagsByName("param");
        for (@NotNull PsiDocTag param : params) {
            @Nullable PsiDocTagValue value = param.getValueElement();
            if (value != null && name.equals(value.getText())) {
                @NotNull PsiElement[] dataElements = param.getDataElements();
                if (dataElements.length > 1) {
                    return dataElements[1].getText();
                }
            }
        }
        return null;
    }
}
