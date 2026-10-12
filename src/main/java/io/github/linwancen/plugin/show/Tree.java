package io.github.linwancen.plugin.show;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import io.github.linwancen.plugin.show.cache.RenderDocCache;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.tree.TreeDoc;
import io.github.linwancen.plugin.show.tree.TreeElements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Tree implements ProjectViewNodeDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(Tree.class);

    @Override
    public void decorate(@NotNull ProjectViewNode node, @NotNull PresentationData data) {
        try {
            decorateImpl(node, data);
        } catch (ProcessCanceledException ignore) {
            // ignore
        } catch (Throwable e) {
            LOG.info("Tree catch Throwable but log to record.", e);
        }
    }

    private void decorateImpl(@NotNull ProjectViewNode<?> node, @NotNull PresentationData data) {
        @NotNull AppSettingsState state = AppSettingsState.getInstance();
        if (!state.showTreeComment) {
            return;
        }
        @Nullable Project project = node.getProject();
        if (project == null) {
            return;
        }
        @Nullable String doc = RenderDocCache.doc(project, node,
                () -> treeDoc(node, project), () -> refreshProjectView(project));
        addText(data, doc);
    }

    private static void refreshProjectView(@NotNull Project project) {
        ProjectView.getInstance(project).refresh();
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

    /**
     * Project View entry: node -> representative element -> shared tree doc.
     * Also used by TreeCopy. All precedence (ext / pattern / resolve / treeDoc / first)
     * lives in {@link TreeDoc#treeElementDoc}.
     */
    @Nullable
    public static String treeDoc(@NotNull ProjectViewNode<?> node, @NotNull Project project) {
        @Nullable PsiElement element = TreeElements.of(node, project);
        return TreeDoc.treeElementDoc(project, element);
    }

    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {
        // not need
    }
}
