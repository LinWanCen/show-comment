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
        if (startOffset == endOffset) {
            return null;
        }
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
        return ResolveElementUtils.resolveElementOf(element, identifier, startOffset, endOffset);
    }
}
