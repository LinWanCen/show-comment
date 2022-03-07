package io.github.linwancen.plugin.show.settings;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent mySettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Show Comment Global.";
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
        modified |= mySettingsComponent.getFromCall() != settings.fromCall;
        modified |= mySettingsComponent.getFromNew() != settings.fromNew;
        modified |= mySettingsComponent.getFromRef() != settings.fromRef;
        modified |= mySettingsComponent.getInJson() != settings.inJson;
        if (EditorColorsManager.getInstance().isDarkEditor()) {
            modified |= !mySettingsComponent.getLineEndColor().equals(settings.lineEndColorDark);
        } else {
            modified |= !mySettingsComponent.getLineEndColor().equals(settings.lineEndColorBright);
        }
        modified |= mySettingsComponent.getFindElementRightToLeft() != settings.findElementRightToLeft;
        modified |= !mySettingsComponent.getLineEndInclude().equals(settings.lineEndInclude);
        modified |= !mySettingsComponent.getLineEndExclude().equals(settings.lineEndExclude);
        modified |= !mySettingsComponent.getLineEndPrefix().equals(settings.lineEndPrefix);
        modified |= !mySettingsComponent.getLineEndCount().equals(String.valueOf(settings.lineEndCount));
        return modified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.showTreeComment = mySettingsComponent.getShowTreeComment();
        settings.showLineEndComment = mySettingsComponent.getShowLineEndComment();
        settings.fromCall = mySettingsComponent.getFromCall();
        settings.fromNew = mySettingsComponent.getFromNew();
        settings.fromRef = mySettingsComponent.getFromRef();
        settings.inJson = mySettingsComponent.getInJson();
        if (EditorColorsManager.getInstance().isDarkEditor()) {
            settings.lineEndColorDark = mySettingsComponent.getLineEndColor();
        } else {
            settings.lineEndColorBright = mySettingsComponent.getLineEndColor();
        }
        JBColor jbColor = new JBColor(settings.lineEndColorBright, settings.lineEndColorDark);
        settings.lineEndTextAttr.setForegroundColor(jbColor);
        settings.findElementRightToLeft = mySettingsComponent.getFindElementRightToLeft();
        settings.lineEndInclude = mySettingsComponent.getLineEndInclude();
        settings.lineEndExclude = mySettingsComponent.getLineEndExclude();
        settings.lineEndIncludeArray = SplitUtils.split(settings.lineEndInclude);
        settings.lineEndExcludeArray = SplitUtils.split(settings.lineEndExclude);
        settings.lineEndPrefix = mySettingsComponent.getLineEndPrefix();
        try {
            settings.lineEndCount = Integer.parseInt(mySettingsComponent.getLineEndCount());
        } catch (NumberFormatException e) {
            mySettingsComponent.setLineEndCount(String.valueOf(settings.lineEndCount));
        }
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setShowTreeComment(settings.showTreeComment);
        mySettingsComponent.setShowLineEndComment(settings.showLineEndComment);
        mySettingsComponent.setFromCall(settings.fromCall);
        mySettingsComponent.setFromNew(settings.fromNew);
        mySettingsComponent.setFromRef(settings.fromRef);
        mySettingsComponent.setInJson(settings.inJson);
        if (EditorColorsManager.getInstance().isDarkEditor()) {
            mySettingsComponent.setLineEndColor(settings.lineEndColorDark);
        } else {
            mySettingsComponent.setLineEndColor(settings.lineEndColorBright);
        }
        mySettingsComponent.setFindElementRightToLeft(settings.findElementRightToLeft);
        mySettingsComponent.setLineEndInclude(settings.lineEndInclude);
        mySettingsComponent.setLineEndExclude(settings.lineEndExclude);
        mySettingsComponent.setLineEndPrefix(settings.lineEndPrefix);
        mySettingsComponent.setLineEndCount(String.valueOf(settings.lineEndCount));
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}