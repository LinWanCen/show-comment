package io.github.linwancen.plugin.show.java;

import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.java.doc.OwnerToPsiDocUtils;
import io.github.linwancen.plugin.show.java.line.NewCallRefToPsiDoc;
import io.github.linwancen.plugin.show.lang.base.DocSkip;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Java file / directory / package / member doc for tree views.
 * Dispatches on the node's representative {@link PsiElement} instead of the node type,
 * so Project View, Commit, Structure and Find can all reuse it.
 */
public class JavaTree {

    private JavaTree() {}

    @Nullable
    public static <T extends SettingsInfo> String treeDoc(@NotNull T info, @NotNull PsiElement element) {
        // refactor: JSON file doc return string
        @Nullable String s = null;
        if (element instanceof PsiFile) {
            s = OwnerToPsiDocUtils.fileDoc(info, (PsiFile) element);
        }
        if (s == null) {
            @Nullable PsiDocComment docComment = nodeDoc(info, element);
            if (docComment == null) {
                return null;
            }
            s = JavaLangDoc.INSTANCE.docElementToStr(info, docComment);
        }
        if (s != null && !DocSkip.skipDoc(info, s)) {
            return s;
        }
        return null;
    }

    @Nullable
    static <T extends SettingsInfo> PsiDocComment nodeDoc(@NotNull T info, @NotNull PsiElement element) {
        if (element instanceof PsiDirectory) {
            return dirDoc((PsiDirectory) element);
        }

        if (element instanceof PsiField) {
            // On Show Members
            @NotNull PsiType type = ((PsiField) element).getType();
            if (type instanceof PsiClassReferenceType) {
                @NotNull PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) type;
                @NotNull PsiJavaCodeReferenceElement reference = psiClassReferenceType.getReference();
                return NewCallRefToPsiDoc.javaCodeDoc(info, reference);
            }
            return null;
        }

        if (element instanceof PsiPackage) {
            // On Packages View
            return OwnerToPsiDocUtils.packageDoc((PsiPackage) element);
        }

        return null;
    }

    @Nullable
    public static PsiDocComment dirDoc(@NotNull PsiDirectory child) {
        while (true) {
            @Nullable PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(child);
            @Nullable PsiDocComment docComment = OwnerToPsiDocUtils.packageDoc(psiPackage);
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
}
