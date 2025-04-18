package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import io.github.linwancen.plugin.show.ext.listener.FileLoader;
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
public class ConfCache extends FileLoader {
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

    @Override
    public boolean skipExt(@Nullable String extension) {
        return !TsvLoader.EXT.equals(extension) && !TsvLoader.REGEXP_EXT.equals(extension);
    }

    @Override
    public void clearAll() {
        EXT_IN_KEY_CACHE.clear();
        KEY_CACHE.clear();
        DOC_CACHE.clear();
        TREE_CACHE.clear();
        JSON_CACHE.clear();
    }

    @Override
    public void remove(@NotNull VirtualFile file, @Nullable String name) {
        if (name != null) {
            int i = name.lastIndexOf('.');
            @NotNull String ext = name.substring(i + 1);
            if (skipExt(ext)) return;
            name = name.substring(0, i);
        } else {
            if (skipExt(file.getExtension())) return;
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

    @Override
    public void copyImpl(@NotNull VirtualFile file, @NotNull VirtualFile newFile) {
        @NotNull String name = newFile.getNameWithoutExtension();
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

    @Override
    public void loadAllImpl(@NotNull Project project) {
        @NotNull Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project,
                TsvLoader.EXT);
        @NotNull StringBuilder sb = new StringBuilder();
        for (@NotNull VirtualFile file : files) {
            loadFileImpl(file, project);
            sb.append(file.getName()).append("\n");
        }
        @NotNull Collection<VirtualFile> files2 = FilenameIndex.getAllFilesByExt(project,
                TsvLoader.REGEXP_EXT);
        for (@NotNull VirtualFile file : files2) {
            loadFileImpl(file, project);
            sb.append(file.getPath()).append("\n");
        }
        if (files.isEmpty()) {
            return;
        }
        if (!project.isDisposed()) {
            ProjectView.getInstance(project).refresh();
        }
        LOG.info("Ext doc conf load all complete {} files\n{}", files.size(), sb);
    }

    @Override
    public void loadFileImpl(@NotNull VirtualFile file, @Nullable Project project) {
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
