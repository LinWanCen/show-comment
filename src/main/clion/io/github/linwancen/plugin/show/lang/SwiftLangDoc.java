package io.github.linwancen.plugin.show.lang;

import com.intellij.psi.PsiElement;
import com.jetbrains.cidr.lang.psi.OCReferenceElement;
import com.jetbrains.swift.SwiftLanguage;
import com.jetbrains.swift.psi.SwiftReferenceElement;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SwiftLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(SwiftLanguage.INSTANCE.getID(), new SwiftLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        // guess
        return List.of(SwiftReferenceElement.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentSwiftBase;
    }

    @Override
    protected @Nullable <T extends SettingsInfo> String resolveDocPrint(@NotNull T info, @NotNull PsiElement resolve) {
        // Copy from C
        return super.resolveDocPrint(info, resolve.getParent());
    }
}
