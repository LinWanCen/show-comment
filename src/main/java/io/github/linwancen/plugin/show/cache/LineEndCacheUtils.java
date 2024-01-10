package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.linwancen.plugin.show.LineEnd;
import io.github.linwancen.plugin.show.bean.LineInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LineEndCacheUtils {
    private static final Logger LOG = LoggerFactory.getLogger(LineEndCacheUtils.class);

    public static final Map<Project, Map<VirtualFile, Map<Integer, LineEndCache>>> cache = new ConcurrentHashMap<>();

    public static @Nullable Collection<LineExtensionInfo> get(@NotNull LineInfo info) {
        try {
            @NotNull LineEndCache lineCache = cache
                    .computeIfAbsent(info.project, a -> new ConcurrentHashMap<>())
                    .computeIfAbsent(info.file, a -> new ConcurrentHashMap<>())
                    .computeIfAbsent(info.lineNumber, a -> new LineEndCache(info.text, info));
            if (lineCache.selectChanged) {
                lineCache.info = info;
            } else if (!info.text.equals(lineCache.code)) {
                lineCache.info = info;
                lineCache.lineExtList.clear();
            }
            checkScheduleAndInit();
            return lineCache.lineExtList;
        } catch (Throwable e) {
            LOG.info("LineEndCacheUtils catch Throwable but log to record.", e);
            return null;
        }
    }

    private static volatile boolean isRun = false;

    private static void checkScheduleAndInit() {
        if (!isRun) {
            synchronized (LineEndCacheUtils.class) {
                if (!isRun) {
                    isRun = true;
                    AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
                        try {
                            cacheUpdate();
                        } catch (Exception e) {
                            LOG.info("LineEndCacheUtils checkScheduleAndInit catch Throwable but log to record.", e);
                        }
                    }, 0L, 1L, TimeUnit.SECONDS);
                }
            }
        }
    }

    private static void cacheUpdate() {
        cache.forEach((project, fileMap) -> {
            try {
                if (project.isDisposed()) {
                    cache.remove(project);
                    return;
                }
                if (DumbService.isDumb(project)) {
                    return;
                }
                fileMap.forEach((file, lineMap) -> lineMap.forEach((lineNumber, lineEndCache) -> {
                    if (lineEndCache.info == null) {
                        return;
                    }
                    ApplicationManager.getApplication().runReadAction(() -> {
                        try {
                            @Nullable LineInfo info = lineEndCache.info;
                            if (info == null) {
                                return;
                            }
                            lineEndCache.info = null;
                            if (lineEndCache.selectChanged) {
                                lineEndCache.selectChanged = false;
                                lineEndCache.lineExtList.clear();
                            }
                            @Nullable LineExtensionInfo lineExt = LineEnd.lineExt(info);
                            if (lineExt != null) {
                                lineEndCache.lineExtList.add(lineExt);
                            }
                            // change after ext is updated
                            lineEndCache.code = info.text;
                        } catch (Exception e) {
                            LOG.info("LineEndCacheUtils lineMap.forEach catch Throwable but log to record.", e);
                        }
                    });
                }));
            } catch (Exception e) {
                LOG.info("LineEndCacheUtils cache.forEach catch Throwable but log to record.", e);
            }
        });
    }
}
