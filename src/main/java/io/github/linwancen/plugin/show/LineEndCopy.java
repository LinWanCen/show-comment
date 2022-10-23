package io.github.linwancen.plugin.show;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import io.github.linwancen.plugin.show.bean.FileInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.StringSelection;

/**
 * on EditorPopupMenu
 */
public class LineEndCopy extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ApplicationManager.getApplication().runReadAction(() -> copyWithDoc(event));
    }

    private void copyWithDoc(@NotNull AnActionEvent event) {
        @Nullable FileInfo fileInfo = FileInfo.of(event);
        if (fileInfo == null) {
            return;
        }
        int startLine = 0;
        int endLine = fileInfo.document.getLineCount() - 1;
        // if select
        @Nullable Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            @NotNull Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
            int start = primaryCaret.getSelectionStart();
            int end = primaryCaret.getSelectionEnd();
            try {
                startLine = fileInfo.document.getLineNumber(start);
                endLine = fileInfo.document.getLineNumber(end);
            } catch (Exception e) {
                return;
            }
        }
        @NotNull String textWithDoc = LineEnd.textWithDoc(fileInfo, startLine, endLine);
        @NotNull StringSelection content = new StringSelection(textWithDoc);
        CopyPasteManager.getInstance().setContents(content);
    }
}