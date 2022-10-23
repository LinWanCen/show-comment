package io.github.linwancen.plugin.show.java.doc;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PsiMethodToPsiDoc {

    private PsiMethodToPsiDoc() {}

    @Nullable
    static PsiDocComment methodSupperNewPropDoc(@NotNull PsiMethod psiMethod) {
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
        PsiDocComment superDoc = supperMethodDoc(psiMethod);
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
        return propMethodDoc(psiMethod, clazz);
    }

    @Nullable
    static PsiDocComment supperMethodDoc(PsiMethod psiMethod) {
        PsiMethod[] superMethods = psiMethod.findSuperMethods();
        for (PsiMethod superMethod : superMethods) {
            PsiDocComment superDoc = OwnerToPsiDocUtils.methodDoc(superMethod);
            if (superDoc != null) {
                return superDoc;
            }
        }
        return null;
    }

    @Nullable
    private static PsiDocComment propMethodDoc(PsiMethod psiMethod, PsiClass psiClass) {
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
