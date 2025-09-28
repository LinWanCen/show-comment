package io.github.linwancen.plugin.show;

import com.intellij.ide.actions.CopyReferenceAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import io.github.linwancen.plugin.show.bean.FileInfo;
import io.github.linwancen.plugin.show.settings.ShowBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * on ProjectViewPopupMenu
 */
public class LineEndAdd extends CopyReferenceAction {

    private static final Logger LOG = LoggerFactory.getLogger(LineEndAdd.class);

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(ShowBundle.message("line.end.add"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            action(event);
        } catch (Throwable e) {
            LOG.info("LineEndAdd catch Throwable but log to record.", e);
        }
    }

    private void action(@NotNull AnActionEvent event) {
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
                () -> addDocAll(project, files), 2);
        confirmation.showInFocusCenter();
    }

    private void addDocAll(@NotNull Project project, @NotNull VirtualFile[] files) {
        @NotNull ArrayList<VirtualFile> list = new ArrayList<>();
        for (@NotNull VirtualFile file : files) {
            VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor<Void>() {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    if (!file.isDirectory()) {
                        list.add(file);
                    }
                    return true;
                }
            });
        }
        if (list.isEmpty()) {
            return;
        }
        new Task.Backgroundable(project, "Show LineEndAdd " + list.size() + " " + list.get(0)) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    for (int i = 0; i < list.size(); i++) {
                        VirtualFile file = list.get(i);
                        if (file.exists()) {
                            indicator.setText(i + " / " + list.size() + " file");
                            indicator.setFraction(1.0 * i / list.size());
                            addDoc(project, file, indicator);
                        }
                    }
                });
            }
        }.queue();
    }

    private void addDoc(@NotNull Project project, @NotNull VirtualFile file, @NotNull ProgressIndicator indicator) {
        @Nullable FileInfo info = FileInfo.of(file, project);
        if (info == null) {
            return;
        }
        int startLine = 0;
        int endLine = info.document.getLineCount() - 1;
        LineEnd.textWithDoc(info, startLine, endLine, indicator, s ->
                info.document.replaceString(0, info.document.getTextLength(), s)
        );
    }
}