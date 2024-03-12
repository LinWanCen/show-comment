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

    @Nullable
    public static BaseLangDoc lineEnd(@NotNull PsiElement element) {
        @Nullable Language language = element.getLanguage();
        while (true) {
            @Nullable BaseLangDoc lineEnd = BaseLangDoc.LANG_DOC_MAP.get(language.getID());
            if (lineEnd != null) {
                return lineEnd;
            }
            language = language.getBaseLanguage();
            if (language == null) {
                return null;
            }
        }
    }
}
