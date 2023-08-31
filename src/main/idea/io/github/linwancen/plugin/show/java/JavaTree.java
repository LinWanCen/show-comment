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
import io.github.linwancen.plugin.show.lang.base.DocSkip;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaTree {

    private JavaTree() {}

    @Nullable
    public static <T extends SettingsInfo> String treeDoc(@NotNull T settingsInfo, ProjectViewNode<?> node,
                                                          @NotNull Project project) {
        @Nullable PsiDocComment docComment = nodeDoc(settingsInfo, node, project);
        if (docComment == null) {
            return null;
        }
        @Nullable String s = JavaLangDoc.INSTANCE.docElementToStr(settingsInfo, docComment);
        if (s != null && !DocSkip.skipDoc(settingsInfo, s)) {
            return s;
        }
        return null;
    }

    @Nullable
    static <T extends SettingsInfo> PsiDocComment nodeDoc(@NotNull T settingsInfo, ProjectViewNode<?> node,
                                                          @NotNull Project project) {
        if (node instanceof PsiFileNode) {
            PsiFile psiFile = ((PsiFileNode) node).getValue();
            return OwnerToPsiDocUtils.fileDoc(psiFile);
        }
        if (node instanceof PsiDirectoryNode) {
            PsiDirectory psiDirectory = ((PsiDirectoryNode) node).getValue();
            return dirDoc(psiDirectory);
        }

        if (node instanceof PsiFieldNode) {
            // On Show Members
            PsiField psiField = ((PsiFieldNode) node).getValue();
            // for @Autowire Bean
            @NotNull PsiType type = psiField.getType();
            if (type instanceof PsiClassReferenceType) {
                @NotNull PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) type;
                @NotNull PsiJavaCodeReferenceElement reference = psiClassReferenceType.getReference();
                return NewCallRefToPsiDoc.javaCodeDoc(settingsInfo, reference);
            }
        }

        if (node instanceof PackageElementNode) {
            // On Packages View
            @NotNull PsiPackage psiPackage = ((PackageElementNode) node).getValue().getPackage();
            return packageDoc(psiPackage);
        }

        // On Packages View, Project Files View
        @Nullable VirtualFile virtualFile = node.getVirtualFile();
        if (virtualFile == null || !virtualFile.isDirectory()) {
            return null;
        }
        @Nullable PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(virtualFile);
        if (psiDirectory == null) {
            return null;
        }
        return dirDoc(psiDirectory);
    }

    @Nullable
    static PsiDocComment dirDoc(@NotNull PsiDirectory child) {
        while (true) {
            @Nullable PsiDocComment docComment = OwnerToPsiDocUtils.dirDoc(child);
            if (docComment != null) {
                return docComment;
            }
            @NotNull AppSettingsState instance = AppSettingsState.getInstance();
            if (!instance.compact) {
                return null;
            }
            @Nullable PsiDirectory parent = child.getParent();
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
    static PsiDocComment packageDoc(@NotNull PsiPackage child) {
        while (true) {
            @Nullable PsiDocComment docComment = OwnerToPsiDocUtils.packageDoc(child);
            if (docComment != null) {
                return docComment;
            }
            @Nullable PsiPackage parent = child.getParentPackage();
            if (parent == null) {
                return null;
            }
            // PsiPackage not implemented getChildren()
            child = parent;
        }
    }
}
