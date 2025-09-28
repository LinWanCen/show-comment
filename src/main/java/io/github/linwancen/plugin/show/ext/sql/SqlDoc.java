package io.github.linwancen.plugin.show.ext.sql;

import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.settings.AbstractSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlDoc {

    @Nullable
    public static String sqlDoc(@NotNull LineInfo info, @NotNull String code) {
        @Nullable String extension = info.file.getExtension();
        if (extension == null) {
            return null;
        }
        @Nullable String s = sqlDocState(extension, info.projectSettings, code);
        if (s != null) {
            return s;
        }
        return sqlDocState(extension, info.globalSettings, code);
    }

    @Nullable
    private static String sqlDocState(@NotNull String extension, @NotNull AbstractSettingsState state,
                                      @NotNull String code) {
        if (!state.sqlSplitEffect) {
            return null;
        }
        Pattern[] patterns = state.sqlSplit.get(extension);
        if (patterns == null) {
            return null;
        }
        for (@NotNull Pattern pattern : patterns) {
            return sqlDocPattern(pattern, code);
        }
        return null;
    }


    @Nullable
    private static String sqlDocPattern(@NotNull Pattern pattern, @NotNull String code) {
        String[] words = pattern.split(code);
        @NotNull Matcher matcher = pattern.matcher(code);

        boolean haveDoc = false;
        boolean haveKey = false;
        @NotNull StringBuilder sb = new StringBuilder();
        for (@NotNull String s : words) {
            haveDoc |= appendDocSQL(sb, s);
            haveKey = appendKeyDocSQL(sb, matcher);
        }
        while (haveKey) {
            haveKey = appendKeyDocSQL(sb, matcher);
        }
        if (!haveDoc) {
            return null;
        }
        return sb.toString();
    }

    private static boolean appendDocSQL(@NotNull StringBuilder sb, @NotNull String word) {
        word = word.trim();
        if (word.isEmpty()) {
            return false;
        }
        @Nullable String tableDoc = SqlCache.TABLE_DOC_CACHE.get(word);
        if (tableDoc != null) {
            sb.append(tableDoc);
            return true;
        }
        @Nullable String columnDoc = SqlCache.COLUMN_DOC_CACHE.get(word);
        if (columnDoc != null) {
            sb.append(columnDoc);
            @Nullable String indexDoc = SqlCache.INDEX_DOC_CACHE.get(word);
            if (indexDoc != null) {
                sb.append(indexSymbol(indexDoc));
            }
            return true;
        }
        sb.append(" ");
        return false;
    }

    @NotNull
    private static String indexSymbol(@NotNull String indexDoc) {
        switch (indexDoc) {
            case "primary":
            case "PRIMARY":
                return " √ P";
            case "UNIQUE":
            case "unique":
                return " √ U";
            case ")":
                return " √";
            default:
                // multi column index seq
                return " √ " + indexDoc;
        }
    }

    private static boolean appendKeyDocSQL(@NotNull StringBuilder sb, @NotNull Matcher matcher) {
        if (!matcher.find()) {
            return false;
        }
        String keyword = matcher.group();
        keyword = unescape(keyword);
        sb.append(keyword);
        return true;
    }

    @NotNull
    private static String unescape(@NotNull String keyword) {
        // &gt;|&lt;|&amp;|&quot;|&apos;
        switch (keyword) {
            case "&gt;":
                return ">";
            case "&lt;":
                return "<";
            case "&amp;":
                return "&";
            case "&quot;":
                return "\"";
            case "&apos;":
                return "'";
            default:
                return " ";
        }
    }
}
