package io.github.linwancen.plugin.show.ext.conf;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * call ConfCache.clearAll
 * <br>Use Reset only for file sort
 */
public class ResetExtDocAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConfCache.clearAll();
    }
}
