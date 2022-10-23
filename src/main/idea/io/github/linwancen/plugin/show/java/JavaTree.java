package io.github.linwancen.plugin.show.java;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.nodes.PackageElementNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFieldNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.java.doc.OwnerToPsiDocUtils;
import io.github.linwancen.plugin.show.java.line.NewCallRefToPsiDoc;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.Nullable;

public class JavaTree {

    private JavaTree() {}

    @Nullable
    public static <T extends SettingsInfo> String treeDoc(T settingsInfo, ProjectViewNode<?> node, Project project) {
        PsiDocComment docComment = nodeDoc(node, project);
        if (docComment == null) {
            return null;
        }
        return JavaLangDoc.INSTANCE.docElementToStr(settingsInfo, docComment);
    }

    @Nullable
    static PsiDocComment nodeDoc(ProjectViewNode<?> node, Project project) {
        if (node instanceof PsiFileNode) {
            PsiFile psiFile = ((PsiFileNode) node).getValue();
            return OwnerToPsiDocUtils.fileDoc(psiFile);
        }
        if (node instanceof PsiDirectoryNode) {
            PsiDirectory psiDirectory = ((PsiDirectoryNode) node).getValue();
            return dirDoc(psiDirectory);
        }

        // if (node instanceof PsiMethodNode) {
        //     // On Show Members
        //     PsiMethod psiMethod = ((PsiMethodNode) node).getValue();
        //     PsiDocComment psiDocComment = OwnerToPsiDocUtils.methodDoc(psiMethod);
        //     if (psiDocComment != null) {
        //         System.out.println("PsiMethodNode" + PsiDocToStrDoc.text(psiDocComment, true));
        //     }
        //     return psiDocComment;
        // }
        if (node instanceof PsiFieldNode) {
            // On Show Members
            PsiField psiField = ((PsiFieldNode) node).getValue();
            // PsiDocComment docComment = OwnerToPsiDocUtils.srcOrByteCodeDoc(psiField);
            // if (docComment != null) {
            //     System.out.println("PsiFieldNode" + PsiDocToStrDoc.text(docComment, true));
            //     return docComment;
            // }
            // for @Autowire Bean
            PsiType type = psiField.getType();
            if (type instanceof PsiClassReferenceType) {
                PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) type;
                PsiJavaCodeReferenceElement reference = psiClassReferenceType.getReference();
                return NewCallRefToPsiDoc.javaCodeDoc(reference);
            }
        }

        // if (node instanceof ClassTreeNode) {
        //     // On Packages View, Project Files View, Show Members
        //     PsiClass psiClass = ((ClassTreeNode) node).getValue();
        //     PsiDocComment psiDocComment = OwnerToPsiDocUtils.srcOrByteCodeDoc(psiClass);
        //     if (psiDocComment != null) {
        //         System.out.println("ClassTreeNode" + PsiDocToStrDoc.text(psiDocComment, true));
        //     }
        //     return psiDocComment;
        // }
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
    static PsiDocComment dirDoc(PsiDirectory child) {
        while (true) {
            PsiDocComment docComment = OwnerToPsiDocUtils.dirDoc(child);
            if (docComment != null) {
                return docComment;
            }
            AppSettingsState instance = AppSettingsState.getInstance();
            if (!instance.compact) {
                return null;
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
    static PsiDocComment packageDoc(PsiPackage child) {
        while (true) {
            PsiDocComment docComment = OwnerToPsiDocUtils.packageDoc(child);
            if (docComment != null) {
                return docComment;
            }
            PsiPackage parent = child.getParentPackage();
            if (parent == null) {
                return null;
            }
            // PsiPackage not implemented getChildren()
            child = parent;
        }
    }
}
