package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * call ConfCache.loadFile, copy, remove
 */
public class ConfFileListener implements BulkFileListener {

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
            forEvent(event);
        }
    }

    private static void forEvent(VFileEvent event) {
        VirtualFile file = event.getFile();
        if (file == null) {
            return;
        }
        if (event instanceof VFilePropertyChangeEvent) {
            VFilePropertyChangeEvent changeEvent = (VFilePropertyChangeEvent) event;
            if ("name".equals(changeEvent.getPropertyName())) {
                String oldName = changeEvent.getOldValue().toString();
                if (oldName.endsWith(ConfCache.EXT)) {
                    // change cache too complicated so remove
                    ConfCache.remove(file, oldName);
                }
            }
        }
        if (!ConfCache.EXT.equals(file.getExtension())) {
            return;
        }
        if (event instanceof VFileMoveEvent) {
            return;
        }
        if (event instanceof VFileDeleteEvent) {
            ConfCache.remove(file, null);
            return;
        }
        if (event instanceof VFileCopyEvent) {
            VFileCopyEvent copyEvent = (VFileCopyEvent) event;
            VirtualFile newFile = copyEvent.findCreatedFile();
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
        ConfCache.loadFile(null, file);
    }
}
