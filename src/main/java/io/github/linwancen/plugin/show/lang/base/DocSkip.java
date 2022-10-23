package io.github.linwancen.plugin.show.lang.base;

import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class DocSkip {

    private DocSkip() {}

    public static boolean skipSign(@NotNull AppSettingsState appSettings,
                                   @NotNull ProjectSettingsState projectSettings, String text) {
        return skipText(text,
                projectSettings.globalFilterEffective, appSettings.lineInclude, appSettings.lineExclude,
                projectSettings.projectFilterEffective, projectSettings.lineInclude, projectSettings.lineExclude);
    }

    private static final Pattern NOT_ASCII_PATTERN = Pattern.compile("[^\u0000-\u007f]");

    public static boolean skipDoc(@NotNull AppSettingsState appSettings,
                                  @NotNull ProjectSettingsState projectSettings, @NotNull String text) {
        if (appSettings.skipAscii && !NOT_ASCII_PATTERN.matcher(text).find()) {
            return true;
        }
        return skipText(text,
                projectSettings.globalFilterEffective, appSettings.docInclude, appSettings.docExclude,
                projectSettings.projectFilterEffective, projectSettings.docInclude, projectSettings.docExclude);
    }

    static boolean skipText(@Nullable String text,
                            boolean appFilterEffective, @NotNull Pattern appDocInclude, @NotNull Pattern appDocExclude,
                            boolean projectFilterEffective, @NotNull Pattern projectDocInclude,
                            @NotNull Pattern projectDocExclude
    ) {
        if (text == null) {
            return true;
        }
        if (appFilterEffective
                && skipText(text, appDocInclude, appDocExclude)) {
            return true;
        }
        if (projectFilterEffective) {
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
        if (include.pattern().length() == 0) {
            return true;
        }
        return include.matcher(text).find();
    }

    static boolean exclude(@NotNull String text, @NotNull Pattern exclude) {
        if (exclude.pattern().length() == 0) {
            return false;
        }
        return exclude.matcher(text).find();
    }
}
