package io.github.linwancen.plugin.show.tree;

import com.intellij.lang.FileASTNode;
import com.intellij.psi.PsiFile;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FilePatternsDoc {
    private FilePatternsDoc() {}

    @Nullable
    static String filePatternsDoc(@NotNull PsiFile psiFile, @NotNull Pattern[] patterns) {
        return filePatternsDoc(null, psiFile, patterns);
    }

    @Nullable
    static String filePatternsDoc(@Nullable SettingsInfo info, @NotNull PsiFile psiFile, @NotNull Pattern[] patterns) {
        @Nullable FileASTNode fileNode = psiFile.getNode();
        if (fileNode == null) {
            return null;
        }
        @NotNull String text = fileNode.getText();
        for (@NotNull Pattern pattern : patterns) {
            @NotNull Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String group = matcher.group(1);
                if (group != null) {
                    if (info != null) {
                        group = DocFilter.cutDoc(group, info, true);
                    }
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
