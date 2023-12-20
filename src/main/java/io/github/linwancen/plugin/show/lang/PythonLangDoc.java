package io.github.linwancen.plugin.show.lang;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.documentation.docstrings.TagBasedDocString;
import com.jetbrains.python.psi.PyDocStringOwner;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.StructuredDocString;
import com.jetbrains.python.toolbox.Substring;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseTagLangDoc;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PythonLangDoc extends BaseTagLangDoc<StructuredDocString> {

    static {
        LANG_DOC_MAP.put(PythonLanguage.INSTANCE.getID(), new PythonLangDoc());
    }

    @Override
    public @NotNull Class<? extends PsiElement> getRefClass() {
        return PyReferenceExpression.class;
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentPy;
    }

    @Override
    protected <T extends SettingsInfo> boolean parseBaseComment(@NotNull T info) {
        return info.appSettings.showLineEndCommentPyBase;
    }

    @Nullable
    @Override
    protected <T extends SettingsInfo> StructuredDocString toDocElement(@NotNull T info,
                                                                        @NotNull PsiElement resolve) {
        if (resolve instanceof PyDocStringOwner) {
            @NotNull PyDocStringOwner pyDocStringOwner = (PyDocStringOwner) resolve;
            return pyDocStringOwner.getStructuredDocString();
        }
        return null;
    }

    @NotNull
    @Override
    protected <T extends SettingsInfo> String descDoc(@NotNull T info,
                                                      @NotNull StructuredDocString structuredDocString) {
        String summary = structuredDocString.getSummary();
        if (StringUtils.isNotEmpty(summary)) {
            return summary;
        }
        @NotNull String description = structuredDocString.getDescription();
        return DocFilter.cutDoc(DocFilter.html2Text(description), info, false);
    }

    @Override
    protected <T extends SettingsInfo> void appendTag(@NotNull T info, @NotNull StringBuilder tagStrBuilder,
                                                      @NotNull StructuredDocString structuredDocString,
                                                      @NotNull String name) {
        if (structuredDocString instanceof TagBasedDocString) {
            @Nullable Substring tagValue = ((TagBasedDocString) structuredDocString).getTagValue(name);
            if (tagValue != null) {
                @NotNull String cutDoc = DocFilter.cutDoc(tagValue.getValue(), info, false);
                tagStrBuilder.append(cutDoc);
            }
        }
    }
}
