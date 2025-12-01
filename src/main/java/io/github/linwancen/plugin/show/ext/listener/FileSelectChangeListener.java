package io.github.linwancen.plugin.show.ext.listener;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * call FileLoader.loadFile
 */
public class FileSelectChangeListener implements FileEditorManagerListener {

    private static final Logger LOG = LoggerFactory.getLogger(FileSelectChangeListener.class);

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        @NotNull Project project = event.getManager().getProject();
        if (project.isDisposed()) {
            return;
        }
        @Nullable VirtualFile file = event.getOldFile();
        if (file == null || !file.isValid()) {
            return;
        }
        if (file.exists()) {
            try {
                FileLoader.EPN.getExtensionList().forEach(fileLoader -> fileLoader.loadFile(file, project));
            } catch (Throwable e) {
                LOG.info("ConfFileChangeListener catch Throwable but log to record.", e);
            }
        }
    }
}
