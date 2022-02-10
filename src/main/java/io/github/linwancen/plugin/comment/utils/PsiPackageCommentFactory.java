package io.github.linwancen.plugin.comment.utils;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.impl.source.tree.JavaDocElementType;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiPackageCommentFactory {

    private PsiPackageCommentFactory() {}

    @Nullable
    public static PsiDocComment from(PsiPackage psiPackage) {
        PsiDirectory[] psiDirectories = psiPackage.getDirectories();
        for (PsiDirectory psiDirectory : psiDirectories) {
            PsiDocComment psiDocComment = from(psiDirectory);
            if (psiDocComment != null) {
                return psiDocComment;
            }
        }
        return null;
    }

    @Nullable
    public static PsiDocComment from(PsiDirectory psiDirectory) {
        PsiFile packageInfoFile = psiDirectory.findFile(PsiPackage.PACKAGE_INFO_FILE);
        return fromPackageInfoFile(packageInfoFile);
    }

    @Nullable
    public static PsiDocComment fromPackageInfoFile(PsiFile packageInfoFile) {
        if (packageInfoFile == null) {
            return null;
        }
        ASTNode astNode = packageInfoFile.getNode();
        if (astNode == null) {
            return null;
        }
        ASTNode docCommentNode = findRelevantCommentNode(astNode);
        if (docCommentNode == null) {
            return null;
        }
        return (PsiDocComment) docCommentNode;
    }

    private static ASTNode findRelevantCommentNode(@NotNull ASTNode fileNode) {
        ASTNode node = fileNode.findChildByType(JavaElementType.PACKAGE_STATEMENT);
        if (node == null) node = fileNode.getLastChildNode();
        while (node != null && node.getElementType() != JavaDocElementType.DOC_COMMENT) {
            node = node.getTreePrev();
        }
        return node;
    }
}
