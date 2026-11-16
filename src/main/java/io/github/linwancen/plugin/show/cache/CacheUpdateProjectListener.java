package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class CacheUpdateProjectListener implements ProjectManagerListener {

    private static final Logger LOG = LoggerFactory.getLogger(CacheUpdateProjectListener.class);

    @Override
    public void projectClosed(@NotNull Project project) {
        try {
            Map<VirtualFile, Map<Integer, LineEndCache>> fileMap = LineEndCacheUtils.cache.remove(project);
            if (fileMap != null) {
                TaskUtils.running.remove(fileMap);
            }
            ScheduledFuture<?> task = LineEndCacheUtils.taskMap.remove(project);
            if (task != null) {
                task.cancel(true);
            }
        } catch (Throwable e) {
            LOG.info("CacheUpdateProjectListener catch Throwable but log to record.", e);
        }
    }
}
