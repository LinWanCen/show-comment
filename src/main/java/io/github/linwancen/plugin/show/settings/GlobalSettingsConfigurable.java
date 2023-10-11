package io.github.linwancen.plugin.show.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GlobalSettingsConfigurable implements Configurable {

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NotNull
    private GlobalSettingsComponent mySettingsComponent;

    @NotNull
    @Override
    public String getDisplayName() {
        return "// Show Comment Global";
    }

    @NotNull
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new GlobalSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        @NotNull GlobalSettingsState settings = GlobalSettingsState.getInstance();
        return AbstractSettingsConfigurable.isModified(settings, mySettingsComponent, false);
    }

    @Override
    public void apply() {
        @NotNull GlobalSettingsState settings = GlobalSettingsState.getInstance();
        AbstractSettingsConfigurable.apply(settings, mySettingsComponent);
    }

    @Override
    public void reset() {
        @NotNull GlobalSettingsState settings = GlobalSettingsState.getInstance();
        reset(settings, mySettingsComponent);
    }

    static void reset(@NotNull GlobalSettingsState settings, GlobalSettingsComponent mySettingsComponent1) {
        AbstractSettingsConfigurable.reset(settings, mySettingsComponent1);
    }

    @Override
    public void disposeUIResources() {
        //noinspection ConstantConditions
        mySettingsComponent = null;
    }

}
