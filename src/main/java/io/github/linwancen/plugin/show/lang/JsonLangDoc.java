package io.github.linwancen.plugin.show.lang;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.ext.conf.ConfCacheGetUtils;
import io.github.linwancen.plugin.show.ext.conf.TsvLoader;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JsonLangDoc extends BaseLangDoc {

    public static final JsonLangDoc INSTANCE = new JsonLangDoc();

    @Override
    public @NotNull Class<? extends PsiElement> getRefClass() {
        return JsonProperty.class;
    }

    @Override
    public boolean show(@NotNull LineInfo lineInfo) {
        return lineInfo.appSettings.showLineEndCommentJson;
    }

    @Override
    public @Nullable String findRefDoc(@NotNull LineInfo lineInfo, @NotNull PsiElement element) {
        @Nullable PsiElement start = lineInfo.viewProvider.findElementAt(lineInfo.startOffset);
        if (start == null) {
            return null;
        }
        PsiElement jsonProperty = start.getNextSibling();
        if (jsonProperty == null) {
            return null;
        }
        return refElementDoc(lineInfo, jsonProperty);
    }

    @Override
    protected @Nullable String refDoc(@NotNull LineInfo lineInfo, @NotNull PsiElement ref) {
        if (!(ref instanceof JsonProperty)) {
            return null;
        }
        @NotNull JsonProperty jsonProperty = (JsonProperty) ref;
        @Nullable String dictDoc = dictDoc(lineInfo, jsonProperty);
        if (dictDoc != null) {
            return dictDoc;
        }
        @NotNull PsiReference[] references = jsonProperty.getNameElement().getReferences();
        for (@NotNull PsiReference reference : references) {
            @Nullable PsiElement resolve = null;
            try {
                resolve = reference.resolve();
            } catch (Throwable ignore) {
                // ignore
            }
            if (resolve == null) {
                continue;
            }
            @Nullable String doc = BaseLangDoc.resolveDoc(lineInfo, resolve);
            if (doc != null) {
                return doc;
            }
        }
        return null;
    }

    @Nullable
    private static String dictDoc(@NotNull LineInfo lineInfo, @NotNull JsonProperty prop) {
        @Nullable JsonValue value = prop.getValue();
        if (value == null || value instanceof JsonArray || value instanceof JsonObject) {
            return null;
        }
        @NotNull GlobalSearchScope scope = GlobalSearchScope.allScope(lineInfo.project);
        String jsonKey = prop.getName();
        String jsonValue = value.getText();
        // Read the json.path before if needed
        return jsonDictDoc(lineInfo, scope, jsonKey, jsonValue);
    }

    @Nullable
    private static String jsonDictDoc(@NotNull LineInfo lineInfo, @NotNull GlobalSearchScope scope, String fileName, String jsonValue) {
        @NotNull Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(lineInfo.project, fileName + ".tsv", scope);
        // one file
        if (files.size() < 2) {
            for (@NotNull VirtualFile file : files) {
                @NotNull Map<String, List<String>> map = TsvLoader.buildMap(file, false);
                List<String> list = map.get(jsonValue);
                if (list != null && list.size() > 1) {
                    return list.get(1);
                }
            }
            return null;
        }
        // multi file
        @NotNull Map<VirtualFile, Map<String, List<String>>> fileMap = new ConcurrentHashMap<>();
        for (@NotNull VirtualFile file : files) {
            @NotNull Map<String, List<String>> map = TsvLoader.buildMap(file, false);
            fileMap.put(file, map);
        }
        @NotNull String path = lineInfo.file.getPath();
        @NotNull SortedMap<String, Map<String, List<String>>> treeMap = ConfCacheGetUtils.filterPath(fileMap, path);
        for (@NotNull Map.Entry<String, Map<String, List<String>>> entry : treeMap.entrySet()) {
            List<String> list = entry.getValue().get(jsonValue);
            if (list != null && list.size() > 1) {
                return list.get(1);
            }
        }
        return null;
    }
}
