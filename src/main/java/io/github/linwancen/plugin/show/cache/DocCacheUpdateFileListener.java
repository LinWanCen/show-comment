package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import io.github.linwancen.plugin.show.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 监听 VFS 物理文件变更（内容保存、属性变动、删除），
 * 精确失效 RenderDocCache 中对应文件的旧文档缓存，并以防抖方式触发项目视图（ProjectView）自愈刷新。
 */
public class DocCacheUpdateFileListener implements BulkFileListener {

    private static final Logger LOG = LoggerFactory.getLogger(DocCacheUpdateFileListener.class);

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        try {
            Set<VirtualFile> changedFiles = new HashSet<>();
            for (@NotNull VFileEvent event : events) {
                if (event instanceof VFileContentChangeEvent
                        || event instanceof VFilePropertyChangeEvent
                        || event instanceof VFileDeleteEvent) {
                    @Nullable VirtualFile file = event.getFile();
                    if (file != null && !file.isDirectory()) {
                        changedFiles.add(file);
                    }
                }
            }
            if (changedFiles.isEmpty()) {
                return;
            }

            @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
            for (@NotNull Project project : openProjects) {
                if (project.isDisposed()) {
                    continue;
                }
                for (@NotNull VirtualFile file : changedFiles) {
                    RenderDocCache.invalidateFile(project, file);
                }
                Tree.refreshProjectView(project);
            }
        } catch (Throwable e) {
            LOG.info("DocCacheUpdateFileListener catch Throwable but log to record.", e);
        }
    }
}
