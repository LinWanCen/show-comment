package io.github.linwancen.plugin.show.java.doc;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.JavaDocElementType;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PackageFileToPsiDoc {

    private PackageFileToPsiDoc() {}

    @Nullable
    static PsiDocComment fromPackageInfoFile(@Nullable PsiFile packageInfoFile) {
        if (packageInfoFile == null) {
            return null;
        }
        ASTNode astNode = packageInfoFile.getNode();
        if (astNode == null) {
            return null;
        }
        @Nullable ASTNode docCommentNode = findRelevantCommentNode(astNode);
        if (docCommentNode == null) {
            return null;
        }
        return (PsiDocComment) docCommentNode;
    }

    @Nullable
    private static ASTNode findRelevantCommentNode(@NotNull ASTNode fileNode) {
        @Nullable ASTNode node = fileNode.findChildByType(JavaElementType.PACKAGE_STATEMENT);
        if (node == null) node = fileNode.getLastChildNode();
        while (node != null && node.getElementType() != JavaDocElementType.DOC_COMMENT) {
            node = node.getTreePrev();
        }
        return node;
    }
}
