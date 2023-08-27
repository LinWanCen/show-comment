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
import com.intellij.psi.PsiReference;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.JsonLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 *
 */
public abstract class BaseLangDoc extends EditorLinePainter {
    public static final Map<String, BaseLangDoc> LANG_DOC_MAP = new LinkedHashMap<>();

    public abstract @Nullable Class<? extends PsiElement> getRefClass();

    public abstract boolean show(@NotNull LineInfo lineInfo);

    @Nullable
    public <T extends SettingsInfo> String treeDoc(T settingsInfo, ProjectViewNode<?> node, Project project) {
        return null;
    }

    @Override
    public @Nullable Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project,
                                                                     @NotNull VirtualFile file, int lineNumber) {
        // Only Load Class
        return null;
    }

    public static @Nullable String langDoc(@NotNull LineInfo lineInfo) {
        @Nullable PsiElement element = lineInfo.viewProvider.findElementAt(lineInfo.endOffset);
        if (element == null) {
            // file end
            element = lineInfo.viewProvider.findElementAt(lineInfo.endOffset - 1);
            if (element == null) {
                return null;
            }
        }
        @NotNull Language language = PsiElementTo.language(element);
        BaseLangDoc lineEnd = LANG_DOC_MAP.get(language.getID());
        if (lineEnd != null && lineEnd.show(lineInfo)) {
            return lineEnd.findRefDoc(lineInfo, element);
        } else if (language == JsonLanguage.INSTANCE && JsonLangDoc.INSTANCE.show(lineInfo)) {
            return JsonLangDoc.INSTANCE.findRefDoc(lineInfo, element);
        }
        return null;
    }

    /**
     * Override like JSON
     */
    @Nullable
    public String findRefDoc(@NotNull LineInfo lineInfo, @NotNull PsiElement element) {
        @Nullable Class<? extends PsiElement> refClass = getRefClass();
        if (refClass == null) {
            return null;
        }
        @Nullable String doc = null;
        @Nullable PsiElement refElement = element;
        while ((refElement = Prev.prevRefChild(lineInfo, refElement, refClass)) != null) {
            PsiElement parent = refElement.getParent();
            @Nullable String filterDoc = refElementDoc(lineInfo, parent);
            if (filterDoc != null) {
                doc = filterDoc;
                refElement = parent;
                break;
            }
        }
        if (refElement == null) {
            return null;
        }
        // before doc
        refElement = Prev.prevRefChild(lineInfo, refElement, refClass);
        if (refElement == null) {
            return doc;
        }
        String text = refElement.getText();
        boolean set = text.startsWith("set");
        PsiElement parent = refElement.getParent();
        @Nullable String before = refElementDoc(lineInfo, parent);
        if (before != null) {
            doc = mergeDoc(set, lineInfo.appSettings.getToSet, before, doc);
        }
        return doc;
    }

    @NotNull
    private String mergeDoc(boolean set, boolean getToSet, String before, String doc) {
        if (set) {
            if (getToSet) {
                // because lambda is -> or =>
                doc = doc + " --> " + before;
            } else {
                doc = before + " = " + doc;
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
    protected String refElementDoc(@NotNull LineInfo lineInfo,
                                   @NotNull PsiElement refElement) {
        @Nullable String refDoc = refDoc(lineInfo, refElement);
        if (refDoc != null && !DocSkip.skipDoc(lineInfo, refDoc)) {
            return refDoc;
        }
        return null;
    }

    /**
     * Override like Java/Json
     */
    @Nullable
    protected String refDoc(@NotNull LineInfo lineInfo, @NotNull PsiElement ref) {
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
            @Nullable String resolveDoc = resolveDoc(lineInfo, resolve);
            if (resolveDoc != null) {
                return resolveDoc;
            }
        }
        return null;
    }

    public static @Nullable <T extends SettingsInfo> String resolveDoc(@NotNull T settingsInfo,
                                                                       @NotNull PsiElement psiElement) {
        // support like java <-> kotlin
        @NotNull Language language = PsiElementTo.language(psiElement);
        BaseLangDoc lineEnd = LANG_DOC_MAP.get(language.getID());
        if (lineEnd == null) {
            return null;
        }
        return lineEnd.resolveDocPrint(settingsInfo, psiElement);
    }

    /**
     * Override like Java/Kotlin
     */
    @Nullable
    protected <T extends SettingsInfo> String resolveDocPrint(@NotNull T lineInfo, @NotNull PsiElement resolve) {
        @Nullable String s = resolveDocRaw(lineInfo, resolve);
        if (s == null) {
            return null;
        }
        @NotNull String cutDoc = DocFilter.cutDoc(s, lineInfo.appSettings, true);
        @NotNull String filterDoc = DocFilter.filterDoc(cutDoc, lineInfo.appSettings, lineInfo.projectSettings);
        if (filterDoc.trim().length() == 0) {
            return null;
        }
        return filterDoc;
    }

    /**
     * Override like JS/Go
     */
    @Nullable
    protected <T extends SettingsInfo> String resolveDocRaw(@NotNull T lineInfo, @NotNull PsiElement resolve) {
        @Nullable FileViewProvider viewProvider = PsiElementTo.viewProvider(resolve);
        if (viewProvider == null) {
            return null;
        }
        @Nullable String doc = ResolveDoc.fromLineEnd(lineInfo, resolve, viewProvider);
        if (doc != null) {
            return doc;
        }
        return ResolveDoc.fromLineUp(lineInfo, resolve, viewProvider, keywords());
    }

    @NotNull
    protected List<String> keywords() {
        return Collections.emptyList();
    }
}
