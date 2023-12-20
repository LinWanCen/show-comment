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
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.ext.TreeExt;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.tree.RelFileDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class Tree implements ProjectViewNodeDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(Tree.class);

    @Override
    public void decorate(@NotNull ProjectViewNode node, @NotNull PresentationData data) {
        try {
            decorateImpl(node, data);
        } catch (Throwable e) {
            LOG.info("Tree catch Throwable but log to record.", e);
        }
    }

    private void decorateImpl(@NotNull ProjectViewNode<?> node, @NotNull PresentationData data) {
        if (!AppSettingsState.getInstance().showTreeComment) {
            return;
        }
        @Nullable Project project = node.getProject();
        if (project == null) {
            return;
        }
        if (DumbService.isDumb(project)) {
            return;
        }
        DumbService.getInstance(project).runReadActionInSmartMode(() ->
                ApplicationManager.getApplication().runReadAction(() -> {
                    @Nullable String doc = treeDoc(node, project);
                    addText(data, doc);
                }));
    }

    static void addText(@NotNull PresentationData data, @Nullable String text) {
        if (text == null) {
            return;
        }
        @NotNull List<ColoredFragment> coloredText = data.getColoredText();
        if (coloredText.isEmpty()) {
            data.addText(data.getPresentableText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        data.addText(" " + text, SimpleTextAttributes.GRAY_ATTRIBUTES);
    }

    @Nullable
    private String treeDoc(@NotNull ProjectViewNode<?> node, @NotNull Project project) {
        @Nullable String doc = TreeExt.doc(node);
        if (doc != null) {
            return doc;
        }
        @NotNull SettingsInfo info = SettingsInfo.of(project, FuncEnum.TREE);
        @Nullable String relFileDoc = RelFileDoc.relFileDoc(node, info);
        if (relFileDoc != null) {
            return relFileDoc;
        }
        Object value = node.getValue();
        if (value instanceof PsiElement) {
            @NotNull PsiElement psiElement = (PsiElement) value;
            @Nullable String docPrint = BaseLangDoc.resolveDoc(info, psiElement);
            if (docPrint != null) {
                return docPrint;
            }
        }
        @NotNull Collection<BaseLangDoc> langDocs = BaseLangDoc.LANG_DOC_MAP.values();
        for (@NotNull BaseLangDoc langDoc : langDocs) {
            @Nullable String s = langDoc.treeDoc(info, node, project);
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
