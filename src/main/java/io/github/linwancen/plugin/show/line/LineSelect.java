package io.github.linwancen.plugin.show.line;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LineSelect {
    /**
     * can not work
     */
    public static boolean notSelectLine(@NotNull Project project, int lineNumber) {
        @Nullable Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return false;
        }
        @NotNull SelectionModel select = editor.getSelectionModel();
        @Nullable VisualPosition start = select.getSelectionStartPosition();
        int lineNum = lineNumber + 1;
        if (start != null && lineNum < start.getLine()) {
            return true;
        }
        @Nullable VisualPosition end = select.getSelectionEndPosition();
        return end != null && lineNum > end.getLine();
    }
}
