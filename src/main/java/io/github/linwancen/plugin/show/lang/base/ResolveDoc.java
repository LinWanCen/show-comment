package io.github.linwancen.plugin.show.lang.base;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
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
    public static <T extends SettingsInfo> String fromLineEnd(@SuppressWarnings("unused") @NotNull T info,
                                                              @NotNull PsiElement resolve,
                                                              @NotNull FileViewProvider resolveViewProvider) {
        @Nullable Document document = resolveViewProvider.getDocument();
        if (document == null) {
            return null;
        }
        @Nullable TextRange resolveRange = resolve.getTextRange();
        if (resolveRange == null) {
            return null;
        }
        int endOffset = resolveRange.getEndOffset();
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
        @Nullable TextRange docRange = docElement.getTextRange();
        if (docRange == null) {
            return null;
        }
        int docEnd = docRange.getEndOffset();
        int docLineNumber = document.getLineNumber(docEnd);
        if (lineNumber != docLineNumber) {
            return null;
        }
        return docElement.getText();
    }

    @Nullable
    public static <T extends SettingsInfo> String fromLineUp(@NotNull T info,
                                                             @NotNull PsiElement resolve,
                                                             @NotNull FileViewProvider resolveViewProvider,
                                                             @NotNull List<String> keywords) {
        @Nullable Document document = resolveViewProvider.getDocument();
        if (document == null) {
            return null;
        }
        // Do not use findChildOfType() because will find any comment at project view tree
        @Nullable PsiElement psiElement = PsiTreeUtil.getChildOfType(resolve, PsiComment.class);
        if (psiElement == null) {
            psiElement = Prev.prevCompactElement(info, resolve, document);
        }
        if (!keywords.isEmpty()) {
            while (psiElement != null) {
                String text = psiElement.getText();
                if (keywords.contains(text)) {
                    psiElement = Prev.prevCompactElement(info, psiElement, document);
                } else {
                    break;
                }
            }
        }
        @NotNull StringBuilder sb = new StringBuilder();
        boolean isComment = psiElement instanceof PsiComment;
        while (isComment) {
            String text = psiElement.getText();
            int thisStartOffset = psiElement.getTextRange().getStartOffset();
            psiElement = Prev.prevCompactElement(info, psiElement, document);
            isComment = psiElement instanceof PsiComment;
            if (!isComment && psiElement != null) {
                int prevEndOffset = psiElement.getTextRange().getEndOffset();
                int thisLineNumber = document.getLineNumber(thisStartOffset);
                int prevLineNumber = document.getLineNumber(prevEndOffset);
                boolean isNotCommentSameLine = thisLineNumber == prevLineNumber;
                if (isNotCommentSameLine) {
                    break;
                }
            }
            if (text != null) {
                sb.insert(0, "\n").insert(0, text);
                if (text.contains("/*")) {
                    break;
                }
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }
}
