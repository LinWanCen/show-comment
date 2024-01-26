package io.github.linwancen.plugin.show.java.doc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNewExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewDoc {

    private NewDoc() {}

    @Nullable
    public static PsiMethod newMethod(@NotNull PsiElement ref) {
        PsiElement parent = ref.getParent();
        if (!(parent instanceof PsiNewExpression)) {
            return null;
        }
        @NotNull PsiNewExpression psiNewExpression = (PsiNewExpression) parent;
        try {
            @Nullable PsiMethod resolve = psiNewExpression.resolveMethod();
            if (resolve == null) {
                return null;
            }
            PsiElement navElement = resolve.getNavigationElement();
            if (navElement instanceof PsiMethod) {
                return (PsiMethod) navElement;
            }
            return resolve;
        } catch (Throwable ignore) {
            // ignore
        }
        return null;
    }
}
