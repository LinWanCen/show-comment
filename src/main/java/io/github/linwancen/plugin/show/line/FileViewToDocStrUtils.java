package io.github.linwancen.plugin.show.line;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.doc.PsiDocToStrDoc;
import io.github.linwancen.plugin.show.ext.LineExt;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * call LineExt, ~LeftToRight, ~RightToLeft
 */
public class FileViewToDocStrUtils {

    private FileViewToDocStrUtils() {}

    /**
     * From Ext Or PsiDoc
     */
    @Nullable
    public static String doc(@NotNull Document document,
                             @Nullable Project project,
                             @Nullable VirtualFile file,
                             @Nullable FileViewProvider viewProvider,
                             int startOffset, int endOffset, @NotNull String text) {
        if (file != null) {
            String extDoc = LineExt.extDoc(project, file.getPath(), file.getName(), file.getExtension(), text);
            if (extDoc != null) {
                return extDoc;
            }
        }
        if (viewProvider == null) {
            return null;
        }
        PsiDocComment docComment = AppSettingsState.getInstance().findElementRightToLeft
                ? FileViewToPsiDocRightToLeft.rightDoc(viewProvider, startOffset, endOffset)
                : FileViewToPsiDocLeftToRight.leftDoc(viewProvider, document, startOffset, endOffset);
        return PsiDocToStrDoc.text(docComment);
    }

    @NotNull
    public static String textWithDoc(@NotNull AppSettingsState settings, @NotNull Document document,
                                     int startLine, int endLine,
                                     @Nullable Project project,
                                     @Nullable VirtualFile file,
                                     @Nullable FileViewProvider viewProvider) {
        StringBuilder sb = new StringBuilder();
        for (int i = startLine; i <= endLine; i++) {
            int startOffset = document.getLineStartOffset(i);
            int endOffset = document.getLineEndOffset(i);
            if (startOffset != endOffset) {
                String text = document.getText(new TextRange(startOffset, endOffset));
                sb.append(text);
                String doc = doc(document, project, file, viewProvider, startOffset, endOffset, text);
                if (doc != null) {
                    sb.append(settings.lineEndPrefix).append(doc);
                }
            }
            sb.append("\n");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }
}
