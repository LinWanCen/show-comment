package io.github.linwancen.plugin.show.settings;

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
import java.math.BigInteger;

@State(
        name = "io.github.linwancen.plugin.show.settings.AppSettingsState",
        storages = @Storage("ShowCommentGlobal.xml")
)
public class AppSettingsState extends AbstractSettingsState implements PersistentStateComponent<AppSettingsState> {

    public boolean showTreeComment = true;
    public boolean showLineEndComment = true;

    public final TextAttributes lineEndTextAttr = new TextAttributes(
            new JBColor(new Color(98, 151, 85), new Color(98, 151, 85)),
            null, null, null, Font.ITALIC);
    public final TextAttributes lineEndJsonTextAttr = new TextAttributes(new JBColor(Gray._140, Gray._140),
            null, null, null, Font.ITALIC);

    public boolean findElementRightToLeft = true;
    public String lineEndPrefix = "   // ";
    public int lineEndCount = 2;
    public int lineEndLen = 0;
    public boolean fromCall = true;
    public boolean fromNew = true;
    public boolean fromRef = true;
    public boolean inJson = true;
    public boolean skipAnnotation = true;
    public boolean skipAscii = false;
    public boolean skipBlank = true;

    public static AppSettingsState getInstance() {
        AppSettingsState service = ApplicationManager.getApplication().getService(AppSettingsState.class);
        if (service == null) {
            return new AppSettingsState();
        }
        return service;
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

    public String getLineEndColor() {
        return Integer.toHexString(lineEndTextAttr.getForegroundColor().getRGB()).toUpperCase();
    }

    public void setLineEndColor(String s) {
        int rgb = new BigInteger(s, 16).intValue();
        lineEndTextAttr.setForegroundColor(new JBColor(new Color(rgb), new Color(rgb)));
    }

    public String getLineEndJsonColor() {
        return Integer.toHexString(lineEndJsonTextAttr.getForegroundColor().getRGB()).toUpperCase();
    }

    public void setLineEndJsonColor(String s) {
        int rgb = new BigInteger(s, 16).intValue();
        lineEndJsonTextAttr.setForegroundColor(new JBColor(new Color(rgb), new Color(rgb)));
    }
}
