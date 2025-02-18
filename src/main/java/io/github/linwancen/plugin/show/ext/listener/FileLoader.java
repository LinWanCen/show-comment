package io.github.linwancen.plugin.show.ext.listener;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ExtensionPointName: fileLoader
 */
public abstract class FileLoader {

    public static final ExtensionPointName<FileLoader> EPN =
            ExtensionPointName.create("io.github.linwancen.show-comment.fileLoader");

    public abstract void clearAll();

    /**
     * not need skipFile(file) skipFile(newFile)
     */
    public abstract void copyImpl(@NotNull VirtualFile file, @NotNull VirtualFile newFile);

    public abstract void remove(@NotNull VirtualFile file, @Nullable String oldName);

    public boolean skipFile(@NotNull VirtualFile file) {
        return skipExt(file.getExtension());
    }

    public boolean skipExt(@Nullable String ext) {
        return false;
    }

    /**
     * protected because not in runReadAction & executeOnPooledThread
     */
    protected abstract void loadAllImpl(Project project);

    /**
     * not need skipFile(file)
     */
    protected abstract void loadFileImpl(@NotNull VirtualFile file, @Nullable Project project);

    public void loadAll(@NotNull Project project) {
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                DumbService.getInstance(project).runReadActionInSmartMode(()
                        -> loadAllImpl(project)));
    }

    void loadFile(@NotNull VirtualFile file, @Nullable Project project) {
        if (skipFile(file)) return;
        if (project != null && DumbService.isDumb(project)) return;
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                ApplicationManager.getApplication().runReadAction(() -> {
                    loadFileImpl(file, project);
                    if (!project.isDisposed()) {
                        ProjectView.getInstance(project).refresh();
                    }
                }));
    }

    public void copy(@NotNull VirtualFile file, @NotNull VirtualFile newFile) {
        if (skipFile(file)) return;
        if (skipFile(newFile)) return;
        copyImpl(file, newFile);
    }
}
