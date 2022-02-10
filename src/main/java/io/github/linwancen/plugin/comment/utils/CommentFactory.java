package io.github.linwancen.plugin.comment.utils;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.Nullable;

public class CommentFactory {

    private CommentFactory() {}

    public static PsiDocComment fromSrcOrByteCode(PsiDocCommentOwner psiElement) {
        PsiElement navElement = psiElement.getNavigationElement();
        if (navElement instanceof PsiDocCommentOwner) {
            psiElement = (PsiDocCommentOwner) navElement;
        }
        return psiElement.getDocComment();
    }

    @Nullable
    public static PsiDocComment from(PsiFile psiFile) {
        if (!(psiFile instanceof PsiJavaFile)) {
            return null;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        if (PsiPackage.PACKAGE_INFO_FILE.equals(psiFile.getName())) {
            return PsiPackageCommentFactory.fromPackageInfoFile(psiFile);
        }
        PsiClass[] classes = psiJavaFile.getClasses();
        if (classes.length == 0) {
            return null;
        }
        PsiClass psiClass = classes[0];
        return fromSrcOrByteCode(psiClass);
    }
}
