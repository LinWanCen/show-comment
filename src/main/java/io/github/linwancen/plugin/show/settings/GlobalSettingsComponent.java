package io.github.linwancen.plugin.show.settings;

import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GlobalSettingsComponent extends AbstractSettingsComponent {

    private final JPanel myMainPanel;
    public GlobalSettingsComponent() {
        @NotNull JButton resetDefault = new JButton(ShowBundle.message("reset.default"));
        resetDefault.addActionListener(e -> GlobalSettingsConfigurable.reset(GlobalSettingsState.DEFAULT_SETTING, this));
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(JPanelFactory.of(resetDefault), 1)
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
        return myMainPanel;
    }

}
