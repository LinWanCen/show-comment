package io.github.linwancen.plugin.show.cache;

import com.intellij.openapi.editor.LineExtensionInfo;
import io.github.linwancen.plugin.show.bean.LineInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LineEndCache {
    @NotNull public volatile String code;
    @NotNull public final List<LineExtensionInfo> lineExtList = new ArrayList<>(1);
    public volatile boolean selectChanged = false;
    /** null if updated */
    @Nullable public volatile LineInfo info;

    public LineEndCache(@NotNull String code, @NotNull LineInfo info) {
        this.code = code;
        this.info = info;
    }
}
