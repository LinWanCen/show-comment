package io.github.linwancen.plugin.show.java.line;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.javadoc.PsiDocMethodOrFieldRef;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.java.doc.OwnerToPsiDocUtils;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.ProjectSettingsState;
import org.jetbrains.annotations.Nullable;

/**
 * call OwnerToPsiDocSkip
 */
public class NewCallRefToPsiDoc {

    private NewCallRefToPsiDoc() {}

    /**
     * @deprecated
     */
    @Nullable
    static PsiDocComment elementDoc(PsiElement element, PsiElement psiIdentifier,
                                    int startOffset, int endOffset) {
        // JavaLangDoc.refDoc
        AppSettingsState instance = AppSettingsState.getInstance();
        if (psiIdentifier != null && "Override".equals(psiIdentifier.getText())) {
            ProjectSettingsState projectSettings = ProjectSettingsState.getInstance(psiIdentifier.getProject());
            PsiMethod psiMethod = PsiTreeUtil.getParentOfType(psiIdentifier, PsiMethod.class);
            if (psiMethod == null) {
                return null;
            }
            PsiDocComment docComment = OwnerToPsiDocUtils.supperMethodDoc(psiMethod);
            return SkipUtils.skipDoc(docComment, instance, projectSettings);
        }
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

    /**
     * @deprecated
     */
    @Nullable
    private static PsiDocComment elementDoc(PsiElement element, int startOffset, int endOffset,
                                            AppSettingsState instance) {
        if (instance.fromCall) {
            PsiDocComment executableDoc = parentMethodDoc(element, startOffset, endOffset);
            if (executableDoc != null) {
                return executableDoc;
            }
        }
        // JavaLangDoc.refDoc
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

    /**
     * @deprecated
     */
    @Nullable
    private static PsiDocComment parentMethodDoc(PsiElement element, int startOffset, int endOffset) {
        // method call
        // JavaLangDoc.resolveDocPrint
        PsiMethodCallExpression call =
                PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class, false, startOffset);
        if (call == null) {
            return null;
        }
        if ((call.getNode().getStartOffset() + call.getNode().getTextLength()) > endOffset) {
            return null;
        }
        try {
            PsiDocComment methodComment = OwnerToPsiDocSkip.refDoc(call.resolveMethod());
            if (methodComment != null) {
                return methodComment;
            }
        } catch (Throwable ignored) {
            // ignored
        }
        return null;
    }

    /**
     * @deprecated
     */
    @Nullable
    private static PsiDocComment parentNewDoc(PsiElement element, int startOffset) {
        // new and Class should same line, so not need if endOffset
        PsiNewExpression newExp = PsiTreeUtil.getParentOfType(element, PsiNewExpression.class, false, startOffset);
        if (newExp == null) {
            return null;
        }
        try {
            PsiDocComment methodComment = OwnerToPsiDocSkip.refDoc(newExp.resolveMethod());
            if (methodComment != null) {
                return methodComment;
            }
        } catch (Throwable ignore) {
            // ignore
        }
        PsiJavaCodeReferenceElement classReference = newExp.getClassReference();
        return javaCodeDoc(classReference);
    }

    /**
     * ::/class/field
     * @deprecated
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
            PsiElement resolve = null;
            try {
                resolve = ((PsiReference) element).resolve();
            } catch (Throwable ignore) {
                // ignore
            }
            if (resolve instanceof PsiDocCommentOwner) {
                return OwnerToPsiDocSkip.refDoc(((PsiDocCommentOwner) resolve));
            }
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
    public static PsiDocComment javaCodeDoc(PsiJavaCodeReferenceElement ref) {
        if (ref == null) {
            return null;
        }
        PsiElement resolve = null;
        try {
            resolve = ref.resolve();
        } catch (Throwable ignore) {
            // ignore
        }
        if (resolve instanceof PsiDocCommentOwner) {
            return OwnerToPsiDocSkip.refDoc(((PsiDocCommentOwner) resolve));
        }
        return null;
    }

    /**
     * @deprecated
     */
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
            PsiElement resolve = null;
            try {
                resolve = reference.resolve();
            } catch (Throwable ignore) {
                // ignore
            }
            if (resolve instanceof PsiMethod) {
                return OwnerToPsiDocSkip.refDoc(((PsiMethod) resolve));
            }
            if (resolve instanceof PsiDocCommentOwner) {
                return OwnerToPsiDocSkip.refDoc(((PsiDocCommentOwner) resolve));
            }
        }
        return null;
    }
}
