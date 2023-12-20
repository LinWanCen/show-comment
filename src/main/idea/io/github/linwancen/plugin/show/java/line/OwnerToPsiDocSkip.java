package io.github.linwancen.plugin.show.java.line;

import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.java.doc.OwnerToPsiDocUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * call SkipUtils
 */
public class OwnerToPsiDocSkip {

    private OwnerToPsiDocSkip() {}

    @Nullable
    public static <T extends SettingsInfo> PsiDocComment refDoc(@NotNull T info,
                                                                @Nullable PsiDocCommentOwner docOwner) {
        if (docOwner == null) {
            return null;
        }
        if (SkipUtils.skipSign(info, docOwner)) {
            return null;
        }
        @Nullable PsiDocComment docComment = docOwner instanceof PsiMethod
                ? OwnerToPsiDocUtils.methodDoc(((PsiMethod) docOwner))
                : OwnerToPsiDocUtils.srcOrByteCodeDoc(docOwner);
        return SkipUtils.skipDoc(info, docComment);
    }
}
