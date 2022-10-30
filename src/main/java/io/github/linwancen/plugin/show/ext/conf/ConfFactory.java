package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.twelvemonkeys.util.LinkedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

class ConfFactory {

    private static final Pattern EMPTY_PATTERN = Pattern.compile("");
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final NotificationGroup REGEXP_LOG =
            new NotificationGroup("Ext Doc Keyword Regexp Compile", NotificationDisplayType.TOOL_WINDOW, true);

    private ConfFactory() {}

    @Nullable
    static Pattern buildPattern(@Nullable Project project, @NotNull String path,
                                @NotNull Map<String, Map<String, List<String>>> map) {
        @NotNull Set<String> exclude = new LinkedSet<>();
        @NotNull StringBuilder sb = new StringBuilder();
        for (@NotNull Map<String, List<String>> keyMap : map.values()) {
            // key() is escape
            for (@NotNull List<String> list : keyMap.values()) {
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
}
