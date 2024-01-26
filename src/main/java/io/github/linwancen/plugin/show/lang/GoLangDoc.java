package io.github.linwancen.plugin.show.lang;

import com.goide.GoLanguage;
import com.goide.documentation.GoDocumentationProvider;
import com.goide.psi.GoReferenceExpressionBase;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GoLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(GoLanguage.INSTANCE.getID(), new GoLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(GoReferenceExpressionBase.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentGo;
    }

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocRaw(@NotNull T info, @NotNull PsiElement resolve) {
        @NotNull List<PsiComment> comments = GoDocumentationProvider.getCommentsForElement(resolve);
        return GoDocumentationProvider.getCommentText(comments, false);
    }
}
