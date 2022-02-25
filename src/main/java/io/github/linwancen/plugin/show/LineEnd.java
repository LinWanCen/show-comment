package io.github.linwancen.plugin.show;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
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
        PsiDocComment docComment = docOwnerFrom(viewProvider, lineNumber);
        String comment = DocTextUtils.text(docComment);
        if (comment == null) {
            return null;
        }
        LineExtensionInfo info = new LineExtensionInfo(" //" + comment, settings.lineEndTextAttr);
        return Collections.singletonList(info);
    }

    private static @Nullable PsiDocComment docOwnerFrom(FileViewProvider viewProvider, int lineNumber) {
        if (viewProvider == null || !viewProvider.hasLanguage(JavaLanguage.INSTANCE)) {
            return null;
        }
        Document document = viewProvider.getDocument();
        if (document == null) {
            return null;
        }
        if (document.getLineCount() < lineNumber) {
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
