package io.github.linwancen.plugin.show.settings;

import org.jetbrains.annotations.NotNull;

public class AbstractSettingsConfigurable {

    private AbstractSettingsConfigurable() {}

    static boolean isModified(@NotNull AbstractSettingsState settings, @NotNull AbstractSettingsComponent component,
                              boolean modified) {
        modified |= !component.getLineEndCount().equals(String.valueOf(settings.lineEndCount));
        modified |= component.getOnlySelectLine() != settings.onlySelectLine;
        modified |= !component.getLineInclude().equals(settings.getLineInclude());
        modified |= !component.getLineExclude().equals(settings.getLineExclude());
        modified |= !component.getDocInclude().equals(settings.getDocInclude());
        modified |= !component.getDocExclude().equals(settings.getDocExclude());
        modified |= !component.getTagInclude().equals(settings.getTagInclude());
        modified |= !component.getTagExclude().equals(settings.getTagExclude());
        modified |= !component.getAttrInclude().equals(settings.getAttrInclude());
        modified |= !component.getAttrExclude().equals(settings.getAttrExclude());
        modified |= component.getDocGetEffect() != settings.docGetEffect;
        modified |= !component.getDocGet().equals(settings.getDocGet());
        modified |= component.getAnnoDocEffect() != settings.annoDocEffect;
        modified |= !component.getAnnoDoc().equals(settings.getAnnoDoc());
        modified |= component.getDirDocEffect() != settings.dirDocEffect;
        modified |= !component.getDirDoc().equals(settings.getDirDoc());
        modified |= component.getFileDocEffect() != settings.fileDocEffect;
        modified |= !component.getFileDoc().equals(settings.getFileDoc());
        return modified;
    }

    static void apply(@NotNull AbstractSettingsState settings, @NotNull AbstractSettingsComponent component) {
        try {
            settings.lineEndCount = Integer.parseInt(component.getLineEndCount());
        } catch (NumberFormatException e) {
            component.setLineEndCount(String.valueOf(settings.lineEndCount));
        }
        settings.onlySelectLine = component.getOnlySelectLine();
        settings.setLineInclude(component.getLineInclude());
        settings.setLineExclude(component.getLineExclude());
        settings.setDocInclude(component.getDocInclude());
        settings.setDocExclude(component.getDocExclude());
        settings.setTagInclude(component.getTagInclude());
        settings.setTagExclude(component.getTagExclude());
        settings.setAttrInclude(component.getAttrInclude());
        settings.setAttrExclude(component.getAttrExclude());
        settings.docGetEffect = component.getDocGetEffect();
        settings.setDocGet(component.getDocGet());
        settings.annoDocEffect = component.getAnnoDocEffect();
        settings.setAnnoDoc(component.getAnnoDoc());
        settings.dirDocEffect = component.getDirDocEffect();
        settings.setDirDoc(component.getDirDoc());
        settings.fileDocEffect = component.getFileDocEffect();
        settings.setFileDoc(component.getFileDoc());
    }

    static void reset(@NotNull AbstractSettingsState settings, @NotNull AbstractSettingsComponent component) {
        component.setLineEndCount(String.valueOf(settings.lineEndCount));
        component.setOnlySelectLine(settings.onlySelectLine);
        component.setLineInclude(settings.getLineInclude());
        component.setLineExclude(settings.getLineExclude());
        component.setDocInclude(settings.getDocInclude());
        component.setDocExclude(settings.getDocExclude());
        component.setTagInclude(settings.getTagInclude());
        component.setTagExclude(settings.getTagExclude());
        component.setAttrInclude(settings.getAttrInclude());
        component.setAttrExclude(settings.getAttrExclude());
        component.setDocGetEffect(settings.docGetEffect);
        component.setDocGet(settings.getDocGet());
        component.setAnnoDocEffect(settings.annoDocEffect);
        component.setAnnoDoc(settings.getAnnoDoc());
        component.setDirDocEffect(settings.dirDocEffect);
        component.setDirDoc(settings.getDirDoc());
        component.setFileDocEffect(settings.fileDocEffect);
        component.setFileDoc(settings.getFileDoc());
    }
}
