package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;

/**
 * call ConfFactory, ConfCacheGetUtils
 */
public class ConfCache {
    private static final Logger LOG = LoggerFactory.getLogger(ConfCache.class);

    static final String KEY_MID_EXT = ".key";
    static final String DOC_MID_EXT = ".doc";
    static final String TREE_MID_EXT = ".tree";
    static final String JSON_MID_EXT = ".json";

    private static final ConcurrentSkipListSet<String> EXT_IN_KEY_CACHE = new ConcurrentSkipListSet<>();
    private static final Map<VirtualFile, Map<String, List<String>>> KEY_CACHE = new ConcurrentHashMap<>();
    private static final Map<VirtualFile, Map<String, List<String>>> DOC_CACHE = new ConcurrentHashMap<>();
    private static final Map<VirtualFile, Map<String, List<String>>> TREE_CACHE = new ConcurrentHashMap<>();
    private static final Map<VirtualFile, Map<String, List<String>>> JSON_CACHE = new ConcurrentHashMap<>();

    private ConfCache() {}

    @Nullable
    public static Pattern pattern(@Nullable Project project,
                                  @NotNull Map<String, Map<String, List<String>>> keyMap, @NotNull String path) {
        return SpiltKeyWordPatternFactory.from(project, path, keyMap);
    }

    @NotNull
    public static Map<String, Map<String, List<String>>> keyMap(@NotNull String path,
                                                                @NotNull String name,
                                                                @Nullable String ext) {
        // file witch not ext can have itself ext doc
        if (ext != null && !EXT_IN_KEY_CACHE.contains(ext)) {
            // faster than find in KEY_CACHE
            return Collections.emptyMap();
        }
        return ConfCacheGetUtils.filterPathNameExt(KEY_MID_EXT, KEY_CACHE, path, name, ext);
    }

    @NotNull
    public static Map<String, Map<String, List<String>>> docMap(@NotNull String path,
                                                                @NotNull String name,
                                                                @Nullable String ext) {
        return ConfCacheGetUtils.filterPathNameExt(DOC_MID_EXT, DOC_CACHE, path, name, ext);
    }

    @NotNull
    public static Map<String, Map<String, List<String>>> treeMap(@NotNull String path) {
        return ConfCacheGetUtils.filterPath(TREE_CACHE, path);
    }

    @NotNull
    public static Map<String, Map<String, List<String>>> jsonMap(@NotNull String path) {
        return ConfCacheGetUtils.filterPath(JSON_CACHE, path);
    }

    public static void clearAll() {
        EXT_IN_KEY_CACHE.clear();
        KEY_CACHE.clear();
        DOC_CACHE.clear();
        TREE_CACHE.clear();
        JSON_CACHE.clear();
    }

    public static void remove(@NotNull VirtualFile file, @Nullable String name) {
        if (name != null) {
            int i = name.lastIndexOf('.');
            name = name.substring(0, i);
        } else {
            name = file.getNameWithoutExtension();
        }
        if (name.endsWith(KEY_MID_EXT)) {
            KEY_CACHE.remove(file);
        } else if (name.endsWith(DOC_MID_EXT)) {
            DOC_CACHE.remove(file);
        } else if (name.endsWith(TREE_MID_EXT)) {
            TREE_CACHE.remove(file);
        } else if (name.endsWith(JSON_MID_EXT)) {
            JSON_CACHE.remove(file);
        }
    }

    public static void copy(@NotNull VirtualFile file, @NotNull VirtualFile newFile) {
        @NotNull String name = file.getNameWithoutExtension();
        if (name.endsWith(KEY_MID_EXT)) {
            copyCache(file, newFile, KEY_CACHE);
        } else if (name.endsWith(DOC_MID_EXT)) {
            copyCache(file, newFile, DOC_CACHE);
        } else if (name.endsWith(TREE_MID_EXT)) {
            copyCache(file, newFile, TREE_CACHE);
        } else if (name.endsWith(JSON_MID_EXT)) {
            copyCache(file, newFile, JSON_CACHE);
        }
    }

    private static void copyCache(@Nullable VirtualFile file, @NotNull VirtualFile newFile,
                                  @NotNull Map<VirtualFile, Map<String, List<String>>> cache) {
        Map<String, List<String>> map = cache.get(file);
        if (map != null) {
            cache.put(newFile, new LinkedHashMap<>(map));
        }
    }

    public static void loadAll(@NotNull Project project) {
        new Task.Backgroundable(project, "Show Load xxx.tree/key/doc/json.tsv") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                ApplicationManager.getApplication().runReadAction(() -> {
                    @NotNull Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project, TsvLoader.EXT);
                    @NotNull StringBuilder sb = new StringBuilder();
                    double i = 0;
                    for (@NotNull VirtualFile file : files) {
                        indicator.setText(file.getName());
                        load(file);
                        i++;
                        indicator.setFraction(i / files.size());
                        sb.append(file.getName()).append("\n");
                    }
                    if (files.isEmpty()) {
                        return;
                    }
                    if (!project.isDisposed()) {
                        ProjectView.getInstance(project).refresh();
                    }
                    LOG.info("Ext doc conf load all complete {} files\n{}", files.size(), sb);
                });
            }
        }.queue();
    }

    public static void loadFile(@NotNull VirtualFile file, @Nullable Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ConfCache.load(file);
            if (project != null && !project.isDisposed()) {
                ProjectView.getInstance(project).refresh();
            }
        });
    }

    private static void load(@NotNull VirtualFile file) {
        if (!TsvLoader.EXT.equals(file.getExtension())) {
            return;
        }
        @NotNull String name = file.getNameWithoutExtension();
        if (name.endsWith(KEY_MID_EXT)) {
            @NotNull String matchName = name.substring(0, name.length() - KEY_MID_EXT.length());
            int i = matchName.lastIndexOf(".");
            if (i > 0) {
                EXT_IN_KEY_CACHE.add(matchName.substring(i + 1));
            }
            KEY_CACHE.put(file, TsvLoader.buildMap(file, true));
        } else if (name.endsWith(DOC_MID_EXT)) {
            DOC_CACHE.put(file, TsvLoader.buildMap(file, false));
        } else if (name.endsWith(TREE_MID_EXT)) {
            TREE_CACHE.put(file, TsvLoader.buildMap(file, false));
        } else if (name.endsWith(JSON_MID_EXT)) {
            JSON_CACHE.put(file, TsvLoader.buildMap(file, false));
        }
    }
}
