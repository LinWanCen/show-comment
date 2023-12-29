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

    protected LineInfo(@NotNull FileInfo info, @NotNull String text,
                       int lineNumber, int startOffset, int endOffset) {
        super(info.file, info.document, info.project, FuncEnum.LINE);
        this.lineNumber = lineNumber;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.text = text;
    }

    public static @Nullable LineInfo of(@NotNull VirtualFile file, @NotNull Project project, int lineNumber) {
        @Nullable FileInfo info = of(file, project);
        if (info == null) {
            return null;
        }
        return of(info, lineNumber);
    }

    public static @Nullable LineInfo of(@NotNull FileInfo info, int lineNumber) {
        // lineNumber start 0, as 1 <= 1 should return
        if (info.document.getLineCount() <= lineNumber) {
            return null;
        }
        try {
            int startOffset = info.document.getLineStartOffset(lineNumber);
            int endOffset = info.document.getLineEndOffset(lineNumber);
            if (startOffset == endOffset) {
                return null;
            }
            @NotNull String text = info.document.getText(new TextRange(startOffset, endOffset));
            return new LineInfo(info, text, lineNumber, startOffset, endOffset);
        } catch (Exception e) {
            return null;
        }
    }
}
