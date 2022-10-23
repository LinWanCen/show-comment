package io.github.linwancen.plugin.show.java.line;

import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.Nullable;

/**
 * call OwnerToPsiDocSkip
 */
public class NewCallRefToPsiDoc {

    private NewCallRefToPsiDoc() {}

    @Nullable
    public static PsiDocComment javaCodeDoc(@Nullable PsiJavaCodeReferenceElement ref) {
        if (ref == null) {
            return null;
        }
        @Nullable PsiElement resolve = null;
        try {
            resolve = ref.resolve();
        } catch (Throwable ignore) {
            // ignore
        }
        if (resolve instanceof PsiDocCommentOwner) {
            return OwnerToPsiDocSkip.refDoc(((PsiDocCommentOwner) resolve));
        }
        return null;
    }
}
