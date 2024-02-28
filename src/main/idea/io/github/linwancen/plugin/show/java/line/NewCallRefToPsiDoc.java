package io.github.linwancen.plugin.show.java.line;

import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * call OwnerToPsiDocSkip
 */
public class NewCallRefToPsiDoc {

    private NewCallRefToPsiDoc() {}

    @Nullable
    public static <T extends SettingsInfo> PsiDocComment javaCodeDoc(@NotNull T info,
                                                                     @Nullable PsiJavaCodeReferenceElement ref) {
        if (ref == null) {
            return null;
        }
        @Nullable PsiElement resolve = null;
        try {
            resolve = ref.resolve();
        } catch (Throwable ignore) {
            // ignore
        }
        if (resolve instanceof PsiDocCommentOwner) {
            try {
                PsiElement navElement = resolve.getNavigationElement();
                if (navElement instanceof PsiDocCommentOwner) {
                    resolve = navElement;
                }
            } catch (Throwable ignore) {
                // ignore
            }
            @NotNull PsiDocCommentOwner psiDocCommentOwner = (PsiDocCommentOwner) resolve;
            return OwnerToPsiDocSkip.refDoc(info, psiDocCommentOwner);
        }
        return null;
    }
}
