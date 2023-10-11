package io.github.linwancen.plugin.show.tree;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.lang.FileASTNode;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.settings.GlobalSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectDoc {

    public static String projectDoc(ProjectViewNode<?> node, @NotNull SettingsInfo settingsInfo) {
        if (!(node instanceof PsiDirectoryNode)) {
            return null;
        }
        @NotNull PsiDirectoryNode psiDirectoryNode = (PsiDirectoryNode) node;
        @Nullable PsiDirectory psiDirectory = psiDirectoryNode.getValue();
        if (psiDirectory == null) {
            return null;
        }
        @NotNull ProjectSettingsState projectSettings = settingsInfo.projectSettings;
        @NotNull GlobalSettingsState globalSettings = settingsInfo.globalSettings;
        if (projectSettings.projectFilterEffective && projectSettings.projectDocEffect) {
            return projectDirDoc(psiDirectory, projectSettings.projectDoc);
        } else if (projectSettings.globalFilterEffective && globalSettings.projectDocEffect) {
            return projectDirDoc(psiDirectory, globalSettings.projectDoc);
        } else {
            return null;
        }
    }

    @Nullable
    private static String projectDirDoc(@NotNull PsiDirectory psiDirectory, @NotNull Pattern[][] projectDoc) {
        for (@NotNull Pattern[] patterns : projectDoc) {
            @Nullable String patternsDoc = patternsDoc(psiDirectory, patterns);
            if (patternsDoc != null) {
                return patternsDoc;
            }
        }
        return null;
    }

    @Nullable
    private static String patternsDoc(@NotNull PsiDirectory psiDirectory, @NotNull Pattern[] patterns) {
        if (patterns.length < 2) {
            return null;
        }
        Pattern filePattern = patterns[0];
        String fileName = filePattern.pattern();
        @Nullable PsiFile file = psiDirectory.findFile(fileName);
        if (file == null) {
            return null;
        }
        @Nullable FileASTNode fileNode = file.getNode();
        if (fileNode == null) {
            return null;
        }
        @NotNull String text = fileNode.getText();
        for (int i = 1; i < patterns.length; i++) {
            @NotNull Matcher matcher = patterns[i].matcher(text);
            if (matcher.find()) {
                String group = matcher.group(1);
                if (group != null) {
                    group = group.trim();
                    if (!group.isEmpty()) {
                        return group;
                    }
                }
            }
        }
        return null;
    }
}
