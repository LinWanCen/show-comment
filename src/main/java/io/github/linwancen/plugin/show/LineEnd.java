package io.github.linwancen.plugin.show;

import com.intellij.json.JsonFileType;
import com.intellij.json.json5.Json5FileType;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.bean.FileInfo;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.ext.LineExt;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LineEnd extends EditorLinePainter {

    private static final Logger LOG = LoggerFactory.getLogger(LineEnd.class);

    @Override
    public @Nullable Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project,
                                                                     @NotNull VirtualFile file, int lineNumber) {
        try {
            return getLineExtensionInfos(project, file, lineNumber);
        } catch (Throwable e) {
            LOG.info("LineEnd catch Throwable but log to record.", e);
            return null;
        }
    }

    @Nullable
    private static List<LineExtensionInfo> getLineExtensionInfos(@NotNull Project project,
                                                                 @NotNull VirtualFile file, int lineNumber) {
        @NotNull AppSettingsState settings = AppSettingsState.getInstance();
        if (!settings.showLineEndComment) {
            return null;
        }
        if (DumbService.isDumb(project)) {
            return null;
        }
        if (!file.exists()) {
            return null;
        }
        @Nullable LineInfo lineInfo = LineInfo.of(file, project, lineNumber);
        @Nullable String doc = lineDocSkipHave(lineInfo);
        if (doc == null) {
            return null;
        }
        @NotNull TextAttributes textAttr = file.getFileType().equals(JsonFileType.INSTANCE)
                || file.getFileType().equals(Json5FileType.INSTANCE)
                ? settings.lineEndJsonTextAttr
                : settings.lineEndTextAttr;
        @NotNull LineExtensionInfo info = new LineExtensionInfo(settings.lineEndPrefix + doc, textAttr);
        return Collections.singletonList(info);
    }

    @NotNull
    public static String textWithDoc(@NotNull FileInfo fileInfo, int startLine, int endLine) {
        @NotNull StringBuilder sb = new StringBuilder();
        for (int i = startLine; i <= endLine; i++) {
            @Nullable LineInfo lineInfo = LineInfo.of(fileInfo, i);
            if (lineInfo == null) {
                sb.append("\n");
                continue;
            }
            sb.append(lineInfo.text);
            @Nullable String doc = lineDocSkipHave(lineInfo);
            if (doc != null) {
                sb.append(lineInfo.appSettings.lineEndPrefix).append(doc);
            }
            sb.append("\n");
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    private static @Nullable String lineDocSkipHave(@Nullable LineInfo lineInfo) {
        @Nullable String doc = lineDoc(lineInfo);
        if (doc == null) {
            return null;
        }
        @NotNull String trimDoc = doc.trim();
        if (lineInfo.text.trim().endsWith(trimDoc)) {
            return null;
        }
        return trimDoc;
    }

    private static @Nullable String lineDoc(@Nullable LineInfo lineInfo) {
        if (lineInfo == null) {
            return null;
        }
        // override some text
        @Nullable String doc = LineExt.doc(lineInfo);
        if (doc != null) {
            return doc;
        }
        return BaseLangDoc.langDoc(lineInfo);
    }
}
