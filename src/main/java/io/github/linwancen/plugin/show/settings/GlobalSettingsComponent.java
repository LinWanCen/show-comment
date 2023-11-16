package io.github.linwancen.plugin.show.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GlobalSettingsComponent extends AbstractSettingsComponent {

    private final JPanel myMainPanel;
    public GlobalSettingsComponent() {
        @NotNull JButton resetDefault = new JButton(ShowBundle.message("reset.default"));
        resetDefault.addActionListener(e -> GlobalSettingsConfigurable.reset(GlobalSettingsState.DEFAULT_SETTING, this));
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(JPanelFactory.of(resetDefault,
                        new JBLabel(ShowBundle.message("line.count")), lineEndCount
                ), 1)
                .addComponent(commonPanel(), 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    @NotNull
    public JComponent getPreferredFocusedComponent() {
        return myMainPanel;
    }

}
