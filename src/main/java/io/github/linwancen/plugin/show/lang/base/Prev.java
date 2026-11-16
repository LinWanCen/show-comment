package io.github.linwancen.plugin.show.lang.base;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    private static boolean hasNonSymbolChar(@NotNull CharSequence text) {
        for (int i = 0, len = text.length(); i < len; i++) {
            char c = text.charAt(i);
            if (!((c >= ':' && c <= '@') || (c >= '[' && c <= '`') || (c >= '{' && c <= '~'))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static PsiElement refClassParent(@NotNull PsiElement element,
                                             @NotNull List<Class<? extends PsiElement>> refClass) {
        PsiElement parent = element.getParent();
        if (parent == null) {
            return null;
        }
        if (notAssignableFrom(refClass, parent)) {
            parent = parent.getParent();
            if (parent == null || notAssignableFrom(refClass, parent)) {
                return null;
            }
        }
        @NotNull String text = PsiUnSaveUtils.getText(element);
        if (!hasNonSymbolChar(text)) {
            return null;
        }
        return parent;
    }

    private static boolean notAssignableFrom(@NotNull List<Class<? extends PsiElement>> refClass,
                                             @NotNull PsiElement parent) {
        if (parent instanceof PsiLiteralValue) {
            return false;
        }
        Class<? extends PsiElement> parentClass = parent.getClass();
        for (int i = 0, size = refClass.size(); i < size; i++) {
            if (refClass.get(i).isAssignableFrom(parentClass)) {
                return false;
            }
        }
        return true;
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
        int firstNewLine = spaceText.indexOf('\n');
        if (firstNewLine >= 0 && spaceText.indexOf('\n', firstNewLine + 1) >= 0) {
            return null;
        }
        return element;
    }
}
