package io.github.linwancen.plugin.show.lang;

import com.intellij.psi.PsiElement;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RDotReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;

import java.util.List;

public class RubyLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(RubyLanguage.INSTANCE.getID(), new RubyLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(RDotReference.class, RIdentifier.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentRubyBase;
    }
}
