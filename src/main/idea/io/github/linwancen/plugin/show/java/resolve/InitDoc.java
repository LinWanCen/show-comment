package io.github.linwancen.plugin.show.java.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.psi.PsiNamedElement;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 你好
 */
public class InitDoc {

    private InitDoc() {}

    public static <T extends SettingsInfo> String initDoc(@NotNull T info, @Nullable String doc,
                                                          @Nullable PsiElement initializer, @NotNull PsiNamedElement variable) {
        if (!info.appSettings.initValue) {
            return doc;
        }
        String name = variable.getName();
        if (name != null && name.length() <= 1) {
            return doc;
        }
        if (!(initializer instanceof PsiLiteralValue)) {
            return doc;
        }
        @Nullable Object value = ((PsiLiteralValue) initializer).getValue();
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
        if (doc.contains(init) || (name != null && name.contains(init))) {
            return doc;
        }
        return doc + "　= " + init;
    }
}
