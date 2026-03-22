package io.github.linwancen.plugin.show.lang.base;

import io.github.linwancen.plugin.show.bean.FuncEnum;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.settings.GlobalSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseAnnoDoc<O> {

    @Nullable
    public <T extends SettingsInfo> String annoDoc(@NotNull T info, @NotNull O owner) {
        @NotNull ProjectSettingsState projectSettings = info.projectSettings;
        @NotNull GlobalSettingsState globalSettings = info.globalSettings;
        // annoDocEffect first because default false
        if (projectSettings.annoDocEffect && projectSettings.projectFilterEffective) {
            @NotNull String[][] annoDocTree = info.funcEnum == FuncEnum.TREE
                    ? projectSettings.annoDocTree
                    : projectSettings.annoDocLine;
            @Nullable String doc = annoDocArr(owner, annoDocTree);
            if (StringUtils.isNotBlank(doc)) {
                return doc;
            }
        }
        if (globalSettings.annoDocEffect && projectSettings.globalFilterEffective) {
            @NotNull String[][] annoDocTree = info.funcEnum == FuncEnum.TREE
                    ? globalSettings.annoDocTree
                    : globalSettings.annoDocLine;
            return annoDocArr(owner, annoDocTree);
        }
        return null;
    }

    @Nullable
    protected String annoDocArr(@NotNull O owner, @NotNull String[][] lines) {
        for (@NotNull String[] arr : lines) {
            if (arr.length < 3) {
                continue;
            }
            @Nullable String s = annoDocMatch(owner, arr);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    @Nullable
    protected String annoDocMatch(@NotNull O owner, @NotNull String[] arr) {
        return null;
    }
}
