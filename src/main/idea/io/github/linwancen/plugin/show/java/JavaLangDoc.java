package io.github.linwancen.plugin.show.java;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.*;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.java.doc.OwnerToPsiDocUtils;
import io.github.linwancen.plugin.show.java.line.OwnerToPsiDocSkip;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
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
    public boolean show(@NotNull LineInfo lineInfo) {
        return lineInfo.appSettings.showLineEndCommentJava;
    }

    @Override
    public @Nullable <T extends SettingsInfo> String treeDoc(T settingsInfo, ProjectViewNode<?> node, Project project) {
        return JavaTree.treeDoc(settingsInfo, node, project);
    }

    @Override
    protected @Nullable <T extends SettingsInfo> String refDoc(@NotNull T lineInfo, @NotNull PsiElement ref) {
        if ("Override".equals(ref.getText())) {
            PsiMethod psiMethod = PsiTreeUtil.getParentOfType(ref, PsiMethod.class);
            if (psiMethod == null) {
                return null;
            }
            // must supper
            PsiDocComment psiDocComment = OwnerToPsiDocUtils.supperMethodDoc(psiMethod);
            return docElementToStr(lineInfo, psiDocComment);
        }
        if (lineInfo.appSettings.fromNew) {
            PsiElement parent = ref.getParent();
            if (parent instanceof PsiNewExpression) {
                PsiNewExpression psiNewExpression = (PsiNewExpression) parent;
                try {
                    PsiMethod resolve = psiNewExpression.resolveMethod();
                    if (resolve != null) {
                        return resolveDocPrint(lineInfo, resolve);
                    }
                } catch (Throwable ignore) {
                    // ignore
                }
            }
        }
        return super.refDoc(lineInfo, ref);
    }

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocPrint(@NotNull T lineInfo, @NotNull PsiElement resolve) {
        String resolveDocPrint = super.resolveDocPrint(lineInfo, resolve);
        if (resolveDocPrint != null) {
            return resolveDocPrint;
        }
        if (lineInfo.appSettings.fromParam && resolve instanceof PsiParameter) {
            return paramDoc((PsiParameter) resolve);
        }
        return null;
    }

    @Nullable
    private String paramDoc(@NotNull PsiParameter psiParameter) {
        PsiMethod method = PsiTreeUtil.getParentOfType(psiParameter, PsiMethod.class);
        if (method == null) {
            return null;
        }
        PsiDocComment psiDocComment = OwnerToPsiDocUtils.methodDoc(method);
        if (psiDocComment == null) {
            return null;
        }
        String name = psiParameter.getName();
        PsiDocTag[] params = psiDocComment.findTagsByName("param");
        for (PsiDocTag param : params) {
            PsiDocTagValue value = param.getValueElement();
            if (value != null && name.equals(value.getText())) {
                PsiElement[] dataElements = param.getDataElements();
                if (dataElements.length > 1) {
                    return dataElements[1].getText();
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    protected PsiDocComment toDocElement(@NotNull PsiElement resolve) {
        if (resolve instanceof PsiDocCommentOwner) {
            PsiDocCommentOwner psiDocCommentOwner = (PsiDocCommentOwner) resolve;
            return OwnerToPsiDocSkip.refDoc(psiDocCommentOwner);
        }
        return null;
    }

    @NotNull
    @Override
    protected <T extends SettingsInfo> String descDoc(@NotNull T lineInfo, @NotNull PsiDocComment psiDocComment) {
        StringBuilder sb = new StringBuilder();
        int lineCount = 0;
        PsiElement[] elements = psiDocComment.getDescriptionElements();
        for (PsiElement element : elements) {
            if (appendElementText(sb, element)) {
                lineCount++;
            }
            if (DocFilter.lineCountOrLenOver(lineInfo.appSettings, sb, lineCount)) {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * @return is new line
     */
    private static boolean appendElementText(StringBuilder sb, PsiElement element) {
        if (element instanceof PsiDocToken) {
            PsiDocToken psiDocToken = (PsiDocToken) element;
            DocFilter.addHtml(sb, psiDocToken.getText());
        }
        if (element instanceof PsiInlineDocTag) {
            PsiInlineDocTag psiInlineDocTag = (PsiInlineDocTag) element;
            PsiElement[] children = psiInlineDocTag.getChildren();
            if (children.length > 3) {
                DocFilter.addHtml(sb, children[3].getText());
            }
        }
        return element instanceof PsiWhiteSpace && sb.length() > 0;
    }

    @Override
    protected <T extends SettingsInfo> void appendTag(@NotNull T lineInfo, @NotNull StringBuilder tagStrBuilder,
                                                      @NotNull PsiDocComment psiDocComment, @NotNull String name) {
        PsiDocTag[] tags = psiDocComment.findTagsByName(name);
        for (PsiDocTag tag : tags) {
            // @see @param should use getDataElements()
            PsiDocTagValue value = tag.getValueElement();
            if (value != null) {
                DocFilter.addHtml(tagStrBuilder, value.getText());
            }
        }
    }
}
