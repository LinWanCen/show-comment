package io.github.linwancen.plugin.show.java.line;

import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.java.doc.PsiMethodToPsiDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * call SkipUtils
 */
public class OwnerToPsiDocSkip {

    private OwnerToPsiDocSkip() {}

    @Nullable
    public static <T extends SettingsInfo> PsiDocComment refDoc(@NotNull T info,
                                                                @NotNull PsiDocCommentOwner docOwner) {
        return SkipUtils.skipDoc(info, refDocWithOutSkip(docOwner));
    }

    @Nullable
    public static <T extends SettingsInfo> PsiDocComment refDocWithOutSkip(@NotNull PsiDocCommentOwner docOwner) {
        return docOwner instanceof PsiMethod
                ? PsiMethodToPsiDoc.methodSupperNewPropDoc(((PsiMethod) docOwner))
                : docOwner.getDocComment();
    }
}
