package io.github.linwancen.plugin.show.lang.base;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResolveDoc {

    private ResolveDoc() {}

    @Nullable
    public static <T extends SettingsInfo> String fromLineEnd(@SuppressWarnings("unused") @NotNull T lineInfo,
                                                              @NotNull PsiElement resolve,
                                                              @NotNull FileViewProvider resolveViewProvider) {
        @Nullable Document document = resolveViewProvider.getDocument();
        if (document == null) {
            return null;
        }
        int endOffset = resolve.getTextRange().getEndOffset();
        int lineNumber;
        int resolveEndOffset;
        try {
            lineNumber = document.getLineNumber(endOffset);
            resolveEndOffset = document.getLineEndOffset(lineNumber);
        } catch (Exception e) {
            return null;
        }
        // end over will return last
        @Nullable PsiElement psiElement = resolveViewProvider.findElementAt(resolveEndOffset);
        if (psiElement == null) {
            return null;
        }
        @Nullable PsiElement docElement = PsiTreeUtil.prevVisibleLeaf(psiElement);
        if (!(docElement instanceof PsiComment)) {
            return null;
        }
        int docEnd = docElement.getTextRange().getEndOffset();
        int docLineNumber = document.getLineNumber(docEnd);
        if (lineNumber != docLineNumber) {
            return null;
        }
        return docElement.getText();
    }

    @Nullable
    public static <T extends SettingsInfo> String fromLineUp(@NotNull T lineInfo,
                                                             @NotNull PsiElement resolve,
                                                             @NotNull FileViewProvider resolveViewProvider,
                                                             @NotNull List<String> keywords) {
        @Nullable Document document = resolveViewProvider.getDocument();
        if (document == null) {
            return null;
        }
        @Nullable PsiElement psiElement = Prev.prevCompactElement(lineInfo, resolve, document);
        if (!keywords.isEmpty()) {
            while (psiElement != null) {
                String text = psiElement.getText();
                if (keywords.contains(text)) {
                    psiElement = Prev.prevCompactElement(lineInfo, psiElement, document);
                } else {
                    break;
                }
            }
        }
        @NotNull StringBuilder sb = new StringBuilder();
        while (psiElement instanceof PsiComment) {
            String text = psiElement.getText();
            if (text != null) {
                sb.insert(0, "\n").insert(0, text);
            }
            psiElement = Prev.prevCompactElement(lineInfo, psiElement, document);
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }
}
