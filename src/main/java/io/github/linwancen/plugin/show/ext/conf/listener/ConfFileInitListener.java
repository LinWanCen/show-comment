package io.github.linwancen.plugin.show.ext.conf.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import io.github.linwancen.plugin.show.ext.conf.ConfCache;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * call ConfCache.loadAll
 */
public class ConfFileInitListener implements ProjectManagerListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConfFileInitListener.class);

    @Override
    public void projectOpened(@NotNull Project project) {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            boolean isInLoadAll = Arrays.stream(stackTrace).anyMatch(stack ->
                    stack.getMethodName().equals("loadAll")
                            && stack.getClassName().equals(ConfCache.class.getName()));
            if (isInLoadAll) {
                return;
            }
            ConfCache.loadAll(project);
        } catch (Throwable e) {
            LOG.info("ConfFileInitListener catch Throwable but log to record.", e);
        }
    }
}
