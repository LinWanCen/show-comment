package io.github.linwancen.plugin.show.ext.conf;

import com.google.common.base.Splitter;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import groovy.json.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TsvLoader {

    private TsvLoader() {}

    public static final String EXT = "tsv";
    private static final Pattern LINE_PATTERN = Pattern.compile("[\\r\\n]++");
    public static final Pattern DEL_PATTERN = Pattern.compile("\\(\\?[^)]++\\)");

    @NotNull
    public static Map<String, List<String>> buildMap(@NotNull VirtualFile file, boolean patternKey) {
        @Nullable Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return Collections.emptyMap();
        }
        @NotNull String text = document.getText();
        // this pattern would skip empty line
        String[] lines = LINE_PATTERN.split(text);
        @NotNull Map<String, List<String>> map = new LinkedHashMap<>();
        for (@NotNull String line : lines) {
            @NotNull List<String> words = Splitter.on('\t').splitToList(line);
            if (!words.isEmpty()) {
                String key = words.get(0);
                if (key.length() == 0) {
                    continue;
                }
                if (patternKey) {
                    key = StringEscapeUtils.unescapeJava(key);
                    String del = DEL_PATTERN.matcher(key).replaceAll("");
                    if (del.length() != 0) {
                        key = del;
                    }
                }
                map.put(key, words);
            }
        }
        return map;
    }
}
