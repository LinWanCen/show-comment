package io.github.linwancen.plugin.comment.settings;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent mySettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Show Comment.";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        boolean modified = mySettingsComponent.getShowTreeComment() != settings.showTreeComment;
        modified |= mySettingsComponent.getShowLineEndComment() != settings.showLineEndComment;
        modified |= !mySettingsComponent.getLineEndColor().equals(settings.lineEndTextAttr.getForegroundColor());
        return modified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.showTreeComment = mySettingsComponent.getShowTreeComment();
        settings.showLineEndComment = mySettingsComponent.getShowLineEndComment();
        settings.lineEndTextAttr.setForegroundColor(jbColor());
    }

    private JBColor jbColor() {
        if (EditorColorsManager.getInstance().isDarkEditor()) {
            return new JBColor(new Color(98, 151, 85), mySettingsComponent.getLineEndColor());
        } else {
            return new JBColor(mySettingsComponent.getLineEndColor(), Gray._140);
        }
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setShowTreeComment(settings.showTreeComment);
        mySettingsComponent.setShowLineEndComment(settings.showLineEndComment);
        mySettingsComponent.setLineEndColor(settings.lineEndTextAttr.getForegroundColor());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
