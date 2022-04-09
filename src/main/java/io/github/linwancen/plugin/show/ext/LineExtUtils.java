package io.github.linwancen.plugin.show.ext;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.linwancen.plugin.show.ext.conf.ConfCache;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineExtUtils {

    private LineExtUtils() {}

    public static String extDoc(@NotNull Project project, @NotNull VirtualFile file,
                                @NotNull Document document, int startOffset, int endOffset) {
        Map<String, Map<String, List<String>>> keyMap = ConfCache.keyMap(project, file);
        if (keyMap.isEmpty()) {
            return null;
        }
        Pattern pattern = ConfCache.pattern(project, file, keyMap);
        if (pattern == null || pattern.pattern().length() == 0) {
            return null;
        }
        Map<String, Map<String, List<String>>> docMap = ConfCache.docMap(project, file);
        if (docMap.isEmpty()) {
            return null;
        }
        Map<String, Map<String, List<String>>> treeMap = ConfCache.treeMap(project, file);
        String text = document.getText(new TextRange(startOffset, endOffset));
        if ("cbl".equals(file.getExtension())) {
            text = cblNotAndOr(text);
        }
        String[] words = pattern.split(text);
        Matcher matcher = pattern.matcher(text);
        return extDoc(keyMap, matcher, docMap, words, treeMap, project);
    }

    private static final Pattern DICT_PATTERN = Pattern.compile("([\\w-]++) ?(NOT)? ?= ?'");
    private static final Pattern AND_OR_PATTERN = Pattern.compile("(AND|OR) ?'");

    @NotNull
    private static String cblNotAndOr(String text) {
        // maybe faster than regexp
        if (!text.contains("=")) {
            return text;
        }
        Matcher matcher = DICT_PATTERN.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        String key = matcher.group(1);
        // put NOT first
        text = matcher.replaceAll("$2 ( $1 = '");
        // add key after AND/OR
        return AND_OR_PATTERN.matcher(text).replaceAll("$1 "+ key + " = '");
    }

    @Nullable
    private static String extDoc(@NotNull Map<String, Map<String, List<String>>> keyMap, @NotNull Matcher matcher,
                                 @NotNull Map<String, Map<String, List<String>>> docMap, @NotNull String[] words,
                                 @NotNull Map<String, Map<String, List<String>>> treeMap, @NotNull Project project) {
        ProjectSettingsState set = ProjectSettingsState.getInstance(project);
        Pattern extReplaceToSpace = set.extReplaceToSpace;
        boolean isReplaceToSpace = extReplaceToSpace.pattern().length() != 0;
        boolean haveDoc = false;
        StringBuilder sb = new StringBuilder();
        for (String s : words) {
            if (appendDoc(set, sb, docMap, treeMap, extReplaceToSpace, isReplaceToSpace, s)) {
                haveDoc = true;
            }
            appendKeyDoc(set, sb, keyMap, matcher);
        }
        if (!haveDoc) {
            return null;
        }
        return sb.toString();
    }

    private static boolean appendDoc(ProjectSettingsState set, StringBuilder sb,
                                     @NotNull Map<String, Map<String, List<String>>> docMap,
                                     @NotNull Map<String, Map<String, List<String>>> treeMap,
                                     Pattern extReplaceToSpace, boolean isReplaceToSpace, String word) {
        word = word.trim();
        if (isReplaceToSpace) {
            word = extReplaceToSpace.matcher(word).replaceAll(" ");
        }
        if (word.length() == 0) {
            return false;
        }
        String wordDoc = DocMapUtils.get(docMap, set, word);
        if (wordDoc != null) {
            sb.append(wordDoc);
            return true;
        }
        String treeDoc = DocMapUtils.get(treeMap, set, word);
        if (treeDoc != null) {
            sb.append(treeDoc);
            return true;
        }
        sb.append(word);
        return false;
    }

    private static void appendKeyDoc(@NotNull ProjectSettingsState set, @NotNull StringBuilder sb,
                                     @NotNull Map<String, Map<String, List<String>>> keyMap,
                                     @NotNull Matcher matcher) {
        if (!matcher.find()) {
            return;
        }
        String keyword = matcher.group();
        // "" if no doc
        String keyDoc = DocMapUtils.get(keyMap, set, keyword);
        if (keyDoc != null) {
            sb.append(" ").append(keyDoc);
        }
        // no one space
        if (sb.length() != 1) {
            sb.append(" ");
        }
    }
}
