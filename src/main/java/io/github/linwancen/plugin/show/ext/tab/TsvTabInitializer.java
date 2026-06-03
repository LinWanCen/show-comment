package io.github.linwancen.plugin.show.ext.tab;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class TsvTabInitializer implements StartupActivity {
    
    @Override
    public void runActivity(@NotNull Project project) {
        initTsvTabHandler();
    }

    public static void initTsvTabHandler() {
        ApplicationManager.getApplication().invokeLater(() -> {
            EditorActionManager actionManager = EditorActionManager.getInstance();
            EditorActionHandler originalHandler = actionManager.getActionHandler(IdeActions.ACTION_EDITOR_TAB);
            if (!(originalHandler instanceof TsvTabHandler)) {
                TsvTabHandler tsvTabHandler = new TsvTabHandler(originalHandler);
                actionManager.setActionHandler(IdeActions.ACTION_EDITOR_TAB, tsvTabHandler);
            }
        });
    }
}
