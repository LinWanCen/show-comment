package io.github.linwancen.plugin.comment.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.Nullable;

public class PsiMethodCommentFactory {

    private PsiMethodCommentFactory() {}

    @Nullable
    public static PsiDocComment from(PsiMethod psiMethod) {
        // .class
        PsiElement navElement = psiMethod.getNavigationElement();
        if (navElement instanceof PsiMethod) {
            psiMethod = (PsiMethod) navElement;
        }

        PsiDocComment docComment = psiMethod.getDocComment();
        if (docComment != null) {
            return docComment;
        }

        // supper
        PsiDocComment superDoc = supperMethodComment(psiMethod);
        if (superDoc != null) {
            return superDoc;
        }


        PsiClass clazz = psiMethod.getContainingClass();
        if (clazz == null) {
            return null;
        }

        // constructor
        if (psiMethod.isConstructor()) {
            return clazz.getDocComment();
        }

        // get/set/is - PropertyDescriptor getReadMethod() getWriteMethod()
        return propMethodComment(psiMethod, clazz);
    }

    @Nullable
    public static PsiDocComment supperMethodComment(PsiMethod psiMethod) {
        PsiMethod[] superMethods = psiMethod.findSuperMethods();
        for (PsiMethod superMethod : superMethods) {
            PsiDocComment superDoc = from(superMethod);
            if (superDoc != null) {
                return superDoc;
            }
        }
        return null;
    }

    @Nullable
    public static PsiDocComment propMethodComment(PsiMethod psiMethod, PsiClass psiClass) {
        String name = psiMethod.getName();
        if (name.length() > 3 && (name.startsWith("get") || name.startsWith("set"))) {
            name = name.substring(3);
        } else if (name.length() > 2 && name.startsWith("is")) {
            name = name.substring(2);
        } else {
            return null;
        }
        char[] chars = name.toCharArray();
        chars[0] += 32;
        name = String.valueOf(chars);
        PsiField fieldByName = psiClass.findFieldByName(name, false);
        if (fieldByName == null) {
            return null;
        }
        return fieldByName.getDocComment();
    }
}
