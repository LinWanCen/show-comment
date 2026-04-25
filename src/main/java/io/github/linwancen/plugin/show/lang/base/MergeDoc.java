package io.github.linwancen.plugin.show.lang.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MergeDoc {
    static void mergeDoc(@NotNull String beforeText, @NotNull String beforeDoc,
                         @Nullable String text, @Nullable String doc,
                         @NotNull StringBuilder builder, boolean getToSet) {
        if (text == null || doc == null) {
            builder.insert(0, beforeDoc);
            return;
        }
        if (!beforeText.startsWith("set")) {
            // doc diff
            if (!beforeDoc.equals(doc)) {
                builder.insert(0, " | ").insert(0, beforeDoc);
            }
            return;
        }
        beforeText = beforeText.substring(3);
        if (text.startsWith("get")) {
            text = text.substring(3);
        } else if (text.startsWith("is")) {
            text = text.substring(2);
        }
        // method name, not doc same
        boolean same = beforeText.equals(text);
        if (getToSet) {
            // because lambda is -> or =>
            builder.append(same ? " ==> " : " --> ").append(beforeDoc);
        } else {
            builder.insert(0, same ? " <== ": " <-- ").insert(0, beforeDoc);
        }
    }
}
