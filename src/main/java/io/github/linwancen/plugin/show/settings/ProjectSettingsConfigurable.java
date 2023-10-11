package io.github.linwancen.plugin.show.settings;

import com.intellij.application.options.ModuleAwareProjectConfigurable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ProjectSettingsConfigurable extends ModuleAwareProjectConfigurable<ProjectSettingsConfigurable> {

    public ProjectSettingsConfigurable(@NotNull Project project) {
        super(project, "show comment displayName", "show comment helpTopic");
    }

    @NotNull
    @Override
    protected ProjectSettingsConfigurable createModuleConfigurable(@NotNull Module module) {
        return new ProjectSettingsConfigurable(module.getProject());
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NotNull
    private ProjectSettingsComponent mySettingsComponent;

    @NotNull
    @Override
    public String getDisplayName() {
        return "// Show Comment Project";
    }

    @NotNull
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
        @NotNull ProjectSettingsState settings = ProjectSettingsState.getInstance(getProject());
        boolean modified = mySettingsComponent.getGlobalFilterEffective() != settings.globalFilterEffective;
        modified |= mySettingsComponent.getProjectFilterEffective() != settings.projectFilterEffective;
        modified = AbstractSettingsConfigurable.isModified(settings, mySettingsComponent, modified);
        return modified;
    }

    @Override
    public void apply() {
        @NotNull ProjectSettingsState settings = ProjectSettingsState.getInstance(getProject());
        settings.globalFilterEffective = mySettingsComponent.getGlobalFilterEffective();
        settings.projectFilterEffective = mySettingsComponent.getProjectFilterEffective();
        AbstractSettingsConfigurable.apply(settings, mySettingsComponent);
    }

    @Override
    public void reset() {
        @NotNull ProjectSettingsState settings = ProjectSettingsState.getInstance(getProject());
        reset(settings, mySettingsComponent);
    }

    static void reset(@NotNull ProjectSettingsState settings, @NotNull ProjectSettingsComponent mySettingsComponent1) {
        mySettingsComponent1.setGlobalFilterEffective(settings.globalFilterEffective);
        mySettingsComponent1.setProjectFilterEffective(settings.projectFilterEffective);
        AbstractSettingsConfigurable.reset(settings, mySettingsComponent1);
    }

    @Override
    public void disposeUIResources() {
        //noinspection ConstantConditions
        mySettingsComponent = null;
    }

}
