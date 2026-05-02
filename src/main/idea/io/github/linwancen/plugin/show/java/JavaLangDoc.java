package io.github.linwancen.plugin.show.java;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCall;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiJvmModifiersOwner;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.java.doc.NewDoc;
import io.github.linwancen.plugin.show.java.doc.PsiMethodToPsiDoc;
import io.github.linwancen.plugin.show.java.line.OwnerToPsiDocSkip;
import io.github.linwancen.plugin.show.java.line.SkipUtils;
import io.github.linwancen.plugin.show.java.resolve.AnnoDocJava;
import io.github.linwancen.plugin.show.java.resolve.EnumDoc;
import io.github.linwancen.plugin.show.java.resolve.InitDoc;
import io.github.linwancen.plugin.show.java.resolve.ParamDoc;
import io.github.linwancen.plugin.show.lang.base.BaseTagLangDoc;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import io.github.linwancen.plugin.show.lang.base.PsiUnSaveUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JavaLangDoc extends BaseTagLangDoc<PsiDocComment> {
    private static final Logger LOG = LoggerFactory.getLogger(JavaLangDoc.class);

    public static final JavaLangDoc INSTANCE = new JavaLangDoc();

    static {
        LANG_DOC_MAP.put(JavaLanguage.INSTANCE.getID(), INSTANCE);
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(PsiJavaCodeReferenceElement.class, PsiDocTagValue.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentJava;
    }

    @Override
    public @Nullable <T extends SettingsInfo> String treeDoc(@NotNull T info, ProjectViewNode<?> node,
                                                             @NotNull Project project) {
        return JavaTree.treeDoc(info, node, project);
    }

    @Override
    protected @Nullable String refDoc(@NotNull LineInfo info, @NotNull PsiElement ref) {
        if ("Override".equals(info.getText(ref))) {
            @Nullable PsiMethod psiMethod = PsiTreeUtil.getParentOfType(ref, PsiMethod.class);
            if (psiMethod == null) {
                return null;
            }
            // must supper
            @Nullable PsiDocComment psiDocComment = PsiMethodToPsiDoc.supperMethodDoc(psiMethod);
            return docElementToStr(info, psiDocComment);
        }
        if (info.appSettings.fromNew) {
            @Nullable PsiMethod resolve = NewDoc.newMethod(ref);
            if (resolve != null) {
                @Nullable String s = resolveDocPrint(info, resolve);
                if (s != null) {
                    return s;
                }
            }
        }
        return super.refDoc(info, ref);
    }

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocPrint(@NotNull T info, @NotNull PsiElement resolve) {
        @Nullable String doc = resolveDocPrintSrc(info, resolve);
        if (!info.appSettings.init || !(resolve instanceof PsiVariable)) {
            return doc;
        }
        @NotNull PsiVariable variable = (PsiVariable) resolve;
        @Nullable PsiExpression initializer = variable.getInitializer();
        if (initializer == null) {
            return doc;
        }
        if (info.appSettings.initRef) {
            try {
                if (initializer instanceof PsiReferenceExpression) {
                    @Nullable PsiElement r = ((PsiReferenceExpression) initializer).resolve();
                    if (r == null) {
                        return doc;
                    }
                    if (r instanceof PsiVariable) {
                        initializer = ((PsiVariable) r).getInitializer();
                    }
                    if (doc == null) {
                        // initializer doc
                        doc = resolveDocPrintSrc(info, r);
                    }
                } else if (doc == null && initializer instanceof PsiCall) {
                    PsiMethod psiMethod = ((PsiCall) initializer).resolveMethod();
                    if (psiMethod == null) {
                        return null;
                    }
                    return resolveDocPrintSrc(info, psiMethod);
                }
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Throwable ignore) {
            }
        }
        return InitDoc.initDoc(info, doc, initializer, variable);
    }

    private <T extends SettingsInfo> @Nullable String resolveDocPrintSrc(@NotNull T info, @NotNull PsiElement resolve) {
        if (SkipUtils.skipSign(info, resolve)) {
            return null;
        }
        @Nullable String resolveDocPrint = super.resolveDocPrint(info, resolve);
        if (resolveDocPrint != null) {
            return resolveDocPrint;
        }
        // no doc comment support get/set
        if (parseBaseComment(info) && resolve instanceof PsiMethod) {
            @Nullable PsiField psiField = PsiMethodToPsiDoc.propMethodField((PsiMethod) resolve);
            if (psiField != null) {
                return super.resolveDocPrint(info, psiField);
            }
        }
        if (info.appSettings.fieldBase && resolve instanceof PsiField) {
            @Nullable PsiField psiField = (PsiField) resolve;
            return resolveDocPrintBase(info, psiField);
        }
        if (info.appSettings.fromParam && resolve instanceof PsiParameter) {
            return ParamDoc.paramDoc((PsiParameter) resolve);
        } else if (info.appSettings.enumDoc && resolve instanceof PsiEnumConstant) {
            return EnumDoc.enumDoc((PsiEnumConstant) resolve);
        }
        if (resolve instanceof PsiJvmModifiersOwner) {
            return AnnoDocJava.INSTANCE.annoDoc(info, (PsiJvmModifiersOwner) resolve);
        }
        return null;
    }

    @Override
    protected <T extends SettingsInfo> boolean parseBaseComment(@NotNull T info) {
        return info.appSettings.showLineEndCommentJavaBase;
    }

    @Nullable
    @Override
    protected <T extends SettingsInfo> PsiDocComment toDocElement(@NotNull T info,
                                                                  @NotNull PsiElement resolve) {
        if (resolve instanceof PsiDocCommentOwner) {
            @NotNull PsiDocCommentOwner psiDocCommentOwner = (PsiDocCommentOwner) resolve;
            return OwnerToPsiDocSkip.refDoc(info, psiDocCommentOwner);
        }
        return null;
    }

    @NotNull
    @Override
    protected <T extends SettingsInfo> String descDoc(@NotNull T info, @NotNull PsiDocComment psiDocComment) {
        @NotNull PsiElement[] elements = psiDocComment.getDescriptionElements();
        return elementsDesc(info, elements);
    }

    @NotNull
    protected <T extends SettingsInfo> String elementsDesc(@NotNull T info, @NotNull PsiElement[] elements) {
        @NotNull StringBuilder sb = new StringBuilder();
        int lineCount = 0;
        for (PsiElement element : elements) {
            if (appendElementText(sb, element)) {
                lineCount++;
            }
            if (DocFilter.lineCountOrLenOver(info, sb, lineCount)) {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * @return is a new line
     */
    protected boolean appendElementText(@NotNull StringBuilder sb, PsiElement element) {
        if (element instanceof PsiWhiteSpace && sb.length() > 0) {
            return true;
        }
        @NotNull PsiElement[] children = element.getChildren();
        if (children.length > 0) {
            if (children.length >= 3) {
                DocFilter.addHtml(sb, PsiUnSaveUtils.getText(children[children.length - 2]));
            }
            return false;
        }
        DocFilter.addHtml(sb, PsiUnSaveUtils.getText(element));
        return false;
    }

    @Override
    protected <T extends SettingsInfo> void appendTag(@NotNull T info, @NotNull StringBuilder tagStrBuilder,
                                                      @NotNull PsiDocComment psiDocComment, @NotNull String name) {
        staticAppendTag(tagStrBuilder, psiDocComment, name);
    }

    public static void staticAppendTag(@NotNull StringBuilder tagStrBuilder,
                                       @NotNull PsiDocComment psiDocComment, @NotNull String name) {
        @NotNull PsiDocTag[] tags = psiDocComment.findTagsByName(name);
        for (@NotNull PsiDocTag tag : tags) {
            @Nullable PsiDocTagValue value = tag.getValueElement();
            if (value != null) {
                DocFilter.addHtml(tagStrBuilder, PsiUnSaveUtils.getText(value));
            } else {
                @NotNull PsiElement[] dataElements = tag.getDataElements();
                if (dataElements.length > 0) {
                    DocFilter.addHtml(tagStrBuilder, PsiUnSaveUtils.getText(dataElements[0]));
                }
            }
        }
    }
}
