package io.github.linwancen.plugin.show.lang.base;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.JsonLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class BaseLangDoc extends EditorLinePainter {
    public static final Map<String, BaseLangDoc> LANG_DOC_MAP = new LinkedHashMap<>();

    public abstract @Nullable Class<? extends PsiElement> getRefClass();

    public abstract boolean show(@NotNull LineInfo info);

    @Nullable
    public <T extends SettingsInfo> String treeDoc(T info, ProjectViewNode<?> node, Project project) {
        return null;
    }

    @Override
    public @Nullable Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project,
                                                                     @NotNull VirtualFile file, int lineNumber) {
        // Only Load Class
        return null;
    }

    public static @Nullable String langDoc(@NotNull LineInfo info) {
        // psiFile.getText() may be not equals document.getText()
        @Nullable FileViewProvider viewProvider = PsiManager.getInstance(info.project).findViewProvider(info.file);
        if (viewProvider == null) {
            return null;
        }
        @Nullable PsiElement element = viewProvider.findElementAt(info.endOffset);
        if (element == null) {
            // file end
            element = viewProvider.findElementAt(info.endOffset - 1);
            if (element == null) {
                return null;
            }
        }
        @NotNull Language language = PsiElementTo.language(element);
        BaseLangDoc lineEnd = LANG_DOC_MAP.get(language.getID());
        if (lineEnd != null && lineEnd.show(info)) {
            return lineEnd.findRefDoc(info, viewProvider, element);
        } else if (language == JsonLanguage.INSTANCE && JsonLangDoc.INSTANCE.show(info)) {
            return JsonLangDoc.INSTANCE.findRefDoc(info, viewProvider, element);
        }
        return null;
    }

    /**
     * Override like JSON
     */
    @Nullable
    public String findRefDoc(@NotNull LineInfo info, @NotNull FileViewProvider viewProvider,
                             @NotNull PsiElement element) {
        @Nullable Class<? extends PsiElement> refClass = getRefClass();
        if (refClass == null) {
            return null;
        }
        @Nullable String doc = null;
        @Nullable String text = null;
        @Nullable PsiElement refElement = element;
        while ((refElement = Prev.prevRefChild(info, refElement, refClass)) != null) {
            PsiElement parent = refElement.getParent();
            @Nullable String filterDoc = refElementDoc(info, parent);
            if (filterDoc != null) {
                doc = filterDoc;
                text = refElement.getText();
                refElement = parent;
                break;
            }
        }
        if (refElement == null) {
            return null;
        }
        // before doc
        refElement = Prev.prevRefChild(info, refElement, refClass);
        if (refElement == null) {
            return doc;
        }
        PsiElement parent = refElement.getParent();
        @Nullable String before = refElementDoc(info, parent);
        if (before != null) {
            doc = mergeDoc(refElement.getText(), text, info.appSettings.getToSet, before, doc);
        }
        return doc;
    }

    @NotNull
    private String mergeDoc(@NotNull String beforeText, @NotNull String text,
                            boolean getToSet, String before, String doc) {
        if (beforeText.startsWith("set")) {
            beforeText = beforeText.substring(3);
            if (text.startsWith("get")) {
                text = text.substring(3);
            } else if (text.startsWith("is")) {
                text = text.substring(2);
            }
            boolean same = beforeText.equals(text);
            if (getToSet) {
                // because lambda is -> or =>
                doc = doc + (same ? " ==> " : " --> ") + before;
            } else {
                doc = before + (same ? " <== " : " <-- ") + doc;
            }
        } else {
            doc = before + " | " + doc;
        }
        return doc;
    }

    /**
     * Override like SQL
     */
    @Nullable
    protected String refElementDoc(@NotNull LineInfo info,
                                   @NotNull PsiElement refElement) {
        @Nullable String refDoc = refDoc(info, refElement);
        if (refDoc != null && !DocSkip.skipDoc(info, refDoc)) {
            return refDoc;
        }
        return null;
    }

    /**
     * Override like Java/Json
     */
    @Nullable
    protected String refDoc(@NotNull LineInfo info, @NotNull PsiElement ref) {
        // kotlin ref.getReference() == null but ref.getReferences().length == 2
        @NotNull PsiReference[] references = ref.getReferences();
        if (references.length < 1) {
            return null;
        }
        for (@NotNull PsiReference reference : references) {
            @Nullable PsiElement resolve;
            try {
                resolve = reference.resolve();
            } catch (Throwable e) {
                // 2021.3: Slow operations are prohibited on EDT.
                // See SlowOperations.assertSlowOperationsAreAllowed javadoc.
                return null;
            }
            if (resolve == null) {
                return null;
            }
            @Nullable String resolveDoc = resolveDoc(info, resolve);
            if (resolveDoc != null) {
                return resolveDoc;
            }
        }
        return null;
    }

    public static @Nullable <T extends SettingsInfo> String resolveDoc(@NotNull T info,
                                                                       @NotNull PsiElement psiElement) {
        // support like java <-> kotlin
        @NotNull Language language = PsiElementTo.language(psiElement);
        BaseLangDoc lineEnd = LANG_DOC_MAP.get(language.getID());
        if (lineEnd == null) {
            return null;
        }
        return lineEnd.resolveDocPrint(info, psiElement);
    }

    /**
     * Override like Java/Kotlin/Python
     */
    @Nullable
    protected <T extends SettingsInfo> String resolveDocPrint(@NotNull T info, @NotNull PsiElement resolve) {
        @Nullable String s = resolveDocRaw(info, resolve);
        if (s == null) {
            return null;
        }
        @NotNull String cutDoc = DocFilter.cutDoc(s, info, true);
        @NotNull String filterDoc = DocFilter.filterDoc(cutDoc, info.globalSettings, info.projectSettings);
        if (filterDoc.trim().isEmpty()) {
            return null;
        }
        return filterDoc;
    }

    /**
     * Override like JS/Go
     */
    @Nullable
    protected <T extends SettingsInfo> String resolveDocRaw(@NotNull T info, @NotNull PsiElement resolve) {
        @Nullable FileViewProvider viewProvider = PsiElementTo.viewProvider(resolve);
        if (viewProvider == null) {
            return null;
        }
        @Nullable String doc = ResolveDoc.fromLineEnd(info, resolve, viewProvider);
        if (doc != null) {
            return doc;
        }
        return ResolveDoc.fromLineUp(info, resolve, viewProvider, keywords());
    }

    @NotNull
    protected List<String> keywords() {
        return Collections.emptyList();
    }
}
