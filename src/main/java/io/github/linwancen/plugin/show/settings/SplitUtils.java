package io.github.linwancen.plugin.show.settings;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.regex.Pattern;

class SplitUtils {

    private SplitUtils() {}

    private static final Pattern SPLIT_PATTERN = Pattern.compile("[^\\w.*]");

    @NotNull
    static String[] split(String lineEndExclude) {
        return Arrays.stream(SPLIT_PATTERN.split(lineEndExclude))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}
