package io.github.linwancen.plugin.show.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.Nullable;

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
            if (appSettings.lineEndCount > 0
                    && lineCount >= appSettings.lineEndCount
                    || appSettings.lineEndLen > 0
                    && sb.length() >= appSettings.lineEndLen) {
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
            sb.append(psiDocToken.getText().trim().replace("<br>", "")).append("  ");
            return true;
        }
        if (element instanceof PsiInlineDocTag) {
            PsiInlineDocTag psiInlineDocTag = (PsiInlineDocTag) element;
            PsiElement[] children = psiInlineDocTag.getChildren();
            if (children.length > 2) {
                sb.append(children[2].getText().trim().replace("<br>", "")).append("  ");
            }
        }
        return false;
    }
}
