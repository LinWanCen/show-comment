package io.github.linwancen.plugin.show.ext;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.ext.conf.ConfCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class TreeExt {

    private TreeExt() {}

    public static @Nullable String doc(ProjectViewNode<?> node) {
        VirtualFile file = node.getVirtualFile();
        if (file == null) {
            return null;
        }
        return TreeExt.extDoc(file);
    }

    public static String extDoc(@NotNull VirtualFile file) {
        Map<String, Map<String, List<String>>> docMap = ConfCache.treeMap(file.getPath(), file.getName(), file.getPath());
        String[] words = {
                file.getName(),
                file.getNameWithoutExtension(),
        };
        String extDoc = GetFromDocMap.get(docMap, words);
        if (extDoc == null) {
            return null;
        }
        return " " + extDoc;
    }
}
