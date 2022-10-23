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
    private final JBCheckBox compact = new JBCheckBox("compact ");
    private final JBTextField treeTags = new JBTextField();
    private final JBCheckBox showLineEndComment = new JBCheckBox("Show line end comment ");
    private final JBCheckBox showLineEndCommentJava = new JBCheckBox("Java ");
    private final JBCheckBox showLineEndCommentSql = new JBCheckBox("sql ");
    private final JBCheckBox showLineEndCommentJson = new JBCheckBox("json ");
    private final JBCheckBox showLineEndCommentJs = new JBCheckBox("js ");
    private final JBCheckBox jsdoc = new JBCheckBox("jsdoc ");
    private final JBCheckBox showLineEndCommentPy = new JBCheckBox("Python ");
    private final JBCheckBox showLineEndCommentGo = new JBCheckBox("Go ");
    private final JBCheckBox showLineEndCommentKotlin = new JBCheckBox("Kotlin ");
    private final JBTextField lineTags = new JBTextField();
    private final JBCheckBox getToSet = new JBCheckBox("get |> set ");
    private final JBCheckBox fromNew = new JBCheckBox("java new ");
    private final JBCheckBox fromParam = new JBCheckBox("java param ");
    private final JBCheckBox skipAnnotation = new JBCheckBox("skip @ ");
    private final JBCheckBox skipAscii = new JBCheckBox("skip English ");
    private final JBCheckBox skipBlank = new JBCheckBox("skip Blank ");
    private final ColorPanel lineEndColor = new ColorPanel();
    private final ColorPanel lineEndJsonColor = new ColorPanel();
    private final JBTextField lineEndPrefix = new JBTextField();
    private final JBTextField lineEndCount = new JBTextField();

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
                .addComponent(JPanelFactory.of(showTreeComment, compact), 1)
                .addComponent(JPanelFactory.of(showLineEndComment,
                        showLineEndCommentJava,
                        showLineEndCommentSql,
                        showLineEndCommentJson,
                        showLineEndCommentJs,
                        jsdoc,
                        showLineEndCommentPy,
                        showLineEndCommentGo,
                        showLineEndCommentKotlin
                ), 1)
                .addLabeledComponent(new JBLabel("tree tags split by |:"), treeTags, 1, true)
                .addLabeledComponent(new JBLabel("line tags split by |:"), lineTags, 1, true)
                .getPanel();
        comment.setBorder(IdeBorderFactory.createTitledBorder("Show"));
        return comment;
    }

    @NotNull
    protected JPanel lineEndFilterPanel() {
        JPanel text = JPanelFactory.of(
                new JBLabel("line count: "), lineEndCount,
                new JBLabel("text color: "), lineEndColor,
                new JBLabel("json text color: "), lineEndJsonColor,
                new JBLabel("prefix: "), lineEndPrefix);
        FormBuilder formBuilder = FormBuilder.createFormBuilder()
                // .addComponent(JPanelFactory.of(findElementRightToLeft))
                .addSeparator()
                // .addComponent(JPanelFactory.of(fromCall, fromNew, fromRef, inJson, skipAnnotation, skipAscii, skipBlank), 1)
                .addComponent(JPanelFactory.of(fromNew, fromParam, getToSet, skipAnnotation, skipAscii, skipBlank), 1)
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

    public boolean getCompact() {
        return compact.isSelected();
    }

    public void setCompact(boolean newStatus) {
        compact.setSelected(newStatus);
    }

    @NotNull
    public String getTreeTags() {
        return treeTags.getText();
    }

    public void setTreeTags(@NotNull String newText) {
        treeTags.setText(newText);
    }

    public boolean getShowLineEndComment() {
        return showLineEndComment.isSelected();
    }

    public void setShowLineEndComment(boolean newStatus) {
        showLineEndComment.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentJava() {
        return showLineEndCommentJava.isSelected();
    }

    public void setShowLineEndCommentJava(boolean newStatus) {
        showLineEndCommentJava.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentSql() {
        return showLineEndCommentSql.isSelected();
    }

    public void setShowLineEndCommentSql(boolean newStatus) {
        showLineEndCommentSql.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentJson() {
        return showLineEndCommentJson.isSelected();
    }

    public void setShowLineEndCommentJson(boolean newStatus) {
        showLineEndCommentJson.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentJs() {
        return showLineEndCommentJs.isSelected();
    }

    public void setShowLineEndCommentJs(boolean newStatus) {
        showLineEndCommentJs.setSelected(newStatus);
    }

    public boolean getJsdoc() {
        return jsdoc.isSelected();
    }

    public void setJsdoc(boolean newStatus) {
        jsdoc.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentPy() {
        return showLineEndCommentPy.isSelected();
    }

    public void setShowLineEndCommentPy(boolean newStatus) {
        showLineEndCommentPy.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentGo() {
        return showLineEndCommentGo.isSelected();
    }

    public void setShowLineEndCommentGo(boolean newStatus) {
        showLineEndCommentGo.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentKotlin() {
        return showLineEndCommentKotlin.isSelected();
    }

    public void setShowLineEndCommentKotlin(boolean newStatus) {
        showLineEndCommentKotlin.setSelected(newStatus);
    }

    @NotNull
    public String getLineTags() {
        return lineTags.getText();
    }

    public void setLineTags(@NotNull String newText) {
        lineTags.setText(newText);
    }

    public boolean getGetToSet() {
        return getToSet.isSelected();
    }

    public void setGetToSet(boolean newStatus) {
        getToSet.setSelected(newStatus);
    }

    public boolean getFromNew() {
        return fromNew.isSelected();
    }

    public void setFromNew(boolean newStatus) {
        fromNew.setSelected(newStatus);
    }

    public boolean getFromParam() {
        return fromParam.isSelected();
    }

    public void setFromParam(boolean newStatus) {
        fromParam.setSelected(newStatus);
    }

    public boolean getSkipAnnotation() {
        return skipAnnotation.isSelected();
    }

    public void setSkipAnnotation(boolean newStatus) {
        skipAnnotation.setSelected(newStatus);
    }

    public boolean getSkipAscii() {
        return skipAscii.isSelected();
    }

    public void setSkipAscii(boolean newStatus) {
        skipAscii.setSelected(newStatus);
    }

    public boolean getSkipBlank() {
        return skipBlank.isSelected();
    }

    public void setSkipBlank(boolean newStatus) {
        skipBlank.setSelected(newStatus);
    }

    public Color getLineEndColor() {
        return lineEndColor.getSelectedColor();
    }

    public void setLineEndColor(Color color) {
        lineEndColor.setSelectedColor(color);
    }

    public Color getLineEndJsonColor() {
        return lineEndJsonColor.getSelectedColor();
    }

    public void setLineEndJsonColor(Color color) {
        lineEndJsonColor.setSelectedColor(color);
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
