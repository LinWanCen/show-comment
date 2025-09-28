package io.github.linwancen.plugin.show.lang.base;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiUnSaveUtils {

    @Contract("!null -> !null")
    @Nullable
    public static String getText(@Nullable PsiElement element) {
        try {
            if (element == null) {
                return null;
            }
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(element.getProject());
            if (documentManager == null) {
                return element.getText();
            }
            if (element instanceof PsiFile) {
                @Nullable Document document = documentManager.getDocument(((PsiFile) element));
                if (document == null) {
                    return element.getText();
                }
                return document.getText();
            }
            PsiFile containingFile = element.getContainingFile();
            if (containingFile == null) {
                return element.getText();
            }
            @Nullable Document document = documentManager.getDocument(containingFile);
            if (document == null) {
                return element.getText();
            }
            return document.getText(element.getTextRange());
        } catch (Exception ignored) {
            return element.getText();
        }
    }

    @Nullable
    public static String fileText(@Nullable Project project, @NotNull VirtualFile file) {
        @Nullable Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
            return document.getText();
        }
        if (project == null) {
            return null;
        }
        @Nullable PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {
            return psiFile.getText();
        }
        return null;
    }
}
