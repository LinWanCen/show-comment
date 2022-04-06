package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * call ConfCache.loadFile
 */
public class ConfFileChangeListener implements FileEditorManagerListener {

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile file = event.getOldFile();
        if (file == null) {
            return;
        }
        if (file.exists()) {
            ConfCache.loadFile(event.getManager().getProject(), file);
        }
    }
}
