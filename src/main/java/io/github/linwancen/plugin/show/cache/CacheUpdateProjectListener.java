package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheUpdateProjectListener implements ProjectManagerListener {

    private static final Logger LOG = LoggerFactory.getLogger(CacheUpdateProjectListener.class);

    @Override
    public void projectClosed(@NotNull Project project) {
        try {
            LineEndCacheUtils.cache.remove(project);
            TreeCacheUtils.cache.remove(project);
        } catch (Throwable e) {
            LOG.info("CacheUpdateProjectListener catch Throwable but log to record.", e);
        }
    }
}
