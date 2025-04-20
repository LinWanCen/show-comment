package io.github.linwancen.plugin.show.java.kt;

import io.github.linwancen.plugin.show.lang.base.BaseAnnoDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtAnnotated;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtProperty;
import org.jetbrains.kotlin.psi.KtPureClassOrObject;
import org.jetbrains.kotlin.psi.KtStringTemplateExpression;
import org.jetbrains.kotlin.psi.ValueArgument;
import org.jetbrains.kotlin.psi.ValueArgumentName;

public class AnnoDocKt extends BaseAnnoDoc<KtAnnotated> {

    public static AnnoDocKt INSTANCE = new AnnoDocKt();

    private AnnoDocKt() {}

    @Nullable
    protected String annoDocMatch(@NotNull KtAnnotated owner, @NotNull String[] arr) {
        if (typeMatch(owner, arr[0])) {
            for (KtAnnotationEntry entry : owner.getAnnotationEntries()) {
                String s = annoDocName(entry, arr);
                if (s != null) {
                    return s;
                }
            }
        }
        return null;
    }

    private static boolean typeMatch(KtAnnotated owner, @NotNull String type) {
        switch (type) {
            case "field":
                return owner instanceof KtProperty;
            case "method":
                return owner instanceof KtFunction;
            case "class":
                return owner instanceof KtPureClassOrObject;
            case "!doc":
            case "all":
                return true;
            default:
                return false;
        }
    }

    @Nullable
    private static String annoDocName(@NotNull KtAnnotationEntry entry, @NotNull String[] arr) {
        Name shortName = entry.getShortName();
        if (shortName == null) {
            return null;
        }
        String name = shortName.asString();
        String annoName = arr[1];
        int i = annoName.lastIndexOf('.');
        if (i > 0) {
            annoName = annoName.substring(i + 1);
        }
        if (!annoName.equals(name)) {
            return null;
        }
        for (ValueArgument argument : entry.getValueArguments()) {
            ValueArgumentName argumentName = argument.getArgumentName();
            String method = argumentName == null ? "value" : argumentName.getAsName().asString();
            if (arr[2].equals(method)) {
                KtExpression expression = argument.getArgumentExpression();
                if (expression instanceof KtStringTemplateExpression) {
                    String text = expression.getText();
                    if (text.length() >= 2) {
                        String s = text.substring(1, text.length() - 1);
                        if (!s.isEmpty()) {
                            return s;
                        }
                    }
                }
            }
        }
        return null;
    }
}
