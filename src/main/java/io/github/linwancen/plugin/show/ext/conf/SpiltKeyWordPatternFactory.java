package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

class SpiltKeyWordPatternFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SpiltKeyWordPatternFactory.class);

    private static final Pattern EMPTY_PATTERN = Pattern.compile("");
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();
    private SpiltKeyWordPatternFactory() {}

    @Nullable
    static Pattern from(@SuppressWarnings("unused") @Nullable Project project, @NotNull String path,
                        @NotNull Map<String, Map<String, List<String>>> map) {
        @NotNull Set<String> exclude = new LinkedHashSet<>();
        @NotNull StringBuilder sb = new StringBuilder();
        for (@NotNull Map<String, List<String>> keyMap : map.values()) {
            // key() is escape
            for (@NotNull List<String> list : keyMap.values()) {
                String key = list.get(0);
                if (key.startsWith("?")) {
                    exclude.add(key.substring(1));
                } else if (!key.isEmpty() && !exclude.contains(key)) {
                    sb.append(key).append("|");
                }
            }
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        @NotNull String regex = sb.toString();
        Pattern pattern = PATTERN_CACHE.get(regex);
        if (pattern != null) {
            return pattern;
        }
        sb.insert(0, "\n");
        sb.insert(0, path);
        sb.insert(0, "\n");
        try {
            @NotNull Pattern compile = Pattern.compile(regex);
            PATTERN_CACHE.put(regex, compile);
            LOG.info("Ext doc keyword regexp compile success {} chars\n{}", regex.length(), sb);
            return compile;
        } catch (Exception e) {
            sb.insert(0, "\n");
            sb.insert(0, e.getLocalizedMessage());
            PATTERN_CACHE.put(regex, EMPTY_PATTERN);
            LOG.warn("Ext doc keyword regexp compile fail {} chars\n{}", regex.length(), sb);
            return null;
        }
    }
}
