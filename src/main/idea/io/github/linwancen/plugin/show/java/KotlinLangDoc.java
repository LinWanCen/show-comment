package io.github.linwancen.plugin.show.java;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiPackageBase;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.java.resolve.AnnoDocKt;
import io.github.linwancen.plugin.show.lang.base.BaseTagLangDoc;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocName;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag;
import org.jetbrains.kotlin.psi.KtAnnotated;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedDeclaration;
import org.jetbrains.kotlin.psi.KtNameReferenceExpression;

import java.util.List;

public class KotlinLangDoc extends BaseTagLangDoc<KDocSection> {

    public static final KotlinLangDoc INSTANCE = new KotlinLangDoc();

    static {
        LANG_DOC_MAP.put("kotlin", INSTANCE);
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(KtNameReferenceExpression.class, KDocName.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentKotlin;
    }

    @Override
    public @Nullable <T extends SettingsInfo> String treeDoc(@NotNull T info, ProjectViewNode<?> node,
                                                             @NotNull Project project) {
        @Nullable KtFile ktFile = ktFile(node, project);
        if (ktFile == null) {
            return null;
        }
        @NotNull String fileName = fileNameWithoutExtension(ktFile);
        @Nullable String firstDoc = null;
        for (@NotNull KtDeclaration declaration : ktFile.getDeclarations()) {
            if (!(declaration instanceof KtNamedDeclaration)) {
                continue;
            }
            @Nullable String name = ((KtNamedDeclaration) declaration).getName();
            @Nullable String doc = resolveDocPrint(info, declaration);
            if (doc == null) {
                continue;
            }
            if (fileName.equals(name) || fileName.equalsIgnoreCase(name)) {
                return doc;
            }
            if (firstDoc == null) {
                firstDoc = doc;
            }
        }
        return firstDoc;
    }

    @Nullable
    private static KtFile ktFile(@NotNull ProjectViewNode<?> node, @NotNull Project project) {
        Object value = node.getValue();
        if (value instanceof KtFile) {
            return (KtFile) value;
        }
        if (value instanceof PsiFile && !(value instanceof KtFile)) {
            return null;
        }
        @Nullable VirtualFile virtualFile = node.getVirtualFile();
        if (virtualFile == null || !virtualFile.isValid() || virtualFile.isDirectory()) {
            return null;
        }
        @Nullable PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile instanceof KtFile) {
            return (KtFile) psiFile;
        }
        return null;
    }

    @NotNull
    private static String fileNameWithoutExtension(@NotNull PsiFile psiFile) {
        @NotNull String fileName = psiFile.getName();
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    @Override
    public @Nullable <T extends SettingsInfo> String resolveDocPrint(@NotNull T info, @NotNull PsiElement resolve) {
        @Nullable String resolveDocPrint = super.resolveDocPrint(info, resolve);
        if (resolveDocPrint != null) {
            return resolveDocPrint;
        }
        if (resolve instanceof KtAnnotated) {
            return AnnoDocKt.INSTANCE.annoDoc(info, (KtAnnotated) resolve);
        }
        return null;
    }

    @Override
    protected <T extends SettingsInfo> boolean parseBaseComment(@NotNull T info) {
        return info.appSettings.showLineEndCommentKotlinBase;
    }

    @Override
    @Nullable
    protected <T extends SettingsInfo> KDocSection toDocElement(@NotNull T info, @NotNull PsiElement resolve) {
        if (resolve instanceof PsiPackageBase) {
            return null;
        }
        @Nullable KDoc kDoc = PsiTreeUtil.getChildOfType(resolve, KDoc.class);
        if (kDoc == null) {
            return null;
        }
        return kDoc.getDefaultSection();
    }

    @NotNull
    @Override
    protected <T extends SettingsInfo> String descDoc(@NotNull T info, @NotNull KDocSection kDocSection) {
        @NotNull String content = kDocSection.getContent();
        return DocFilter.cutDoc(content, info, false);
    }

    @Override
    protected <T extends SettingsInfo> void appendTag(@NotNull T info, @NotNull StringBuilder tagStrBuilder,
                                                      @NotNull KDocSection kDocSection, @NotNull String name) {
        @NotNull List<KDocTag> tags = kDocSection.findTagsByName(name);
        for (@NotNull KDocTag tag : tags) {
            @NotNull String content = tag.getContent();
            @NotNull String cutDoc = DocFilter.cutDoc(content, info, false);
            tagStrBuilder.append(cutDoc);
        }
    }
}
