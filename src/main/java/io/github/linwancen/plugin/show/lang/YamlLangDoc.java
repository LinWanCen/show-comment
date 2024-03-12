package io.github.linwancen.plugin.show.lang;

import com.intellij.psi.PsiElement;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.List;

public class YamlLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(YAMLLanguage.INSTANCE.getID(), new YamlLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(YAMLKeyValue.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentYaml;
    }

    @Nullable
    @Override
    protected <T extends SettingsInfo> String resolveDocPrint(@NotNull T info, @NotNull PsiElement resolve) {
        return null;
    }
}
