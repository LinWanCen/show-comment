package io.github.linwancen.plugin.comment.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "io.github.linwancen.plugin.comment.settings.ProjectSettingsState",
        storages = @Storage("ShowCommentProject.xml")
)
public class ProjectSettingsState implements PersistentStateComponent<ProjectSettingsState> {

    public boolean globalFilterEffective = true;
    public boolean projectFilterEffective = false;
    public String lineEndInclude = "";
    public String lineEndExclude = "";
    public String[] lineEndIncludeArray = {};
    public String[] lineEndExcludeArray = {};

    public static ProjectSettingsState getInstance(Project project) {
        return ServiceManager.getService(project, ProjectSettingsState.class);
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
