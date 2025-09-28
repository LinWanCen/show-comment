package io.github.linwancen.plugin.show.cache;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import io.github.linwancen.plugin.show.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class TreeCacheUtils {

    private TreeCacheUtils() {}

    private static final Logger LOG = LoggerFactory.getLogger(TreeCacheUtils.class);

    public static final Map<Project, Map<ProjectViewNode<?>, TreeCache>> cache = new ConcurrentHashMap<>();
    public static final Map<Project, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();

    @Nullable
    public static String treeDoc(@NotNull ProjectViewNode<?> node, @NotNull Project project) {
        try {
            @NotNull TreeCache treeCache = cache
                    .computeIfAbsent(project, a -> new ConcurrentHashMap<>())
                    .computeIfAbsent(node, a -> new TreeCache());
            treeCache.needUpdate = true;
            TaskUtils.init(taskMap, project, cache, a -> cacheUpdate(project, a));
            return treeCache.doc;
        } catch (ProcessCanceledException ignored) {
            return null;
        } catch (Throwable e) {
            LOG.info("TreeCacheUtils catch Throwable but log to record.", e);
            return null;
        }
    }

    private static void cacheUpdate(@NotNull Project project, @NotNull Map<ProjectViewNode<?>, TreeCache> nodeCache) {
        try {
            nodeCache.forEach((node, treeCache) -> {
                if (treeCache.needUpdate) {
                    try {
                        if (project.isDisposed() || DumbService.isDumb(project)) {
                            return;
                        }
                        treeCache.doc = Tree.treeDoc(node, project);
                        treeCache.needUpdate = false;
                    } catch (ProcessCanceledException ignored) {
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
    }
}
