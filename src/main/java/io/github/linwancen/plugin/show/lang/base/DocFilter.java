package io.github.linwancen.plugin.show.lang.base;

import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocFilter {

    private DocFilter() {}

    private static final Pattern DOC_PATTERN = Pattern.compile("(?m" +
            // ///// xx line start
            ")^ *//++ *+" +
            // /**** xx block start
            "|^ */\\*++ *+" +
            // ****/ xx block end, not only line start and must before ****
            "| *\\*++/.*" +
            // **** xx  block body
            "|^ *\\*++ *+" +
            // #### xx  python and shell start
            "|^ *#++ *+"
    );

    private static final Pattern LINE_SEPARATOR_PATTERN = Pattern.compile("[\r\n]");

    /**
     * delete / * # in resolveDocPrint()
     * end with space
     */
    @NotNull
    public static String cutDoc(String text,
                                @NotNull AppSettingsState appSettings, boolean deletePrefix) {
        String[] split = LINE_SEPARATOR_PATTERN.split(text);
        int lineCount = 0;
        @NotNull StringBuilder sb = new StringBuilder();
        for (@NotNull String s : split) {
            if (deletePrefix) {
                s = DOC_PATTERN.matcher(s).replaceAll("");
            }
            s = s.trim();
            sb.append(s);
            if (!s.isEmpty()) {
                sb.append(" ");
            }
            lineCount++;
            if (lineCountOrLenOver(appSettings, sb, lineCount)) break;
        }
        return sb.toString();
    }

    public static boolean lineCountOrLenOver(@NotNull AppSettingsState appSettings,
                                             @NotNull StringBuilder sb, int lineCount) {
        boolean countOver = appSettings.lineEndCount > 0 && lineCount >= appSettings.lineEndCount;
        boolean lenOver = appSettings.lineEndLen > 0 && sb.length() >= appSettings.lineEndLen;
        return countOver || lenOver;
    }

    /**
     * filter doc text in resolveDocPrint()
     */
    @NotNull
    public static String filterDoc(@NotNull String text,
                                   @NotNull AppSettingsState appSettings,
                                   @NotNull ProjectSettingsState projectSettings) {
        // docGetEffect first because default false
        if (projectSettings.docGetEffect && projectSettings.projectFilterEffective) {
            return filterDoc(text, projectSettings.docGet);
        } else if (appSettings.docGetEffect && projectSettings.globalFilterEffective) {
            return filterDoc(text, appSettings.docGet);
        } else {
            return text;
        }
    }

    @NotNull
    public static String filterDoc(@NotNull String text, @NotNull Pattern docGet) {
        // if effect skip check empty
        @NotNull Matcher m = docGet.matcher(text);
        if (m.find()) {
            return m.group(m.groupCount());
        }
        // one line
        return text;
    }

    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]++>");

    /**
     * trim end with space
     */
    public static void addHtml(@NotNull StringBuilder sb, @NotNull String s) {
        @NotNull String deleteHtml = html2Text(s);
        if (!deleteHtml.isEmpty()) {
            sb.append(deleteHtml);
        }
    }

    @NotNull
    public static String html2Text(@NotNull String s) {
        return HTML_PATTERN.matcher(s).replaceAll(" ").trim();
    }
}
