package io.github.linwancen.plugin.show.ext.action;

import com.intellij.ide.actions.CopyReferenceAction;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import io.github.linwancen.plugin.show.ext.listener.FileLoader;
import io.github.linwancen.plugin.show.settings.ShowBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * call ConfCache.clearAll
 * <br>Use Reset only for file sort
 */
public class ResetExtDocAction extends CopyReferenceAction {

    private static final Logger LOG = LoggerFactory.getLogger(ResetExtDocAction.class);

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(ShowBundle.message("reset.ext.doc"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            FileLoader.EPN.getExtensionList().forEach(FileLoader::clearAll);
            @Nullable Project project = e.getProject();
            if (project == null) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(() ->
                    ProjectView.getInstance(project).refresh());
        } catch (Throwable t) {
            LOG.info("ConfFileChangeListener catch Throwable but log to record.", t);
        }
    }
}
