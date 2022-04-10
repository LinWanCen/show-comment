package io.github.linwancen.plugin.show.ext;

import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.ext.conf.ConfCache;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class TreeExt {

    private TreeExt() {}

    public static String extDoc(@NotNull VirtualFile file) {
        Map<String, Map<String, List<String>>> docMap = ConfCache.treeMap(file.getPath());
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
