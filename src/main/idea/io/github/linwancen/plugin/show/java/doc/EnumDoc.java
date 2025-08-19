package io.github.linwancen.plugin.show.java.doc;

import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import io.github.linwancen.plugin.show.lang.base.PsiUnSaveUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumDoc {

    private EnumDoc() {}

    /**
     * Yes(1, "Yes") show 1-Yes
     */
    @Nullable
    public static String enumDoc(@NotNull PsiEnumConstant psiEnumConstant) {
        @Nullable PsiExpressionList args = psiEnumConstant.getArgumentList();
        if (args == null) {
            return null;
        }
        @NotNull PsiExpression[] exps = args.getExpressions();
        if (exps.length == 0) {
            return null;
        }
        return Arrays.stream(exps)
                .map(exp -> PsiUnSaveUtils.getText(exp).replace("\"", ""))
                .collect(Collectors.joining("-"));
    }
}
