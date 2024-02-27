package io.github.linwancen.plugin.show.java;

import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.java.line.OwnerToPsiDocSkip;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.scala.ScalaLanguage;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression;
import org.jetbrains.plugins.scala.lang.scaladoc.psi.api.ScDocResolvableCodeReference;
import org.jetbrains.plugins.scala.lang.scaladoc.psi.api.ScDocTag;

import java.util.List;

public class ScalaLangDoc extends JavaLangDoc {

    public static final ScalaLangDoc INSTANCE = new ScalaLangDoc();

    static {
        LANG_DOC_MAP.put(ScalaLanguage.INSTANCE.getID(), INSTANCE);
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(ScReferenceExpression.class, ScDocResolvableCodeReference.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentScala;
    }

    @Override
    protected <T extends SettingsInfo> boolean parseBaseComment(@NotNull T info) {
        return info.appSettings.showLineEndCommentScalaBase;
    }

    @Nullable
    @Override
    protected <T extends SettingsInfo> PsiDocComment toDocElement(@NotNull T info,
                                                                  @NotNull PsiElement resolve) {
        if (resolve instanceof PsiDocCommentOwner) {
            @NotNull PsiDocCommentOwner psiDocCommentOwner = (PsiDocCommentOwner) resolve;
            return OwnerToPsiDocSkip.refDocWithOutSkip(info, psiDocCommentOwner);
        }
        return null;
    }

    @NotNull
    @Override
    protected <T extends SettingsInfo> String descDoc(@NotNull T info, @NotNull PsiDocComment psiDocComment) {
        @NotNull PsiElement[] elements = psiDocComment.getChildren();
        StringBuilder sb = new StringBuilder();
        for (PsiElement element : elements) {
            if (!(element instanceof PsiDocTag)) {
                sb.append(element.getText());
            }
        }
        return DocFilter.cutDoc(sb.toString(), info, true);
    }

    @Override
    protected <T extends SettingsInfo> void appendTag(@NotNull T info, @NotNull StringBuilder tagStrBuilder,
                                                      @NotNull PsiDocComment psiDocComment, @NotNull String name) {
        @NotNull PsiDocTag[] tags = psiDocComment.findTagsByName("@" + name);
        for (@NotNull PsiDocTag tag : tags) {
            if (tag instanceof ScDocTag) {
                ScDocTag scDocTag = (ScDocTag) tag;
                String doc = scDocTag.getCommentDataText();
                DocFilter.addHtml(tagStrBuilder, doc);
            }
        }
    }
}
