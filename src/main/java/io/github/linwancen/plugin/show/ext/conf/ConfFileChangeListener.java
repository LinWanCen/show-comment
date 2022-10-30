package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * call ConfCache.loadFile
 */
public class ConfFileChangeListener implements FileEditorManagerListener {

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        @Nullable VirtualFile file = event.getOldFile();
        if (file == null) {
            return;
        }
        if (file.exists()) {
            ConfCache.loadFile(file);
        }
    }
}
