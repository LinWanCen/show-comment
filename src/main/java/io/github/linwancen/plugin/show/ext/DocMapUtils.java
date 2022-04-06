package io.github.linwancen.plugin.show.ext;

import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class DocMapUtils {

    private DocMapUtils() {}

    @Nullable
    static String get(@NotNull Map<String, Map<String, List<String>>> docMap,
                      @NotNull ProjectSettingsState set, @NotNull String... words) {
        List<String> keywordDoc = list(docMap, words);
        if (keywordDoc.size() >= set.extDocColumn) {
            return keywordDoc.get(set.extDocColumn - 1);
        }
        return null;
    }

    @NotNull
    private static List<String> list(@NotNull Map<String, Map<String, List<String>>> docMap, @NotNull String... words) {
        for (Map.Entry<String, Map<String, List<String>>> entry : docMap.entrySet()) {
            Map<String, List<String>> map = entry.getValue();
            for (String word : words) {
                List<String> wordDoc = map.get(word);
                if (wordDoc != null) {
                    return wordDoc;
                }
            }
        }
        return Collections.emptyList();
    }
}
