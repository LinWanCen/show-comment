package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * call ConfCache.clearAll
 * <br>Use Reset only for file sort
 */
public class ResetExtDocAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConfCache.clearAll();
        @Nullable Project project = e.getProject();
        if (project == null) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() ->
                ProjectView.getInstance(project).refresh());
    }
}
