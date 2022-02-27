package io.github.linwancen.plugin.show.line;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

public class LineDocRightToLeftUtils {

    private LineDocRightToLeftUtils() {}

    @Nullable
    public static PsiDocComment rightDoc(FileViewProvider viewProvider, int startOffset, int endOffset) {
        // End is always white, can not -1 because @see class.name need it
        PsiElement element = viewProvider.findElementAt(endOffset, JavaLanguage.INSTANCE);
        if (element == null) {
            return null;
        }
        PsiElement identifier;
        PsiDocComment psiDocComment;
        while (true) {
            identifier = PsiTreeUtil.prevVisibleLeaf(element);
            if (identifier != null && identifier.getTextRange().getStartOffset() < startOffset) {
                identifier = null;
            }
            if (identifier == null || identifier instanceof PsiIdentifier) {
                psiDocComment = LineDocUtils.elementDoc(element, identifier, startOffset, endOffset);
                if (psiDocComment != null) {
                    return psiDocComment;
                }
            }
            if (identifier == null) {
                return null;
            }
            element = identifier;
        }
    }
}
