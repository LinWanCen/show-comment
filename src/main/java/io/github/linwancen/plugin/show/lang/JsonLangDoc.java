package io.github.linwancen.plugin.show.lang;

import com.intellij.json.JsonLanguage;
import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.ext.GetFromDocMap;
import io.github.linwancen.plugin.show.ext.conf.ConfCache;
import io.github.linwancen.plugin.show.ext.conf.ConfCacheGetUtils;
import io.github.linwancen.plugin.show.ext.conf.TsvLoader;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

public class JsonLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(JsonLanguage.INSTANCE.getID(), new JsonLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(JsonProperty.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentJson;
    }

    @Override
    public @Nullable String findRefDoc(@NotNull LineInfo info, @NotNull FileViewProvider viewProvider,
                                       @NotNull PsiElement element) {
        @Nullable PsiElement start = viewProvider.findElementAt(info.startOffset);
        if (start == null) {
            return null;
        }
        PsiElement jsonProperty = start.getNextSibling();
        if (jsonProperty == null) {
            return null;
        }
        return refElementDoc(info, jsonProperty);
    }

    @Override
    protected @Nullable String refDoc(@NotNull LineInfo info, @NotNull PsiElement ref) {
        if (!(ref instanceof JsonProperty)) {
            return null;
        }
        @NotNull JsonProperty jsonProperty = (JsonProperty) ref;
        @Nullable String extDoc = extDoc(info, jsonProperty);
        if (extDoc != null) {
            return extDoc;
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
            @Nullable String doc = BaseLangDoc.resolveDoc(info, resolve);
            if (doc != null) {
                return doc;
            }
        }
        return null;
    }

    @Nullable
    private static String extDoc(@NotNull LineInfo info, @NotNull JsonProperty prop) {
        @Nullable JsonValue value = prop.getValue();
        if (value == null || value instanceof JsonArray || value instanceof JsonObject) {
            return null;
        }
        @NotNull GlobalSearchScope scope = GlobalSearchScope.allScope(info.project);
        @NotNull String jsonKey = prop.getName();
        String jsonValue = info.getText(value);
        // Read the json.path before if needed
        @Nullable String dictDoc = jsonDictDoc(info, scope, jsonKey, jsonValue);
        if (dictDoc != null) {
            return dictDoc;
        }
        @NotNull Map<String, Map<String, List<String>>> jsonMap = ConfCache.jsonMap(info.file.getPath());
        return GetFromDocMap.get(jsonMap, jsonKey);
    }

    @Nullable
    private static String jsonDictDoc(@NotNull LineInfo info,
                                      @NotNull GlobalSearchScope scope, String jsonKey, String jsonValue) {
        @NotNull String name = jsonKey + ".tsv";
        @NotNull Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(info.project, name, scope);
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
        @NotNull String path = info.file.getPath();
        @NotNull SortedMap<String, Map<String, List<String>>> sortedMap = ConfCacheGetUtils.filterPath(fileMap, path);
        return GetFromDocMap.get(sortedMap, jsonValue);
    }

    @Nullable
    @Override
    protected <T extends SettingsInfo> String resolveDocPrint(@NotNull T info, @NotNull PsiElement resolve) {
        return null;
    }
}
