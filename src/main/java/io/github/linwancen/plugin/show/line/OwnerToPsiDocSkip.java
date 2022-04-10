package io.github.linwancen.plugin.show.line;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.doc.OwnerToPsiDocUtils;
import org.jetbrains.annotations.Nullable;

/**
 * call RefToPsiDoc, PsiClassSkip
 */
class OwnerToPsiDocSkip {

    private OwnerToPsiDocSkip() {}

    static PsiDocComment methodDoc(@Nullable PsiMethod psiMethod) {
        if (skip(psiMethod)) {
            return null;
        }
        return OwnerToPsiDocUtils.methodDoc(psiMethod);
    }

    static PsiDocComment refDoc(@Nullable PsiDocCommentOwner docOwner) {
        if (skip(docOwner)) {
            return null;
        }
        if (docOwner instanceof PsiMethod) {
            return OwnerToPsiDocUtils.methodDoc(((PsiMethod) docOwner));
        }
        return OwnerToPsiDocUtils.srcOrByteCodeDoc(docOwner);
    }

    private static boolean skip(@Nullable PsiDocCommentOwner docOwner) {
        if (docOwner == null) {
            return true;
        }
        if (docOwner instanceof PsiClass) {
            return PsiClassSkip.skip((PsiClass) docOwner, docOwner.getProject());
        }
        return PsiClassSkip.skip(docOwner.getContainingClass(), docOwner.getProject());
    }
}
