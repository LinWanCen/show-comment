package io.github.linwancen.plugin.show;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import io.github.linwancen.plugin.show.line.FileViewToDocStrUtils;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;

/**
 * on ProjectViewPopupMenu
 */
public class LineEndAdd extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        VirtualFile[] files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (files == null) {
            return;
        }
        ListPopup confirmation = JBPopupFactory.getInstance().createConfirmation(
                "Add Line Comment?", "Add and replace files!", "Don't add.",
                () -> addDocAll(project, files), 2);
        confirmation.showInFocusCenter();
    }

    private void addDocAll(@NotNull Project project, @NotNull VirtualFile[] files) {
        AppSettingsState settings = AppSettingsState.getInstance();
        for (VirtualFile file : files) {
            VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor<Void>() {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    if (!file.isDirectory()) {
                        addDoc(project, file, settings);
                    }
                    return true;
                }
            });
        }
    }

    private void addDoc(@NotNull Project project, @NotNull VirtualFile file, @NotNull AppSettingsState settings) {
        FileViewProvider viewProvider = PsiManager.getInstance(project).findViewProvider(file);
        if (viewProvider == null) {
            return;
        }
        Document document = viewProvider.getDocument();
        if (document == null) {
            return;
        }
        int startLine = 0;
        int endLine = document.getLineCount() - 1;
        String textWithDoc = FileViewToDocStrUtils.textWithDoc(settings, document,
                startLine, endLine, project, file, viewProvider);
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.replaceString(0, document.getTextLength() - 1, textWithDoc)
        );
    }
}