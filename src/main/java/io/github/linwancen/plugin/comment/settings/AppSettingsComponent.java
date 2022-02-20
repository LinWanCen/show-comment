package io.github.linwancen.plugin.comment.settings;

import com.intellij.ui.ColorPanel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class AppSettingsComponent extends AbstractSettingsComponent {

    private final JPanel myMainPanel;
    private final JBCheckBox showTreeComment = new JBCheckBox("Show tree comment ");
    private final JBCheckBox showLineEndComment = new JBCheckBox("Show line end comment ");
    private final ColorPanel lineEndColor = new ColorPanel();
    private final JBCheckBox findElementRightToLeft = new JBCheckBox("Find element right to left");

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(showPanel(), 1)
                .addComponent(colorPanel(), 1)
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
    private JPanel colorPanel() {
        JPanel color = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JLabel("line end text color:"), lineEndColor)
                .getPanel();
        color.setBorder(IdeBorderFactory.createTitledBorder("Color"));
        return color;
    }

    @NotNull
    protected JPanel lineEndFilterPanel() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder()
                .addComponent(findElementRightToLeft)
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
}
