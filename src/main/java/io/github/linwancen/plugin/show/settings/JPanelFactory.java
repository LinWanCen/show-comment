package io.github.linwancen.plugin.show.settings;

import javax.swing.*;
import java.awt.*;

public class JPanelFactory {

    private JPanelFactory() {}

    public static JPanel of(Component... components) {
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (Component component : components) {
            if (component != null) {
                jPanel.add(component);
            }
        }
        return jPanel;
    }
}
