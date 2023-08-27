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
import org.apache.commons.lang.StringUtils;
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
    public boolean show(@NotNull LineInfo lineInfo) {
        return lineInfo.appSettings.showLineEndCommentPy;
    }

    @Nullable
    @Override
    protected <T extends SettingsInfo> StructuredDocString toDocElement(@NotNull T settingsInfo,
                                                                        @NotNull PsiElement resolve) {
        if (resolve instanceof PyDocStringOwner) {
            @NotNull PyDocStringOwner pyDocStringOwner = (PyDocStringOwner) resolve;
            return pyDocStringOwner.getStructuredDocString();
        }
        return null;
    }

    @NotNull
    @Override
    protected <T extends SettingsInfo> String descDoc(@NotNull T lineInfo,
                                                      @NotNull StructuredDocString structuredDocString) {
        String summary = structuredDocString.getSummary();
        if (StringUtils.isNotEmpty(summary)) {
            return summary;
        }
        @NotNull String description = structuredDocString.getDescription();
        return DocFilter.cutDoc(DocFilter.html2Text(description), lineInfo.appSettings, false);
    }

    @Override
    protected <T extends SettingsInfo> void appendTag(@NotNull T lineInfo, @NotNull StringBuilder tagStrBuilder,
                                                      @NotNull StructuredDocString structuredDocString,
                                                      @NotNull String name) {
        if (structuredDocString instanceof TagBasedDocString) {
            @Nullable Substring tagValue = ((TagBasedDocString) structuredDocString).getTagValue(name);
            if (tagValue != null) {
                @NotNull String cutDoc = DocFilter.cutDoc(tagValue.getValue(), lineInfo.appSettings, false);
                tagStrBuilder.append(cutDoc);
            }
        }
    }
}
