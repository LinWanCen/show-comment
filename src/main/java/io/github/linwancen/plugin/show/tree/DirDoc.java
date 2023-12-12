package io.github.linwancen.plugin.show.tree;

import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import io.github.linwancen.plugin.show.settings.AbstractSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;

class DirDoc {

    @Nullable
    static String dirDoc(@NotNull PsiDirectoryNode node, @NotNull AbstractSettingsState settings) {
        if (!settings.dirDocEffect) {
            return null;
        }
        @NotNull Map<String, Pattern[]> patternMap = settings.dirDoc;
        @Nullable PsiDirectory psiDirectory = node.getValue();
        if (psiDirectory == null) {
            return null;
        }
        for (@NotNull Map.Entry<String, Pattern[]> patterns : patternMap.entrySet()) {
            @Nullable PsiFile psiFile = psiDirectory.findFile(patterns.getKey());
            if (psiFile != null) {
                @Nullable String doc = FilePatternsDoc.filePatternsDoc(psiFile, patterns.getValue());
                if (doc != null) {
                    return doc;
                }
            }
        }
        return null;
    }
}
