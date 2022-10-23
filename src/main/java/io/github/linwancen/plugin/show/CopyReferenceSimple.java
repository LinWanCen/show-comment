package io.github.linwancen.plugin.show;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.CopyReferenceAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

public class CopyReferenceSimple extends CopyReferenceAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        String tip = IdeBundle.message("copy.reference");
        if (tip != null && tip.replace("\u001B", "").equals(e.getPresentation().getText())) {
            e.getPresentation().setText("Copy ClassName.MethodName");
        }
    }

    private static final Pattern QUALIFIED_PATTERN = Pattern.compile("[\\w.]+\\.");

    @Override
    protected String getQualifiedName(Editor editor, List<PsiElement> elements) {
        String qualifiedName = super.getQualifiedName(editor, elements);
        if (qualifiedName == null) {
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
