package io.github.linwancen.plugin.show.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.javadoc.*;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class PsiDocToStrDoc {

    private PsiDocToStrDoc() {}

    @Nullable
    public static String text(@Nullable PsiDocComment psiDocComment, boolean isTree) {
        if (psiDocComment == null) {
            return null;
        }
        AppSettingsState appSettings = AppSettingsState.getInstance();
        int lineCount = 0;
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
        if (tags.length() > 0) {
            if (sb.length() > 0) {
                sb.append("@ ");
            }
            sb.append(tags);
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    @NotNull
    private static StringBuilder tags(@NotNull PsiDocComment psiDocComment, boolean isTree,
                                                  AppSettingsState appSettings) {
        StringBuilder sb = new StringBuilder();
        PsiDocTag[] tags = psiDocComment.getTags();
        for (PsiDocTag tag : tags) {
            String name = tag.getName();
            if (isTree ? appSettings.treeTags.contains(name) : appSettings.lineTags.contains(name)) {
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
        String deleteHtml = HTML_PATTERN.matcher(s.trim()).replaceAll("");
        if (deleteHtml.length() > 0) {
            sb.append(deleteHtml).append(" ");
        }
    }
}
