package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.editor.LineExtensionInfo;
import io.github.linwancen.plugin.show.bean.LineInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LineEndCache {
    @NotNull public String code;
    @NotNull public List<LineExtensionInfo> lineExtList;
    public boolean selectChanged = false;
    /** null if updated */
    @Nullable public volatile LineInfo info;

    public LineEndCache(@NotNull String code, @NotNull List<LineExtensionInfo> right, @NotNull LineInfo info) {
        this.code = code;
        this.lineExtList = right;
        this.info = info;
    }

    public void updated() {
        selectChanged = false;
        info = null;
    }
}
