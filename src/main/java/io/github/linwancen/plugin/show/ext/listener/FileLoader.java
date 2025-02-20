package io.github.linwancen.plugin.show.ext.listener;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExtensionPointName: fileLoader
 */
public abstract class FileLoader {

    public static final ExtensionPointName<FileLoader> EPN =
            ExtensionPointName.create("io.github.linwancen.show-comment.fileLoader");

    public final Map<VirtualFile, String> fileDoc = new ConcurrentHashMap<>();

    @Nullable
    public String treeDoc(@Nullable VirtualFile virtualFile) {
        if (virtualFile == null) {
            return null;
        }
        return fileDoc.get(virtualFile);
    }

    public void clearAll() {
        fileDoc.clear();
    }

    /**
     * not need skipFile(file) skipFile(newFile)
     */
    public void copyImpl(@NotNull VirtualFile file, @NotNull VirtualFile newFile) {
        String s = fileDoc.get(file);
        if (s != null) {
            fileDoc.put(newFile, s);
        }
    }

    public void remove(@NotNull VirtualFile file, @Nullable String oldName) {
        if (oldName == null) {
            fileDoc.remove(file);
        }
    }

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

    public void visitChildrenRecursively(@NotNull Project project, @NotNull VirtualFile dir, @NotNull StringBuilder sb) {
        VfsUtil.visitChildrenRecursively(dir, new VirtualFileVisitor<Void>() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (file.isDirectory()) {
                    return true;
                }
                loadFileImpl(file, project);
                sb.append(file.getPath()).append("\n");
                return true;
            }
        });
    }

    void loadFile(@NotNull VirtualFile file, @Nullable Project project) {
        if (skipFile(file)) return;
        if (project != null && DumbService.isDumb(project)) return;
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                ApplicationManager.getApplication().runReadAction(() -> {
                    loadFileImpl(file, project);
                    if (project != null && !project.isDisposed()) {
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
