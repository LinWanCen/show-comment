package io.github.linwancen.plugin.comment.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@State(
        name = "io.github.linwancen.plugin.comment.settings.AppSettingsState",
        storages = @Storage("ShowCommentPlugin.xml")
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    public boolean showLineEndComment = true;
    public boolean showTreeComment = true;

    private static final JBColor lineEndColor = new JBColor(new Color(98, 151, 85), Gray._140);
    public final TextAttributes lineEndTextAttr = new TextAttributes(lineEndColor,
            null, null, null, Font.ITALIC);

    public static AppSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsState.class);
    }

    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
