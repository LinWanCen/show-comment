package io.github.linwancen.plugin.show.ext;

import io.github.linwancen.plugin.show.ext.conf.ConfCache;
import io.github.linwancen.plugin.show.bean.LineInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineExt {

    private LineExt() {}

    public static @Nullable String doc(@NotNull LineInfo lineInfo) {
        int i = lineInfo.text.indexOf(lineInfo.appSettings.lineEndPrefix);
        @NotNull String code = i <= 0 ? lineInfo.text : lineInfo.text.substring(0, i);
        @Nullable String extDoc = LineExt.extDoc(lineInfo, code);
        if (extDoc == null) {
            return null;
        }
        extDoc = extDoc.trim();
        if (lineInfo.text.endsWith(extDoc)) {
            return null;
        }
        return extDoc;
    }

    @Nullable
    public static String extDoc(@NotNull LineInfo lineInfo, @NotNull String code) {
        @NotNull String path = lineInfo.file.getPath();
        @NotNull String name = lineInfo.file.getName();
        @Nullable String ext = lineInfo.file.getExtension();
        @NotNull Map<String, Map<String, List<String>>> keyMap = ConfCache.keyMap(path, name, ext);
        if (keyMap.isEmpty()) {
            return null;
        }
        @Nullable Pattern pattern = ConfCache.pattern(lineInfo.project, keyMap, path);
        if (pattern == null || pattern.pattern().length() == 0) {
            return null;
        }
        @NotNull Map<String, Map<String, List<String>>> docMap = ConfCache.docMap(path, name, ext);
        @NotNull Map<String, Map<String, List<String>>> treeMap = ConfCache.treeMap(path);
        if (docMap.isEmpty() && treeMap.isEmpty()) {
            return null;
        }
        if (ext == null || "cbl".equals(ext) || "cob".equals(ext) || "cobol".equals(ext)) {
            code = cblNotAndOr(code);
        }
        String[] words = pattern.split(code);
        @NotNull Matcher matcher = pattern.matcher(code);
        return extDoc(keyMap, matcher, docMap, words, treeMap);
    }

    private static final Pattern DICT_PATTERN = Pattern.compile("([\\w-]++) ?(NOT)? ?= ?\\(? ?'");
    private static final Pattern AND_OR_PATTERN = Pattern.compile("(AND|OR) ?'");

    @NotNull
    private static String cblNotAndOr(@NotNull String text) {
        // maybe faster than regexp
        if (!text.contains("=")) {
            return text;
        }
        @NotNull Matcher matcher = DICT_PATTERN.matcher(text);
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
        boolean haveKey = false;
        @NotNull StringBuilder sb = new StringBuilder();
        for (@NotNull String s : words) {
            haveDoc |= appendDoc(sb, s, docMap, treeMap);
            haveKey = appendKeyDoc(sb, matcher, keyMap);
        }
        while (haveKey) {
            haveKey = appendKeyDoc(sb, matcher, keyMap);
        }
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
        @Nullable String wordDoc = GetFromDocMap.get(docMap, word);
        if (wordDoc != null) {
            sb.append(wordDoc);
            return true;
        }
        @Nullable String treeDoc = GetFromDocMap.get(treeMap, word);
        if (treeDoc != null) {
            sb.append(treeDoc);
            return true;
        }
        // not word doc ues word
        sb.append(word);
        return false;
    }

    private static boolean appendKeyDoc(@NotNull StringBuilder sb,
                                        @NotNull Matcher matcher,
                                        @NotNull Map<String, Map<String, List<String>>> keyMap) {
        if (!matcher.find()) {
            return false;
        }
        String keyword = matcher.group();
        // "" if no doc
        @Nullable String keyDoc = GetFromDocMap.get(keyMap, keyword);
        if (keyDoc != null) {
            sb.append(" ").append(keyDoc);
        }
        // no one space
        if (sb.length() != 1) {
            sb.append(" ");
        }
        return true;
    }
}
