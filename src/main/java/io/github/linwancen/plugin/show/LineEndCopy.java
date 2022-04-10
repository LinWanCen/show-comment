package io.github.linwancen.plugin.show;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import io.github.linwancen.plugin.show.line.FileViewToDocStrUtils;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

/**
 * on EditorPopupMenu
 */
public class LineEndCopy extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            return;
        }
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }
        FileViewProvider viewProvider = psiFile.getViewProvider();
        Document document = viewProvider.getDocument();
        if (document == null) {
            return;
        }
        int startLine = 0;
        int endLine = document.getLineCount() - 1;

        // if select
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
            int start = primaryCaret.getSelectionStart();
            int end = primaryCaret.getSelectionEnd();
            if (start != end) {
                startLine = document.getLineNumber(start);
                endLine = document.getLineNumber(end);
            }
        }

        AppSettingsState settings = AppSettingsState.getInstance();

        String textWithDoc = FileViewToDocStrUtils.textWithDoc(settings, document,
                startLine, endLine, project, file, viewProvider);
        StringSelection content = new StringSelection(textWithDoc);
        CopyPasteManager.getInstance().setContents(content);
    }
}