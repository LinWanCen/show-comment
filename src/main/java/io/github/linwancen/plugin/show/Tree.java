package io.github.linwancen.plugin.show;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.nodes.*;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import io.github.linwancen.plugin.show.doc.DocTextUtils;
import io.github.linwancen.plugin.show.doc.DocUtils;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.Nullable;

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

        PsiDocComment docComment = nodeDoc(node, project);
        if (docComment == null) {
            return;
        }

        String commentText = DocTextUtils.text(docComment);
        if (commentText == null) {
            return;
        }
        List<ColoredFragment> coloredText = data.getColoredText();
        if (coloredText.isEmpty()) {
            data.addText(data.getPresentableText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        data.addText(commentText, SimpleTextAttributes.GRAY_ATTRIBUTES);
    }

    @Nullable
    private PsiDocComment nodeDoc(ProjectViewNode<?> node, Project project) {
        if (node instanceof PsiFileNode) {
            PsiFile psiFile = ((PsiFileNode) node).getValue();
            return DocUtils.fileDoc(psiFile);
        }
        if (node instanceof PsiDirectoryNode) {
            PsiDirectory psiDirectory = ((PsiDirectoryNode) node).getValue();
            return dirDoc(psiDirectory);
        }

        if (node instanceof PsiMethodNode) {
            // On Show Members
            PsiMethod psiMethod = ((PsiMethodNode) node).getValue();
            return DocUtils.methodDoc(psiMethod);
        }
        if (node instanceof PsiFieldNode) {
            // On Show Members
            PsiField psiField = ((PsiFieldNode) node).getValue();
            return DocUtils.srcOrByteCodeDoc(psiField);
        }

        if (node instanceof ClassTreeNode) {
            // On Packages View, Project Files View, Show Members
            PsiClass psiClass = ((ClassTreeNode) node).getValue();
            return DocUtils.srcOrByteCodeDoc(psiClass);
        }
        if (node instanceof PackageElementNode) {
            // On Packages View
            PsiPackage psiPackage = ((PackageElementNode) node).getValue().getPackage();
            return packageDoc(psiPackage);
        }

        // On Packages View, Project Files View
        VirtualFile virtualFile = node.getVirtualFile();
        if (virtualFile == null || !virtualFile.isDirectory()) {
            return null;
        }
        PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(virtualFile);
        if (psiDirectory == null) {
            return null;
        }
        return dirDoc(psiDirectory);
    }

    @Nullable
    private PsiDocComment dirDoc(PsiDirectory child) {
        while (true) {
            PsiDocComment docComment = DocUtils.dirDoc(child);
            if (docComment != null) {
                return docComment;
            }
            PsiDirectory parent = child.getParent();
            if (parent == null) {
                return null;
            }
            if (parent.getChildren().length != 1) {
                return null;
            }
            child = parent;
        }
    }

    @Nullable
    private PsiDocComment packageDoc(PsiPackage child) {
        while (true) {
            PsiDocComment docComment = DocUtils.packageDoc(child);
            if (docComment != null) {
                return docComment;
            }
            PsiPackage parent = child.getParentPackage();
            if (parent == null) {
                return null;
            }
            if (parent.getChildren().length != 1) {
                return null;
            }
            child = parent;
        }
    }

    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {
        // not need
    }
}
