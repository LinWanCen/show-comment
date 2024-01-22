package io.github.linwancen.plugin.show.java.doc;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiMethodToPsiDoc {

    private PsiMethodToPsiDoc() {}

    @Nullable
    public static PsiDocComment methodSupperNewPropDoc(@NotNull PsiMethod psiMethod) {
        @Nullable PsiDocComment docComment = psiMethod.getDocComment();
        if (docComment != null) {
            return docComment;
        }

        // supper recursion
        @Nullable PsiDocComment superDoc = supperMethodDoc(psiMethod);
        if (superDoc != null) {
            return superDoc;
        }


        @Nullable PsiClass clazz = psiMethod.getContainingClass();
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
    public static PsiDocComment supperMethodDoc(@NotNull PsiMethod psiMethod) {
        @NotNull PsiMethod[] superMethods;
        try {
            superMethods = psiMethod.findSuperMethods();
        } catch (Exception e) {
            return null;
        }
        for (@NotNull PsiMethod superMethod : superMethods) {
            // .class
            @Nullable PsiElement navElement;
            try {
                navElement = superMethod.getNavigationElement();
            } catch (Exception e) {
                return null;
            }
            if (navElement instanceof PsiMethod) {
                superMethod = (PsiMethod) navElement;
            }
            @Nullable PsiDocComment superDoc = PsiMethodToPsiDoc.methodSupperNewPropDoc(superMethod);
            if (superDoc != null) {
                return superDoc;
            }
        }
        return null;
    }

    @Nullable
    public static PsiField propMethodField(@NotNull PsiMethod psiMethod) {
        @Nullable PsiClass clazz = psiMethod.getContainingClass();
        if (clazz == null) {
            return null;
        }
        return propMethodClassField(psiMethod, clazz);
    }

    @Nullable
    static PsiField propMethodClassField(@NotNull PsiMethod psiMethod, @NotNull PsiClass psiClass) {
        @NotNull String name = psiMethod.getName();
        if (name.length() > 3 && (name.startsWith("get") || name.startsWith("set"))) {
            name = name.substring(3);
        } else if (name.length() > 2 && name.startsWith("is")) {
            name = name.substring(2);
        } else {
            return null;
        }
        @NotNull char[] chars = name.toCharArray();
        // Lower Case
        chars[0] += 32;
        name = String.valueOf(chars);
        return psiClass.findFieldByName(name, false);
    }

    @Nullable
    private static PsiDocComment propMethodDoc(@NotNull PsiMethod psiMethod, @NotNull PsiClass psiClass) {
        @Nullable PsiField fieldByName = propMethodClassField(psiMethod, psiClass);
        if (fieldByName == null) {
            return null;
        }
        return fieldByName.getDocComment();
    }
}
