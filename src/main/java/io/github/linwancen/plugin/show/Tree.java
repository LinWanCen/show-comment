package io.github.linwancen.plugin.show;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import io.github.linwancen.plugin.show.bean.FuncEnum;
import io.github.linwancen.plugin.show.ext.TreeExt;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class Tree implements ProjectViewNodeDecorator {

    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {
        if (!AppSettingsState.getInstance().showTreeComment) {
            return;
        }
        Project project = node.getProject();
        if (project == null) {
            return;
        }
        if (DumbService.isDumb(project)) {
            return;
        }
        ApplicationManager.getApplication().runReadAction(() -> {
            String doc = treeDoc(node, project);
            if (doc == null) {
                return;
            }
            List<ColoredFragment> coloredText = data.getColoredText();
            if (coloredText.isEmpty()) {
                data.addText(data.getPresentableText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
            data.addText(" " + doc, SimpleTextAttributes.GRAY_ATTRIBUTES);
        });
    }

    @Nullable
    private String treeDoc(ProjectViewNode<?> node, @NotNull Project project) {
        String doc = TreeExt.doc(node);
        if (doc != null) {
            return doc;
        }
        SettingsInfo settingsInfo = SettingsInfo.of(project, FuncEnum.TREE);
        Object value = node.getValue();
        if (value instanceof PsiElement) {
            PsiElement psiElement = (PsiElement) value;
            String docPrint = BaseLangDoc.resolveDoc(settingsInfo, psiElement);
            if (docPrint != null) {
                return docPrint;
            }
        }
        Collection<BaseLangDoc> langDocs = BaseLangDoc.LANG_DOC_MAP.values();
        for (BaseLangDoc langDoc : langDocs) {
            String s = langDoc.treeDoc(settingsInfo, node, project);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {
        // not need
    }
}
