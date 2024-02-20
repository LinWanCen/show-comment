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
 * <br>https://intellij-support.jetbrains.com/hc/en-us/community/posts/16807048482450-How-to-get-CSharp-References-comment
 * <br>Rider uses the ReSharper SDK (i.e., not the IntelliJ SDK) to parse C# syntax.
 * <br>Therefore, expect to write C#/.NET to support your feature.
 * <br>I think the inspections sample project could be helpful to get started with how things are working.
 * <br>Since I expect you also want to use some of your existing infrastructure, you must send data between C# and Kotlin code.
 * <br>For that, I recommend looking into the protocol sample project (check the README).
 * <br>https://github.com/JetBrains/resharper-rider-plugin/tree/master/samples/CodeInspections
 * <br>https://github.com/JetBrains/resharper-rider-plugin/tree/master/samples/RdProtocol
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
