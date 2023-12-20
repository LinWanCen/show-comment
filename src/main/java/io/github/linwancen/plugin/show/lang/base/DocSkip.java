package io.github.linwancen.plugin.show.lang.base;

import io.github.linwancen.plugin.show.bean.SettingsInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class DocSkip {

    private DocSkip() {}

    public static <T extends SettingsInfo> boolean skipSign(@NotNull T info, String text) {
        return skipText(info, text,
                info.globalSettings.lineInclude, info.globalSettings.lineExclude,
                info.projectSettings.lineInclude, info.projectSettings.lineExclude);
    }

    private static final Pattern NOT_ASCII_PATTERN = Pattern.compile("[^\u0000-\u007f]");

    public static <T extends SettingsInfo> boolean skipDoc(@NotNull T info, @NotNull String text) {
        if (info.appSettings.skipAscii && !NOT_ASCII_PATTERN.matcher(text).find()) {
            return true;
        }
        return skipText(info, text,
                info.globalSettings.docInclude, info.globalSettings.docExclude,
                info.projectSettings.docInclude, info.projectSettings.docExclude);
    }

    static <T extends SettingsInfo> boolean skipText(@NotNull T info, @Nullable String text,
                            @NotNull Pattern appDocInclude, @NotNull Pattern appDocExclude,
                            @NotNull Pattern projectDocInclude, @NotNull Pattern projectDocExclude
    ) {
        if (text == null) {
            return true;
        }
        if (info.projectSettings.globalFilterEffective
                && skipText(text, appDocInclude, appDocExclude)) {
            return true;
        }
        if (info.projectSettings.projectFilterEffective) {
            return skipText(text, projectDocInclude, projectDocExclude);
        }
        return false;
    }

    static boolean skipText(@NotNull String text, @NotNull Pattern include, @NotNull Pattern exclude) {
        if (exclude(text, exclude)) {
            return true;
        }
        return !include(text, include);
    }

    static boolean include(@NotNull String text, @NotNull Pattern include) {
        if (include.pattern().isEmpty()) {
            return true;
        }
        return include.matcher(text).find();
    }

    static boolean exclude(@NotNull String text, @NotNull Pattern exclude) {
        if (exclude.pattern().isEmpty()) {
            return false;
        }
        return exclude.matcher(text).find();
    }
}
