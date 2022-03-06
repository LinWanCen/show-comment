package io.github.linwancen.plugin.show.line;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.javadoc.PsiDocMethodOrFieldRef;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import org.jetbrains.annotations.Nullable;

class LineDocUtils {

    private LineDocUtils() {}

    @Nullable
    static PsiDocComment elementDoc(PsiElement element, PsiElement psiIdentifier,
                                    int startOffset, int endOffset) {
        AppSettingsState instance = AppSettingsState.getInstance();
        if (element != null) {
            PsiDocComment elementDoc = elementDoc(element, startOffset, endOffset, instance);
            if (elementDoc != null) {
                return elementDoc;
            }
        }
        if (instance.fromRef) {
            return refDoc(psiIdentifier, endOffset);
        }
        return null;
    }

    @Nullable
    private static PsiDocComment elementDoc(PsiElement element, int startOffset, int endOffset,
                                            AppSettingsState instance) {
        if (instance.fromCall) {
            PsiDocComment executableDoc = parentMethodDoc(element, startOffset, endOffset);
            if (executableDoc != null) {
                return executableDoc;
            }
        }
        if (instance.fromNew) {
            PsiDocComment newDoc = parentNewDoc(element, startOffset);
            if (newDoc != null) {
                return newDoc;
            }
        }
        if (instance.fromRef) {
            return docRefDoc(element);
        }
        return null;
    }

    @Nullable
    private static PsiDocComment parentMethodDoc(PsiElement element, int startOffset, int endOffset) {
        // method call
        PsiMethodCallExpression call =
                PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class, false, startOffset);
        if (call == null) {
            return null;
        }
        if ((call.getNode().getStartOffset() + call.getNode().getTextLength()) > endOffset) {
            return null;
        }
        try {
            PsiDocComment methodComment = SkipDocUtils.methodDoc(call.resolveMethod());
            if (methodComment != null) {
                return methodComment;
            }
        } catch (Exception ignored) {
            // ignored
        }
        return null;
    }

    @Nullable
    private static PsiDocComment parentNewDoc(PsiElement element, int startOffset) {
        // new and Class should same line, so not need if endOffset
        PsiNewExpression newExp = PsiTreeUtil.getParentOfType(element, PsiNewExpression.class, false, startOffset);
        if (newExp == null) {
            return null;
        }
        PsiDocComment methodComment = SkipDocUtils.methodDoc(newExp.resolveMethod());
        if (methodComment != null) {
            return methodComment;
        }
        PsiJavaCodeReferenceElement classReference = newExp.getClassReference();
        return javaCodeDoc(classReference);
    }

    /**
     * ::/class/field
     */
    @Nullable
    private static PsiDocComment refDoc(PsiElement element, int endOffset) {
        if (element == null) {
            return null;
        }
        // while for xx.xx.xx... when left to right, only once when right to left
        PsiElement parent;
        while ((parent = element.getParent()) instanceof PsiReference) {
            if (parent.getTextRange().getEndOffset() > endOffset) {
                break;
            }
            element = parent;
        }
        if (element instanceof PsiReference) {
            PsiElement resolve = ((PsiReference) element).resolve();
            if (resolve instanceof PsiDocCommentOwner) {
                return SkipDocUtils.refDoc(((PsiDocCommentOwner) resolve));
            }
        }
        // for right to left field
        if (parent instanceof PsiDocCommentOwner) {
            return SkipDocUtils.refDoc(((PsiDocCommentOwner) parent));
        }
        // for right to left for
        if (parent instanceof PsiForeachStatement) {
            PsiParameter iterationParameter = ((PsiForeachStatement) parent).getIterationParameter();
            PsiTypeElement typeElement = iterationParameter.getTypeElement();
            if (typeElement == null) {
                return null;
            }
            PsiJavaCodeReferenceElement ref = typeElement.getInnermostComponentReferenceElement();
            return javaCodeDoc(ref);
        }
        return null;
    }


    @Nullable
    private static PsiDocComment javaCodeDoc(PsiJavaCodeReferenceElement ref) {
        if (ref == null) {
            return null;
        }
        PsiElement resolve = ref.resolve();
        if (resolve instanceof PsiDocCommentOwner) {
            return SkipDocUtils.refDoc(((PsiDocCommentOwner) resolve));
        }
        return null;
    }

    @Nullable
    private static PsiDocComment docRefDoc(PsiElement element) {
        PsiElement parent = element.getParent();
        if (parent instanceof PsiDocMethodOrFieldRef) {
            element = parent;
        }
        if (element instanceof PsiDocMethodOrFieldRef) {
            PsiReference reference = element.getReference();
            if (reference == null) {
                return null;
            }
            PsiElement resolve = reference.resolve();
            if (resolve instanceof PsiMethod) {
                return SkipDocUtils.methodDoc(((PsiMethod) resolve));
            }
            if (resolve instanceof PsiDocCommentOwner) {
                return SkipDocUtils.refDoc(((PsiDocCommentOwner) resolve));
            }
        }
        return null;
    }
}
