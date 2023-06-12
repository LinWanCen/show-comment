package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * call ConfCache.loadAll
 */
public class ConfFileInitListener implements ProjectManagerListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConfFileInitListener.class);

    @Override
    public void projectOpened(@NotNull Project project) {
        try {
            ConfCache.loadAll(project);
        } catch (Throwable e) {
            LOG.info("ConfFileInitListener catch Throwable but log to record.", e);
        }
    }
}
