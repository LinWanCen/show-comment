package io.github.linwancen.plugin.show.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProjectSettingsComponent extends AbstractSettingsComponent {

    private final JPanel myMainPanel;
    private final JBCheckBox globalFilterEffective = new JBCheckBox(ShowBundle.message("global.include.exclude.effective"));
    private final JBCheckBox projectFilterEffective = new JBCheckBox(ShowBundle.message("project.include.exclude.effective"));

    public ProjectSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(lineEndFilterPanel(), 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @NotNull
    protected JPanel lineEndFilterPanel() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder()
                .addComponent(globalFilterEffective)
                .addComponent(projectFilterEffective)
                .addSeparator();
        return commonLineEndFilter(formBuilder);
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    @NotNull
    public JComponent getPreferredFocusedComponent() {
        return projectFilterEffective;
    }


    public boolean getGlobalFilterEffective() {
        return globalFilterEffective.isSelected();
    }

    public void setGlobalFilterEffective(boolean newStatus) {
        globalFilterEffective.setSelected(newStatus);
    }

    public boolean getProjectFilterEffective() {
        return projectFilterEffective.isSelected();
    }

    public void setProjectFilterEffective(boolean newStatus) {
        projectFilterEffective.setSelected(newStatus);
    }
}
