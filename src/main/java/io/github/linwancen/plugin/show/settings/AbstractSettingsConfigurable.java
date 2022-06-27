package io.github.linwancen.plugin.show.settings;

public class AbstractSettingsConfigurable {

    private AbstractSettingsConfigurable() {}

    static boolean isModified(AbstractSettingsState settings, AbstractSettingsComponent component, boolean modified) {
        modified |= !component.getLineInclude().equals(settings.getLineInclude());
        modified |= !component.getLineExclude().equals(settings.getLineExclude());
        modified |= !component.getDocInclude().equals(settings.getDocInclude());
        modified |= !component.getDocExclude().equals(settings.getDocExclude());
        modified |= component.getDocGetEffect() != settings.docGetEffect;
        modified |= !component.getDocGet().equals(settings.getDocGet());
        return modified;
    }

    static void apply(AbstractSettingsState settings, AbstractSettingsComponent component) {
        settings.setLineInclude(component.getLineInclude());
        settings.setLineExclude(component.getLineExclude());
        settings.setDocInclude(component.getDocInclude());
        settings.setDocExclude(component.getDocExclude());
        settings.docGetEffect = component.getDocGetEffect();
        settings.setDocGet(component.getDocGet());
    }

    static void reset(AbstractSettingsState settings, AbstractSettingsComponent component) {
        component.setLineInclude(settings.getLineInclude());
        component.setLineExclude(settings.getLineExclude());
        component.setDocInclude(settings.getDocInclude());
        component.setDocExclude(settings.getDocExclude());
        component.setDocGetEffect(settings.docGetEffect);
        component.setDocGet(settings.getDocGet());
    }
}
