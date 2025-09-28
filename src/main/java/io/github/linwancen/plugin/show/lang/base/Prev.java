package io.github.linwancen.plugin.show.lang.base;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

public class Prev {

    private Prev() {}

    @Nullable
    public static PsiElement prevRefChild(@NotNull LineInfo info, @NotNull PsiElement element,
                                          @NotNull List<Class<? extends PsiElement>> refClass) {
        @Nullable PsiElement prevParent = refClassParent(element, refClass);
        while ((element = PsiTreeUtil.prevVisibleLeaf(element)) != null) {
            if (element.getTextRange().getEndOffset() < info.startOffset) {
                return null;
            }
            @Nullable PsiElement parent = refClassParent(element, refClass);
            if (parent != null) {
                // skip b in a.b.c
                if (prevParent != null
                        && prevParent.getTextRange().getEndOffset() < info.endOffset
                        && prevParentChildEqParent(prevParent, refClass, parent)) {
                    prevParent = parent;
                } else {
                    if (!(element instanceof PsiComment)) {
                        return element;
                    }
                }
            }
        }
        return null;
    }

    private static boolean prevParentChildEqParent(@NotNull PsiElement prevParent,
                                                   @NotNull List<Class<? extends PsiElement>> refClass,
                                                   @NotNull PsiElement parent) {
        for (@NotNull Class<? extends PsiElement> c : refClass) {
            if (PsiTreeUtil.findChildOfType(prevParent, c) == parent) {
                return true;
            }
        }
        return false;
    }

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[^" +
            ":-@" +
            "\\[-`" +
            "{-~" +
            "]++");

    @Nullable
    private static PsiElement refClassParent(@NotNull PsiElement element,
                                             @NotNull List<Class<? extends PsiElement>> refClass) {
        @NotNull String text = PsiUnSaveUtils.getText(element);
        if (!SYMBOL_PATTERN.matcher(text).find()) {
            return null;
        }
        PsiElement parent = element.getParent();
        if (parent == null) {
            return null;
        }
        if (notAssignableFrom(refClass, parent)) {
            parent = parent.getParent();
            if (parent == null) {
                return null;
            }
        }
        if (notAssignableFrom(refClass, parent)) {
            return null;
        }
        return parent;
    }

    private static boolean notAssignableFrom(@NotNull List<Class<? extends PsiElement>> refClass,
                                             @NotNull PsiElement parent) {
        return refClass.stream().noneMatch(it -> it.isAssignableFrom(parent.getClass()));
    }

    public static @Nullable <T extends SettingsInfo> PsiElement prevCompactElement(
            @SuppressWarnings("unused") @NotNull T info, @NotNull PsiElement resolve, @NotNull Document document) {
        @Nullable PsiElement element = PsiTreeUtil.prevVisibleLeaf(resolve);
        @Nullable PsiComment psiComment = PsiTreeUtil.getParentOfType(element, PsiComment.class);
        if (psiComment != null) {
            return psiComment;
        }
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
