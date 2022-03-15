package io.github.linwancen.plugin.show.json;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JsonRef extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    final PsiField psiField;
    final List<PsiField> tips;

    public JsonRef(@NotNull PsiElement element, @NotNull PsiField psiField, @NotNull List<PsiField> tips) {
        super(element);
        this.psiField = psiField;
        this.tips = tips;
    }

    /**
     * do not use it because PsiReference.resolveReference() is @Experimental
     */
    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        return new ResolveResult[]{new PsiElementResolveResult(psiField)};
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return psiField;
    }

    /**
     * I don't know how to use it
     */
    @Override
    public Object @NotNull [] getVariants() {
        return tips.toArray();
    }
}