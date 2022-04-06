package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

/**
 * call ConfCache.loadAll
 */
public class ConfFileInitListener implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull Project project) {
        ConfCache.loadAll(project);
    }

}
