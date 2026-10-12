package io.github.linwancen.plugin.show.tree;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts a view node (Project View / Commit / Structure / Find) into the
 * {@link PsiElement} that represents it, and back. Language hooks only see the
 * element, never the node, so every tree-family view can reuse them.
 */
public final class TreeElements {

    private TreeElements() {}

    /**
     * ProjectViewNode -> representative element (value if PSI, else resolve from its roots/file).
     */
    @Nullable
    public static PsiElement of(@NotNull ProjectViewNode<?> node, @NotNull Project project) {
        @Nullable Object value = node.getValue();
        if (value instanceof PsiElement) {
            return (PsiElement) value;
        }
        // support package view package
        for (@NotNull VirtualFile root : node.getRoots()) {
            @Nullable PsiElement element = ofFile(root, project);
            if (element != null) {
                return element;
            }
        }
        return ofFile(node.getVirtualFile(), project);
    }

    /** VirtualFile -> PsiFile or PsiDirectory, null when it cannot be resolved. */
    @Nullable
    public static PsiElement ofFile(@Nullable VirtualFile file, @NotNull Project project) {
        if (file == null || !file.isValid()) {
            return null;
        }
        @NotNull PsiManager psiManager = PsiManager.getInstance(project);
        return file.isDirectory() ? psiManager.findDirectory(file) : psiManager.findFile(file);
    }

    /**
     * The file an element lives in, for file-keyed lookups (tsv / external doc).
     * PsiFile/PsiDirectory map directly; other elements fall back to their containing file.
     */
    @Nullable
    public static VirtualFile virtualFile(@NotNull PsiElement element) {
        if (element instanceof PsiFile) {
            return ((PsiFile) element).getVirtualFile();
        }
        if (element instanceof PsiDirectory) {
            return ((PsiDirectory) element).getVirtualFile();
        }
        // PsiDirectory#getContainingFile is null, so it is handled above
        @Nullable PsiFile containingFile = element.getContainingFile();
        return containingFile != null ? containingFile.getVirtualFile() : null;
    }
}
