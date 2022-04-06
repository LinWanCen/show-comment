package io.github.linwancen.plugin.show.ext.conf;

import com.google.common.base.Splitter;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.twelvemonkeys.util.LinkedSet;
import groovy.json.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

class ConfFactory {

    private static final Pattern EMPTY_PATTERN = Pattern.compile("");
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final NotificationGroup REGEXP_LOG =
            new NotificationGroup("Ext Doc Keyword Regexp Compile", NotificationDisplayType.TOOL_WINDOW, true);
    private static final NotificationGroup DATA_LOG =
            new NotificationGroup("Ext Doc Data", NotificationDisplayType.BALLOON, true);

    private ConfFactory() {}

    @Nullable
    static Pattern buildPattern(@Nullable Project project, @NotNull String path,
                                @NotNull Map<String, Map<String, List<String>>> map) {
        Set<String> exclude = new LinkedSet<>();
        StringBuilder sb = new StringBuilder();
        for (Map<String, List<String>> keyMap : map.values()) {
            // key() is escape
            for (List<String> list : keyMap.values()) {
                String key = list.get(0);
                if (key.startsWith("?")) {
                    exclude.add(key.substring(1));
                } else if (key.length() > 0 && !exclude.contains(key)) {
                    sb.append(key).append("|");
                }
            }
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        String regex = sb.toString();
        Pattern pattern = PATTERN_CACHE.get(regex);
        if (pattern != null) {
            return pattern;
        }
        sb.insert(0, "\n");
        sb.insert(0, path);
        sb.insert(0, "\n");
        try {
            Pattern compile = Pattern.compile(regex);
            PATTERN_CACHE.put(regex, compile);
            REGEXP_LOG.createNotification("Ext doc keyword regexp compile success", regex.length() + " chars",
                    sb.toString(), NotificationType.INFORMATION).notify(project);
            return compile;
        } catch (Exception e) {
            sb.insert(0, "\n");
            sb.insert(0, e.getLocalizedMessage());
            PATTERN_CACHE.put(regex, EMPTY_PATTERN);
            REGEXP_LOG.createNotification("Ext doc keyword regexp compile fail", regex.length() + " chars",
                    sb.toString(), NotificationType.ERROR).notify(project);
            return null;
        }
    }

    public static final Pattern DEL_PATTERN = Pattern.compile("\\(\\?[^)]++\\)");

    @NotNull
    static Map<String, List<String>> buildMap(@Nullable Project project, @NotNull String path,
                                              @NotNull String[] lines, boolean isKey) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (String line : lines) {
            List<String> words = Splitter.on('\t').splitToList(line);
            if (!words.isEmpty()) {
                String key = words.get(0);
                if (key.length() == 0) {
                    continue;
                }
                if (isKey) {
                    key = StringEscapeUtils.unescapeJava(key);
                    String del = DEL_PATTERN.matcher(key).replaceAll("");
                    if (del.length() != 0) {
                        key = del;
                    }
                }
                map.put(key, words);
            }
        }
        DATA_LOG.createNotification("Ext doc file load complete", map.size() + " lines",
                "\n" + path, NotificationType.INFORMATION).notify(project);
        return map;
    }
}
