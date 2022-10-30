package io.github.linwancen.plugin.show.ext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GetFromDocMap {

    private GetFromDocMap() {}

    @Nullable
    public static String get(@NotNull Map<String, Map<String, List<String>>> docMap, @NotNull String... words) {
        @NotNull List<String> keywordDoc = list(docMap, words);
        if (keywordDoc.size() >= 2) {
            return keywordDoc.get(1);
        }
        return null;
    }

    @NotNull
    private static List<String> list(@NotNull Map<String, Map<String, List<String>>> docMap, @NotNull String... words) {
        for (@NotNull Map.Entry<String, Map<String, List<String>>> entry : docMap.entrySet()) {
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
