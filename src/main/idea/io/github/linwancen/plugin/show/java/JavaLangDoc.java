package io.github.linwancen.plugin.show.java;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.*;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.java.doc.AnnoDoc;
import io.github.linwancen.plugin.show.java.doc.EnumDoc;
import io.github.linwancen.plugin.show.java.doc.OwnerToPsiDocUtils;
import io.github.linwancen.plugin.show.java.doc.ParamDoc;
import io.github.linwancen.plugin.show.java.doc.PsiMethodToPsiDoc;
import io.github.linwancen.plugin.show.java.line.OwnerToPsiDocSkip;
import io.github.linwancen.plugin.show.lang.base.BaseTagLangDoc;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaLangDoc extends BaseTagLangDoc<PsiDocComment> {

    public static final JavaLangDoc INSTANCE = new JavaLangDoc();

    static {
        LANG_DOC_MAP.put(JavaLanguage.INSTANCE.getID(), INSTANCE);
    }

    @Override
    public @NotNull Class<? extends PsiElement> getRefClass() {
        return PsiJavaCodeReferenceElement.class;
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
        if ("Override".equals(ref.getText())) {
            @Nullable PsiMethod psiMethod = PsiTreeUtil.getParentOfType(ref, PsiMethod.class);
            if (psiMethod == null) {
                return null;
            }
            // must supper
            @Nullable PsiDocComment psiDocComment = OwnerToPsiDocUtils.supperMethodDoc(psiMethod);
            return docElementToStr(info, psiDocComment);
        }
        if (info.appSettings.fromNew) {
            PsiElement parent = ref.getParent();
            if (parent instanceof PsiNewExpression) {
                @NotNull PsiNewExpression psiNewExpression = (PsiNewExpression) parent;
                try {
                    @Nullable PsiMethod resolve = psiNewExpression.resolveMethod();
                    if (resolve != null) {
                        return resolveDocPrint(info, resolve);
                    }
                } catch (Throwable ignore) {
                    // ignore
                }
            }
        }
        return super.refDoc(info, ref);
    }

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocPrint(@NotNull T info, @NotNull PsiElement resolve) {
        @Nullable String resolveDocPrint = super.resolveDocPrint(info, resolve);
        if (resolveDocPrint != null) {
            return resolveDocPrint;
        }
        // no doc comment support get set
        if (parseBaseComment(info) && resolve instanceof PsiMethod) {
            @Nullable PsiField psiField = PsiMethodToPsiDoc.propMethodField((PsiMethod) resolve);
            if (psiField != null) {
                return super.resolveDocPrint(info, psiField);
            }
        }
        if (info.appSettings.fromParam && resolve instanceof PsiParameter) {
            return ParamDoc.paramDoc((PsiParameter) resolve);
        } else if (info.appSettings.enumDoc && resolve instanceof PsiEnumConstant) {
            return EnumDoc.enumDoc((PsiEnumConstant) resolve);
        }
        if (resolve instanceof PsiJvmModifiersOwner) {
            return AnnoDoc.annoDoc(info, (PsiJvmModifiersOwner) resolve);
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
        @NotNull StringBuilder sb = new StringBuilder();
        int lineCount = 0;
        @NotNull PsiElement[] elements = psiDocComment.getDescriptionElements();
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
     * @return is new line
     */
    private static boolean appendElementText(@NotNull StringBuilder sb, PsiElement element) {
        if (element instanceof PsiDocToken) {
            @NotNull PsiDocToken psiDocToken = (PsiDocToken) element;
            DocFilter.addHtml(sb, psiDocToken.getText());
        }
        if (element instanceof PsiInlineDocTag) {
            @NotNull PsiInlineDocTag psiInlineDocTag = (PsiInlineDocTag) element;
            @NotNull PsiElement[] children = psiInlineDocTag.getChildren();
            if (children.length >= 3) {
                DocFilter.addHtml(sb, children[children.length - 2].getText());
            }
        }
        return element instanceof PsiWhiteSpace && sb.length() > 0;
    }

    @Override
    protected <T extends SettingsInfo> void appendTag(@NotNull T info, @NotNull StringBuilder tagStrBuilder,
                                                      @NotNull PsiDocComment psiDocComment, @NotNull String name) {
        @NotNull PsiDocTag[] tags = psiDocComment.findTagsByName(name);
        for (@NotNull PsiDocTag tag : tags) {
            // @see @param should use getDataElements()
            @Nullable PsiDocTagValue value = tag.getValueElement();
            if (value != null) {
                DocFilter.addHtml(tagStrBuilder, value.getText());
            }
        }
    }
}
