package io.github.linwancen.plugin.show.java.doc;

import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumDoc {

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
                .map(exp -> exp.getText().replace("\"", ""))
                .collect(Collectors.joining("-"));
    }
}
