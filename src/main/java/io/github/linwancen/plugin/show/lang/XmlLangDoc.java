package io.github.linwancen.plugin.show.lang;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.lang.base.PsiUnSaveUtils;
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

    @Override
    public @Nullable <T extends SettingsInfo> String treeDoc(@NotNull T info, @NotNull ProjectViewNode<?> node,
                                                             @NotNull Project project) {
        @Nullable VirtualFile virtualFile = node.getVirtualFile();
        if (virtualFile == null || virtualFile.isDirectory()) {
            return null;
        }
        @Nullable FileViewProvider viewProvider = PsiManager.getInstance(project).findViewProvider(virtualFile);
        if (viewProvider == null) {
            return null;
        }
        @Nullable PsiElement psiElement = viewProvider.findElementAt(0);
        if (psiElement == null || !"<!--".equals(PsiUnSaveUtils.getText(psiElement))) {
            @Nullable Document document = viewProvider.getDocument();
            if (document == null) {
                return null;
            }
            int lineCount = document.getLineCount();
            // lineNumber start 0, as 1 <= 1 should return
            if (lineCount <= 1) {
                return null;
            }
            // because in html 1st line must <!DOCTYPE html>
            int i = document.getLineStartOffset(1);
            psiElement = viewProvider.findElementAt(i);
        }
        if (psiElement == null || !"<!--".equals(PsiUnSaveUtils.getText(psiElement))) {
            return null;
        }
        PsiElement parent = psiElement.getParent();
        if (!(parent instanceof PsiComment)) {
            return null;
        }
        @NotNull PsiElement[] children = parent.getChildren();
        if (children.length < 2) {
            return null;
        }
        @Nullable String doc = PsiUnSaveUtils.getText(children[1]);
        // Copyright or copyright
        //noinspection SpellCheckingInspection
        if (doc == null || doc.contains("opyright")) {
            return null;
        }
        XmlCache.indexDocToDirDoc(virtualFile, doc);
        return doc;
    }

    @Nullable
    @Override
    public String findRefDoc(@NotNull LineInfo info, @NotNull FileViewProvider viewProvider, @NotNull PsiElement element) {
        @NotNull ExtensionPointName<XmlLangDoc> epn = ExtensionPointName.create("io.github.linwancen.show-comment.xmlLangDoc");
        @NotNull List<XmlLangDoc> extensionList = epn.getExtensionList();
        for (@NotNull XmlLangDoc xmlLangDoc : extensionList) {
            @Nullable String doc = xmlLangDoc.findRefDoc(info, viewProvider, element);
            if (doc != null && !doc.trim().isEmpty()) {
                return doc;
            }
        }
        return null;
    }
}
