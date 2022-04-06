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
            // one line end
            return null;
        }
        AppSettingsState setting = AppSettingsState.getInstance();
        while (true) {
            PsiElement identifier = PsiTreeUtil.prevVisibleLeaf(element);
            if (identifier != null && identifier.getTextRange().getStartOffset() < startOffset) {
                identifier = null;
            }
            PsiDocComment docComment = psiDoc(setting, identifier, element, startOffset, endOffset);
            if (docComment != null) {
                return docComment;
            }
            if (identifier == null) {
                return null;
            }
            element = identifier;
        }
    }

    public static PsiDocComment psiDoc(AppSettingsState setting,
                                       PsiElement identifier, PsiElement element,
                                       int startOffset, int endOffset) {
        if (identifier != null && !(identifier instanceof PsiIdentifier)) {
            return null;
        }
        if (setting.inJson && element.getLanguage().is(JsonLanguage.INSTANCE)) {
            return JsonDocUtils.jsonDoc(element, startOffset, endOffset);
        }
        return LineDocUtils.elementDoc(element, identifier, startOffset, endOffset);
    }
}
