package io.github.linwancen.plugin.show.java.resolve;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiVariable;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InitDoc {

    private InitDoc() {}

    public static <T extends SettingsInfo> String initDoc(@NotNull T info, @Nullable String doc,
                                                          @Nullable PsiExpression initializer, @NotNull PsiVariable variable) {
        if (!info.appSettings.initValue) {
            return doc;
        }
        if (!(initializer instanceof PsiLiteralExpression)) {
            return doc;
        }
        @Nullable Object value = ((PsiLiteralExpression) initializer).getValue();
        if (value == null) {
            return doc;
        }
        String init = value.toString();
        if (init.isBlank()) {
            init = '"' + init + '"';
        }
        // use not ASCII space not skip
        if (doc == null) {
            return "　= " + init;
        }
        // skip like 1-YES
        String name = variable.getName();
        if (doc.contains(init) || (name != null && name.contains(init))) {
            return doc;
        }
        return doc + "　= " + init;
    }
}
