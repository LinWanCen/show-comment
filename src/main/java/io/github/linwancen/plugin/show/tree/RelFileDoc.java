package io.github.linwancen.plugin.show.tree;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.settings.AbstractSettingsState;
import io.github.linwancen.plugin.show.settings.GlobalSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RelFileDoc {

    @Nullable
    public static String relFileDoc(ProjectViewNode<?> node, @NotNull SettingsInfo settingsInfo) {
        @NotNull ProjectSettingsState projectSettings = settingsInfo.projectSettings;
        @NotNull GlobalSettingsState globalSettings = settingsInfo.globalSettings;
        if (projectSettings.projectFilterEffective) {
            return relDoc(node, projectSettings);
        } else if (projectSettings.globalFilterEffective) {
            return relDoc(node, globalSettings);
        } else {
            return null;
        }
    }

    @Nullable
    private static String relDoc(ProjectViewNode<?> node, @NotNull AbstractSettingsState settings) {
        if (node instanceof PsiFileNode) {
            return FileDoc.fileDoc((PsiFileNode) node, settings);
        }
        if (node instanceof PsiDirectoryNode) {
            return DirDoc.dirDoc((PsiDirectoryNode) node, settings);
        }
        return null;
    }
}
