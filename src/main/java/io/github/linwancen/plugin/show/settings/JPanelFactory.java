package io.github.linwancen.plugin.show.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class JPanelFactory {

    private JPanelFactory() {}

    @NotNull
    public static JPanel of(@NotNull Component... components) {
        @NotNull JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (@Nullable Component component : components) {
            if (component != null) {
                jPanel.add(component);
            }
        }
        return jPanel;
    }
}
