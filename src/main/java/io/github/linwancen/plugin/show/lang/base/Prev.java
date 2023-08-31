package io.github.linwancen.plugin.show.lang.base;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class Prev {

    private Prev() {}

    @Nullable
    public static PsiElement prevRefChild(@NotNull LineInfo lineInfo, @NotNull PsiElement element,
                                          @NotNull Class<? extends PsiElement> refClass) {
        PsiElement prevParent = refClassParent(element, refClass);
        while ((element = PsiTreeUtil.prevVisibleLeaf(element)) != null) {
            if (element.getTextRange().getEndOffset() < lineInfo.startOffset) {
                return null;
            }
            @Nullable PsiElement parent = refClassParent(element, refClass);
            if (parent != null) {
                // skip b in a.b.c
                if (prevParent != null
                        && prevParent.getTextRange().getEndOffset() < lineInfo.endOffset
                        && PsiTreeUtil.findChildOfType(prevParent, refClass) == parent) {
                    prevParent = parent;
                } else {
                    return element;
                }
            }
        }
        return null;
    }

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[^" +
            ":-@" +
            "\\[-`" +
            "{-~" +
            "]++");

    @Nullable
    private static PsiElement refClassParent(@NotNull PsiElement element,
                                             @NotNull Class<? extends PsiElement> refClass) {
        String text = element.getText();
        if (!SYMBOL_PATTERN.matcher(text).find()) {
            return null;
        }
        PsiElement parent = element.getParent();
        if (parent == null) {
            return null;
        }
        if (!refClass.isAssignableFrom(parent.getClass())) {
            parent = parent.getParent();
            if (parent == null) {
                return null;
            }
        }
        if (!refClass.isAssignableFrom(parent.getClass())) {
            return null;
        }
        return parent;
    }

    public static @Nullable <T extends SettingsInfo> PsiElement prevCompactElement(
            @SuppressWarnings("unused") @NotNull T lineInfo, @NotNull PsiElement resolve, @NotNull Document document) {
        @Nullable PsiElement element = PsiTreeUtil.prevVisibleLeaf(resolve);
        if (element == null) {
            return null;
        }
        int endOffset = element.getTextRange().getEndOffset();
        int startOffset = resolve.getTextRange().getStartOffset();
        String spaceText;
        try {
            spaceText = document.getText(TextRange.create(endOffset, startOffset));
        } catch (Exception e) {
            return null;
        }
        @NotNull String replace = spaceText.replace("\n", "");
        if (spaceText.length() - replace.length() > 1) {
            return null;
        }
        return element;
    }
}
