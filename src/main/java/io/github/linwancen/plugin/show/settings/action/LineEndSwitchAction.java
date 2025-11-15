package io.github.linwancen.plugin.show.settings.action;

import com.intellij.ide.actions.CopyReferenceAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.linwancen.plugin.show.settings.AppSettingsState;
import io.github.linwancen.plugin.show.settings.ShowBundle;
import org.jetbrains.annotations.NotNull;

public class LineEndSwitchAction extends CopyReferenceAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(ShowBundle.message("show.line.end.comment"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        AppSettingsState.getInstance().showLineEndComment = !AppSettingsState.getInstance().showLineEndComment;
    }
}
