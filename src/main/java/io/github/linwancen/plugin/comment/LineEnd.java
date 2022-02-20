package io.github.linwancen.plugin.comment;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import io.github.linwancen.plugin.comment.settings.AppSettingsState;
import io.github.linwancen.plugin.comment.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class LineEnd extends EditorLinePainter {

    @Override
    public @Nullable Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project,
                                                                     @NotNull VirtualFile file, int lineNumber) {
        AppSettingsState settings = AppSettingsState.getInstance();
        if (!settings.showLineEndComment) {
            return null;
        }
        if (DumbService.isDumb(project)) {
            return null;
        }
        FileViewProvider viewProvider = PsiManager.getInstance(project).findViewProvider(file);
        PsiElement psiElement = resolveElementFrom(viewProvider, lineNumber);
        PsiDocComment docComment = psiDocCommentFrom(psiElement);
        String comment = PsiDocCommentUtils.getCommentText(docComment);
        if (comment == null) {
            return null;
        }
        LineExtensionInfo info = new LineExtensionInfo(" //" + comment, settings.lineEndTextAttr);
        return Collections.singletonList(info);
    }

    private static @Nullable PsiElement resolveElementFrom(FileViewProvider viewProvider, int lineNumber) {
        if (viewProvider == null || !viewProvider.hasLanguage(JavaLanguage.INSTANCE)) {
            return null;
        }
        Document document = viewProvider.getDocument();
        if (document == null) {
            return null;
        }
        if (document.getLineCount() < lineNumber) {
            return null;
        }
        int startOffset = document.getLineStartOffset(lineNumber);
        int endOffset = document.getLineEndOffset(lineNumber);
        if (AppSettingsState.getInstance().findElementRightToLeft) {
            return ResolveElementRightToLeftUtils.resolveElement(viewProvider, startOffset, endOffset);
        }
        return ResolveElementLeftToRightUtils.resolveElement(viewProvider, document, startOffset, endOffset);
    }

    @Nullable
    private PsiDocComment psiDocCommentFrom(PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }
        if (psiElement instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) psiElement;
            if (SkipUtils.skip(psiClass, psiClass.getProject())) {
                return null;
            }
            return CommentFactory.fromSrcOrByteCode(psiClass);
        }
        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            if (SkipUtils.skip(psiMethod.getContainingClass(), psiMethod.getProject())) {
                return null;
            }
            return PsiMethodCommentFactory.from(psiMethod);
        }
        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            if (SkipUtils.skip(psiField.getContainingClass(), psiField.getProject())) {
                return null;
            }
            return CommentFactory.fromSrcOrByteCode(psiField);
        }
        return null;
    }
}
