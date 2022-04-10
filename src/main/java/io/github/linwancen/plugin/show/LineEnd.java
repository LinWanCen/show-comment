package io.github.linwancen.plugin.show;

import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import io.github.linwancen.plugin.show.line.FileViewToDocStrUtils;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class LineEnd extends EditorLinePainter {

    @Override
    public @Nullable Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project,
                                                                     @NotNull VirtualFile file, int lineNumber) {
        AppSettingsState settings = AppSettingsState.getInstance();
        if (!settings.showLineEndComment) {
            return null;
        }
        if (DumbService.isDumb(project)) {
            return null;
        }
        if (!file.exists()) {
            return null;
        }
        String doc = doc(project, file, lineNumber);
        if (doc == null) {
            return null;
        }
        TextAttributes textAttr = file.getFileType().equals(JsonFileType.INSTANCE)
                ? settings.lineEndJsonTextAttr
                : settings.lineEndTextAttr;
        LineExtensionInfo info = new LineExtensionInfo(settings.lineEndPrefix + doc, textAttr);
        return Collections.singletonList(info);
    }

    private static @Nullable String doc(@NotNull Project project,
                                        @NotNull VirtualFile file, int lineNumber) {
        FileViewProvider viewProvider = PsiManager.getInstance(project).findViewProvider(file);
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return null;
        }
        return doc(document, lineNumber, project, file, viewProvider);
    }

    @Nullable
    private static String doc(@NotNull Document document, int lineNumber,
                              @Nullable Project project,
                              @Nullable VirtualFile file,
                              @Nullable FileViewProvider viewProvider) {
        // lineNumber start 0, as 1 <= 1 should return
        if (document.getLineCount() <= lineNumber) {
            return null;
        }
        int startOffset = document.getLineStartOffset(lineNumber);
        int endOffset = document.getLineEndOffset(lineNumber);
        if (startOffset == endOffset) {
            return null;
        }
        String text = document.getText(new TextRange(startOffset, endOffset));
        return FileViewToDocStrUtils.doc(document, project, file, viewProvider, startOffset, endOffset, text);
    }
}
