package io.github.linwancen.plugin.show.doc;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.Nullable;

public class DocUtils {

    private DocUtils() {}

    public static PsiDocComment srcOrByteCodeDoc(PsiDocCommentOwner psiDocCommentOwner) {
        PsiElement navElement = psiDocCommentOwner.getNavigationElement();
        if (navElement instanceof PsiDocCommentOwner) {
            psiDocCommentOwner = (PsiDocCommentOwner) navElement;
        }
        return psiDocCommentOwner.getDocComment();
    }

    @Nullable
    public static PsiDocComment methodDoc(PsiMethod psiMethod) {
        return MethodDocUtils.methodSupperNewPropDoc(psiMethod);
    }

    @Nullable
    public static PsiDocComment packageDoc(PsiPackage psiPackage) {
        PsiDirectory[] psiDirectories = psiPackage.getDirectories();
        for (PsiDirectory psiDirectory : psiDirectories) {
            PsiDocComment psiDocComment = dirDoc(psiDirectory);
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
            return PackageDocUtils.fromPackageInfoFile(psiFile);
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
        PsiFile file = psiDirectory.findFile(PsiPackage.PACKAGE_INFO_FILE);
        return PackageDocUtils.fromPackageInfoFile(file);
    }
}
