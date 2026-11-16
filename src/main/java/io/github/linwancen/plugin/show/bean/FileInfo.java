package io.github.linwancen.plugin.show.bean;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
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
    public final @NotNull InjectedLanguageManager inject;

    protected FileInfo(@NotNull VirtualFile file, @NotNull Document document, @NotNull Project project,
                       @NotNull FuncEnum funcEnum) {
        super(project, funcEnum);
        this.file = file;
        this.document = document;
        this.inject = InjectedLanguageManager.getInstance(project);
    }

    public static @NotNull FileInfo of(@NotNull VirtualFile file, @NotNull Document document, @NotNull Project project) {
        return new FileInfo(file, document, project, FuncEnum.LINE);
    }

    public static @Nullable FileInfo of(@NotNull VirtualFile file, @NotNull Project project) {
        @Nullable Document document = FileDocumentManager.getInstance().getCachedDocument(file);
        if (document == null) {
            try {
                document = ReadAction.compute(() -> FileDocumentManager.getInstance().getDocument(file));
            } catch (Throwable ignored) {
                // 在最新版 IDEA paint (UI渲染) 周期内禁止请求读写锁 (LockingForbiddenException)。
                // 此时若未命中缓存文档，不可且无法同步从磁盘加载，安全返回 null。
                return null;
            }
        }
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
        if (!file.isValid()) {
            return null;
        }
        @NotNull Project project = psiFile.getProject();
        return new FileInfo(file, document, project, FuncEnum.LINE);
    }

    public String getText(@NotNull PsiElement element) {
        try {
            TextRange range = element.getTextRange();
            return document.getText(range);
        } catch (Exception ignored) {
            return element.getText();
        }
    }
}
