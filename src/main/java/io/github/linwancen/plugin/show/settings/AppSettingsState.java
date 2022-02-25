package io.github.linwancen.plugin.show.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@State(
        name = "io.github.linwancen.plugin.comment.settings.AppSettingsState",
        storages = @Storage("ShowCommentGlobal.xml")
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    public boolean showLineEndComment = true;
    public boolean showTreeComment = true;

    @SuppressWarnings("all")
    public Color lineEndColorBright = new Color(98, 151, 85);
    @SuppressWarnings("all")
    public Color lineEndColorDark = new Color(98, 151, 85);
    public final TextAttributes lineEndTextAttr = new TextAttributes(new JBColor(lineEndColorBright, lineEndColorDark),
            null, null, null, Font.ITALIC);

    public boolean findElementRightToLeft = true;
    public String lineEndInclude = "";
    public String lineEndExclude = "java.";
    public String[] lineEndIncludeArray = {};
    public String[] lineEndExcludeArray = {"java."};

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
