package io.github.linwancen.plugin.show.doc;

import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

public class JsonDocUtils {

    private JsonDocUtils() {}

    /**
     * depend on JsonJump
     */
    @Nullable
    public static PsiDocComment jsonDoc(PsiElement element, int startOffset, int endOffset) {
        JsonProperty jsonProp = PsiTreeUtil.getParentOfType(element, JsonProperty.class, true, startOffset);
        if (jsonProp == null || jsonProp.getNameElement().getTextRange().getEndOffset() > endOffset) {
            return null;
        }
        for (PsiReference reference : jsonProp.getNameElement().getReferences()) {
            PsiElement resolve = reference.resolve();
            if (resolve instanceof PsiDocCommentOwner) {
                PsiDocCommentOwner owner = (PsiDocCommentOwner) resolve;
                PsiDocComment docComment = DocUtils.srcOrByteCodeDoc(owner);
                if (docComment != null) {
                    return docComment;
                }
            }
        }
        return null;
    }
}
