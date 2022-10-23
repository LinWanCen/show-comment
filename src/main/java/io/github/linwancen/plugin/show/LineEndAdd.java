package io.github.linwancen.plugin.show;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import io.github.linwancen.plugin.show.bean.FileInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * on ProjectViewPopupMenu
 */
public class LineEndAdd extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        @Nullable Project project = event.getProject();
        if (project == null) {
            return;
        }
        @Nullable VirtualFile[] files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (files == null) {
            return;
        }
        @NotNull ListPopup confirmation = JBPopupFactory.getInstance().createConfirmation(
                "Add Line Comment?", "Add and replace files!", "Don't add.",
                () -> ApplicationManager.getApplication().runReadAction(() -> addDocAll(project, files)), 2);
        confirmation.showInFocusCenter();
    }

    private void addDocAll(@NotNull Project project, @NotNull VirtualFile[] files) {
        for (@NotNull VirtualFile file : files) {
            VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor<Void>() {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    if (!file.isDirectory()) {
                        ApplicationManager.getApplication().runReadAction(() -> addDoc(project, file));
                    }
                    return true;
                }
            });
        }
    }

    private void addDoc(@NotNull Project project, @NotNull VirtualFile file) {
        @Nullable FileInfo fileInfo = FileInfo.of(file, project);
        if (fileInfo == null) {
            return;
        }
        int startLine = 0;
        int endLine = fileInfo.document.getLineCount() - 1;
        @NotNull String textWithDoc = LineEnd.textWithDoc(fileInfo, startLine, endLine);
        WriteCommandAction.runWriteCommandAction(project, () ->
                fileInfo.document.replaceString(0, fileInfo.document.getTextLength() - 1, textWithDoc)
        );
    }
}