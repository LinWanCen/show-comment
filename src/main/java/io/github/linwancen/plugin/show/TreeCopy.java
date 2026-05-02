package io.github.linwancen.plugin.show;

import com.intellij.ide.actions.CopyReferenceAction;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiQualifiedNamedElement;
import io.github.linwancen.plugin.show.settings.ShowBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;

/**
 * on ProjectViewPopupMenu
 */
public class TreeCopy extends CopyReferenceAction {

    private static final Logger LOG = LoggerFactory.getLogger(TreeCopy.class);

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(ShowBundle.message("tree.copy"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            copyFileListWithDoc(event);
        } catch (Throwable e) {
            LOG.info("TreeCopy catch Throwable but log to record.", e);
        }
    }

    private void copyFileListWithDoc(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        Navigatable[] arr = event.getData(PlatformDataKeys.NAVIGATABLE_ARRAY);
        if (arr == null) {
            return;
        }

        new Task.Backgroundable(project, "Show TreeCopy " + arr.length) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                DumbService.getInstance(project).runReadActionInSmartMode(() ->
                        ApplicationManager.getApplication().runReadAction(() -> {
                            @NotNull StringBuilder sb = new StringBuilder();
                            int totalNodes = arr.length;
                            InputEvent inputEvent = event.getInputEvent();
                            for (int i = 0; i < totalNodes; i++) {
                                Navigatable navigatable = arr[i];
                                if (!(navigatable instanceof ProjectViewNode)) {
                                    continue;
                                }
                                ProjectViewNode<?> node = (ProjectViewNode<?>) navigatable;
                                indicator.setText2((i + 1) + " / " + totalNodes);
                                indicator.setFraction(1.0 * i / totalNodes);
                                String name = toName(inputEvent, node);
                                sb.append(name);
                                @Nullable String comment = Tree.treeDoc(node, project);
                                if (comment != null && !comment.isBlank()) {
                                    sb.append("\t").append(comment);
                                }
                                sb.append("\n");
                            }
                            @NotNull StringSelection content = new StringSelection(sb.toString());
                            CopyPasteManager.getInstance().setContents(content);
                        }));
            }
        }.queue();
    }

    private static String toName(InputEvent inputEvent, ProjectViewNode<?> node) {
        if (!inputEvent.isControlDown()) {
            return node.getName();
        }
        // Ctrl
        if (!inputEvent.isShiftDown()) {
            VirtualFile file = node.getVirtualFile();
            return file != null ? file.getName() : node.getName();
        }
        // Ctrl + Shift
        if (!inputEvent.isAltDown()) {
            VirtualFile file = node.getVirtualFile();
            return file != null ? file.getPath() : node.getName();
        }
        // Ctrl + Shift + Alt
        Object value = node.getValue();
        if (value instanceof PsiQualifiedNamedElement) {
            PsiQualifiedNamedElement psiElement = (PsiQualifiedNamedElement) value;
            String qualifiedName = psiElement.getQualifiedName();
            return qualifiedName != null ? qualifiedName : node.getName();
        }
        return node.getName();
    }
}
