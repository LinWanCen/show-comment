package io.github.linwancen.plugin.show.ext.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import io.github.linwancen.plugin.show.settings.ShowBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * create package-info.java or show_comment_plugin.tree.tsv
 * <br>on ProjectViewPopupMenu
 */
public class CreateExtDocAction extends AnAction {

    private static final Logger LOG = LoggerFactory.getLogger(CreateExtDocAction.class);

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(ShowBundle.message("create.ext.doc"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            createExtDoc(event);
        } catch (Throwable e) {
            LOG.info("CreateExtDocAction catch Throwable but log to record.", e);
        }
    }

    private static void createExtDoc(@NotNull AnActionEvent event) {
        @Nullable Project project = event.getProject();
        if (project == null) {
            return;
        }
        VirtualFile[] files = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (files == null || files.length == 0) {
            return;
        }
        DumbService.getInstance(project).runWhenSmart(() -> {
            if (files.length == 1) {
                VirtualFile file = files[0];
                if (file.isDirectory() && hasJavaOrKtFiles(file)) {
                    createPackageInfoJava(project, file);
                    return;
                }
            }
            createTreeTsvForSelection(project, files);
        });
    }

    private static boolean hasJavaOrKtFiles(@NotNull VirtualFile dir) {
        boolean[] found = {false};
        VfsUtil.visitChildrenRecursively(dir, new VirtualFileVisitor<Void>() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (!file.isDirectory()) {
                    String ext = file.getExtension();
                    if ("java".equals(ext) || "kt".equals(ext)) {
                        found[0] = true;
                    }
                }
                return !found[0];
            }
        });
        return found[0];
    }

    private static void createPackageInfoJava(@NotNull Project project, @NotNull VirtualFile dir) {
        ApplicationManager.getApplication().invokeLater(() -> {
            VirtualFile existing = dir.findChild("package-info.java");
            if (existing != null) {
                openAndMoveToOffset(project, existing);
                return;
            }
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    @NotNull String packageName = resolvePackageName(project, dir);
                    // language="java"
                    @NotNull String content = "/**\n *\n */\npackage " + packageName + ";\n";
                    VirtualFile vf = dir.createChildData(project, "package-info.java");
                    vf.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                    openAndMoveToOffset(project, vf);
                } catch (IOException e) {
                    LOG.info("Create package-info.java failed.", e);
                }
            });
        });
    }

    private static void openAndMoveToOffset(@NotNull Project project, @NotNull VirtualFile vf) {
        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
        Editor editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
        if (editor == null) return;

        disableDocRendering(editor);

        @NotNull String text = editor.getDocument().getText();
        String ls = text.contains("\r\n") ? "\r\n" : "\n";

        String multiPattern = "/**" + ls + " * ";
        int multiIdx = text.indexOf(multiPattern);
        if (multiIdx >= 0) {
            editor.getCaretModel().moveToOffset(multiIdx + 6 + ls.length());
            return;
        }

        String multiEmptyPattern = "/**" + ls + " *" + ls;
        int multiEmptyIdx = text.indexOf(multiEmptyPattern);
        if (multiEmptyIdx >= 0) {
            int insertOffset = multiEmptyIdx + 3 + ls.length() + 2;
            WriteCommandAction.runWriteCommandAction(project, () -> {
                editor.getDocument().insertString(insertOffset, " ");
                editor.getCaretModel().moveToOffset(insertOffset + 1);
            });
            return;
        }

        int singleIdx = text.indexOf("/** ");
        if (singleIdx >= 0) {
            editor.getCaretModel().moveToOffset(singleIdx + 4);
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String comment = "/**" + ls + " * " + ls + " */" + ls;
            editor.getDocument().insertString(0, comment);
            editor.getCaretModel().moveToOffset(6 + ls.length());
        });
    }

    /**
     * Disable Render Doc Comments to show raw text, so cursor can move into comment.
     * Use reflection for compatibility with IDEs that may not have DocRenderManager.
     */
    private static void disableDocRendering(@NotNull Editor editor) {
        try {
            Class<?> clazz = Class.forName(
                    "com.intellij.codeInsight.documentation.render.DocRenderManager");
            Method method = clazz.getMethod("setDocRenderingEnabled", Editor.class, boolean.class);
            method.invoke(null, editor, false);
        } catch (Exception ignore) {
            // fallback: expand existing fold regions
            editor.getFoldingModel().runBatchFoldingOperation(() -> {
                for (FoldRegion region : editor.getFoldingModel().getAllFoldRegions()) {
                    if (!region.isExpanded()) {
                        region.setExpanded(true);
                    }
                }
            });
        }
    }

    @NotNull
    private static String resolvePackageName(@NotNull Project project, @NotNull VirtualFile dir) {
        try {
            ProjectRootManager prm = ProjectRootManager.getInstance(project);
            for (VirtualFile root : prm.getContentSourceRoots()) {
                String rootPath = root.getPath();
                String dirPath = dir.getPath();
                if (dirPath.startsWith(rootPath)) {
                    String relPath = dirPath.substring(rootPath.length());
                    if (relPath.startsWith("/")) {
                        relPath = relPath.substring(1);
                    }
                    return relPath.replace('/', '.');
                }
            }
        } catch (Exception e) {
            LOG.info("Resolve package name from source root failed.", e);
        }
        return "";
    }

    private static void createTreeTsvForSelection(@NotNull Project project, @NotNull VirtualFile[] files) {
        @Nullable VirtualFile dir = commonParent(files);
        if (dir == null) {
            return;
        }
        @NotNull StringBuilder sb = new StringBuilder();
        for (VirtualFile f : files) {
            sb.append(f.getName()).append("\t\n");
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            @Nullable VirtualFile existing = findTreeTsv(dir);
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    VirtualFile vf;
                    if (existing != null) {
                        sb.insert(0, "\n");
                        sb.insert(0, new String(existing.contentsToByteArray(), StandardCharsets.UTF_8));
                        vf = existing;
                    } else {
                        vf = dir.createChildData(project, "show_comment_plugin.tree.tsv");
                    }
                    vf.setBinaryContent(sb.toString().getBytes(StandardCharsets.UTF_8));
                    FileEditorManager.getInstance(project).openFile(vf, true);
                } catch (IOException e) {
                    LOG.info("Write .tree.tsv failed.", e);
                }
            });
        });
    }

    @Nullable
    private static VirtualFile findTreeTsv(@NotNull VirtualFile dir) {
        VirtualFile[] children = dir.getChildren();
        if (children == null) return null;
        for (VirtualFile child : children) {
            if (!child.isDirectory() && child.getName().endsWith(".tree.tsv")) {
                return child;
            }
        }
        return null;
    }

    @Nullable
    private static VirtualFile commonParent(@NotNull VirtualFile[] files) {
        VirtualFile common = null;
        for (VirtualFile f : files) {
            @Nullable VirtualFile cur = f.getParent();
            if (cur == null) {
                return null;
            }
            if (common == null) {
                common = cur;
            } else {
                String curPath = cur.getPath();
                String commonPath = common.getPath();
                while (!curPath.equals(commonPath) && !curPath.startsWith(commonPath + "/")) {
                    common = common.getParent();
                    if (common == null) {
                        return null;
                    }
                    commonPath = common.getPath();
                }
            }
        }
        return common;
    }
}
