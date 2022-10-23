package io.github.linwancen.plugin.show.java.line;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.java.doc.PsiDocToStrDoc;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * call LineExt, ~LeftToRight, ~RightToLeft
 * @deprecated JavaLangDoc
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
        AppSettingsState setting = AppSettingsState.getInstance();
        if (file != null) {
            int i = text.indexOf(setting.lineEndPrefix);
            String code = i <= 0 ? text : text.substring(0, i);
            String extDoc = "LineExt.extDoc(project, file.getPath(), file.getName(), file.getExtension(), code)";
            if (extDoc != null) {
                extDoc = extDoc.trim();
                // LineEnd.lineDocSkipHave
                if (text.endsWith(extDoc)) {
                    return null;
                }
                return extDoc;
            }
        }
        if (viewProvider == null) {
            return null;
        }
        PsiDocComment docComment = setting.findElementRightToLeft
                ? FileViewToPsiDocRightToLeft.rightDoc(viewProvider, startOffset, endOffset)
                : FileViewToPsiDocLeftToRight.leftDoc(viewProvider, document, startOffset, endOffset);
        String strDoc = PsiDocToStrDoc.text(docComment, false);
        if (strDoc == null) {
            return null;
        }
        strDoc = strDoc.trim();
        if (text.endsWith(strDoc)) {
            return null;
        }
        return strDoc;
    }

    @NotNull
    public static String textWithDoc(@NotNull AppSettingsState settings, @NotNull Document document,
                                     int startLine, int endLine,
                                     @Nullable Project project,
                                     @Nullable VirtualFile file,
                                     @Nullable FileViewProvider viewProvider) {
        // textWithDoc
        StringBuilder sb = new StringBuilder();
        for (int i = startLine; i <= endLine; i++) {
            int startOffset;
            int endOffset;
            try {
                startOffset = document.getLineStartOffset(i);
                endOffset = document.getLineEndOffset(i);
            } catch (Exception e) {
                continue;
            }
            if (startOffset != endOffset) {
                String text = document.getText(new TextRange(startOffset, endOffset));
                sb.append(text);
                String doc = doc(document, project, file, viewProvider, startOffset, endOffset, text);
                if (doc != null && !text.endsWith(doc)) {
                    sb.append(settings.lineEndPrefix).append(doc);
                }
            }
            sb.append("\n");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }
}
