package io.github.linwancen.plugin.show.lang.vue;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment;
import com.intellij.lang.ecmascript6.psi.ES6ImportCall;
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding;
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.linwancen.plugin.show.ext.listener.FileLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * call ConfFactory, ConfCacheGetUtils
 */
public class VueRouterCache extends FileLoader {
    private static final Logger LOG = LoggerFactory.getLogger(VueRouterCache.class);

    public static final Map<VirtualFile, String> DOC_CACHE = new ConcurrentHashMap<>();

    @Nullable
    public static String fileDoc(@Nullable VirtualFile virtualFile) {
        if (virtualFile == null) {
            return null;
        }
        return DOC_CACHE.get(virtualFile);
    }

    @Override
    public void clearAll() {
        DOC_CACHE.clear();
    }

    @Override
    public void remove(@NotNull VirtualFile file, @Nullable String oldName) {
        if (oldName == null) {
            DOC_CACHE.remove(file);
        }
    }

    @Override
    public boolean skipFile(@NotNull VirtualFile file) {
        @Nullable String ext = file.getExtension();
        return !file.getPath().contains("src/router") && !"js".equals(ext) && !"ts".equals(ext);
    }

    @Override
    public void copyImpl(@NotNull VirtualFile file, @NotNull VirtualFile newFile) {
        String s = DOC_CACHE.get(file);
        if (s != null) {
            DOC_CACHE.put(newFile, s);
        }
    }

    @Override
    public void loadAllImpl(@NotNull Project project) {
        @NotNull Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(project,
                "package.json", GlobalSearchScope.projectScope(project));
        @NotNull StringBuilder sb = new StringBuilder();
        for (@NotNull VirtualFile file : files) {
            VirtualFile parent = file.getParent();
            if (parent == null) {
                continue;
            }
            @Nullable VirtualFile src = parent.findChild("src");
            if (src == null) {
                continue;
            }
            @Nullable VirtualFile router = src.findChild("router");
            if (router == null) {
                continue;
            }
            VfsUtil.visitChildrenRecursively(router, new VirtualFileVisitor<Void>() {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    if (file.isDirectory()) {
                        return super.visitFile(file);
                    }
                    loadFileImpl(file, project);
                    sb.append(file.getPath()).append("\n");
                    return super.visitFile(file);
                }
            });
        }
        if (files.isEmpty()) {
            return;
        }
        if (!project.isDisposed()) {
            ProjectView.getInstance(project).refresh();
        }
        LOG.info("Vue Router load all complete {} files\n{}", files.size(), sb);
    }

    @Override
    public void loadFileImpl(@NotNull VirtualFile file, @Nullable Project project) {
        if (project == null) {
            return;
        }
        @Nullable PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) {
            return;
        }
        @Nullable ES6ExportDefaultAssignment export = PsiTreeUtil.findChildOfType(psiFile,
                ES6ExportDefaultAssignment.class);
        if (export == null) {
            return;
        }
        @Nullable JSArrayLiteralExpression arr = PsiTreeUtil.findChildOfAnyType(export, JSArrayLiteralExpression.class);
        if (arr == null) {
            return;
        }
        parseArr(arr);
    }

    @Nullable
    private static VirtualFile parseArr(JSArrayLiteralExpression arr) {
        @Nullable VirtualFile virtualFile = null;
        @NotNull List<JSObjectLiteralExpression> list = PsiTreeUtil.getChildrenOfTypeAsList(arr,
                JSObjectLiteralExpression.class);
        for (@NotNull JSObjectLiteralExpression obj : list) {
            @Nullable String title = parseTitle(obj);
            @Nullable JSProperty children = obj.findProperty("children");
            if (children != null) {
                @Nullable JSExpression value = children.getValue();
                if (value instanceof JSArrayLiteralExpression) {
                    @NotNull JSArrayLiteralExpression childrenArr = (JSArrayLiteralExpression) value;
                    @Nullable VirtualFile subFile = parseArr(childrenArr);
                    // common component dir
                    if (subFile != null) {
                        VirtualFile file = subFile.getParent();
                        if (file != null && title != null) {
                            virtualFile = file;
                            DOC_CACHE.put(virtualFile, title);
                        }
                    }
                }
            }
            if (title != null) {
                @Nullable VirtualFile file = parseComponent(obj);
                if (file != null) {
                    virtualFile = file;
                    DOC_CACHE.put(virtualFile, title);
                    if ("index.vue".equals(virtualFile.getName())) {
                        virtualFile = virtualFile.getParent();
                        DOC_CACHE.put(virtualFile, title);
                    }
                }
            }
        }
        return virtualFile;
    }

    @Nullable
    private static String parseTitle(@NotNull JSObjectLiteralExpression obj) {
        @Nullable JSProperty meta = obj.findProperty("meta");
        if (meta == null) {
            return null;
        }
        @Nullable JSObjectLiteralExpression metaObj = meta.getObjectLiteralExpressionInitializer();
        if (metaObj == null) {
            return null;
        }
        @Nullable JSProperty titleProp = metaObj.findProperty("title");
        if (titleProp == null) {
            return null;
        }
        @Nullable JSExpression value = titleProp.getValue();
        if (value instanceof JSObjectLiteralExpression) {
            @NotNull JSObjectLiteralExpression i18n = (JSObjectLiteralExpression) value;
            // zh_CN
            @NotNull String lang = Locale.getDefault().toString();
            @Nullable JSProperty langProp = i18n.findProperty(lang);
            if (langProp == null) {
                return null;
            }
            value = langProp.getValue();
        }
        if (value instanceof JSLiteralExpression) {
            return ((JSLiteralExpression) value).getStringValue();
        }
        return null;
    }

    @Nullable
    private static VirtualFile parseComponent(@NotNull JSObjectLiteralExpression obj) {
        @Nullable JSProperty component = obj.findProperty("component");
        if (component == null) {
            return null;
        }
        @Nullable PsiElement value = component.getValue();
        if (value instanceof JSReferenceExpression) {
            @NotNull JSReferenceExpression ref = (JSReferenceExpression) value;
            @Nullable PsiElement resolve;
            try {
                resolve = ref.resolve();
            } catch (Throwable ignored) {
                return null;
            }
            // import A from ""
            if (resolve instanceof ES6ImportedBinding) {
                @NotNull ES6ImportedBinding binding = (ES6ImportedBinding) resolve;
                @NotNull Collection<PsiElement> elements = binding.findReferencedElements();
                for (@NotNull PsiElement element : elements) {
                    if (element instanceof PsiFile) {
                        return ((PsiFile) element).getVirtualFile();
                    }
                }
            }
            // A = () => import('')
            value = resolve;
        }
        // () => import("")
        @Nullable ES6ImportCall importCall = PsiTreeUtil.findChildOfType(value, ES6ImportCall.class);
        if (importCall == null) {
            return null;
        }
        @NotNull Collection<PsiElement> elements = importCall.resolveReferencedElements();
        for (PsiElement element : elements) {
            @Nullable PsiFile psiFile = PsiTreeUtil.getParentOfType(element, PsiFile.class);
            if (psiFile != null) {
                return psiFile.getVirtualFile();
            }
        }
        return null;
    }
}
