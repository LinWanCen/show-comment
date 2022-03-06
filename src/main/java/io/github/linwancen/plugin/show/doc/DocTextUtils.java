package io.github.linwancen.plugin.show.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DocTextUtils {

    private DocTextUtils() {}

    @Nullable
    public static String text(@Nullable PsiDocComment psiDocComment) {
        if (psiDocComment == null) {
            return null;
        }
        AppSettingsState appSettings = AppSettingsState.getInstance();
        List<String> comments = new ArrayList<>();
        PsiElement[] elements = psiDocComment.getDescriptionElements();
        for (PsiElement element : elements) {
            if (element instanceof PsiDocToken) {
                PsiDocToken psiDocToken = (PsiDocToken) element;
                comments.add(psiDocToken.getText());
            } else if (element instanceof PsiInlineDocTag) {
                PsiInlineDocTag psiInlineDocTag = (PsiInlineDocTag) element;
                PsiElement[] children = psiInlineDocTag.getChildren();
                if (children.length > 2) {
                    comments.add(children[2].getText());
                }
            }
            if (appSettings.lineEndCount > 0 && comments.size() >= appSettings.lineEndCount) {
                break;
            }
        }
        if (comments.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(" ");
        for (String s : comments) {
            sb.append(s.trim().replace("<br>", "")).append("  ");
        }
        return sb.toString();
    }
}
