package io.github.linwancen.plugin.show.settings;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class AbstractSettingsComponent {

    protected final JBTextField lineEndInclude = new JBTextField();
    protected final JBTextField lineEndExclude = new JBTextField();


    @NotNull
    protected JPanel commonLineEndFilter(FormBuilder formBuilder) {
        JPanel lineEndFilter = formBuilder
                .addComponent(new JBLabel("Separated by ',' or ' ' etc."))
                .addComponent(new JBLabel("Use '' to include all or exclude none."))
                .addSeparator()
                .addLabeledComponent(new JBLabel("line end include start with: "), lineEndInclude, 1, true)
                .addLabeledComponent(new JBLabel("line end exclude start with: "), lineEndExclude, 1, true)
                .getPanel();
        lineEndFilter.setBorder(IdeBorderFactory.createTitledBorder(
                "Line End Comment"));
        return lineEndFilter;
    }

    @NotNull
    public String getLineEndInclude() {
        return lineEndInclude.getText();
    }

    public void setLineEndInclude(@NotNull String newText) {
        lineEndInclude.setText(newText);
    }

    @NotNull
    public String getLineEndExclude() {
        return lineEndExclude.getText();
    }

    public void setLineEndExclude(@NotNull String newText) {
        lineEndExclude.setText(newText);
    }
}
