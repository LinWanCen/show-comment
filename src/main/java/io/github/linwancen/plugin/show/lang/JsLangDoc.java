package io.github.linwancen.plugin.show.lang;

import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.lang.javascript.psi.JSPsiReferenceElement;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JsLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(JavascriptLanguage.INSTANCE.getID(), new JsLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(JSPsiReferenceElement.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentJs;
    }

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocRaw(@NotNull T info, @NotNull PsiElement resolve) {
        @Nullable PsiComment psiComment = JSDocumentationUtils.findOwnDocCommentForImplicitElement(resolve);
        if (psiComment == null) {
            if (info.appSettings.showLineEndCommentJsBase) {
                return super.resolveDocRaw(info, resolve);
            }
            return null;
        }
        return psiComment.getText();
    }
}
