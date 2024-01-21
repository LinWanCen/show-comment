package io.github.linwancen.plugin.show;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import io.github.linwancen.plugin.show.bean.FileInfo;
import io.github.linwancen.plugin.show.settings.ShowBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.StringSelection;

/**
 * on EditorPopupMenu
 */
public class LineEndCopy extends DumbAwareAction {

    private static final Logger LOG = LoggerFactory.getLogger(LineEndCopy.class);

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(ShowBundle.message("line.end.copy"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            copyWithDoc(event);
        } catch (Throwable e) {
            LOG.info("LineEndCopy catch Throwable but log to record.", e);
        }
    }

    private void copyWithDoc(@NotNull AnActionEvent event) {
        @Nullable Project project = event.getProject();
        if (project == null) {
            return;
        }
        @Nullable FileInfo info = FileInfo.of(event);
        if (info == null) {
            return;
        }
        new Task.Backgroundable(project, "Show LineEndCopy " + info.file.getName()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                DumbService.getInstance(project).runReadActionInSmartMode(() ->
                        ApplicationManager.getApplication().runReadAction(() -> {
                            int startLine = 0;
                            int endLine = info.document.getLineCount() - 1;
                            // if select
                            @Nullable Editor editor = event.getData(CommonDataKeys.EDITOR);
                            if (editor != null) {
                                @NotNull Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
                                int start = primaryCaret.getSelectionStart();
                                int end = primaryCaret.getSelectionEnd();
                                try {
                                    startLine = info.document.getLineNumber(start);
                                    endLine = info.document.getLineNumber(end);
                                } catch (Exception e) {
                                    return;
                                }
                            }
                            LineEnd.textWithDoc(info, startLine, endLine, indicator, s -> {
                                @NotNull StringSelection content = new StringSelection(s);
                                CopyPasteManager.getInstance().setContents(content);
                            });
                        }));
            }
        }.queue();
    }
}