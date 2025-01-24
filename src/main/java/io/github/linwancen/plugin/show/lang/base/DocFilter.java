package io.github.linwancen.plugin.show.lang.base;

import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.settings.AbstractSettingsState;
import io.github.linwancen.plugin.show.settings.GlobalSettingsState;
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
            // **** xx block body
            "|^ *\\*++ *+" +
            // #### xx python and sh start
            "|^ *#++ *+" +
            // -- xx SQL
            "|^ *--++ *+"
    );

    private static final Pattern LINE_SEPARATOR_PATTERN = Pattern.compile("[\r\n]");

    /**
     * line and delete /*# in resolveDocPrint()
     * end with space
     */
    @NotNull
    public static <T extends SettingsInfo> String cutDoc(@NotNull String text, @NotNull T info, boolean deletePrefix) {
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
            if (lineCountOrLenOver(info, sb, lineCount)) break;
        }
        return sb.toString();
    }

    public static <T extends SettingsInfo> boolean lineCountOrLenOver(@NotNull T info,
                                                                      @NotNull StringBuilder sb, int lineCount) {
        boolean overProject = info.projectSettings.projectFilterEffective
                && lineCountOrLenOverInfo(info.projectSettings, sb, lineCount);
        boolean overGlobal = info.projectSettings.globalFilterEffective
                && lineCountOrLenOverInfo(info.globalSettings, sb, lineCount);
        return overGlobal || overProject;
    }

    private static boolean lineCountOrLenOverInfo(@NotNull AbstractSettingsState appSettings,
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
                                   @NotNull GlobalSettingsState globalSettingsState,
                                   @NotNull ProjectSettingsState projectSettings) {
        // not effective both because regexp is slow
        // docGetEffect first because default false
        if (projectSettings.docGetEffect && projectSettings.projectFilterEffective) {
            return filterPattern(text, projectSettings.docGet);
        } else if (globalSettingsState.docGetEffect && projectSettings.globalFilterEffective) {
            return filterPattern(text, globalSettingsState.docGet);
        } else {
            return text;
        }
    }

    @NotNull
    public static String filterPattern(@NotNull String text, @NotNull Pattern docGet) {
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
            sb.append(deleteHtml).append(" ");
        }
    }

    @NotNull
    public static String html2Text(@NotNull String s) {
        return HTML_PATTERN.matcher(s).replaceAll(" ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }
}
