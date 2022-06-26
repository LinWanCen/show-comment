package io.github.linwancen.plugin.show.ext;

import com.intellij.openapi.project.Project;
import io.github.linwancen.plugin.show.ext.conf.ConfCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineExt {

    private LineExt() {}

    public static String extDoc(@Nullable Project project,
                                @NotNull String path, @NotNull String name, @Nullable String ext,
                                @NotNull String text) {
        Map<String, Map<String, List<String>>> keyMap = ConfCache.keyMap(path, name, ext);
        if (keyMap.isEmpty()) {
            return null;
        }
        Pattern pattern = ConfCache.pattern(project, keyMap, path);
        if (pattern == null || pattern.pattern().length() == 0) {
            return null;
        }
        Map<String, Map<String, List<String>>> docMap = ConfCache.docMap(path, name, ext);
        if (docMap.isEmpty()) {
            return null;
        }
        Map<String, Map<String, List<String>>> treeMap = ConfCache.treeMap(path);
        if (ext == null || "cbl".equals(ext) || "cob".equals(ext) || "cobol".equals(ext)) {
            text = cblNotAndOr(text);
        }
        String[] words = pattern.split(text);
        Matcher matcher = pattern.matcher(text);
        return extDoc(keyMap, matcher, docMap, words, treeMap);
    }

    private static final Pattern DICT_PATTERN = Pattern.compile("([\\w-]++) ?(NOT)? ?= ?\\(? ?'");
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
        if (matcher.group(2) != null) {
            text = matcher.replaceAll(" $2 ( $1 = '");
        }
        // add key after AND/OR
        return AND_OR_PATTERN.matcher(text).replaceAll("$1 " + key + " = '");
    }

    @Nullable
    private static String extDoc(@NotNull Map<String, Map<String, List<String>>> keyMap, @NotNull Matcher matcher,
                                 @NotNull Map<String, Map<String, List<String>>> docMap, @NotNull String[] words,
                                 @NotNull Map<String, Map<String, List<String>>> treeMap) {
        boolean haveDoc = false;
        StringBuilder sb = new StringBuilder();
        for (String s : words) {
            if (appendDoc(sb, s, docMap, treeMap)) {
                haveDoc = true;
            }
            appendKeyDoc(sb, matcher, keyMap);
        }
        int before;
        do {
            before = sb.length();
            appendKeyDoc(sb, matcher, keyMap);
        } while (sb.length() != before);
        if (!haveDoc) {
            return null;
        }
        return sb.toString();
    }

    private static boolean appendDoc(@NotNull StringBuilder sb, @NotNull String word,
                                     @NotNull Map<String, Map<String, List<String>>> docMap,
                                     @NotNull Map<String, Map<String, List<String>>> treeMap) {
        word = word.trim();
        if (word.length() == 0) {
            return false;
        }
        String wordDoc = GetFromDocMap.get(docMap, word);
        if (wordDoc != null) {
            sb.append(wordDoc);
            return true;
        }
        String treeDoc = GetFromDocMap.get(treeMap, word);
        if (treeDoc != null) {
            sb.append(treeDoc);
            return true;
        }
        sb.append(word);
        return false;
    }

    private static void appendKeyDoc(@NotNull StringBuilder sb,
                                     @NotNull Matcher matcher,
                                     @NotNull Map<String, Map<String, List<String>>> keyMap) {
        if (!matcher.find()) {
            return;
        }
        String keyword = matcher.group();
        // "" if no doc
        String keyDoc = GetFromDocMap.get(keyMap, keyword);
        if (keyDoc != null) {
            sb.append(" ").append(keyDoc);
        }
        // no one space
        if (sb.length() != 1) {
            sb.append(" ");
        }
    }
}
