package io.github.linwancen.plugin.show.lang.base;

import com.intellij.psi.PsiElement;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseTagLangDoc<DocElement> extends BaseLangDoc {

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocPrint(@NotNull T settingsInfo,
                                                                     @NotNull PsiElement resolve) {
        @Nullable DocElement docElement = toDocElement(settingsInfo, resolve);
        if (docElement == null && parseBaseComment(settingsInfo)) {
            return super.resolveDocPrint(settingsInfo, resolve);
        }
        return docElementToStr(settingsInfo, docElement);
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

    protected abstract <T extends SettingsInfo> boolean parseBaseComment(T settingsInfo);

    @Nullable
    protected abstract <T extends SettingsInfo> DocElement toDocElement(@NotNull T settingsInfo,
                                                                        @NotNull PsiElement resolve);

    /**
     * cut / * # not filter text
     */
    @NotNull
    protected abstract <T extends SettingsInfo> String descDoc(@NotNull T lineInfo, @NotNull DocElement docElement);

    protected abstract <T extends SettingsInfo> void appendTag(@NotNull T lineInfo,
                                                               @NotNull StringBuilder tagStrBuilder,
                                                               @NotNull DocElement docElement, @NotNull String name);
}
