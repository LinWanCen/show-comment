package io.github.linwancen.plugin.comment;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.comment.settings.AppSettingsState;
import io.github.linwancen.plugin.comment.utils.CommentFactory;
import io.github.linwancen.plugin.comment.utils.PsiDocCommentUtils;
import io.github.linwancen.plugin.comment.utils.PsiMethodCommentFactory;
import io.github.linwancen.plugin.comment.utils.SkipUtils;
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
        PsiElement psiElement = psiElementFrom(viewProvider, lineNumber);
        PsiDocComment docComment = psiDocCommentFrom(psiElement);
        String comment = PsiDocCommentUtils.getCommentText(docComment);
        if (comment == null) {
            return null;
        }
        LineExtensionInfo info = new LineExtensionInfo(" //" + comment, settings.lineEndTextAttr);
        return Collections.singletonList(info);
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

    protected static final String[] KEYS = {
            "=",
    };

    private static @Nullable PsiElement psiElementFrom(FileViewProvider viewProvider, int lineNumber) {
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
        String text = document.getText(new TextRange(startOffset, endOffset));
        int offset = 0;
        for (String s : KEYS) {
            int i = text.indexOf(s);
            if (i > 0) {
                offset += (i + s.length());
                break;
            }
        }
        text = text.substring(offset);
        boolean startWithSymbol = false;
        char[] chars = text.toCharArray();
        // skip symbol and space
        for (char c : chars) {
            if (Character.isLetter(c)) {
                break;
            }
            if (!Character.isSpaceChar(c)) {
                startWithSymbol = true;
            }
            offset++;
        }
        offset += startOffset;
        if (startWithSymbol) {
            startOffset = 0;
        }
        PsiElement element = viewProvider.findElementAt(offset, JavaLanguage.INSTANCE);
        PsiIdentifier psiIdentifier = psiIdentifier(endOffset, element);
        if (psiIdentifier == null) {
            return null;
        }
        return parentElementOf(psiIdentifier, startOffset, endOffset);
    }

    @Nullable
    private static PsiIdentifier psiIdentifier(int endOffset, PsiElement element) {
        if (element == null) {
            return null;
        }
        while (!(element instanceof PsiIdentifier)) {
            element = PsiTreeUtil.nextVisibleLeaf(element);
            if (element == null) {
                return null;
            }
            if (element.getTextRange().getEndOffset() > endOffset) {
                return null;
            }
        }
        return (PsiIdentifier) element;
    }

    @Nullable
    private static PsiElement parentElementOf(PsiElement psiIdentifier, int startOffset, int endOffset) {
        // method call
        PsiMethodCallExpression call =
                PsiTreeUtil.getParentOfType(psiIdentifier, PsiMethodCallExpression.class, false, startOffset);
        if (call != null) {
            // skip double comment when method call in new line:
            // someObject // someMethodComment
            //   .someMethod(); // someMethodComment
            if ((call.getNode().getStartOffset() + call.getNode().getTextLength()) > endOffset) {
                return null;
            }
            try {
                return call.resolveMethod();
            } catch (Exception e) {
                return null;
            }
        }
        // new
        PsiElement newPsiElement = newMethodOrClass(psiIdentifier, startOffset);
        if (newPsiElement != null) {
            return newPsiElement;
        }
        // ::/class/field
        PsiReference psiReference = parentPsiReference(psiIdentifier, endOffset);
        if (psiReference != null) {
            return psiReference.resolve();
        }
        return null;
    }

    @Nullable
    private static PsiElement newMethodOrClass(PsiElement element, int startOffset) {
        // new and Class should same line
        PsiNewExpression newExp = PsiTreeUtil.getParentOfType(element, PsiNewExpression.class, false, startOffset);
        if (newExp != null) {
            PsiMethod psiMethod = newExp.resolveMethod();
            if (psiMethod != null) {
                return psiMethod;
            }
            PsiJavaCodeReferenceElement classReference = newExp.getClassReference();
            if (classReference != null) {
                return classReference.resolve();
            }
        }
        return null;
    }

    @Nullable
    private static PsiReference parentPsiReference(PsiElement element, int endOffset) {
        PsiElement parent;
        while ((parent = element.getParent()) instanceof PsiReference) {
            if (parent.getTextRange().getEndOffset() > endOffset) {
                break;
            }
            element = parent;
        }
        if (element instanceof PsiReference) {
            return (PsiReference) element;
        }
        return null;
    }
}
