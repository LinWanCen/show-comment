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

    public static PsiDocComment srcOrByteCodeDoc(PsiDocCommentOwner psiDocCommentOwner) {
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
    public static PsiDocComment supperMethodDoc(PsiMethod psiMethod) {
        return PsiMethodToPsiDoc.supperMethodDoc(psiMethod);
    }

    @Nullable
    public static PsiDocComment packageDoc(@Nullable PsiPackage psiPackage) {
        if (psiPackage == null) {
            return null;
        }
        String name = psiPackage.getName();
        if (name == null || name.length() == 0) {
            return null;
        }
        PsiDirectory[] psiDirectories = psiPackage.getDirectories();
        for (PsiDirectory psiDirectory : psiDirectories) {
            PsiFile file = psiDirectory.findFile(PsiPackage.PACKAGE_INFO_FILE);
            PsiDocComment psiDocComment = PackageFileToPsiDoc.fromPackageInfoFile(file);
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
            PsiClass[] psiClasses = PsiClassUtils.nameToClass(psiFile.getName(), psiFile.getProject());
            // for "xxx ClassName.xxx"
            if (psiClasses.length == 0) {
                VirtualFile virtualFile = psiFile.getVirtualFile();
                psiClasses = PsiClassUtils.encClass(virtualFile, psiFile.getProject());
            }
            for (PsiClass psiClass : psiClasses) {
                PsiDocComment docComment = srcOrByteCodeDoc(psiClass);
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
        PsiClassOwner psiClassOwner = (PsiClassOwner) psiFile;
        PsiClass[] classes = psiClassOwner.getClasses();
        if (classes.length == 0) {
            return null;
        }
        PsiClass psiClass = classes[0];
        return srcOrByteCodeDoc(psiClass);
    }

    @Nullable
    public static PsiDocComment dirDoc(PsiDirectory psiDirectory) {
        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
        return packageDoc(psiPackage);
    }
}
