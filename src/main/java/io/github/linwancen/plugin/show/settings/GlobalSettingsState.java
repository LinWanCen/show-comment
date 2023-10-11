package io.github.linwancen.plugin.show.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "io.github.linwancen.plugin.show.settings.GlobalSettingsState",
        storages = @Storage("ShowCommentGlobal.xml")
)
public class GlobalSettingsState extends AbstractSettingsState implements PersistentStateComponent<GlobalSettingsState> {

    public static final GlobalSettingsState DEFAULT_SETTING = new GlobalSettingsState();

    @NotNull
    public static GlobalSettingsState getInstance() {
        GlobalSettingsState service = ApplicationManager.getApplication().getService(GlobalSettingsState.class);
        if (service == null) {
            return new GlobalSettingsState();
        }
        return service;
    }

    @Nullable
    @Override
    public GlobalSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GlobalSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
