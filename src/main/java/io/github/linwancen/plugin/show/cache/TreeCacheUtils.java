package io.github.linwancen.plugin.show.cache;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.linwancen.plugin.show.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TreeCacheUtils {

    private TreeCacheUtils() {}

    private static final Logger LOG = LoggerFactory.getLogger(TreeCacheUtils.class);

    public static final Map<Project, Map<ProjectViewNode<?>, TreeCache>> cache = new ConcurrentHashMap<>();

    @Nullable
    public static String treeDoc(@NotNull ProjectViewNode<?> node, @NotNull Project project) {
        try {
            @NotNull TreeCache treeCache = cache
                    .computeIfAbsent(project, a -> new ConcurrentHashMap<>())
                    .computeIfAbsent(node, a -> new TreeCache());
            treeCache.needUpdate = true;
            checkScheduleAndInit(project);
            return treeCache.doc;
        } catch (ProcessCanceledException e) {
            return null;
        } catch (Throwable e) {
            LOG.info("TreeCacheUtils catch Throwable but log to record.", e);
            return null;
        }
    }

    private static volatile boolean isRun = false;

    private static void checkScheduleAndInit(@NotNull Project project) {
        if (!isRun) {
            if (DumbService.isDumb(project)) {
                return;
            }
            synchronized (TreeCacheUtils.class) {
                if (!isRun) {
                    isRun = true;
                    AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
                        try {
                            ReadAction.nonBlocking(TreeCacheUtils::cacheUpdate)
                                    .inSmartMode(project)
                                    .submit(AppExecutorUtil.getAppExecutorService());
                        } catch (Throwable e) {
                            LOG.info("TreeCacheUtils checkScheduleAndInit catch Throwable but log to record.", e);
                        }
                    }, 0L, 1L, TimeUnit.SECONDS);
                }
            }
        }
    }

    private static void cacheUpdate() {
        cache.forEach((project, nodeCache) -> {
            try {
                if (project.isDisposed()) {
                    cache.remove(project);
                    return;
                }
                nodeCache.forEach((node, treeCache) -> {
                    if (treeCache.needUpdate) {
                        try {
                            if (project.isDisposed() || DumbService.isDumb(project)) {
                                return;
                            }
                            treeCache.doc = Tree.treeDoc(node, project);
                            treeCache.needUpdate = false;
                        } catch (ProcessCanceledException ignore) {
                            // ignore
                        } catch (Throwable e) {
                            LOG.info("TreeCacheUtils nodeCache.forEach catch Throwable but log to record.", e);
                        }
                    }
                });
            } catch (ProcessCanceledException ignored) {
            } catch (IllegalStateException ignore) {
                // ignore inSmartMode(project) throw:
                // @NotNull method com/intellij/openapi/project/impl/ProjectImpl.getEarlyDisposable must not return null
            } catch (Throwable e) {
                LOG.info("TreeCacheUtils cache.forEach catch Throwable but log to record.", e);
            }
        });
    }
}
