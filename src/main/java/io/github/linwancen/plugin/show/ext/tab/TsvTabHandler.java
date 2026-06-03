package io.github.linwancen.plugin.show.ext.tab;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class TsvTabHandler extends EditorActionHandler {

    private final EditorActionHandler myOriginalHandler;

    public TsvTabHandler(@Nullable EditorActionHandler originalHandler) {
        this.myOriginalHandler = originalHandler;
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        AppSettingsState settings = AppSettingsState.getInstance();
        if (settings.tsvTab) {
            Document document = editor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (virtualFile != null && "tsv".equalsIgnoreCase(virtualFile.getExtension())) {
                CaretModel caretModel = editor.getCaretModel();
                List<Caret> carets = caretModel.getAllCarets().stream()
                        // Insert from back to front to avoid affecting the cursor position in front
                        .sorted((c1, c2) -> Integer.compare(c2.getOffset(), c1.getOffset()))
                        .collect(Collectors.toList());
                ApplicationManager.getApplication().runWriteAction(() -> {
                    for (Caret c : carets) {
                        int offset = c.getOffset();
                        document.insertString(offset, "\t");
                        c.moveToOffset(offset + 1);
                    }
                });
                return;
            }
        }

        if (myOriginalHandler != null) {
            myOriginalHandler.execute(editor, caret, dataContext);
        }
    }
}
