package io.github.linwancen.plugin.show.line;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.doc.JsonDocUtils;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
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
        PsiElement element = viewProvider.findElementAt(offset);
        if (element == null) {
            return null;
        }
        AppSettingsState instance = AppSettingsState.getInstance();
        if (instance.inJson && element.getLanguage().is(JsonLanguage.INSTANCE)) {
            return JsonDocUtils.jsonDoc(element, startOffset, endOffset);
        }
        if (startWithSymbol) {
            startOffset = 0;
        }
        return nextDoc(element, startOffset, endOffset);
    }

    @Nullable
    private static PsiDocComment nextDoc(PsiElement element, int startOffset, int endOffset) {
        while (element.getTextRange().getEndOffset() < endOffset) {
            if (element instanceof PsiIdentifier) {
                PsiDocComment psiDocComment = LineDocUtils.elementDoc(element, element, startOffset, endOffset);
                if (psiDocComment != null) {
                    return psiDocComment;
                }
            }
            element = PsiTreeUtil.nextVisibleLeaf(element);
            if (element == null) {
                return null;
            }
        }
        return null;
    }
}
