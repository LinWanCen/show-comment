package io.github.linwancen.plugin.show.tree;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserChangeNode;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserFilePathNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.lang.reflect.Field;

public class CommitViewDecorator extends StructureViewDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(CommitViewDecorator.class);

    @Nullable
    public static Field field;

    static {
        try {
            Class<?> clazz = Class.forName("com.intellij.openapi.vcs.changes.ui.ChangesTreeCellRenderer");
            field = clazz.getDeclaredField("textRenderer");
            field.setAccessible(true);
        } catch (ClassNotFoundException e) {
            LOG.error("CommitViewDecorator ClassNotFoundException:{}", e.getMessage());
        } catch (NoSuchFieldException e) {
            LOG.error("CommitViewDecorator NoSuchFieldException:{}", e.getMessage());
        }
    }

    public CommitViewDecorator(@NotNull Project project) {
        super(project);
    }

    @Override
    protected boolean disable(@NotNull AppSettingsState state) {
        return !state.showCommitComment;
    }

    @NotNull
    @Override
    protected String toolWindowId() {
        return "Commit";
    }

    @NotNull
    @Override
    protected CommentRenderer commentRenderer(TreeCellRenderer renderer) {
        return new CommitCommentRenderer(renderer, project);
    }

    static class CommitCommentRenderer extends CommentRenderer {

        public CommitCommentRenderer(@NotNull TreeCellRenderer delegate, @NotNull Project project) {
            super(delegate, project);
        }

        protected boolean disable(@NotNull AppSettingsState state) {
            return !state.showCommitComment;
        }

        @Override
        protected void appendComment(Object value, Component comp) {
            try {
                if (field == null) {
                    return;
                }
                Object textRenderer = field.get(comp);
                if (textRenderer instanceof ColoredTreeCellRenderer) {
                    appendCommentRenderer((ColoredTreeCellRenderer) textRenderer, value);
                }
            } catch (Exception e) {
                LOG.info("extractTextRenderer reflection failed", e);
            }
        }

        @Nullable
        @Override
        protected String toComment(Object node) {
            VirtualFile file = toVirtualFile(node);
            if (file == null || !file.isValid()) {
                return null;
            }
            return TreeDoc.treeElementDoc(project, TreeElements.ofFile(file, project));
        }

        @Nullable
        private VirtualFile toVirtualFile(Object node) {
            if (node instanceof ChangesBrowserChangeNode) {
                Change change = ((ChangesBrowserChangeNode) node).getUserObject();
                return change.getVirtualFile();
            }
            if (node instanceof ChangesBrowserFilePathNode) {
                FilePath filePath = ((ChangesBrowserFilePathNode) node).getUserObject();
                return filePath.getVirtualFile();
            }
            return null;
        }

        @Override
        protected void append(@NotNull ColoredTreeCellRenderer renderer, String comment) {
            SimpleColoredComponent.ColoredIterator iterator = renderer.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                // have path
                if (iterator.hasNext()) {
                    String text = iterator.next();
                    iterator.setFragment(" " + comment + " " + text);
                    return;
                }
            }
            renderer.append(" " + comment, SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
    }
}
