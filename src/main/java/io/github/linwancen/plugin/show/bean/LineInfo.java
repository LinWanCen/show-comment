package io.github.linwancen.plugin.show.bean;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LineInfo extends FileInfo {
    public final int lineNumber;
    public final int startOffset;
    public final int endOffset;
    public final @NotNull String text;

    protected LineInfo(@NotNull FileInfo fileInfo, @NotNull String text,
                       int lineNumber, int startOffset, int endOffset) {
        super(fileInfo.file, fileInfo.document, fileInfo.project, fileInfo.viewProvider, FuncEnum.LINE);
        this.lineNumber = lineNumber;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.text = text;
    }

    public static @Nullable LineInfo of(@NotNull VirtualFile file, @NotNull Project project, int lineNumber) {
        FileInfo fileInfo = of(file, project);
        if (fileInfo == null) {
            return null;
        }
        return of(fileInfo, lineNumber);
    }

    public static @Nullable LineInfo of(@NotNull FileInfo fileInfo, int lineNumber) {
        // lineNumber start 0, as 1 <= 1 should return
        if (fileInfo.document.getLineCount() <= lineNumber) {
            return null;
        }
        try {
            int startOffset = fileInfo.document.getLineStartOffset(lineNumber);
            int endOffset = fileInfo.document.getLineEndOffset(lineNumber);
            if (startOffset == endOffset) {
                return null;
            }
            String text = fileInfo.document.getText(new TextRange(startOffset, endOffset));
            return new LineInfo(fileInfo, text, lineNumber, startOffset, endOffset);
        } catch (Exception e) {
            return null;
        }
    }
}
