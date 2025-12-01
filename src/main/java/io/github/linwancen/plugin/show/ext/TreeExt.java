package io.github.linwancen.plugin.show.ext;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.ext.conf.ConfCache;
import io.github.linwancen.plugin.show.ext.listener.FileLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class TreeExt {

    private TreeExt() {}

    public static @Nullable String doc(@NotNull ProjectViewNode<?> node) {
        @Nullable VirtualFile file = node.getVirtualFile();
        if (file == null || !file.isValid()) {
            return null;
        }
        return TreeExt.extDoc(file);
    }

    @Nullable
    public static String extDoc(@NotNull VirtualFile file) {
        @NotNull Map<String, Map<String, List<String>>> docMap = ConfCache.treeMap(file.getPath());
        @NotNull String[] words = {
                file.getName(),
                file.getNameWithoutExtension(),
        };
        @Nullable String extDoc = GetFromDocMap.get(docMap, words);
        if (extDoc != null) {
            return " " + extDoc;
        }
        @NotNull List<FileLoader> fileLoaders = FileLoader.EPN.getExtensionList();
        for (@NotNull FileLoader fileLoader : fileLoaders) {
            @Nullable String s = fileLoader.treeDoc(file);
            if (s != null) {
                return s;
            }
        }
        return null;
    }
}
