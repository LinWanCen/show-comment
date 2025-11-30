package io.github.linwancen.plugin.show.ext.listener;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * call FileLoader.loadAll
 */
public class FileLoadInitListener implements DumbService.DumbModeListener, ProjectManagerListener {

    private static final Logger LOG = LoggerFactory.getLogger(FileLoadInitListener.class);
    private static final Map<Project, Boolean> PROJECT_LOAD_MAP = new ConcurrentHashMap<>() {};

    @Override
    public void exitDumbMode() {
        try {
            @NotNull Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (@NotNull Project project : projects) {
                PROJECT_LOAD_MAP.computeIfAbsent(project, k -> {
                    FileLoader.loadAll(project);
                    return true;
                });
            }
        } catch (Throwable e) {
            LOG.info("ConfFileInitListener catch Throwable but log to record.", e);
        }
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        PROJECT_LOAD_MAP.remove(project);
    }
}
