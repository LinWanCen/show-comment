package io.github.linwancen.plugin.show.tree.first;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import io.github.linwancen.plugin.show.lang.base.PsiUnSaveUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirstDoc {

    private FirstDoc() {}

    @Nullable
    public static String firstDoc(ProjectViewNode<?> node, @NotNull SettingsInfo info) {
        if (!info.appSettings.treeFirst) {
            return null;
        }
        @Nullable VirtualFile virtualFile = node.getVirtualFile();
        if (virtualFile == null || virtualFile.isDirectory()) {
            return null;
        }
        Project project = node.getProject();
        if (project == null) {
            return null;
        }
        @Nullable FileViewProvider viewProvider = PsiManager.getInstance(project).findViewProvider(virtualFile);
        if (viewProvider == null) {
            return null;
        }
        @Nullable PsiElement psiElement = viewProvider.findElementAt(0);
        if (psiElement == null || notDoc(psiElement)) {
            @Nullable Document document = viewProvider.getDocument();
            if (document == null) {
                return null;
            }
            int lineCount = document.getLineCount();
            // lineNumber start 0, as 1 <= 1 should return
            if (lineCount <= 1) {
                return null;
            }
            // because in HTML 1st line must <!DOCTYPE HTML>
            int i = document.getLineStartOffset(1);
            psiElement = viewProvider.findElementAt(i);
            if (psiElement == null || notDoc(psiElement)) {
                return null;
            }
        }
        if (!(psiElement instanceof PsiComment)) {
            psiElement = psiElement.getParent();
            if (!(psiElement instanceof PsiComment)) {
                return null;
            }
        }
        @NotNull String doc = PsiUnSaveUtils.getText(psiElement);
        // Copyright or copyright
        //noinspection SpellCheckingInspection
        if (doc.contains("opyright")) {
            return null;
        }
        String cutDoc = DocFilter.cutDoc(doc, info, true);
        FirstDocToDirCache.indexDocToDirDoc(virtualFile, cutDoc);
        return cutDoc;
    }

    private static final Pattern COMMENT_PATTERN = Pattern.compile("<!--|/\\*|//|#(?!!)");
    private static boolean notDoc(@NotNull PsiElement psiElement) {
        String text = PsiUnSaveUtils.getText(psiElement);
        Matcher matcher = COMMENT_PATTERN.matcher(text);
        return !matcher.find();
    }
}
