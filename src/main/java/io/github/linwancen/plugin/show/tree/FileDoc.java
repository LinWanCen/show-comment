package io.github.linwancen.plugin.show.tree;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import io.github.linwancen.plugin.show.settings.AbstractSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;

class FileDoc {
    private FileDoc() {}

    @Nullable
    static String fileDoc(@NotNull PsiFile psiFile, @NotNull AbstractSettingsState settings) {
        if (!settings.fileDocEffect) {
            return null;
        }
        @NotNull Map<String, Pattern[]> patternMap = settings.fileDoc;
        @Nullable VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        @Nullable String extension = virtualFile.getExtension();
        if (extension == null) {
            extension = virtualFile.getName();
        }
        @Nullable Pattern[] patterns = patternMap.get(extension);
        if (patterns == null) {
            return null;
        }
        return FilePatternsDoc.filePatternsDoc(psiFile, patterns);
    }
}
