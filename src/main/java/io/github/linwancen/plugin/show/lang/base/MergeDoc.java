package io.github.linwancen.plugin.show.lang.base;

import org.jetbrains.annotations.NotNull;

public class MergeDoc {
    @NotNull
    static String mergeDoc(@NotNull String beforeText, @NotNull String text,
                           @NotNull String beforeDoc, @NotNull String doc, boolean getToSet) {
        if (!beforeText.startsWith("set")) {
            // doc same
            if (beforeDoc.equals(doc)) {
                return doc;
            }
            return beforeDoc + " | " + doc;
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
            return doc + (same ? " ==> " : " --> ") + beforeDoc;
        } else {
            return beforeDoc + (same ? " <== " : " <-- ") + doc;
        }
    }
}
