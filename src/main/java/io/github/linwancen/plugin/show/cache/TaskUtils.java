package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TaskUtils {
    private static final Logger LOG = LoggerFactory.getLogger(TaskUtils.class);

    public static <T> void init(
            @NotNull Map<Project, ScheduledFuture<?>> taskMap,
            @NotNull Project project,
            @NotNull Map<Project, T> cache,
            @NotNull Consumer<T> func
    ) {
        taskMap.computeIfAbsent(project,
                project1 -> AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
                    try {
                        T t = cache.get(project);
                        if (t == null) {
                            return;
                        }
                        ReadAction.nonBlocking(() -> {
                                    if (project.isDisposed()) {
                                        cache.remove(project);
                                        ScheduledFuture<?> task = taskMap.remove(project);
                                        if (task != null) {
                                            task.cancel(true);
                                        }
                                        return;
                                    }
                                    func.accept(t);
                                })
                                .inSmartMode(project)
                                .coalesceBy(t)
                                .submit(AppExecutorUtil.getAppExecutorService());
                    } catch (ProcessCanceledException ignored) {
                    } catch (Throwable e) {
                        LOG.info("TaskUtils init catch Throwable but log to record.", e);
                    }
                }, 0L, 1L, TimeUnit.SECONDS));
    }
}
