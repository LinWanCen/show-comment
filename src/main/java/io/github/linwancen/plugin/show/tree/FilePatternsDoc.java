package io.github.linwancen.plugin.show.tree;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FilePatternsDoc {
    private FilePatternsDoc() {}

    @Nullable
    static String filePatternsDoc(@NotNull PsiFile psiFile, @NotNull Pattern[] patterns) {
        @NotNull CharSequence text = psiFile.getViewProvider().getContents();
        if (text.length() == 0) {
            return null;
        }
        for (@NotNull Pattern pattern : patterns) {
            @NotNull Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String group = matcher.group(1);
                if (group != null) {
                    group = group.trim();
                    if (!group.isEmpty()) {
                        return group;
                    }
                }
            }
        }
        return null;
    }
}
