package io.github.linwancen.plugin.show.bean;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileInfo extends SettingsInfo {
    public final @NotNull VirtualFile file;
    public final @NotNull Document document;
    public final @NotNull Project project;
    public final @NotNull InjectedLanguageManager inject;

    protected FileInfo(@NotNull VirtualFile file, @NotNull Document document, @NotNull Project project,
                       @NotNull FuncEnum funcEnum) {
        super(project, funcEnum);
        this.project = project;
        this.file = file;
        this.document = document;
        this.inject = InjectedLanguageManager.getInstance(project);
    }

    public static @Nullable FileInfo of(@NotNull VirtualFile file, @NotNull Project project) {
        @Nullable Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return null;
        }
        return new FileInfo(file, document, project, FuncEnum.LINE);
    }

    public static @Nullable FileInfo of(@NotNull AnActionEvent event) {
        @Nullable PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return null;
        }
        return of(psiFile);
    }

    public static @Nullable FileInfo of(@NotNull PsiFile psiFile) {
        @NotNull FileViewProvider viewProvider = psiFile.getViewProvider();
        @Nullable Document document = viewProvider.getDocument();
        if (document == null) {
            return null;
        }
        @NotNull VirtualFile file = viewProvider.getVirtualFile();
        @NotNull Project project = psiFile.getProject();
        return new FileInfo(file, document, project, FuncEnum.LINE);
    }

    public String getText(PsiElement element) {
        try {
            TextRange range = element.getTextRange();
            int startOffset = range.getStartOffset();
            int endOffset = range.getEndOffset();
            return document.getText().substring(startOffset, endOffset);
        } catch (Exception ignored) {
            return element.getText();
        }
    }
}
