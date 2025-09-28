package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.LineEnd;
import io.github.linwancen.plugin.show.bean.LineInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class LineEndCacheUtils {

    private LineEndCacheUtils() {}

    private static final Logger LOG = LoggerFactory.getLogger(LineEndCacheUtils.class);

    public static final Map<Project, Map<VirtualFile, Map<Integer, LineEndCache>>> cache = new ConcurrentHashMap<>();
    public static final Map<Project, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();

    public static @Nullable Collection<LineExtensionInfo> get(@NotNull LineInfo info) {
        try {
            @NotNull Map<Integer, LineEndCache> lineMap = cache
                    .computeIfAbsent(info.project, a -> new ConcurrentHashMap<>())
                    .computeIfAbsent(info.file, a -> new ConcurrentHashMap<>());
            @NotNull LineEndCache lineCache = lineMap
                    .computeIfAbsent(info.lineNumber, a -> new LineEndCache(info));
            @NotNull LineInfo oldInfo = lineCache.info;
            lineCache.info = info;
            lineCache.show = true;
            TaskUtils.init(taskMap, info.project, cache, a -> cacheUpdate(info.project, a));
            @Nullable List<LineExtensionInfo> list = lineCache.map.get(info.text);
            // load from other line
            if (list == null && info.lineCount != oldInfo.lineCount) {
                int oldLineNumber = info.lineNumber - info.lineCount + oldInfo.lineCount;
                @Nullable LineEndCache oldLineCache = lineMap.get(oldLineNumber);
                if (oldLineCache != null) {
                    list = oldLineCache.map.get(info.text);
                    if (list != null) {
                        lineCache.map.put(info.text, list);
                    }
                }
            }
            if (oldInfo.lineCount == info.lineCount) {
                lineCache.map.entrySet().removeIf(it -> !it.getKey().equals(info.text));
            }
            if (list == null) {
                // because may be updated
                list = lineCache.map.get(info.text);
            }
            return list;
        } catch (Throwable e) {
            LOG.info("LineEndCacheUtils catch Throwable but log to record.", e);
            return null;
        }
    }

    private static void cacheUpdate(@NotNull Project project, @NotNull Map<VirtualFile, Map<Integer, LineEndCache>> fileMap) {
        try {
            if (project.isDisposed() || DumbService.isDumb(project)) {
                return;
            }
            fileMap.forEach((file, lineMap) -> {
                if (!file.isValid()) {
                    return;
                }
                lineMap.forEach((lineNumber, lineCache) -> {
                    @NotNull LineInfo info = lineCache.info;
                    @Nullable List<LineExtensionInfo> list = lineCache.map.get(info.text);
                    if (!(lineCache.needUpdate() || list == null)) {
                        return;
                    }
                    try {
                        @Nullable LineExtensionInfo lineExt = LineEnd.lineExt(info);
                        @Nullable LineInfo info2 = LineInfo.of(info, lineNumber);
                        if (info2 == null || !info2.text.equals(info.text)) {
                            return;
                        }
                        if (list != null) {
                            list.clear();
                        }
                        // fix delete line get doc from before line because PsiFile be not updated
                        if ("}".equals(info.text.trim())) {
                            return;
                        }
                        if (lineExt != null) {
                            if (list != null) {
                                list.add(lineExt);
                            } else {
                                @NotNull ArrayList<LineExtensionInfo> lineExtList = new ArrayList<>(1);
                                lineExtList.add(lineExt);
                                lineCache.map.put(info.text, lineExtList);
                            }
                        }
                        lineCache.updated();
                    } catch (ProcessCanceledException ignored) {
                    } catch (Throwable e) {
                        @Nullable String msg = e.getMessage();
                        if (msg == null || !msg.contains("File is not valid")) {
                            LOG.info("LineEndCacheUtils lineMap.forEach catch Throwable but log to record.", e);
                        }
                    }
                });
            });
        } catch (ProcessCanceledException ignored) {
        } catch (IllegalStateException ignore) {
            // ignore inSmartMode(project) throw:
            // @NotNull method com/intellij/openapi/project/impl/ProjectImpl.getEarlyDisposable must not
            // return null
        } catch (Throwable e) {
            LOG.info("LineEndCacheUtils cache.forEach catch Throwable but log to record.", e);
        }
    }
}
