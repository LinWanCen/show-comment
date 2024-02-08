package io.github.linwancen.plugin.show.lang;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseTagLangDoc;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhpLangDoc extends BaseTagLangDoc<PhpDocComment> {

    static {
        LANG_DOC_MAP.put(PhpLanguage.INSTANCE.getID(), new PhpLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(PhpReference.class, PhpDocRef.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentPhp;
    }

    @Override
    protected <T extends SettingsInfo> boolean parseBaseComment(@NotNull T info) {
        return info.appSettings.showLineEndCommentPhpBase;
    }

    @Nullable
    @Override
    protected <T extends SettingsInfo> PhpDocComment toDocElement(@NotNull T info, @NotNull PsiElement resolve) {
        if (resolve instanceof PhpNamedElement) {
            @NotNull PhpNamedElement phpNamedElement = (PhpNamedElement) resolve;
            return phpNamedElement.getDocComment();
        }
        return null;
    }

    @NotNull
    @Override
    protected <T extends SettingsInfo> String descDoc(@NotNull T info, @NotNull PhpDocComment phpDocComment) {
        String text = phpDocComment.getText();
        return DocFilter.cutDoc(text, info, true);
    }

    @Override
    protected <T extends SettingsInfo> void appendTag(@NotNull T info, @NotNull StringBuilder tagStrBuilder,
                                                      @NotNull PhpDocComment phpDocComment, @NotNull String name) {
        @Nullable PhpDocTag[] tags = PsiTreeUtil.getChildrenOfType(phpDocComment, PhpDocTag.class);
        if (tags == null) {
            return;
        }
        for (@NotNull PhpDocTag tag : tags) {
            if (("@" + name).equals(tag.getName())) {
                tagStrBuilder.append(tag.getTagValue());
            }
        }
    }
}
