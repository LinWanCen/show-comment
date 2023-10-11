package io.github.linwancen.plugin.show.settings;

import com.intellij.ui.ColorPanel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AppSettingsComponent {

    private final JPanel myMainPanel;
    private final JBCheckBox showTreeComment = new JBCheckBox(ShowBundle.message("show.tree.comment"));
    private final JBCheckBox compact = new JBCheckBox(ShowBundle.message("compact"));
    private final JBTextField treeTags = new JBTextField();
    private final JBCheckBox showLineEndComment = new JBCheckBox(ShowBundle.message("show.line.end.comment"));
    private final JBCheckBox showLineEndCommentJava = new JBCheckBox("Java ");
    private final JBCheckBox showLineEndCommentJavaBase = new JBCheckBox("// Java ");
    private final JBCheckBox showLineEndCommentKotlin = new JBCheckBox("Kotlin ");
    private final JBCheckBox showLineEndCommentKotlinBase = new JBCheckBox("// Kotlin ");
    private final JBCheckBox showLineEndCommentJs = new JBCheckBox("js ");
    private final JBCheckBox showLineEndCommentJsBase = new JBCheckBox("// js ");
    private final JBCheckBox showLineEndCommentPy = new JBCheckBox("Python ");
    private final JBCheckBox showLineEndCommentPyBase = new JBCheckBox("# Python ");
    private final JBCheckBox showLineEndCommentGo = new JBCheckBox("Go ");
    private final JBCheckBox showLineEndCommentGoBase = new JBCheckBox("// Go ");
    private final JBCheckBox showLineEndCommentSql = new JBCheckBox("sql ");
    private final JBCheckBox showLineEndCommentJson = new JBCheckBox("json ");
    private final JBTextField lineTags = new JBTextField();
    private final JBCheckBox getToSet = new JBCheckBox("get --> set ");
    private final JBCheckBox fromNew = new JBCheckBox("java new ");
    private final JBCheckBox fromParam = new JBCheckBox("java @param ");
    private final JBCheckBox skipAnnotation = new JBCheckBox(ShowBundle.message("skip.anno"));
    private final JBCheckBox skipAscii = new JBCheckBox(ShowBundle.message("skip.english"));
    private final JBCheckBox skipBlank = new JBCheckBox(ShowBundle.message("skip.blank"));
    private final ColorPanel lineEndColor = new ColorPanel();
    private final ColorPanel lineEndJsonColor = new ColorPanel();
    private final JBTextField lineEndPrefix = new JBTextField();
    private final JBTextField lineEndCount = new JBTextField();

    public AppSettingsComponent() {
        @NotNull JButton resetDefault = new JButton(ShowBundle.message("reset.default"));
        resetDefault.addActionListener(e -> AppSettingsConfigurable.reset(AppSettingsState.DEFAULT_SETTING, this));
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(resetDefault, 1)
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
                        showLineEndCommentKotlin,
                        showLineEndCommentJs,
                        showLineEndCommentPy,
                        showLineEndCommentGo
                ), 1)
                .addComponent(JPanelFactory.of(
                        showLineEndCommentSql,
                        showLineEndCommentJson,
                        showLineEndCommentJavaBase,
                        showLineEndCommentKotlinBase,
                        showLineEndCommentJsBase,
                        showLineEndCommentPyBase
                ), 1)
                .addLabeledComponent(new JBLabel(ShowBundle.message("tree.tags")), treeTags, 1, true)
                .addLabeledComponent(new JBLabel(ShowBundle.message("line.tags")), lineTags, 1, true)
                .getPanel();
        comment.setBorder(IdeBorderFactory.createTitledBorder(ShowBundle.message("show")));
        return comment;
    }

    @NotNull
    protected JPanel lineEndFilterPanel() {
        @NotNull JPanel text = JPanelFactory.of(
                new JBLabel(ShowBundle.message("line.count")), lineEndCount,
                new JBLabel(ShowBundle.message("text.color")), lineEndColor,
                new JBLabel(ShowBundle.message("text.color.json")), lineEndJsonColor,
                new JBLabel(ShowBundle.message("prefix")), lineEndPrefix);
        FormBuilder formBuilder = FormBuilder.createFormBuilder()
                .addSeparator()
                .addComponent(JPanelFactory.of(fromNew, fromParam, getToSet, skipAnnotation, skipAscii, skipBlank), 1)
                .addSeparator()
                .addComponent(text)
                .addSeparator();
        return formBuilder.getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    @NotNull
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

    // region line end
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

    public boolean getShowLineEndCommentKotlin() {
        return showLineEndCommentKotlin.isSelected();
    }

    public void setShowLineEndCommentKotlin(boolean newStatus) {
        showLineEndCommentKotlin.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentJs() {
        return showLineEndCommentJs.isSelected();
    }

    public void setShowLineEndCommentJs(boolean newStatus) {
        showLineEndCommentJs.setSelected(newStatus);
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

    public boolean getShowLineEndCommentJavaBase() {
        return showLineEndCommentJavaBase.isSelected();
    }

    public void setShowLineEndCommentJavaBase(boolean newStatus) {
        showLineEndCommentJavaBase.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentKotlinBase() {
        return showLineEndCommentKotlinBase.isSelected();
    }

    public void setShowLineEndCommentKotlinBase(boolean newStatus) {
        showLineEndCommentKotlinBase.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentJsBase() {
        return showLineEndCommentJsBase.isSelected();
    }

    public void setShowLineEndCommentJsBase(boolean newStatus) {
        showLineEndCommentJsBase.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentPyBase() {
        return showLineEndCommentPyBase.isSelected();
    }

    public void setShowLineEndCommentPyBase(boolean newStatus) {
        showLineEndCommentPyBase.setSelected(newStatus);
    }

    public boolean getShowLineEndCommentGoBase() {
        return showLineEndCommentGoBase.isSelected();
    }

    public void setShowLineEndCommentGoBase(boolean newStatus) {
        showLineEndCommentGoBase.setSelected(newStatus);
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

    // endregion line end

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

    @Nullable
    public Color getLineEndColor() {
        return lineEndColor.getSelectedColor();
    }

    public void setLineEndColor(Color color) {
        lineEndColor.setSelectedColor(color);
    }

    @Nullable
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
