package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConfCacheGetUtils {

    private static final String SKIP_PATH = "doc";
    private static final String MATCH_STR = "%";

    private ConfCacheGetUtils() {}

    /**
     * <br>file:
     * <br>a/b/c.ext
     * <br>
     * <br>configure file priority:
     * <br>a/b/c.ext.key.tsv
     * <br>a/b/x.c.ext.key.tsv
     * <br>a/b/ext.key.tsv
     * <br>a/b/x.ext.key.tsv
     * <br>a/c.ext.key.tsv
     *
     * @return {@code <sortKey, T>}
     */
    @NotNull
    static <T> TreeMap<String, T> filterPathNameExt(@NotNull String confMidExt,
                                                    @NotNull Map<VirtualFile, T> cache,
                                                    @NotNull String path, @NotNull String name, @Nullable String ext) {
        @NotNull TreeMap<String, T> map = new TreeMap<>();
        int max = path.length();
        int length = String.valueOf(max).length();
        for (@NotNull Map.Entry<VirtualFile, T> entry : cache.entrySet()) {
            VirtualFile confFile = entry.getKey();
            @NotNull String confName = confFile.getNameWithoutExtension();
            @NotNull String confPath = confFile.getPath();
            int level = level(path, confPath);
            if (level == 0) {
                continue;
            }
            String levelStr = StringUtils.leftPad(String.valueOf(max - level), length, '0');
            if ((name + confMidExt).equals(confName)) {
                map.put(levelStr + "\b" + confPath, entry.getValue());
            } else if (confName.endsWith((name + confMidExt))) {
                map.put(levelStr + "\t" + confPath, entry.getValue());
            } else {
                if (ext == null) {
                    ext = "";
                }
                if ((ext + confMidExt).equals(confName)) {
                    map.put(levelStr + "\n" + confPath, entry.getValue());
                } else if (confName.endsWith((ext + confMidExt))) {
                    map.put(levelStr + "\f" + confPath, entry.getValue());
                }
            }
        }
        return map;
    }

    /**
     * <br>file:
     * <br>a/b/c.ext
     * <br>
     * <br>configure file priority:
     * <br>a/b/any.tree.tsv
     * <br>a/any.tree.tsv
     *
     * @return {@code <sortKey, T>}
     */
    @NotNull
    public static <T> SortedMap<String, T> filterPath(@NotNull Map<VirtualFile, T> cache, @NotNull String path) {
        @NotNull TreeMap<String, T> map = new TreeMap<>();
        int max = path.length();
        int length = String.valueOf(max).length();
        for (@NotNull Map.Entry<VirtualFile, T> entry : cache.entrySet()) {
            VirtualFile confFile = entry.getKey();
            @NotNull String confPath = confFile.getPath();
            int level = level(path, confPath);
            if (level == 0) {
                continue;
            }
            String levelStr = StringUtils.leftPad(String.valueOf(max - level), length, '0');
            map.put(levelStr + confPath, entry.getValue());
        }
        return map;
    }

    private static int level(String path, String confPath) {
        path = srcPath(path);
        confPath = srcPath(confPath);
        String[] paths = StringUtils.split(path, '/');
        String[] confPaths = StringUtils.split(confPath, '/');
        int length = confPaths.length;
        if (length > 1) {
            String lastFolder = confPaths[length - 2];
            if (lastFolder.startsWith("-")) {
                try {
                    int i = Integer.parseInt(lastFolder);
                    length = length + i - 1;
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        if (length > paths.length) {
            return 0;
        }
        for (int i = 0; i < length; i++) {
            if (!match(paths[i], confPaths[i])) {
                return i;
            }
        }
        return length;
    }

    @NotNull
    private static String srcPath(@NotNull String path) {
        int i = path.indexOf('!');
        if (i != -1) {
            return path.substring(i + 1);
        }
        i = path.indexOf("/resources");
        if (i != -1) {
            return path.substring(i + "/resources".length());
        }
        return path;
    }

    private static boolean match(@NotNull String path, @NotNull String confPath) {
        if (confPath.equals(path) || SKIP_PATH.equals(confPath)) {
            return true;
        }
        boolean end = false;
        boolean start = false;
        if (confPath.startsWith(MATCH_STR)) {
            confPath = confPath.substring(1);
            end = true;
        }
        if (confPath.endsWith(MATCH_STR)) {
            confPath = confPath.substring(0, confPath.length() - 1);
            start = true;
        }
        return end && path.endsWith(confPath) || start && path.startsWith(confPath);
    }
}
