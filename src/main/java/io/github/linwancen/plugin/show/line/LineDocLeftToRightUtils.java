package io.github.linwancen.plugin.show.line;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

public class LineDocLeftToRightUtils {

    private LineDocLeftToRightUtils() {}

    static final String[] KEYS = {
            "=",
    };

    @Nullable
    public static PsiDocComment leftDoc(FileViewProvider viewProvider, Document document,
                                        int startOffset, int endOffset) {
        String text = document.getText(new TextRange(startOffset, endOffset));
        int offset = 0;
        for (String s : KEYS) {
            int i = text.indexOf(s);
            if (i > 0) {
                offset += (i + s.length());
                break;
            }
        }
        text = text.substring(offset);
        boolean startWithSymbol = false;
        char[] chars = text.toCharArray();
        // skip symbol and space
        for (char c : chars) {
            if (Character.isLetter(c)) {
                break;
            }
            if (!Character.isSpaceChar(c)) {
                startWithSymbol = true;
            }
            offset++;
        }
        offset += startOffset;
        if (startWithSymbol) {
            startOffset = 0;
        }
        PsiElement element = viewProvider.findElementAt(offset, JavaLanguage.INSTANCE);
        PsiIdentifier psiIdentifier = leftIdentifier(element, endOffset);
        return LineDocUtils.elementDoc(psiIdentifier, psiIdentifier, startOffset, endOffset);
    }

    @Nullable
    private static PsiIdentifier leftIdentifier(PsiElement element, int endOffset) {
        if (element == null) {
            return null;
        }
        while (!(element instanceof PsiIdentifier)) {
            element = PsiTreeUtil.nextVisibleLeaf(element);
            if (element == null || element.getTextRange().getEndOffset() > endOffset) {
                return null;
            }
        }
        return (PsiIdentifier) element;
    }
}
