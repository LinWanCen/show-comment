package io.github.linwancen.plugin.comment.utils;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

public class ResolveElementRightToLeftUtils {

    private ResolveElementRightToLeftUtils() {}

    @Nullable
    public static PsiElement resolveElement(FileViewProvider viewProvider, int startOffset, int endOffset) {
        // End is always white, can not -1 because @see class.name need it
        PsiElement element = viewProvider.findElementAt(endOffset, JavaLanguage.INSTANCE);
        if (element == null) {
            return null;
        }
        PsiElement identifier;
        while (!((identifier = PsiTreeUtil.prevVisibleLeaf(element)) instanceof PsiIdentifier)) {
            if (identifier == null || identifier.getTextRange().getStartOffset() < startOffset) {
                break;
            }
            element = identifier;
        }
        // if in prev line, set it null.
        if (identifier != null && identifier.getTextRange().getStartOffset() < startOffset) {
            identifier = null;
        }
        return ResolveElementUtils.resolveElementOf(element, identifier, startOffset, endOffset);
    }
}
