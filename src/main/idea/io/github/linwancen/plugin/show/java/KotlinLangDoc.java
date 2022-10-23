package io.github.linwancen.plugin.show.java;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.file.PsiPackageBase;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseTagLangDoc;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.kdoc.psi.api.KDoc;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection;
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag;
import org.jetbrains.kotlin.psi.KtNameReferenceExpression;

import java.util.List;

public class KotlinLangDoc extends BaseTagLangDoc<KDocSection> {

    static {
        LANG_DOC_MAP.put(KotlinLanguage.INSTANCE.getID(), new KotlinLangDoc());
    }

    @Override
    public @NotNull Class<? extends PsiElement> getRefClass() {
        return KtNameReferenceExpression.class;
    }

    @Override
    public boolean show(@NotNull LineInfo lineInfo) {
        return lineInfo.appSettings.showLineEndCommentKotlin;
    }

    @Override
    @Nullable
    protected KDocSection toDocElement(@NotNull PsiElement resolve) {
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
    protected <T extends SettingsInfo> String descDoc(@NotNull T lineInfo, @NotNull KDocSection kDocSection) {
        @NotNull String content = kDocSection.getContent();
        return DocFilter.cutDoc(content, lineInfo.appSettings, false);
    }

    @Override
    protected <T extends SettingsInfo> void appendTag(@NotNull T lineInfo, @NotNull StringBuilder tagStrBuilder,
                                                      @NotNull KDocSection kDocSection, @NotNull String name) {
        @NotNull List<KDocTag> tags = kDocSection.findTagsByName(name);
        for (@NotNull KDocTag tag : tags) {
            @NotNull String content = tag.getContent();
            @NotNull String cutDoc = DocFilter.cutDoc(content, lineInfo.appSettings, false);
            tagStrBuilder.append(cutDoc);
        }
    }
}
