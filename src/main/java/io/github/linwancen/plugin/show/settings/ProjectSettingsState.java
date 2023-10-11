package io.github.linwancen.plugin.show.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "io.github.linwancen.plugin.show.settings.ProjectSettingsState",
        storages = @Storage("ShowCommentProject.xml")
)
public class ProjectSettingsState extends AbstractSettingsState implements PersistentStateComponent<ProjectSettingsState> {

    public static final ProjectSettingsState DEFAULT_SETTING = new ProjectSettingsState();

    public boolean globalFilterEffective = true;
    public boolean projectFilterEffective = false;

    @NotNull
    public static ProjectSettingsState getInstance(@NotNull Project project) {
        ProjectSettingsState service = project.getService(ProjectSettingsState.class);
        if (service == null) {
            return new ProjectSettingsState();
        }
        return service;
    }

    @Nullable
    @Override
    public ProjectSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ProjectSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
