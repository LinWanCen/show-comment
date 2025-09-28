package io.github.linwancen.plugin.show.ext.sql;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import io.github.linwancen.plugin.show.ext.listener.FileLoader;
import io.github.linwancen.plugin.show.lang.base.PsiUnSaveUtils;
import io.github.linwancen.plugin.show.settings.GlobalSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlCache extends FileLoader {
    private static final Logger LOG = LoggerFactory.getLogger(SqlCache.class);

    /**
     * support sql in other project
     */
    public static final Map<String, String> TABLE_DOC_CACHE = new ConcurrentHashMap<>();
    public static final Map<String, String> COLUMN_DOC_CACHE = new ConcurrentHashMap<>();
    public static final Map<String, String> INDEX_DOC_CACHE = new ConcurrentHashMap<>();

    private SqlCache() {}

    @Nullable
    @Override
    public String treeDoc(@NotNull VirtualFile virtualFile) {
        String name = virtualFile.getNameWithoutExtension();
        return TABLE_DOC_CACHE.get(name);
    }

    @Override
    public void clearAll() {
        TABLE_DOC_CACHE.clear();
        COLUMN_DOC_CACHE.clear();
    }

    @Override
    public void remove(@NotNull VirtualFile file, @Nullable String name) {}

    @Override
    public void copyImpl(@NotNull VirtualFile file, @NotNull VirtualFile newFile) {}

    @Override
    public void loadAllImpl(@NotNull Project project) {
        GlobalSettingsState g = GlobalSettingsState.getInstance();
        ProjectSettingsState p = ProjectSettingsState.getInstance(project);
        if (!g.sqlSplitEffect && !p.sqlSplitEffect) {
            return;
        }
        @NotNull Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project, "sql");
        @NotNull StringBuilder sb = new StringBuilder();
        for (@NotNull VirtualFile file : files) {
            loadFileImpl(file, project);
            sb.append(file.getName()).append("\n");
        }
        if (files.isEmpty()) {
            return;
        }
        if (!project.isDisposed()) {
            ProjectView.getInstance(project).refresh();
        }
        LOG.info("SQL doc load all complete {} files\n{}", files.size(), sb);
    }

    @Override
    public void loadFileImpl(@NotNull VirtualFile file, @Nullable Project project) {
        String code = PsiUnSaveUtils.fileText(project, file);
        if (code == null) {
            return;
        }
        GlobalSettingsState g = GlobalSettingsState.getInstance();
        loadSqlDoc(code, TABLE_DOC_CACHE, g.tableDocEffect, g.tableDoc);
        loadSqlDoc(code, COLUMN_DOC_CACHE, g.columnDocEffect, g.columnDoc);
        loadSqlDoc(code, INDEX_DOC_CACHE, g.indexDocEffect, g.indexDoc);
        if (project == null) {
            return;
        }
        ProjectSettingsState p = ProjectSettingsState.getInstance(project);
        loadSqlDoc(code, TABLE_DOC_CACHE, p.tableDocEffect, p.tableDoc);
        loadSqlDoc(code, COLUMN_DOC_CACHE, p.columnDocEffect, p.columnDoc);
        loadSqlDoc(code, INDEX_DOC_CACHE, p.indexDocEffect, p.indexDoc);
    }

    public static final Pattern KEY_PATTERN = Pattern.compile("`?, *`?");

    private static void loadSqlDoc(@NotNull String code, @NotNull Map<String, String> map,
                                   boolean effect, Map<String, Pattern[]> patternMap) {
        if (!effect) {
            return;
        }
        for (Pattern[] patterns : patternMap.values()) {
            for (Pattern pattern : patterns) {
                Matcher m = pattern.matcher(code);
                while (m.find()) {
                    String key = m.group(1);
                    String comment = m.group(2);
                    if (!key.contains(",")) {
                        putComment(map, key, comment);
                        continue;
                    }
                    // multi column index seq
                    String[] split = KEY_PATTERN.split(key);
                    for (int i = 0; i < split.length; i++) {
                        String k = split[i];
                        putComment(map, k, String.valueOf(i + 1));
                    }
                }
            }
        }
    }

    private static final Pattern IGNORE_KEY_PATTERN = Pattern.compile("(?i)^(id|name)$");

    private static void putComment(@NotNull Map<String, String> map, @NotNull String k, String comment) {
        if (IGNORE_KEY_PATTERN.matcher(k).find()) {
            return;
        }
        String s = map.get(k);
        if (s != null && !s.equalsIgnoreCase(comment) && !s.contains(comment)) {
            map.put(k, s + " | " + comment);
        } else {
            map.put(k, comment);
        }
    }
}
