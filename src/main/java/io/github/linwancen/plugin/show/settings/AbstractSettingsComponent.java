package io.github.linwancen.plugin.show.settings;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class AbstractSettingsComponent {

    protected final JBTextField lineInclude = new JBTextField();
    protected final JBTextField lineExclude = new JBTextField();

    public final JBTextField docInclude = new JBTextField();
    public final JBTextField docExclude = new JBTextField();

    @NotNull
    protected JPanel commonLineEndFilter(FormBuilder formBuilder) {
        JPanel lineEndFilter = formBuilder
                .addComponent(new JBLabel("Separated by '|' (Regexp), use '' to include all or exclude none."))
                .addSeparator()
                .addLabeledComponent(new JBLabel("className#memberName include Regexp: "), lineInclude, 1, true)
                .addLabeledComponent(new JBLabel("className#memberName exclude Regexp: "), lineExclude, 1, true)
                .addSeparator()
                .addLabeledComponent(new JBLabel("comment include Regexp: "), docInclude, 1, true)
                .addLabeledComponent(new JBLabel("comment exclude Regexp: "), docExclude, 1, true)
                .getPanel();
        lineEndFilter.setBorder(IdeBorderFactory.createTitledBorder(
                "Line End Comment"));
        return lineEndFilter;
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
}
