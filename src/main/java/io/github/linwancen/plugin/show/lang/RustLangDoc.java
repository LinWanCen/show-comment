package io.github.linwancen.plugin.show.lang;

import com.intellij.psi.PsiElement;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rust.lang.RsLanguage;
import org.rust.lang.core.psi.RsMethodCall;
import org.rust.lang.core.psi.RsPath;

import java.util.List;

public class RustLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(RsLanguage.INSTANCE.getID(), new RustLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(RsPath.class, RsMethodCall.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentRustBase;
    }

    @Nullable
    @Override
    protected <T extends SettingsInfo> String resolveDocPrint(@NotNull T info, @NotNull PsiElement resolve) {
        @Nullable String s = super.resolveDocPrint(info, resolve);
        if (s != null && s.startsWith("!")) {
            return s.substring(1);
        }
        return s;
    }
}
