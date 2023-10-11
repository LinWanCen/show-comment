package io.github.linwancen.plugin.show.settings;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;

public abstract class AbstractSettingsComponent {

    private final JBTextField lineInclude = new JBTextField();
    private final JBTextField lineExclude = new JBTextField();

    private final JBTextField docInclude = new JBTextField();
    private final JBTextField docExclude = new JBTextField();

    private final JBCheckBox docGetEffect = new JBCheckBox("");
    private final JBTextField docGet = new JBTextField();
    private final JBCheckBox projectDocEffect = new JBCheckBox("");
    private final JBTextArea projectDoc = new JBTextArea();

    @NotNull
    protected JPanel commonLineEndFilter(FormBuilder formBuilder) {
        formBuilder = formBuilder
                .addComponent(new JBLabel(ShowBundle.message("regexp.tip")))
                .addSeparator()
                .addLabeledComponent(new JBLabel(ShowBundle.message("sign.include.regexp")), lineInclude, 1, true)
                .addLabeledComponent(new JBLabel(ShowBundle.message("sign.exclude.regexp")), lineExclude, 1, true)
                .addSeparator()
                .addLabeledComponent(new JBLabel(ShowBundle.message("comment.include.regexp")), docInclude, 1, true)
                .addLabeledComponent(new JBLabel(ShowBundle.message("comment.exclude.regexp")), docExclude, 1, true)
                .addSeparator();
        formBuilder = add(formBuilder, docGetEffect, docGet, ShowBundle.message("get.doc.regexp"));
        formBuilder = add(formBuilder, projectDocEffect, projectDoc, ShowBundle.message("project.doc.regexp"));
        JPanel lineEndFilter = formBuilder.getPanel();
        lineEndFilter.setBorder(IdeBorderFactory.createTitledBorder(ShowBundle.message("line.end.comment")));
        return lineEndFilter;
    }

    protected FormBuilder add(@NotNull FormBuilder formBuilder, JBCheckBox jbCheckBox,
                              @NotNull JTextComponent jTextComponent, @NotNull String tip) {
        return formBuilder.addLabeledComponent(JPanelFactory.of(jbCheckBox, new JBLabel(tip)), jTextComponent, 1, true);
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


    public boolean getProjectEffect() {
        return projectDocEffect.isSelected();
    }

    public void setProjectEffect(boolean newStatus) {
        projectDocEffect.setSelected(newStatus);
    }

    @NotNull
    public String getProjectDoc() {
        return projectDoc.getText();
    }

    public void setProjectDoc(@NotNull String newText) {
        projectDoc.setText(newText);
    }
}
