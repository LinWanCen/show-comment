package io.github.linwancen.plugin.show.java.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.javadoc.*;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * @deprecated BaseTagLangDoc
 */
public class PsiDocToStrDoc {

    private PsiDocToStrDoc() {}

    @Nullable
    public static String text(@Nullable PsiDocComment psiDocComment, boolean isTree) {
        if (psiDocComment == null) {
            return null;
        }
        AppSettingsState appSettings = AppSettingsState.getInstance();
        int lineCount = 0;
        // JavaLangDoc.descDoc
        StringBuilder sb = new StringBuilder();
        PsiElement[] elements = psiDocComment.getDescriptionElements();
        for (PsiElement element : elements) {
            if (appendElementText(sb, element)) {
                lineCount++;
            }
            boolean countOver = appSettings.lineEndCount > 0 && lineCount >= appSettings.lineEndCount;
            boolean lenOver = appSettings.lineEndLen > 0 && sb.length() >= appSettings.lineEndLen;
            if (countOver || lenOver) {
                break;
            }
        }
        StringBuilder tags = tags(psiDocComment, isTree, appSettings);
        // BaseTagLangDoc.docElementToStr
        if (tags.length() > 0) {
            if (sb.length() > 0) {
                sb.append("@ ");
            }
            sb.append(tags);
        }
        String text = sb.toString();
        if (text.trim().length() == 0) {
            return null;
        }
        ProjectSettingsState projectSettings = ProjectSettingsState.getInstance(psiDocComment.getProject());
        return DocFilter.filterDoc(text, appSettings, projectSettings);
    }

    @NotNull
    private static StringBuilder tags(@NotNull PsiDocComment psiDocComment, boolean isTree,
                                      AppSettingsState appSettings) {
        // JavaLangDoc.descDoc
        StringBuilder sb = new StringBuilder();
        String[] names = isTree ? appSettings.treeTags : appSettings.lineTags;
        for (String name : names) {
            PsiDocTag[] tags = psiDocComment.findTagsByName(name);
            for (PsiDocTag tag : tags) {
                // @see @param should use getDataElements()
                PsiDocTagValue value = tag.getValueElement();
                if (value != null) {
                    addHtml(sb, value.getText());
                }
            }
        }
        return sb;
    }

    /**
     * @return is new line
     */
    private static boolean appendElementText(StringBuilder sb, PsiElement element) {
        // JavaLangDoc.appendElementText
        if (element instanceof PsiDocToken) {
            PsiDocToken psiDocToken = (PsiDocToken) element;
            addHtml(sb, psiDocToken.getText());
        }
        if (element instanceof PsiInlineDocTag) {
            PsiInlineDocTag psiInlineDocTag = (PsiInlineDocTag) element;
            PsiElement[] children = psiInlineDocTag.getChildren();
            if (children.length > 3) {
                addHtml(sb, children[3].getText());
            }
        }
        return element instanceof PsiWhiteSpace && sb.length() > 0;
    }

    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]++>");

    private static void addHtml(StringBuilder sb, String s) {
        // DocFilter.addHtml
        String deleteHtml = HTML_PATTERN.matcher(s.trim()).replaceAll("");
        if (deleteHtml.length() > 0) {
            sb.append(deleteHtml).append(" ");
        }
    }
}
