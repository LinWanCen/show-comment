package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import io.github.linwancen.plugin.show.settings.ShowBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * call ConfCache.loadAll
 */
public class ReloadExtDocAction extends DumbAwareAction {

    private static final Logger LOG = LoggerFactory.getLogger(ReloadExtDocAction.class);

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(ShowBundle.message("reload.ext.doc"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            @Nullable Project project = e.getProject();
            if (project == null) {
                return;
            }
            ConfCache.loadAll(project);
        } catch (Throwable t) {
            LOG.info("ReloadExtDocAction catch Throwable but log to record.", t);
        }
    }
}
