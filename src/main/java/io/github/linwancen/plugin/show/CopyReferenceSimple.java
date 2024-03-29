package io.github.linwancen.plugin.show;

import com.intellij.ide.actions.CopyReferenceAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import io.github.linwancen.plugin.show.settings.ShowBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

public class CopyReferenceSimple extends CopyReferenceAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(ShowBundle.message("copy.class.method.or.file.line"));
    }

    private static final Pattern QUALIFIED_PATTERN = Pattern.compile("[\\w.]+\\.");

    @Nullable
    @Override
    protected String getQualifiedName(@NotNull Editor editor, List elements) {
        try {
            return simpleName(editor, elements);
        } catch (Exception e) {
            return e.toString();
        }
    }

    @Nullable
    private String simpleName(@NotNull Editor editor, List elements) {
        // because 2nd param is List<PsiElement> in 2020.1 and List<? extends PsiElement> in new version
        //noinspection unchecked
        String qualifiedName = super.getQualifiedName(editor, elements);
        if (qualifiedName == null) {
            @NotNull Document document = editor.getDocument();
            @Nullable Project project = editor.getProject();
            if (project == null) {
                return null;
            }
            @Nullable PsiFile file = PsiDocumentManager.getInstance(project).getCachedPsiFile(document);
            if (file != null) {
                // getFileFqn(file) => file.getName()
                return file.getName() + ":" + (editor.getCaretModel().getLogicalPosition().line + 1);
            }
            return null;
        }
        int i = qualifiedName.indexOf("(");
        if (i > 0) {
            return QUALIFIED_PATTERN.matcher(qualifiedName).replaceAll("").replace('#', '.');
        }
        i = qualifiedName.lastIndexOf(".");
        if (i > 0) {
            return qualifiedName.substring(i + 1).replace('#', '.');
        }
        return qualifiedName;
    }
}
