package io.github.linwancen.plugin.show.java.line;

import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.java.doc.OwnerToPsiDocUtils;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * call SkipUtils
 */
public class OwnerToPsiDocSkip {

    private OwnerToPsiDocSkip() {}

    public static PsiDocComment refDoc(@Nullable PsiDocCommentOwner docOwner) {
        if (docOwner == null) {
            return null;
        }
        @NotNull AppSettingsState appSettings = AppSettingsState.getInstance();
        @NotNull ProjectSettingsState projectSettings = ProjectSettingsState.getInstance(docOwner.getProject());
        if (SkipUtils.skipSign(docOwner, appSettings, projectSettings)) {
            return null;
        }
        @Nullable PsiDocComment docComment = docOwner instanceof PsiMethod
                ? OwnerToPsiDocUtils.methodDoc(((PsiMethod) docOwner))
                : OwnerToPsiDocUtils.srcOrByteCodeDoc(docOwner);
        return SkipUtils.skipDoc(docComment, appSettings, projectSettings);
    }
}
