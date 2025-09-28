package io.github.linwancen.plugin.show.settings;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;

public abstract class AbstractSettingsComponent {

    protected final JBTextField lineEndCount = new JBTextField();

    private final JBCheckBox onlySelectLine = new JBCheckBox(ShowBundle.message("only.select.line"));

    private final JBTextField lineInclude = new JBTextField();
    private final JBTextField lineExclude = new JBTextField();

    private final JBTextField docInclude = new JBTextField();
    private final JBTextField docExclude = new JBTextField();

    private final JBTextField tagInclude = new JBTextField();
    private final JBTextField tagExclude = new JBTextField();

    private final JBTextField attrInclude = new JBTextField();
    private final JBTextField attrExclude = new JBTextField();

    private final JBCheckBox docGetEffect = new JBCheckBox("");
    private final JBTextField docGet = new JBTextField();
    private final JBCheckBox annoDocEffect = new JBCheckBox("");
    private final JBTextArea annoDoc = new JBTextArea();

    // region PatternMap: dirDoc fileDoc sqlSplit tableDoc columnDoc indexDoc

    private final JBCheckBox dirDocEffect = new JBCheckBox("");
    private final JBTextArea dirDoc = new JBTextArea();
    private final JBCheckBox fileDocEffect = new JBCheckBox("");
    private final JBTextArea fileDoc = new JBTextArea();

    protected final JBCheckBox sqlSplitEffect = new JBCheckBox("");
    private final JBTextArea sqlSplit = new JBTextArea();
    private final JBCheckBox tableDocEffect = new JBCheckBox("");
    private final JBTextArea tableDoc = new JBTextArea();
    private final JBCheckBox columnDocEffect = new JBCheckBox("");
    private final JBTextArea columnDoc = new JBTextArea();
    private final JBCheckBox indexDocEffect = new JBCheckBox("");
    private final JBTextArea indexDoc = new JBTextArea();

    // endregion

    @NotNull
    protected JPanel commonPanel() {
        return FormBuilder.createFormBuilder()
                .addComponent(lineEndPanel(), 1)
                .addComponent(webPanel(), 1)
                .addComponent(treePanel(), 1)
                .getPanel();
    }

    @NotNull
    protected JPanel lineEndPanel() {
        FormBuilder builder = FormBuilder.createFormBuilder()
                // .addComponent(onlySelectLine)
                // .addSeparator()
                .addComponent(new JBLabel(ShowBundle.message("regexp.tip")))
                .addSeparator()
                .addLabeledComponent(new JBLabel(ShowBundle.message("sign.include.regexp")), lineInclude, 1, true)
                .addLabeledComponent(new JBLabel(ShowBundle.message("sign.exclude.regexp")), lineExclude, 1, true)
                .addSeparator()
                .addLabeledComponent(new JBLabel(ShowBundle.message("comment.include.regexp")), docInclude, 1, true)
                .addLabeledComponent(new JBLabel(ShowBundle.message("comment.exclude.regexp")), docExclude, 1, true)
                .addSeparator();
        Border border = docGet.getBorder();
        annoDoc.setBorder(border);
        dirDoc.setBorder(border);
        fileDoc.setBorder(border);
        @NotNull JPanel getLabel = JPanelFactory.of(docGetEffect, new JBLabel(ShowBundle.message("get.doc.regexp")));
        builder = builder.addLabeledComponent(getLabel, docGet, 1, true);
        @NotNull JPanel annoLabel = JPanelFactory.of(annoDocEffect, new JBLabel(ShowBundle.message("anno.doc")));
        builder = builder.addLabeledComponent(annoLabel, annoDoc, 1, true);
        JPanel panel = builder.getPanel();
        panel.setBorder(IdeBorderFactory.createTitledBorder(ShowBundle.message("line.end.comment")));
        return panel;
    }

