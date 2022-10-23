package io.github.linwancen.plugin.show.lang;

import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.lang.javascript.psi.JSPsiReferenceElement;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(JavascriptLanguage.INSTANCE.getID(), new JsLangDoc());
    }

    @Override
    public @NotNull Class<? extends PsiElement> getRefClass() {
        return JSPsiReferenceElement.class;
    }

    @Override
    public boolean show(@NotNull LineInfo lineInfo) {
        return lineInfo.appSettings.showLineEndCommentJs;
    }

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocRaw(@NotNull T lineInfo, @NotNull PsiElement resolve) {
        @Nullable PsiComment psiComment = JSDocumentationUtils.findOwnDocCommentForImplicitElement(resolve);
        if (psiComment == null) {
            return null;
        }
        String text = psiComment.getText();
        if (text != null) {
            return text;
        }
        if (!lineInfo.appSettings.jsDoc) {
            return super.resolveDocRaw(lineInfo, resolve);
        }
        return null;
    }
}
