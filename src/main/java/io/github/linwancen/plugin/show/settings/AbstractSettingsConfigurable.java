package io.github.linwancen.plugin.show.settings;

import org.jetbrains.annotations.NotNull;

public class AbstractSettingsConfigurable {

    private AbstractSettingsConfigurable() {}

    static boolean isModified(@NotNull AbstractSettingsState settings, @NotNull AbstractSettingsComponent component,
                              boolean modified) {
        modified |= !component.getLineEndCount().equals(String.valueOf(settings.lineEndCount));
        modified |= !component.getLineInclude().equals(settings.getLineInclude());
        modified |= !component.getLineExclude().equals(settings.getLineExclude());
        modified |= !component.getDocInclude().equals(settings.getDocInclude());
        modified |= !component.getDocExclude().equals(settings.getDocExclude());
        modified |= component.getDocGetEffect() != settings.docGetEffect;
        modified |= !component.getDocGet().equals(settings.getDocGet());
        modified |= component.getProjectEffect() != settings.projectDocEffect;
        modified |= !component.getProjectDoc().equals(settings.getProjectDoc());
        return modified;
    }

    static void apply(@NotNull AbstractSettingsState settings, @NotNull AbstractSettingsComponent component) {
        try {
            settings.lineEndCount = Integer.parseInt(component.getLineEndCount());
        } catch (NumberFormatException e) {
            component.setLineEndCount(String.valueOf(settings.lineEndCount));
        }
        settings.setLineInclude(component.getLineInclude());
        settings.setLineExclude(component.getLineExclude());
        settings.setDocInclude(component.getDocInclude());
        settings.setDocExclude(component.getDocExclude());
        settings.docGetEffect = component.getDocGetEffect();
        settings.setDocGet(component.getDocGet());
        settings.projectDocEffect = component.getProjectEffect();
        settings.setProjectDoc(component.getProjectDoc());
    }

    static void reset(@NotNull AbstractSettingsState settings, @NotNull AbstractSettingsComponent component) {
        component.setLineEndCount(String.valueOf(settings.lineEndCount));
        component.setLineInclude(settings.getLineInclude());
        component.setLineExclude(settings.getLineExclude());
        component.setDocInclude(settings.getDocInclude());
        component.setDocExclude(settings.getDocExclude());
        component.setDocGetEffect(settings.docGetEffect);
        component.setDocGet(settings.getDocGet());
        component.setProjectEffect(settings.projectDocEffect);
        component.setProjectDoc(settings.getProjectDoc());
    }
}
