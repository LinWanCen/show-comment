package io.github.linwancen.plugin.show.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class PsiDocToStrDoc {

    private PsiDocToStrDoc() {}

    @Nullable
    public static String text(@Nullable PsiDocComment psiDocComment) {
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
        if (sb.length() == 0) {
            return null;
        }
        sb.insert(0, " ");
        return sb.toString();
    }

    private static boolean appendElementText(StringBuilder sb, PsiElement element) {
        if (element instanceof PsiDocToken) {
            PsiDocToken psiDocToken = (PsiDocToken) element;
            sb.append(deleteHtml(psiDocToken.getText()));
            sb.append("  ");
            return true;
        }
        if (element instanceof PsiInlineDocTag) {
            PsiInlineDocTag psiInlineDocTag = (PsiInlineDocTag) element;
            PsiElement[] children = psiInlineDocTag.getChildren();
            if (children.length > 2) {
                sb.append(deleteHtml(children[2].getText()));
                sb.append("  ");
            }
        }
        return false;
    }

    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]++>");

    private static String deleteHtml(String s) {
        return HTML_PATTERN.matcher(s.trim()).replaceAll("");
    }
}
