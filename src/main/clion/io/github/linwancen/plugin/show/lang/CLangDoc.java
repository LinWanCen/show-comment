package io.github.linwancen.plugin.show.lang;

import com.intellij.psi.PsiElement;
import com.jetbrains.cidr.lang.OCLanguage;
import com.jetbrains.cidr.lang.psi.OCReferenceElement;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(OCLanguage.getInstance().getID(), new CLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(OCReferenceElement.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentCBase;
    }

    @Override
    protected @Nullable <T extends SettingsInfo> String resolveDocPrint(@NotNull T info, @NotNull PsiElement resolve) {
        return super.resolveDocPrint(info, resolve.getParent());
    }
}
