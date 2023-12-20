package io.github.linwancen.plugin.show.lang.base;

import com.intellij.psi.PsiElement;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseTagLangDoc<DocElement> extends BaseLangDoc {

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocPrint(@NotNull T info,
                                                                     @NotNull PsiElement resolve) {
        @Nullable DocElement docElement = toDocElement(info, resolve);
        if (docElement == null && parseBaseComment(info)) {
            return super.resolveDocPrint(info, resolve);
        }
        return docElementToStr(info, docElement);
    }

    @Nullable
    public <T extends SettingsInfo> String docElementToStr(@NotNull T info, @Nullable DocElement docElement) {
        if (docElement == null) {
            return null;
        }
        // desc
        @NotNull String descDoc = descDoc(info, docElement).trim();
        @NotNull String desc = DocFilter.filterDoc(descDoc, info.globalSettings, info.projectSettings);
        // tag
        @NotNull StringBuilder tagStrBuilder = new StringBuilder();
        @NotNull String[] names = info.tagNames();
        for (@NotNull String name : names) {
            appendTag(info, tagStrBuilder, docElement, name);
        }
        if (!desc.isEmpty()) {
            if (tagStrBuilder.length() > 0) {
                tagStrBuilder.insert(0, " @ ");
            }
            tagStrBuilder.insert(0, desc);
        }
        @NotNull String text = tagStrBuilder.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return text;
    }

    protected abstract <T extends SettingsInfo> boolean parseBaseComment(T info);

    @Nullable
    protected abstract <T extends SettingsInfo> DocElement toDocElement(@NotNull T info,
                                                                        @NotNull PsiElement resolve);

    /**
     * cut / * # not filter text
     */
    @NotNull
    protected abstract <T extends SettingsInfo> String descDoc(@NotNull T info, @NotNull DocElement docElement);

    protected abstract <T extends SettingsInfo> void appendTag(@NotNull T info,
                                                               @NotNull StringBuilder tagStrBuilder,
                                                               @NotNull DocElement docElement, @NotNull String name);
}
