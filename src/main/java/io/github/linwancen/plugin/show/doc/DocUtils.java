package io.github.linwancen.plugin.show.doc;

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
        if (!(psiFile instanceof PsiJavaFile)) {
            return null;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        if (PsiPackage.PACKAGE_INFO_FILE.equals(psiFile.getName())) {
            return PackageDocUtils.fromPackageInfoFile(psiFile);
        }
        PsiClass[] classes = psiJavaFile.getClasses();
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
