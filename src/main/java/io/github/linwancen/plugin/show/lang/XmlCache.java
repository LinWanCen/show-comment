package io.github.linwancen.plugin.show.lang;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.ext.listener.FileLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * for html, in here because {@code <!--} is based XML
 */
public class XmlCache extends FileLoader {

    public static void indexDocToDirDoc(@NotNull VirtualFile virtualFile, @Nullable String doc) {
        if (doc != null && "index".equals(virtualFile.getNameWithoutExtension())) {
            VirtualFile parent = virtualFile.getParent();
            if (parent != null) {
                @Nullable XmlCache extension = FileLoader.EPN.findExtension(XmlCache.class);
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
