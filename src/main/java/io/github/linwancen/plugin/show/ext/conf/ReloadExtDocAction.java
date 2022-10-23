package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * call ConfCache.loadAll
 */
public class ReloadExtDocAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        @Nullable Project project = e.getProject();
        if (project == null) {
            return;
        }
        ConfCache.loadAll(project);
    }
}
