package io.github.linwancen.plugin.show.bean;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.GlobalSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;

public class SettingsInfo {
    public final @NotNull Project project;
    public final @NotNull AppSettingsState appSettings;
    public final @NotNull GlobalSettingsState globalSettings;
    public final @NotNull ProjectSettingsState projectSettings;
    public final @NotNull FuncEnum funcEnum;
    public final @NotNull DumbService dumbService;

    protected SettingsInfo(@NotNull Project project, @NotNull FuncEnum funcEnum) {
        this.project = project;
        this.funcEnum = funcEnum;
        this.appSettings = AppSettingsState.getInstance();
        this.globalSettings = GlobalSettingsState.getInstance();
        this.projectSettings = ProjectSettingsState.getInstance(project);
        this.dumbService = DumbService.getInstance(project);
    }

    public static @NotNull SettingsInfo of(@NotNull Project project, @NotNull FuncEnum funcEnum) {
        return new SettingsInfo(project, funcEnum);
    }

    /** treeTags/lineTags */
    @NotNull
    public String[] tagNames() {
        return funcEnum == FuncEnum.TREE
                ? appSettings.treeTags
                : appSettings.lineTags;
    }

    public boolean isDumb() {
        return dumbService.isDumb();
    }
}
