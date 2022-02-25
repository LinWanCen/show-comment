package io.github.linwancen.plugin.show.settings;

import com.intellij.application.options.ModuleAwareProjectConfigurable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ProjectSettingsConfigurable extends ModuleAwareProjectConfigurable<ProjectSettingsConfigurable> {

    public ProjectSettingsConfigurable(@NotNull Project project) {
        super(project, "show comment displayName", "show comment helpTopic");
    }

    @NotNull
    @Override
    protected ProjectSettingsConfigurable createModuleConfigurable(Module module) {
        return new ProjectSettingsConfigurable(module.getProject());
    }

    private ProjectSettingsComponent mySettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Show Comment Project.";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new ProjectSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(getProject());
        boolean modified = mySettingsComponent.getGlobalFilterEffective() != settings.globalFilterEffective;
        modified |= mySettingsComponent.getProjectFilterEffective() != settings.projectFilterEffective;
        modified |= !mySettingsComponent.getLineEndExclude().equals(settings.lineEndInclude);
        modified |= !mySettingsComponent.getLineEndInclude().equals(settings.lineEndExclude);
        return modified;
    }

    @Override
    public void apply() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(getProject());
        settings.lineEndInclude = mySettingsComponent.getLineEndInclude();
        settings.lineEndExclude = mySettingsComponent.getLineEndExclude();
        settings.globalFilterEffective = mySettingsComponent.getGlobalFilterEffective();
        settings.projectFilterEffective = mySettingsComponent.getProjectFilterEffective();
        settings.lineEndIncludeArray = SplitUtils.split(settings.lineEndInclude);
        settings.lineEndExcludeArray = SplitUtils.split(settings.lineEndExclude);
    }

    @Override
    public void reset() {
        ProjectSettingsState settings = ProjectSettingsState.getInstance(getProject());
        mySettingsComponent.setGlobalFilterEffective(settings.globalFilterEffective);
        mySettingsComponent.setProjectFilterEffective(settings.projectFilterEffective);
        mySettingsComponent.setLineEndInclude(settings.lineEndInclude);
        mySettingsComponent.setLineEndExclude(settings.lineEndExclude);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
