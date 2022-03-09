package io.github.linwancen.plugin.show;

import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.doc.DocTextUtils;
import io.github.linwancen.plugin.show.line.LineDocLeftToRightUtils;
import io.github.linwancen.plugin.show.line.LineDocRightToLeftUtils;
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
        FileViewProvider viewProvider = PsiManager.getInstance(project).findViewProvider(file);
        PsiDocComment docComment = doc(project, viewProvider, lineNumber);
        String comment = DocTextUtils.text(docComment);
        if (comment == null) {
            return null;
        }
        TextAttributes textAttr = file.getFileType().equals(JsonFileType.INSTANCE)
                ? settings.lineEndJsonTextAttr
                : settings.lineEndTextAttr;
        LineExtensionInfo info = new LineExtensionInfo(settings.lineEndPrefix + comment, textAttr);
        return Collections.singletonList(info);
    }

    private static @Nullable PsiDocComment doc(Project project, FileViewProvider viewProvider, int lineNumber) {
        if (viewProvider == null) {
            return null;
        }
        Document document = viewProvider.getDocument();
        if (document == null) {
            return null;
        }
        // lineNumber start 0, as 1 <= 1 should return
        if (document.getLineCount() <= lineNumber) {
            return null;
        }
        int startOffset = document.getLineStartOffset(lineNumber);
        int endOffset = document.getLineEndOffset(lineNumber);
        if (startOffset == endOffset) {
            return null;
        }
        if (AppSettingsState.getInstance().findElementRightToLeft) {
            return LineDocRightToLeftUtils.rightDoc(viewProvider, startOffset, endOffset);
        }
        return LineDocLeftToRightUtils.leftDoc(viewProvider, document, startOffset, endOffset);
    }
}
