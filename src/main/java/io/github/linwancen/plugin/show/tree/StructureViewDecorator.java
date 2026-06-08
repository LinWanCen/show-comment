package io.github.linwancen.plugin.show.tree;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ApplicationManager;
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

    private final Project project;

    public StructureViewDecorator(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
        if (project.isDisposed()) {
            return;
        }
        @NotNull AppSettingsState state = AppSettingsState.getInstance();
        if (!state.showStructureComment) {
            return;
        }
        decorateStructureView();
    }

    private void decorateStructureView() {
        @NotNull ToolWindowManager twm = ToolWindowManager.getInstance(project);
        @Nullable ToolWindow tw = twm.getToolWindow("Structure");
        if (tw == null || !tw.isVisible()) {
            return;
        }
        JComponent component = tw.getComponent();
        decorateTreesInComponent(component);
    }

    private void decorateTreesInComponent(@NotNull Component component) {
        if (component instanceof JTree) {
            @NotNull JTree tree = (JTree) component;
            @NotNull TreeCellRenderer renderer = tree.getCellRenderer();
            if (!(renderer instanceof StructureCommentRenderer)) {
                tree.setCellRenderer(new StructureCommentRenderer(renderer, project));
            }
            return;
        }
        if (component instanceof Container) {
            for (@NotNull Component child : ((Container) component).getComponents()) {
                decorateTreesInComponent(child);
            }
        }
    }

    /**
     * Custom TreeCellRenderer that wraps the original renderer and appends
     * doc comments to structure view tree nodes.
     */
    static class StructureCommentRenderer implements TreeCellRenderer {

        private final TreeCellRenderer delegate;
        private final Project project;

        StructureCommentRenderer(@NotNull TreeCellRenderer delegate, @NotNull Project project) {
            this.delegate = delegate;
            this.project = project;
        }

        @Override
        public Component getTreeCellRendererComponent(@NotNull JTree tree, Object value,
                                                      boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            @NotNull Component comp = delegate.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);
            try {
                if (!AppSettingsState.getInstance().showStructureComment) {
                    return comp;
                }
                if (project.isDisposed()) {
                    return comp;
                }
                if (DumbService.getInstance(project).isDumb()) {
                    return comp;
                }
                if (comp instanceof ColoredTreeCellRenderer) {
                    appendComment((ColoredTreeCellRenderer) comp, value);
                }
            } catch (ProcessCanceledException ignore) {
                // ignore
            } catch (Throwable e) {
                LOG.info("StructureCommentRenderer catch Throwable but log to record.", e);
            }
            return comp;
        }

        private void appendComment(@NotNull ColoredTreeCellRenderer renderer, Object value) {
            @Nullable PsiElement element = extractPsiElement(value);
            if (element == null) {
                return;
            }
            DumbService.getInstance(project).runReadActionInSmartMode(() ->
                    ApplicationManager.getApplication().runReadAction(() -> {
                        if (!element.isValid()) {
                            return;
                        }
                        @NotNull SettingsInfo info = SettingsInfo.of(project, FuncEnum.TREE);
                        if (DumbService.getInstance(project).isDumb()) {
                            return;
                        }
                        @Nullable String comment = BaseLangDoc.resolveDoc(info, element);
                        if (comment != null && !comment.isEmpty()) {
                            renderer.append(" " + comment, SimpleTextAttributes.GRAY_ATTRIBUTES);
                        }
                    }));
        }

        @Nullable
        private static PsiElement extractPsiElement(Object value) {
            Object descriptor = value;
            if (value instanceof DefaultMutableTreeNode) {
                descriptor = ((DefaultMutableTreeNode) value).getUserObject();
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
    }
}
