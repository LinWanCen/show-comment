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
import java.util.Locale;

@State(
        name = "io.github.linwancen.plugin.show.settings.AppSettingsState",
        storages = @Storage("ShowCommentGlobal.xml")
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    public static final AppSettingsState DEFAULT_SETTING = new AppSettingsState();

    public boolean showTreeComment = true;
    public boolean compact = true;
    public boolean treeCache = true;

    public boolean showLineEndComment = true;
    public boolean lineEndCache = true;
    public boolean showLineEndCommentJava = true;
    public boolean showLineEndCommentJavaBase = false;
    public boolean showLineEndCommentKotlin = true;
    public boolean showLineEndCommentKotlinBase = false;
    public boolean showLineEndCommentJs = true;
    public boolean showLineEndCommentJsBase = false;
    public boolean showLineEndCommentPhp = true;
    public boolean showLineEndCommentPhpBase = false;
    public boolean showLineEndCommentPy = true;
    public boolean showLineEndCommentPyBase = false;
    public boolean showLineEndCommentGo = true;
    public boolean showLineEndCommentGoBase = false;
    public boolean showLineEndCommentRustBase = true;
    public boolean showLineEndCommentCBase = true;
    public boolean showLineEndCommentSql = true;
    public boolean showLineEndCommentJson = true;

    @NotNull
    public String[] treeTags = {"author"};
    @NotNull
    public String[] lineTags = {};

    public final TextAttributes lineEndTextAttr = new TextAttributes(
            new JBColor(new Color(98, 151, 85), new Color(98, 151, 85)),
            null, null, null, Font.ITALIC);
    public final TextAttributes lineEndJsonTextAttr = new TextAttributes(new JBColor(Gray._140, Gray._140),
            null, null, null, Font.ITALIC);

    @NotNull
    public String lineEndPrefix = "   // ";
    public boolean getToSet = true;
    public boolean fromNew = true;
    public boolean fromParam = false;
    public boolean enumDoc = true;
    public boolean skipAnnotation = true;
    public boolean skipAscii = !"en".equals(Locale.getDefault().getLanguage());
    public boolean skipBlank = true;

    @NotNull
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

    @NotNull
    public String getLineEndColor() {
        return Integer.toHexString(lineEndTextAttr.getForegroundColor().getRGB()).toUpperCase();
    }

    public void setLineEndColor(@NotNull String s) {
        int rgb = new BigInteger(s, 16).intValue();
        lineEndTextAttr.setForegroundColor(new JBColor(new Color(rgb), new Color(rgb)));
    }

    @NotNull
    public String getLineEndJsonColor() {
        return Integer.toHexString(lineEndJsonTextAttr.getForegroundColor().getRGB()).toUpperCase();
    }

    public void setLineEndJsonColor(@NotNull String s) {
        int rgb = new BigInteger(s, 16).intValue();
        lineEndJsonTextAttr.setForegroundColor(new JBColor(new Color(rgb), new Color(rgb)));
    }
}
