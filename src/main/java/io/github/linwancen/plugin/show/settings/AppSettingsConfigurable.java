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
        modified |= mySettingsComponent.getShowLineEndCommentForCall() != settings.showLineEndCommentForCall;
        modified |= mySettingsComponent.getShowLineEndCommentForNew() != settings.showLineEndCommentForNew;
        modified |= mySettingsComponent.getShowLineEndCommentForRef() != settings.showLineEndCommentForRef;
        if (EditorColorsManager.getInstance().isDarkEditor()) {
            modified |= !mySettingsComponent.getLineEndColor().equals(settings.lineEndColorDark);
        } else {
            modified |= !mySettingsComponent.getLineEndColor().equals(settings.lineEndColorBright);
        }
        modified |= mySettingsComponent.getFindElementRightToLeft() != settings.findElementRightToLeft;
        modified |= !mySettingsComponent.getLineEndInclude().equals(settings.lineEndInclude);
        modified |= !mySettingsComponent.getLineEndExclude().equals(settings.lineEndExclude);
        return modified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.showTreeComment = mySettingsComponent.getShowTreeComment();
        settings.showLineEndComment = mySettingsComponent.getShowLineEndComment();
        settings.showLineEndCommentForCall = mySettingsComponent.getShowLineEndCommentForCall();
        settings.showLineEndCommentForNew = mySettingsComponent.getShowLineEndCommentForNew();
        settings.showLineEndCommentForRef = mySettingsComponent.getShowLineEndCommentForRef();
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
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.setShowTreeComment(settings.showTreeComment);
        mySettingsComponent.setShowLineEndComment(settings.showLineEndComment);
        mySettingsComponent.setShowLineEndCommentForCall(settings.showLineEndCommentForCall);
        mySettingsComponent.setShowLineEndCommentForNew(settings.showLineEndCommentForNew);
        mySettingsComponent.setShowLineEndCommentForRef(settings.showLineEndCommentForRef);
        if (EditorColorsManager.getInstance().isDarkEditor()) {
            mySettingsComponent.setLineEndColor(settings.lineEndColorDark);
        } else {
            mySettingsComponent.setLineEndColor(settings.lineEndColorBright);
        }
        mySettingsComponent.setFindElementRightToLeft(settings.findElementRightToLeft);
        mySettingsComponent.setLineEndInclude(settings.lineEndInclude);
        mySettingsComponent.setLineEndExclude(settings.lineEndExclude);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
