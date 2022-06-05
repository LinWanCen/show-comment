package io.github.linwancen.plugin.show.line;

import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.show.doc.OwnerToPsiDocUtils;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.Nullable;

/**
 * call OwnerToPsiDocUtils, SkipUtils
 */
class OwnerToPsiDocSkip {

    private OwnerToPsiDocSkip() {}

    static PsiDocComment refDoc(@Nullable PsiDocCommentOwner docOwner) {
        if (docOwner == null) {
            return null;
        }
        AppSettingsState appSettings = AppSettingsState.getInstance();
        ProjectSettingsState projectSettings = ProjectSettingsState.getInstance(docOwner.getProject());
        if (SkipUtils.skipSign(docOwner, appSettings, projectSettings)) {
            return null;
        }
        PsiDocComment docComment = docOwner instanceof PsiMethod
                ? OwnerToPsiDocUtils.methodDoc(((PsiMethod) docOwner))
                : OwnerToPsiDocUtils.srcOrByteCodeDoc(docOwner);
        return SkipUtils.skipDoc(docComment, appSettings, projectSettings);
    }
}
