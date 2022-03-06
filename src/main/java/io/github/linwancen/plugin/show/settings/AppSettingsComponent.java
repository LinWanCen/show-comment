package io.github.linwancen.plugin.show.settings;

import com.intellij.ui.ColorPanel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class AppSettingsComponent extends AbstractSettingsComponent {

    private final JPanel myMainPanel;
    private final JBCheckBox showTreeComment = new JBCheckBox("Show tree comment ");
    private final JBCheckBox showLineEndComment = new JBCheckBox("Show line end comment ");
    private final JBCheckBox fromCall = new JBCheckBox("call ");
    private final JBCheckBox fromNew = new JBCheckBox("new ");
    private final JBCheckBox fromRef = new JBCheckBox("ref ");
    private final JBCheckBox inJson = new JBCheckBox("in json ");
    private final ColorPanel lineEndColor = new ColorPanel();
    private final JBCheckBox findElementRightToLeft = new JBCheckBox("Find element right to left");
    protected final JBTextField lineEndPrefix = new JBTextField();
    protected final JBTextField lineEndCount = new JBTextField();

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(showPanel(), 1)
                .addComponent(lineEndFilterPanel(), 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @NotNull
    private JPanel showPanel() {
        JPanel comment = FormBuilder.createFormBuilder()
                .addComponent(showTreeComment, 1)
                .addComponent(showLineEndComment, 1)
                .getPanel();
        comment.setBorder(IdeBorderFactory.createTitledBorder("Show"));
        return comment;
    }

    @NotNull
    protected JPanel lineEndFilterPanel() {
        JPanel text = JPanelFactory.of(
                new JBLabel("object count: "), lineEndCount,
                new JBLabel("text color: "), lineEndColor,
                new JBLabel("prefix: "), lineEndPrefix);
        FormBuilder formBuilder = FormBuilder.createFormBuilder()
                .addComponent(JPanelFactory.of(findElementRightToLeft))
                .addSeparator()
                .addComponent(JPanelFactory.of(fromCall, fromNew, fromRef, inJson), 1)
                .addSeparator()
                .addComponent(text)
                .addSeparator();
        return commonLineEndFilter(formBuilder);
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return showTreeComment;
    }


    public boolean getShowTreeComment() {
        return showTreeComment.isSelected();
    }

    public void setShowTreeComment(boolean newStatus) {
        showTreeComment.setSelected(newStatus);
    }

    public boolean getShowLineEndComment() {
        return showLineEndComment.isSelected();
    }

    public void setShowLineEndComment(boolean newStatus) {
        showLineEndComment.setSelected(newStatus);
    }

    public boolean getFromCall() {
        return fromCall.isSelected();
    }

    public void setFromCall(boolean newStatus) {
        fromCall.setSelected(newStatus);
    }

    public boolean getFromNew() {
        return fromNew.isSelected();
    }

    public void setFromNew(boolean newStatus) {
        fromNew.setSelected(newStatus);
    }

    public boolean getFromRef() {
        return fromRef.isSelected();
    }

    public void setFromRef(boolean newStatus) {
        fromRef.setSelected(newStatus);
    }

    public boolean getInJson() {
        return inJson.isSelected();
    }

    public void setInJson(boolean newStatus) {
        inJson.setSelected(newStatus);
    }

    public Color getLineEndColor() {
        return lineEndColor.getSelectedColor();
    }

    public void setLineEndColor(Color color) {
        lineEndColor.setSelectedColor(color);
    }

    public boolean getFindElementRightToLeft() {
        return findElementRightToLeft.isSelected();
    }

    public void setFindElementRightToLeft(boolean newStatus) {
        findElementRightToLeft.setSelected(newStatus);
    }

    @NotNull
    public String getLineEndPrefix() {
        return lineEndPrefix.getText();
    }

    public void setLineEndPrefix(@NotNull String newText) {
        lineEndPrefix.setText(newText);
    }

    @NotNull
    public String getLineEndCount() {
        return lineEndCount.getText();
    }

    public void setLineEndCount(@NotNull String newText) {
        lineEndCount.setText(newText);
    }
}
