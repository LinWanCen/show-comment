package io.github.linwancen.plugin.show.java.doc;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * call PackageFileToPsiDoc, PsiMethodToPsiDoc, PsiClassUtils
 */
public class OwnerToPsiDocUtils {

    private OwnerToPsiDocUtils() {}

    @Nullable
    public static PsiDocComment srcOrByteCodeDoc(@NotNull PsiDocCommentOwner psiDocCommentOwner) {
        PsiElement navElement = psiDocCommentOwner.getNavigationElement();
        if (navElement instanceof PsiDocCommentOwner) {
            psiDocCommentOwner = (PsiDocCommentOwner) navElement;
        }
        return psiDocCommentOwner.getDocComment();
    }

    @Nullable
    public static PsiDocComment methodDoc(@NotNull PsiMethod psiMethod) {
        return PsiMethodToPsiDoc.methodSupperNewPropDoc(psiMethod);
    }

    @Nullable
    public static PsiDocComment supperMethodDoc(@NotNull PsiMethod psiMethod) {
        return PsiMethodToPsiDoc.supperMethodDoc(psiMethod);
    }

    @Nullable
    public static PsiDocComment packageDoc(@Nullable PsiPackage psiPackage) {
        if (psiPackage == null) {
            return null;
        }
        @Nullable String name = psiPackage.getName();
        if (name == null || name.length() == 0) {
            return null;
        }
        @NotNull PsiDirectory[] psiDirectories = psiPackage.getDirectories();
        for (@NotNull PsiDirectory psiDirectory : psiDirectories) {
            @Nullable PsiFile file = psiDirectory.findFile(PsiPackage.PACKAGE_INFO_FILE);
            @Nullable PsiDocComment psiDocComment = PackageFileToPsiDoc.fromPackageInfoFile(file);
            if (psiDocComment != null) {
                return psiDocComment;
            }
        }
        return null;
    }

    @Nullable
    public static PsiDocComment fileDoc(PsiFile psiFile) {
        if (!(psiFile instanceof PsiClassOwner)) {
            // for SPI
            @NotNull PsiClass[] psiClasses = PsiClassUtils.nameToClass(psiFile.getName(), psiFile.getProject());
            // for "xxx ClassName.xxx"
            if (psiClasses.length == 0) {
                VirtualFile virtualFile = psiFile.getVirtualFile();
                psiClasses = PsiClassUtils.encClass(virtualFile, psiFile.getProject());
            }
            for (@NotNull PsiClass psiClass : psiClasses) {
                @Nullable PsiDocComment docComment = srcOrByteCodeDoc(psiClass);
                if (docComment != null) {
                    // Inaccurate when there are classes with the same name
                    return docComment;
                }
            }
            return null;
        }
        if (PsiPackage.PACKAGE_INFO_FILE.equals(psiFile.getName())) {
            return PackageFileToPsiDoc.fromPackageInfoFile(psiFile);
        }
        @NotNull PsiClassOwner psiClassOwner = (PsiClassOwner) psiFile;
        @NotNull PsiClass[] classes = psiClassOwner.getClasses();
        if (classes.length == 0) {
            return null;
        }
        PsiClass psiClass = classes[0];
        return srcOrByteCodeDoc(psiClass);
    }

    @Nullable
    public static PsiDocComment dirDoc(@NotNull PsiDirectory psiDirectory) {
        @Nullable PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
        return packageDoc(psiPackage);
    }
}
