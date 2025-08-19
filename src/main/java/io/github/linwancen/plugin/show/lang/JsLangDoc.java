package io.github.linwancen.plugin.show.lang;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.lang.javascript.psi.JSPsiReferenceElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.ext.listener.FileLoader;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.lang.base.PsiUnSaveUtils;
import io.github.linwancen.plugin.show.lang.vue.VueRouterCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JsLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(JavascriptLanguage.INSTANCE.getID(), new JsLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(JSPsiReferenceElement.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentJs;
    }

    @Override
    public @Nullable <T extends SettingsInfo> String treeDoc(@NotNull T info, @NotNull ProjectViewNode<?> node,
                                                             @NotNull Project project) {
        @Nullable VirtualFile virtualFile = node.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        @Nullable PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            return null;
        }
        @Nullable ES6ExportDefaultAssignment export = PsiTreeUtil.findChildOfType(psiFile,
                ES6ExportDefaultAssignment.class);
        if (export == null) {
            return null;
        }
        @Nullable String doc = resolveDocPrint(info, export);
        if (doc != null && "index".equals(virtualFile.getNameWithoutExtension())) {
            VirtualFile parent = virtualFile.getParent();
            if (parent != null) {
                @Nullable VueRouterCache extension = FileLoader.EPN.findExtension(VueRouterCache.class);
                if (extension != null) {
                    extension.fileDoc.put(parent, doc);
                }
            }
        }
        return doc;
    }

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocRaw(@NotNull T info, @NotNull PsiElement resolve) {
        @Nullable PsiComment psiComment = JSDocumentationUtils.findOwnDocCommentForImplicitElement(resolve);
        if (psiComment == null) {
            if (info.appSettings.showLineEndCommentJsBase) {
                return super.resolveDocRaw(info, resolve);
            }
            return null;
        }
        return PsiUnSaveUtils.getText(psiComment);
    }
}
