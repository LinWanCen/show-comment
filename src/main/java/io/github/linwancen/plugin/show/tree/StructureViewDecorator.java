package io.github.linwancen.plugin.show.tree;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import io.github.linwancen.plugin.show.bean.FuncEnum;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.cache.RenderDocCache;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Decorates the Structure View tree with doc comments.
 * Listens for tool window state changes and installs a custom cell renderer
 * that appends resolved doc comments to structure view tree nodes.
 */
public class StructureViewDecorator implements ToolWindowManagerListener {

    private static final Logger LOG = LoggerFactory.getLogger(StructureViewDecorator.class);

    protected final Project project;

    public StructureViewDecorator(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
        if (project.isDisposed()) {
            return;
        }
        @NotNull AppSettingsState state = AppSettingsState.getInstance();
        if (disable(state)) {
            return;
        }
        decorateStructureView();
    }

    protected boolean disable(@NotNull AppSettingsState state) {
        return !state.showStructureComment;
    }

    private void decorateStructureView() {
        @NotNull ToolWindowManager twm = ToolWindowManager.getInstance(project);
        @Nullable ToolWindow tw = twm.getToolWindow(toolWindowId());
        if (tw == null || !tw.isVisible()) {
            return;
        }
        JComponent component = tw.getComponent();
        decorateTreesInComponent(component);
    }

    @NotNull
    protected String toolWindowId() {
        return "Structure";
    }

    private void decorateTreesInComponent(@NotNull Component component) {
        if (component instanceof JTree) {
            @NotNull JTree tree = (JTree) component;
            @NotNull TreeCellRenderer renderer = tree.getCellRenderer();
            if (!(renderer instanceof CommentRenderer)) {
                tree.setCellRenderer(commentRenderer(renderer));
            }
            return;
        }
        if (component instanceof Container) {
            for (@NotNull Component child : ((Container) component).getComponents()) {
                decorateTreesInComponent(child);
            }
        }
    }

    @NotNull
    protected CommentRenderer commentRenderer(TreeCellRenderer renderer) {
        return new CommentRenderer(renderer, project);
    }

    /**
     * Custom TreeCellRenderer that wraps the original renderer and appends
     * doc comments to structure view tree nodes.
     */
    protected static class CommentRenderer implements TreeCellRenderer {

        protected final TreeCellRenderer delegate;
        protected final Project project;

        /**
         * Last painted tree, to repaint when a doc is computed async.
         */
        private JTree lastTree;

        protected CommentRenderer(@NotNull TreeCellRenderer delegate, @NotNull Project project) {
            this.delegate = delegate;
            this.project = project;
        }

        protected boolean disable(@NotNull AppSettingsState state) {
            return !state.showStructureComment;
        }

        @Override
        public Component getTreeCellRendererComponent(@NotNull JTree tree, Object node,
                                                      boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            @NotNull Component comp = delegate.getTreeCellRendererComponent(
                    tree, node, selected, expanded, leaf, row, hasFocus);
            try {
                if (disable(AppSettingsState.getInstance())) {
                    return comp;
                }
                if (project.isDisposed()) {
                    return comp;
                }
                if (DumbService.getInstance(project).isDumb()) {
                    return comp;
                }
                lastTree = tree;
                appendComment(node, comp);
            } catch (ProcessCanceledException ignore) {
                // ignore
            } catch (Throwable e) {
                LOG.info("{} getTreeCellRendererComponent catch Throwable but log to record.",
                        this.getClass().getSimpleName(), e);
            }
            return comp;
        }

        protected void appendComment(Object value, Component comp) {
            if (comp instanceof ColoredTreeCellRenderer) {
                appendCommentRenderer((ColoredTreeCellRenderer) comp, value);
            }
        }

        /**
         * Non-blocking: only reads the memory cache of the per-project RenderDocCache service
         * and schedules an async compute when missing.
         * Never call runReadAction / runReadActionInSmartMode / waitForSmartMode here:
         * this runs on EDT while painting (holding the AWTTreeLock) and a blocking wait can
         * deadlock against a background write action waiting for the EDT (IDE freeze).
         */
        protected void appendCommentRenderer(@NotNull ColoredTreeCellRenderer renderer, Object node) {
            @Nullable String comment = RenderDocCache.doc(project, node,
                    () -> toComment(node), this::repaintTree);
            if (comment != null && !comment.isEmpty()) {
                append(renderer, comment);
            }
        }

        private void repaintTree() {
            @Nullable JTree tree = lastTree;
            if (tree != null) {
                tree.repaint();
            }
        }

        @Nullable
        protected String toComment(Object node) {
            @Nullable PsiElement element = extractPsiElement(node);
            if (element == null) {
                return null;
            }
            if (!element.isValid()) {
                return null;
            }
            @NotNull SettingsInfo info = SettingsInfo.of(project, FuncEnum.TREE);
            return BaseLangDoc.resolveDoc(info, element);
        }

        @Nullable
        protected static PsiElement extractPsiElement(Object node) {
            Object descriptor = node;
            if (node instanceof DefaultMutableTreeNode) {
                descriptor = ((DefaultMutableTreeNode) node).getUserObject();
            }
            if (descriptor instanceof AbstractTreeNode) {
                Object treeElement = ((AbstractTreeNode<?>) descriptor).getValue();
                if (treeElement instanceof StructureViewTreeElement) {
                    Object elementValue = ((StructureViewTreeElement) treeElement).getValue();
                    if (elementValue instanceof PsiElement) {
                        return (PsiElement) elementValue;
                    }
                }
                if (treeElement instanceof PsiElement) {
                    return (PsiElement) treeElement;
                }
            }
            return null;
        }

        protected void append(@NotNull ColoredTreeCellRenderer renderer, String comment) {
            renderer.append(" " + comment, SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
    }
}
