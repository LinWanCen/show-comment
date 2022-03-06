package io.github.linwancen.plugin.show.line;

import com.intellij.json.JsonLanguage;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.doc.JsonDocUtils;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.Nullable;

public class LineDocRightToLeftUtils {

    private LineDocRightToLeftUtils() {}

    @Nullable
    public static PsiDocComment rightDoc(FileViewProvider viewProvider, int startOffset, int endOffset) {
        // End is always white, can not -1 because @see class.name need it
        PsiElement element = viewProvider.findElementAt(endOffset);
        if (element == null) {
            return null;
        }
        AppSettingsState instance = AppSettingsState.getInstance();
        PsiElement identifier;
        PsiDocComment psiDocComment;
        while (true) {
            identifier = PsiTreeUtil.prevVisibleLeaf(element);
            if (identifier != null && identifier.getTextRange().getStartOffset() < startOffset) {
                identifier = null;
            }
            if (identifier == null || identifier instanceof PsiIdentifier) {
                if (instance.inJson && element.getLanguage().is(JsonLanguage.INSTANCE)) {
                    return JsonDocUtils.jsonDoc(element, startOffset, endOffset);
                }
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
