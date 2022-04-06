package io.github.linwancen.plugin.show.ext;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.ext.conf.ConfCache;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class TreeExtUtils {

    private TreeExtUtils() {}

    public static String extDoc(@NotNull Project project, @NotNull VirtualFile file) {
        Map<String, Map<String, List<String>>> docMap = ConfCache.treeMap(project, file);
        ProjectSettingsState set = ProjectSettingsState.getInstance(project);
        String[] words = {
                file.getName(),
                file.getNameWithoutExtension(),
        };
        String extDoc = DocMapUtils.get(docMap, set, words);
        if (extDoc == null) {
            return null;
        }
        return " " + extDoc;
    }
}
