package io.github.linwancen.plugin.show.tree;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.usages.impl.rules.FileGroupingRule;
import io.github.linwancen.plugin.show.bean.FuncEnum;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.ext.TreeExt;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

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
            PsiElement element = extractPsiElementFromUsage(node);
            if (element == null || !element.isValid()) {
                return null;
            }

            PsiFile psiFile = element.getContainingFile();
            if (psiFile != null) {
                VirtualFile virtualFile = psiFile.getVirtualFile();
                if (virtualFile != null) {
                    String s = TreeExt.extDoc(virtualFile);
                    if (s != null) {
                        return s;
                    }
                }
            }

            @NotNull SettingsInfo info = SettingsInfo.of(project, FuncEnum.TREE);
            return BaseLangDoc.resolveDoc(info, element);
        }

        @Nullable
        private PsiElement extractPsiElementFromUsage(Object node) {
            Object descriptor = node;
            if (node instanceof DefaultMutableTreeNode) {
                descriptor = ((DefaultMutableTreeNode) node).getUserObject();
            }

            if (descriptor instanceof FileGroupingRule.FileUsageGroup) {
                return ((FileGroupingRule.FileUsageGroup) descriptor).getPsiFile();
            }
            return null;
        }
    }
}
