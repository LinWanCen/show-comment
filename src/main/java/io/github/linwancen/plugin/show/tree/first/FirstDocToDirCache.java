package io.github.linwancen.plugin.show.tree.first;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.ext.listener.FileLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * index.html index.vue index.js
 */
public class FirstDocToDirCache extends FileLoader {

    public static void indexDocToDirDoc(@NotNull VirtualFile virtualFile, @Nullable String doc) {
        if (doc != null && "index".equals(virtualFile.getNameWithoutExtension())) {
            VirtualFile parent = virtualFile.getParent();
            if (parent != null) {
                @Nullable FirstDocToDirCache extension = FileLoader.EPN.findExtension(FirstDocToDirCache.class);
                if (extension != null) {
                    extension.fileDoc.put(parent, doc);
                }
            }
        }
    }

    @Override
    protected void loadAllImpl(Project project) {}

    @Override
    protected void loadFileImpl(@NotNull VirtualFile file, @Nullable Project project) {}
}
