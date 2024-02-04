package io.github.linwancen.plugin.show.lang;

import com.intellij.psi.PsiElement;
import com.jetbrains.rider.ideaInterop.fileTypes.csharp.CSharpLanguage;
import com.jetbrains.rider.ideaInterop.fileTypes.csharp.psi.CSharpDummyNode;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Doesn't work because I don't know how to get Reference for CSharp PsiElement
 * <br>https://intellij-support.jetbrains.com/hc/en-us/requests/4228491
 */
public class CsLineEnd extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(CSharpLanguage.INSTANCE.getID(), new CsLineEnd());
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return true;
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(CSharpDummyNode.class);
    }
}
