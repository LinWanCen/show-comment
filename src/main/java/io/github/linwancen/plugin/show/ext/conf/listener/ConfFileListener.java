package io.github.linwancen.plugin.show.ext.conf.listener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import io.github.linwancen.plugin.show.ext.conf.ConfCache;
import io.github.linwancen.plugin.show.ext.conf.TsvLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * call ConfCache.loadFile, copy, remove
 */
public class ConfFileListener implements BulkFileListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConfFileListener.class);

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        try {
            for (@NotNull VFileEvent event : events) {
                forEvent(event);
            }
        } catch (Throwable e) {
            LOG.info("ConfFileListener catch Throwable but log to record.", e);
        }
    }

    private static void forEvent(@NotNull VFileEvent event) {
        @Nullable VirtualFile file = event.getFile();
        if (file == null) {
            return;
        }
        if (event instanceof VFileMoveEvent) {
            return;
        }
        if (event instanceof VFilePropertyChangeEvent) {
            @NotNull VFilePropertyChangeEvent changeEvent = (VFilePropertyChangeEvent) event;
            if ("name".equals(changeEvent.getPropertyName())) {
                String oldName = changeEvent.getOldValue().toString();
                if (oldName.endsWith(TsvLoader.EXT) || oldName.endsWith(TsvLoader.REGEXP_EXT)) {
                    // change cache too complicated so remove
                    ConfCache.remove(file, oldName);
                }
            }
        }
        if (!TsvLoader.EXT.equals(file.getExtension()) && !TsvLoader.REGEXP_EXT.equals(file.getExtension())) {
            return;
        }
        if (event instanceof VFileDeleteEvent) {
            ConfCache.remove(file, null);
            return;
        }
        if (event instanceof VFileCopyEvent) {
            @NotNull VFileCopyEvent copyEvent = (VFileCopyEvent) event;
            @Nullable VirtualFile newFile = copyEvent.findCreatedFile();
            if (newFile == null) {
                return;
            }
            try {
                ConfCache.copy(file, newFile);
            } catch (Exception ignored) {
                // ignore
            }
            return;
        }
        // VFileCreateEvent
        // VFileContentChangeEvent
        ConfCache.loadFile(file, null);
    }
}
