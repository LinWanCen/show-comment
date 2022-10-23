package io.github.linwancen.plugin.show.jump;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JsonRef<T extends PsiElement> extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    final T psiField;
    final List<T> tips;

    public JsonRef(@NotNull PsiElement element, @NotNull T psiField, @NotNull List<T> tips) {
        super(element);
        this.psiField = psiField;
        this.tips = tips;
    }

    /**
     * do not use it because PsiReference.resolveReference() is @Experimental
     */
    @Override
    public @NotNull ResolveResult[] multiResolve(boolean incompleteCode) {
        return new ResolveResult[]{new PsiElementResolveResult(psiField)};
    }

    @Override
    public @Nullable PsiElement resolve() {
        return psiField;
    }

    /**
     * I don't know how to use it
     */
    @Override
    public @NotNull Object[] getVariants() {
        return tips.toArray();
    }
}