package io.github.linwancen.plugin.show.lang;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyReferenceExpression;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;

public class PythonLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(PythonLanguage.INSTANCE.getID(), new PythonLangDoc());
    }

    @Override
    public @NotNull Class<? extends PsiElement> getRefClass() {
        return PyReferenceExpression.class;
    }

    @Override
    public boolean show(@NotNull LineInfo lineInfo) {
        return lineInfo.appSettings.showLineEndCommentPy;
    }
}
