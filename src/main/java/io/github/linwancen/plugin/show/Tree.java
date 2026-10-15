package io.github.linwancen.plugin.show;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.linwancen.plugin.show.cache.RenderDocCache;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.tree.TreeDoc;
import io.github.linwancen.plugin.show.tree.TreeElements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Tree implements ProjectViewNodeDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(Tree.class);

    /**
     * How long to wait before really refreshing, so a burst of docs finishing at the same time
     * (the whole tree computes at once after a project view rebuild) becomes one refresh.
     */
    private static final long REFRESH_DELAY_MS = 200L;

    /**
     * true = a refresh is already queued: one refresh repaints the whole view, so further requests
     * of the same burst can be dropped. One entry per project, removed before the refresh runs.
     */
    private static final Set<Project> REFRESH_QUEUED = ConcurrentHashMap.newKeySet();

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

    /**
     * Called on EDT after a doc was computed (see {@link RenderDocCache#doc}): a project view
     * node keeps its old presentation once painted, so the view must be refreshed to show the doc.
     * <p>
     * Refreshes are delayed and coalesced: after a project view rebuild the whole tree computes at
     * once, and hundreds of immediate refreshes would rebuild the tree hundreds of times.
     */
    private static void refreshProjectView(@NotNull Project project) {
        if (!REFRESH_QUEUED.add(project)) {
            // a refresh of this project is already queued: it repaints the whole view anyway
            return;
        }
        AppExecutorUtil.getAppScheduledExecutorService().schedule(() ->
                ApplicationManager.getApplication().invokeLater(() -> {
                    REFRESH_QUEUED.remove(project);
                    if (!project.isDisposed()) {
                        ProjectView.getInstance(project).refresh();
                    }
                }), REFRESH_DELAY_MS, TimeUnit.MILLISECONDS);
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
