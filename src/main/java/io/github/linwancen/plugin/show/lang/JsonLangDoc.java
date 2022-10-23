package io.github.linwancen.plugin.show.lang;

import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.bean.LineInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonLangDoc extends BaseLangDoc {

    public static final JsonLangDoc INSTANCE = new JsonLangDoc();

    @Override
    public @NotNull Class<? extends PsiElement> getRefClass() {
        return JsonProperty.class;
    }

    @Override
    public boolean show(@NotNull LineInfo lineInfo) {
        return lineInfo.appSettings.showLineEndCommentJson;
    }

    @Override
    public @Nullable String findRefDoc(@NotNull LineInfo lineInfo, @NotNull PsiElement element) {
        PsiElement start = lineInfo.viewProvider.findElementAt(lineInfo.startOffset);
        if (start == null) {
            return null;
        }
        PsiElement jsonProperty = start.getNextSibling();
        if (jsonProperty == null) {
            return null;
        }
        return refElementDoc(lineInfo, jsonProperty);
    }

    @Override
    protected @Nullable <T extends SettingsInfo> String refDoc(@NotNull T lineInfo, @NotNull PsiElement ref) {
        if (!(ref instanceof JsonProperty)) {
            return null;
        }
        JsonProperty jsonProperty = (JsonProperty) ref;
        PsiReference[] references = jsonProperty.getNameElement().getReferences();
        for (PsiReference reference : references) {
            PsiElement resolve = null;
            try {
                resolve = reference.resolve();
            } catch (Throwable ignore) {
                // ignore
            }
            if (resolve == null) {
                continue;
            }
            String doc = BaseLangDoc.resolveDoc(lineInfo, resolve);
            if (doc != null) {
                return doc;
            }
        }
        return null;
    }
}
