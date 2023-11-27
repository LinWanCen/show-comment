package io.github.linwancen.plugin.show.settings;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PatternMapUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PatternMapUtils.class);

    public static final Pattern LINE_PATTERN = Pattern.compile("[\\r\\n]++");
    public static final Pattern SPLIT_PATTERN = Pattern.compile("\\|\\|");

    @NotNull
    public static Map<String, Pattern[]> toMap(@NotNull String fileDoc) {
        String[] lines = LINE_PATTERN.split(fileDoc);
        @NotNull Map<String, Pattern[]> map = new LinkedHashMap<>(lines.length, 1);
        for (String line : lines) {
            String[] split = SPLIT_PATTERN.split(line);
            if (split.length > 1) {
                @NotNull ArrayList<Pattern> patterns = new ArrayList<>(split.length - 1);
                for (int i = 1; i < split.length; i++) {
                    try {
                        patterns.add(Pattern.compile(split[i]));
                    } catch (Exception ignore) {}
                }
                map.put(split[0], patterns.toArray(new Pattern[0]));
            }
        }
        return map;
    }

    @NotNull
    public static String toString(@NotNull Map<String, Pattern[]> patternMap) {
        return patternMap.entrySet().stream().map(entry ->
                entry.getKey() + "||" + Stream.of(entry.getValue())
                        .map(Pattern::pattern)
                        .collect(Collectors.joining("||"))
        ).collect(Collectors.joining("\n"));
    }
}
