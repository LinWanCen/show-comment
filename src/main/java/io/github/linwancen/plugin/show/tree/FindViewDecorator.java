package io.github.linwancen.plugin.show.tree;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.impl.rules.FileGroupingRule;
import com.intellij.usages.rules.UsageInFile;
import io.github.linwancen.plugin.show.bean.FileInfo;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.tree.find.FindGroupElementUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

public class FindViewDecorator extends StructureViewDecorator {

    public FindViewDecorator(@NotNull Project project) {
        super(project);
    }

    @Override
    protected boolean disable(@NotNull AppSettingsState state) {
        return !state.showFindComment;
    }

    @NotNull
    @Override
    protected String toolWindowId() {
        return "Find";
    }

    @NotNull
    @Override
    protected CommentRenderer commentRenderer(TreeCellRenderer renderer) {
        return new FindCommentRenderer(renderer, project);
    }

    static class FindCommentRenderer extends CommentRenderer {

        public FindCommentRenderer(@NotNull TreeCellRenderer delegate, @NotNull Project project) {
            super(delegate, project);
        }

        @Override
        protected boolean disable(@NotNull AppSettingsState state) {
            return !state.showFindComment;
        }

        @Nullable
        @Override
        protected String toComment(Object node) {
            if (!(node instanceof DefaultMutableTreeNode)) {
                return null;
            }
            Object descriptor = ((DefaultMutableTreeNode) node).getUserObject();
            // line doc
            if (descriptor instanceof UsageInfo2UsageAdapter) {
                UsageInfo2UsageAdapter adapter = (UsageInfo2UsageAdapter) descriptor;
                @Nullable VirtualFile file = adapter.getFile();
                if (file == null) {
                    return null;
                }
                @Nullable Document document = adapter.getDocument();
                if (document == null) {
                    return null;
                }
                @NotNull FileInfo fileInfo = FileInfo.of(file, document, project);
                int offset = adapter.getUsageInfo().getNavigationOffset();
                // stale usage after the document changed: navigation offset may be -1 or out of range
                if (offset < 0 || offset > document.getTextLength()) {
                    return null;
                }
                int lineNumber = document.getLineNumber(offset);
                LineInfo lineInfo = LineInfo.of(fileInfo, lineNumber);
                if (lineInfo == null) {
                    return null;
                }
                return BaseLangDoc.langDoc(lineInfo);
            }

            PsiElement element = extractPsiElementFromUsage(node, descriptor);
            return TreeDoc.treeElementDoc(project, element);
        }

        @Nullable
        private PsiElement extractPsiElementFromUsage(Object node, Object descriptor) {
            // group node (package / method / file group): take the element the group itself
            // exposes to actions, e.g. the PsiPackage of a "group by package" row
            @Nullable PsiElement groupElement = FindGroupElementUtils.of(descriptor);
            if (groupElement != null) {
                return groupElement;
            }
            if (!(descriptor instanceof FileGroupingRule.FileUsageGroup)) {
                return null;
            }
            @Nullable VirtualFile virtualFile = findFileBelow((DefaultMutableTreeNode) node);
            if (virtualFile == null || !virtualFile.isValid()) {
                return null;
            }
            return PsiManager.getInstance(project).findFile(virtualFile);
        }

        /**
         * The group node does not expose its file, but every usage below it is a {@link UsageInFile}
         * (FileGroupingRule only builds a file group for such usages), so the file is read from the
         * first usage node found below this group node.
         * <p>
         * Runs on a pooled thread while the EDT may add or drop tree nodes: only index-based reads
         * are used and an out-of-range index just means "not resolvable right now".
         */
        @Nullable
        private static VirtualFile findFileBelow(@NotNull TreeNode node) {
            try {
                if (node instanceof DefaultMutableTreeNode) {
                    @Nullable Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
                    if (userObject instanceof UsageInFile) {
                        @Nullable VirtualFile file = ((UsageInFile) userObject).getFile();
                        if (file != null) {
                            return file;
                        }
                    }
                }
                int count = node.getChildCount();
                for (int i = 0; i < count; i++) {
                    @Nullable VirtualFile file = findFileBelow(node.getChildAt(i));
                    if (file != null) {
                        return file;
                    }
                }
            } catch (IndexOutOfBoundsException ignore) {
                // the node was removed or restructured while being read
            }
            return null;
        }
    }
}
