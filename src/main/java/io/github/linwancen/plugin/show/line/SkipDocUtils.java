package io.github.linwancen.plugin.show.line;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.doc.DocUtils;
import org.jetbrains.annotations.Nullable;

class SkipDocUtils {

    private SkipDocUtils() {}

    static PsiDocComment methodDoc(@Nullable PsiMethod psiMethod) {
        if (skip(psiMethod)) {
            return null;
        }
        return DocUtils.methodDoc(psiMethod);
    }

    static PsiDocComment refDoc(@Nullable PsiDocCommentOwner docOwner) {
        if (skip(docOwner)) {
            return null;
        }
        if (docOwner instanceof PsiMethod) {
            return DocUtils.methodDoc(((PsiMethod) docOwner));
        }
        return DocUtils.srcOrByteCodeDoc(docOwner);
    }

    static boolean skip(@Nullable PsiDocCommentOwner docOwner) {
        if (docOwner == null) {
            return true;
        }
        if (docOwner instanceof PsiClass) {
            return SkipUtils.skip((PsiClass) docOwner, docOwner.getProject());
        }
        return SkipUtils.skip(docOwner.getContainingClass(), docOwner.getProject());
    }
}
