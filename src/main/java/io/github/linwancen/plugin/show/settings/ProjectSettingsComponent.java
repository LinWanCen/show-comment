package io.github.linwancen.plugin.show.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProjectSettingsComponent extends AbstractSettingsComponent {

    private final JPanel myMainPanel;
    private final JBCheckBox globalFilterEffective = new JBCheckBox(ShowBundle.message("global.settings.effective"));
    private final JBCheckBox projectFilterEffective = new JBCheckBox(ShowBundle.message("project.settings.effective"));

    public ProjectSettingsComponent() {
        @NotNull JButton resetDefault = new JButton(ShowBundle.message("reset.default"));
        resetDefault.addActionListener(e -> ProjectSettingsConfigurable.reset(ProjectSettingsState.DEFAULT_SETTING, this));
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(JPanelFactory.of(resetDefault, globalFilterEffective, projectFilterEffective), 1)
                .addComponent(lineEndFilterPanel(), 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @NotNull
    protected JPanel lineEndFilterPanel() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder()
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
