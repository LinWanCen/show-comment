package io.github.linwancen.plugin.show.lang.base;

import com.intellij.psi.PsiElement;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseTagLangDoc<DocElement> extends BaseLangDoc {

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocPrint(@NotNull T lineInfo, @NotNull PsiElement resolve) {
        @Nullable DocElement docElement = toDocElement(resolve);
        if (docElement == null) {
            return null;
        }
        return docElementToStr(lineInfo, docElement);
    }

    @Nullable
    public <T extends SettingsInfo> String docElementToStr(@NotNull T lineInfo, @Nullable DocElement docElement) {
        if (docElement == null) {
            return null;
        }
        // desc
        @NotNull String descDoc = descDoc(lineInfo, docElement).trim();
        @NotNull String desc = DocFilter.filterDoc(descDoc, lineInfo.appSettings, lineInfo.projectSettings);
        // tag
        @NotNull StringBuilder tagStrBuilder = new StringBuilder();
        @NotNull String[] names = lineInfo.tagNames();
        for (@NotNull String name : names) {
            appendTag(lineInfo, tagStrBuilder, docElement, name);
        }
        if (desc.length() > 0) {
            if (tagStrBuilder.length() > 0) {
                tagStrBuilder.insert(0, " @ ");
            }
            tagStrBuilder.insert(0, desc);
        }
        @NotNull String text = tagStrBuilder.toString().trim();
        if (text.length() == 0) {
            return null;
        }
        return text;
    }

    @Nullable
    protected abstract DocElement toDocElement(@NotNull PsiElement resolve);

    /**
     * cut / * # not filter text
     */
    @NotNull
    protected abstract <T extends SettingsInfo> String descDoc(@NotNull T lineInfo, @NotNull DocElement docElement);

    protected abstract <T extends SettingsInfo> void appendTag(@NotNull T lineInfo,
                                                               @NotNull StringBuilder tagStrBuilder,
                                                               @NotNull DocElement docElement, @NotNull String name);
}
