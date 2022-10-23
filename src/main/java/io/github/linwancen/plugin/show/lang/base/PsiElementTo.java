package io.github.linwancen.plugin.show.lang.base;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiElementTo {

    private PsiElementTo() {}

    public static @Nullable FileViewProvider viewProvider(@NotNull PsiElement resolve) {
        PsiFile psiFile = resolve.getContainingFile();
        if (psiFile == null) {
            return null;
        }
        VirtualFile resolveFile = psiFile.getVirtualFile();
        if (resolveFile == null) {
            return null;
        }
        return PsiManager.getInstance(resolve.getProject()).findViewProvider(resolveFile);
    }

    public static @NotNull Language language(@NotNull PsiElement element) {
        @NotNull Language lang = element.getLanguage();
        @Nullable Language base = lang.getBaseLanguage();
        if (base != null) {
            return base;
        }
        return lang;
    }
}
