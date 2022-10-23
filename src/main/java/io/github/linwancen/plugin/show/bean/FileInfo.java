package io.github.linwancen.plugin.show.bean;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileInfo extends SettingsInfo {
    public final @NotNull VirtualFile file;
    public final @NotNull Document document;
    public final @NotNull Project project;
    public final @NotNull FileViewProvider viewProvider;

    protected FileInfo(@NotNull VirtualFile file, @NotNull Document document, @NotNull Project project,
                       @NotNull FileViewProvider viewProvider, @NotNull FuncEnum funcEnum) {
        super(project, funcEnum);
        this.project = project;
        this.file = file;
        this.viewProvider = viewProvider;
        this.document = document;
    }

    public static @Nullable FileInfo of(@NotNull VirtualFile file, @NotNull Project project){
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return null;
        }
        FileViewProvider viewProvider = PsiManager.getInstance(project).findViewProvider(file);
        if (viewProvider == null) {
            return null;
        }
        return new FileInfo(file, document, project, viewProvider, FuncEnum.LINE);
    }

    public static @Nullable FileInfo of(@NotNull AnActionEvent event){
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return null;
        }
        FileViewProvider viewProvider = psiFile.getViewProvider();
        Document document = viewProvider.getDocument();
        if (document == null) {
            return null;
        }
        VirtualFile file = viewProvider.getVirtualFile();
        Project project = psiFile.getProject();
        return new FileInfo(file, document, project, viewProvider, FuncEnum.LINE);
    }
}