    @NotNull
    private JPanel webPanel() {
        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(ShowBundle.message("tag.include.regexp")), tagInclude, 1, true)
                .addLabeledComponent(new JBLabel(ShowBundle.message("tag.exclude.regexp")), tagExclude, 1, true)
                .addLabeledComponent(new JBLabel(ShowBundle.message("attr.include.regexp")), attrInclude, 1, true)
                .addLabeledComponent(new JBLabel(ShowBundle.message("attr.exclude.regexp")), attrExclude, 1, true)
                .getPanel();
        panel.setBorder(IdeBorderFactory.createTitledBorder(ShowBundle.message("web.comment")));
        return panel;
    }

    @NotNull
    private JPanel treePanel() {
        @NotNull JPanel dirLabel = JPanelFactory.of(dirDocEffect, new JBLabel(ShowBundle.message("dir.doc.regexp")));
        @NotNull JPanel fileLabel = JPanelFactory.of(fileDocEffect, new JBLabel(ShowBundle.message("file.doc.regexp")));
        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(dirLabel, dirDoc, 1, true)
                .addLabeledComponent(fileLabel, fileDoc, 1, true)
                .getPanel();
        panel.setBorder(IdeBorderFactory.createTitledBorder(ShowBundle.message("tree.comment")));
        return panel;
    }

    @SuppressWarnings("unused")
    @NotNull
    private JPanel sqlPanel() {
        // sqlSplit tableDoc columnDoc indexDoc
        @NotNull JPanel sqlLabel = JPanelFactory.of(sqlSplitEffect, new JBLabel(ShowBundle.message("sql.split.regexp")));
        @NotNull JPanel tableLabel = JPanelFactory.of(tableDocEffect, new JBLabel(ShowBundle.message("table.doc.regexp")));
        @NotNull JPanel columnLabel = JPanelFactory.of(columnDocEffect, new JBLabel(ShowBundle.message("column.doc.regexp")));
        @NotNull JPanel indexLabel = JPanelFactory.of(indexDocEffect, new JBLabel(ShowBundle.message("index.doc.regexp")));
        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(sqlLabel, sqlSplit, 1, true)
                .addLabeledComponent(tableLabel, tableDoc, 1, true)
                .addLabeledComponent(columnLabel, columnDoc, 1, true)
                .addLabeledComponent(indexLabel, indexDoc, 1, true)
                .getPanel();
        panel.setBorder(IdeBorderFactory.createTitledBorder(ShowBundle.message("sql.comment")));
        return panel;
    }

    @NotNull
    public String getLineEndCount() {
        return lineEndCount.getText();
    }

    public void setLineEndCount(@NotNull String newText) {
        lineEndCount.setText(newText);
    }

    public boolean getOnlySelectLine() {
        return onlySelectLine.isSelected();
    }

    public void setOnlySelectLine(boolean newStatus) {
        onlySelectLine.setSelected(newStatus);
    }

    @NotNull
    public String getLineInclude() {
        return lineInclude.getText();
    }

    public void setLineInclude(@NotNull String newText) {
        lineInclude.setText(newText);
    }

    @NotNull
    public String getLineExclude() {
        return lineExclude.getText();
    }

    public void setLineExclude(@NotNull String newText) {
        lineExclude.setText(newText);
    }


    @NotNull
    public String getDocInclude() {
        return docInclude.getText();
    }

    public void setDocInclude(@NotNull String newText) {
        docInclude.setText(newText);
    }

    @NotNull
    public String getDocExclude() {
        return docExclude.getText();
    }

    public void setDocExclude(@NotNull String newText) {
        docExclude.setText(newText);
    }

    @NotNull
    public String getTagInclude() {
        return tagInclude.getText();
    }

    public void setTagInclude(@NotNull String newText) {
        tagInclude.setText(newText);
    }

    @NotNull
    public String getTagExclude() {
        return tagExclude.getText();
    }

    public void setTagExclude(@NotNull String newText) {
        tagExclude.setText(newText);
    }

    @NotNull
    public String getAttrInclude() {
        return attrInclude.getText();
    }

    public void setAttrInclude(@NotNull String newText) {
        attrInclude.setText(newText);
    }

    @NotNull
    public String getAttrExclude() {
        return attrExclude.getText();
    }

    public void setAttrExclude(@NotNull String newText) {
        attrExclude.setText(newText);
    }


    public boolean getDocGetEffect() {
        return docGetEffect.isSelected();
    }

    public void setDocGetEffect(boolean newStatus) {
        docGetEffect.setSelected(newStatus);
    }

    @NotNull
    public String getDocGet() {
        return docGet.getText();
    }

    public void setDocGet(@NotNull String newText) {
        docGet.setText(newText);
    }


    public boolean getAnnoDocEffect() {
        return annoDocEffect.isSelected();
    }

    public void setAnnoDocEffect(boolean newStatus) {
        annoDocEffect.setSelected(newStatus);
    }

    @NotNull
    public String getAnnoDoc() {
        return annoDoc.getText();
    }

    public void setAnnoDoc(@NotNull String newText) {
        annoDoc.setText(newText);
    }

    // region PatternMap

    public boolean getDirDocEffect() {
        return dirDocEffect.isSelected();
    }

    public void setDirDocEffect(boolean newStatus) {
        dirDocEffect.setSelected(newStatus);
    }

    @NotNull
    public String getDirDoc() {
        return dirDoc.getText();
    }

    public void setDirDoc(@NotNull String newText) {
        dirDoc.setText(newText);
    }


    public boolean getFileDocEffect() {
        return fileDocEffect.isSelected();
    }

    public void setFileDocEffect(boolean newStatus) {
        fileDocEffect.setSelected(newStatus);
    }

    @NotNull
    public String getFileDoc() {
        return fileDoc.getText();
    }

    public void setFileDoc(@NotNull String newText) {
        fileDoc.setText(newText);
    }

    public boolean getSqlSplitEffect() {
        return sqlSplitEffect.isSelected();
    }

    public void setSqlSplitEffect(boolean newStatus) {
        sqlSplitEffect.setSelected(newStatus);
    }

    @NotNull
    public String getSqlSplit() {
        return sqlSplit.getText();
    }

    public void setSqlSplit(@NotNull String newText) {
        sqlSplit.setText(newText);
    }

    public boolean getTableDocEffect() {
        return tableDocEffect.isSelected();
    }

    public void setTableDocEffect(boolean newStatus) {
        tableDocEffect.setSelected(newStatus);
    }

    @NotNull
    public String getTableDoc() {
        return tableDoc.getText();
    }

    public void setTableDoc(@NotNull String newText) {
        tableDoc.setText(newText);
    }

    public boolean getColumnDocEffect() {
        return columnDocEffect.isSelected();
    }

    public void setColumnDocEffect(boolean newStatus) {
        columnDocEffect.setSelected(newStatus);
    }

    @NotNull
    public String getColumnDoc() {
        return columnDoc.getText();
    }

    public void setColumnDoc(@NotNull String newText) {
        columnDoc.setText(newText);
    }

    public boolean getIndexDocEffect() {
        return indexDocEffect.isSelected();
    }

    public void setIndexDocEffect(boolean newStatus) {
        indexDocEffect.setSelected(newStatus);
    }

    @NotNull
    public String getIndexDoc() {
        return indexDoc.getText();
    }

    public void setIndexDoc(@NotNull String newText) {
        indexDoc.setText(newText);
    }

    // endregion PatternMap for AI: dirDoc fileDoc sqlSplit tableDoc columnDoc indexDoc
}
