package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * call ConfCache.loadFile
 */
public class CacheUpdateEditorListener implements FileEditorManagerListener {

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        @NotNull Project project = event.getManager().getProject();
        @Nullable VirtualFile file = event.getNewFile();
        if (file == null || !file.isValid()) {
            return;
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (project.isDisposed()) {
                LineEndCacheUtils.cache.remove(project);
                return;
            }
            @Nullable Map<VirtualFile, Map<Integer, LineEndCache>> fileMap = LineEndCacheUtils.cache.get(project);
            if (fileMap == null) {
                return;
            }
            @Nullable Map<Integer, LineEndCache> lineMap = fileMap.get(file);
            if (lineMap == null) {
                return;
            }
            lineMap.forEach((integer, lineEndCache) -> lineEndCache.selectChanged = true);
        });
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        @NotNull Project project = source.getProject();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (project.isDisposed()) {
                LineEndCacheUtils.cache.remove(project);
                return;
            }
            @Nullable Map<VirtualFile, Map<Integer, LineEndCache>> fileMap = LineEndCacheUtils.cache.get(project);
            if (fileMap != null) {
                fileMap.remove(file);
            }
        });
    }
}
