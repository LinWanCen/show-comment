package io.github.linwancen.plugin.show.lang;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class XmlLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(XMLLanguage.INSTANCE.getID(), new XmlLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(XmlAttribute.class, XmlTag.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return true;
    }

    @Nullable
    @Override
    public String findRefDoc(@NotNull LineInfo info, @NotNull FileViewProvider viewProvider, @NotNull PsiElement element) {
        ExtensionPointName<XmlLangDoc> epn = ExtensionPointName.create("io.github.linwancen.show-comment.xmlLangDoc");
        List<XmlLangDoc> extensionList = epn.getExtensionList();
        for (XmlLangDoc xmlLangDoc : extensionList) {
            String doc = xmlLangDoc.findRefDoc(info, viewProvider, element);
            if (doc != null && !doc.trim().isEmpty()) {
                return doc;
            }
        }
        return null;
    }
}
