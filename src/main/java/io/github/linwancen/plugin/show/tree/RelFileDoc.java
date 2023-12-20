package io.github.linwancen.plugin.show.tree;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.settings.AbstractSettingsState;
import io.github.linwancen.plugin.show.settings.GlobalSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RelFileDoc {
    private RelFileDoc() {}

    @Nullable
    public static String relFileDoc(ProjectViewNode<?> node, @NotNull SettingsInfo info) {
        @NotNull ProjectSettingsState projectSettings = info.projectSettings;
        @NotNull GlobalSettingsState globalSettings = info.globalSettings;
        if (projectSettings.projectFilterEffective) {
            @Nullable String doc = relDoc(node, projectSettings);
            if (StringUtils.isNotBlank(doc)) {
                return doc;
            }
        }
        if (projectSettings.globalFilterEffective) {
            return relDoc(node, globalSettings);
        }
        return null;
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
