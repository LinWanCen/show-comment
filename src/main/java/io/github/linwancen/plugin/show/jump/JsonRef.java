package io.github.linwancen.plugin.show.jump;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JsonRef<T extends PsiElement> extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    @NotNull
    final T psiField;
    @NotNull
    final List<Object> variants;

    public JsonRef(@NotNull PsiElement element, @NotNull T psiField, @NotNull List<Object> variants) {
        super(element);
        this.psiField = psiField;
        this.variants = variants;
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
        return variants.toArray();
    }
}