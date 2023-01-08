package io.github.linwancen.plugin.show.settings;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class AbstractSettingsComponent {

    private final JBTextField lineInclude = new JBTextField();
    private final JBTextField lineExclude = new JBTextField();

    private final JBTextField docInclude = new JBTextField();
    private final JBTextField docExclude = new JBTextField();

    private final JBCheckBox docGetEffect = new JBCheckBox("");
    private final JBTextField docGet = new JBTextField();

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
        formBuilder = add(formBuilder, docGetEffect, this.docGet, ShowBundle.message("get.doc.regexp"));
        JPanel lineEndFilter = formBuilder.getPanel();
        lineEndFilter.setBorder(IdeBorderFactory.createTitledBorder(ShowBundle.message("line.end.comment")));
        return lineEndFilter;
    }

    protected FormBuilder add(@NotNull FormBuilder formBuilder, JBCheckBox jbCheckBox,
                              @NotNull JBTextField jbTextField, @NotNull String tip) {
        return formBuilder.addLabeledComponent(JPanelFactory.of(jbCheckBox, new JBLabel(tip)), jbTextField, 1, true);
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
}
