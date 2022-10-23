package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;

/**
 * call ConfFactory, ConfCacheGetUtils
 */
public class ConfCache {

    private static final Pattern LINE_PATTERN = Pattern.compile("[\\r\\n]++");

    static final String EXT = "tsv";
    static final String KEY_MID_EXT = ".key";
    static final String DOC_MID_EXT = ".doc";
    static final String TREE_MID_EXT = ".tree";

    private static final ConcurrentSkipListSet<String> EXT_IN_KEY_CACHE = new ConcurrentSkipListSet<>();
    private static final Map<VirtualFile, Map<String, List<String>>> KEY_CACHE = new ConcurrentHashMap<>();
    private static final Map<VirtualFile, Map<String, List<String>>> DOC_CACHE = new ConcurrentHashMap<>();
    private static final Map<VirtualFile, Map<String, List<String>>> TREE_CACHE = new ConcurrentHashMap<>();

    private ConfCache() {}

    @Nullable
    public static Pattern pattern(@Nullable Project project,
                                  @NotNull Map<String, Map<String, List<String>>> keyMap, @NotNull String path) {
        return ConfFactory.buildPattern(project, path, keyMap);
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
    public static Map<String, Map<String, List<String>>> treeMap(@NotNull String path,
                                                                 @NotNull String name,
                                                                 @Nullable String ext) {
        return ConfCacheGetUtils.filterPath(TREE_CACHE, path, name, ext);
    }

    static void clearAll() {
        EXT_IN_KEY_CACHE.clear();
        KEY_CACHE.clear();
        DOC_CACHE.clear();
        TREE_CACHE.clear();
    }

    static void remove(@NotNull VirtualFile file, String name) {
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
        }
    }

    static void copy(@NotNull VirtualFile file, @NotNull VirtualFile newFile) {
        String name = file.getNameWithoutExtension();
        if (name.endsWith(KEY_MID_EXT)) {
            copyCache(file, newFile, KEY_CACHE);
        } else if (name.endsWith(DOC_MID_EXT)) {
            copyCache(file, newFile, DOC_CACHE);
        } else if (name.endsWith(TREE_MID_EXT)) {
            copyCache(file, newFile, TREE_CACHE);
        }
    }

    private static void copyCache(@Nullable VirtualFile file, @NotNull VirtualFile newFile,
                                  @NotNull Map<VirtualFile, Map<String, List<String>>> cache) {
        Map<String, List<String>> map = cache.get(file);
        if (map != null) {
            cache.put(newFile, new LinkedHashMap<>(map));
        }
    }

    private static final NotificationGroup LOAD_ALL_LOG =
            new NotificationGroup("Ext Doc Conf Load All", NotificationDisplayType.BALLOON, true);

    static void loadAll(@NotNull Project project) {
        DumbService.getInstance(project).runReadActionInSmartMode(() ->
                ApplicationManager.getApplication().runReadAction(() -> {
                    Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project, EXT);
                    StringBuilder sb = new StringBuilder();
                    for (VirtualFile file : files) {
                        load(project, file);
                        sb.append(file.getName()).append("\n");
                    }
                    if (files.isEmpty()) {
                        return;
                    }
                    LOAD_ALL_LOG.createNotification("Ext doc conf load all complete", files.size() + " files",
                            sb.toString(), NotificationType.INFORMATION).notify(project);
                }));
    }

    static void loadFile(@Nullable Project project, @NotNull VirtualFile file) {
        ApplicationManager.getApplication().runReadAction(() -> ConfCache.load(project, file));
    }

    private static void load(@Nullable Project project, @NotNull VirtualFile file) {
        if (!ConfCache.EXT.equals(file.getExtension())) {
            return;
        }
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return;
        }
        String text = document.getText();
        String name = file.getNameWithoutExtension();
        // this pattern would skip empty line
        String[] lines = LINE_PATTERN.split(text);
        if (name.endsWith(KEY_MID_EXT)) {
            String matchName = name.substring(0, name.length() - KEY_MID_EXT.length());
            int i = matchName.lastIndexOf(".");
            if (i > 0) {
                EXT_IN_KEY_CACHE.add(matchName.substring(i + 1));
            }
            KEY_CACHE.put(file, ConfFactory.buildMap(project, file.getPath(), lines, true));
        } else if (name.endsWith(DOC_MID_EXT)) {
            DOC_CACHE.put(file, ConfFactory.buildMap(project, file.getPath(), lines, false));
        } else if (name.endsWith(TREE_MID_EXT)) {
            TREE_CACHE.put(file, ConfFactory.buildMap(project, file.getPath(), lines, false));
        }
    }
}
