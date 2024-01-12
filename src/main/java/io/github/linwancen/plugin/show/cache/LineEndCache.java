package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.editor.LineExtensionInfo;
import io.github.linwancen.plugin.show.bean.LineInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LineEndCache {
    @NotNull
    public final Map<String, List<LineExtensionInfo>> map = new ConcurrentHashMap<>();
    public volatile boolean show = true;
    public volatile boolean selectChanged = false;
    @NotNull
    public volatile LineInfo info;

    public LineEndCache(@NotNull LineInfo info) {
        this.info = info;
    }

    public boolean needUpdate() {
        return show && selectChanged;
    }

    public void updated() {
        show = false;
        selectChanged = false;
    }
}
