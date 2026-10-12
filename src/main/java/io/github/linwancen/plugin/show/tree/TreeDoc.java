package io.github.linwancen.plugin.show.tree;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.linwancen.plugin.show.bean.FuncEnum;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.ext.TreeExt;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.tree.first.FirstDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shared entry for every tree-family view (Project View, Commit, Structure, Find).
 * <p>
 * It orchestrates, in precedence order: external/tsv doc -> settings-driven file/dir
 * patterns -> the element's own doc comment -> each language's tree/file-level doc ->
 * the file's first comment line. Views only need to produce a {@link PsiElement}.
 * <p>
 * Runs inside {@code RenderDocCache} compute (pooled thread, smart mode), so PSI access
 * here is safe; null / invalid elements are rejected up-front.
 */
public final class TreeDoc {

    private TreeDoc() {}

    @Nullable
    public static String treeElementDoc(@NotNull Project project, @Nullable PsiElement element) {
        if (element == null || !element.isValid()) {
            return null;
        }
        @NotNull SettingsInfo info = SettingsInfo.of(project, FuncEnum.TREE);

        @Nullable VirtualFile file = TreeElements.virtualFile(element);
        if (file != null) {
            @Nullable String ext = TreeExt.extDoc(file);
            if (ext != null) {
                return ext;
            }
        }

        @Nullable String rel = RelFileDoc.relFileDoc(element, info);
        if (rel != null) {
            return rel;
        }

        @Nullable String doc = BaseLangDoc.resolveDoc(info, element);
        if (doc != null) {
            return doc;
        }

        for (@NotNull BaseLangDoc langDoc : BaseLangDoc.LANG_DOC_MAP.values()) {
            @Nullable String s = langDoc.treeDoc(info, element);
            if (s != null) {
                return s;
            }
        }

        if (element instanceof PsiFile) {
            return FirstDoc.firstDoc((PsiFile) element, info);
        }
        return null;
    }
}
