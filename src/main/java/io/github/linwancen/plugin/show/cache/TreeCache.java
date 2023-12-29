package io.github.linwancen.plugin.show.cache;

import org.jetbrains.annotations.Nullable;

public class TreeCache {
    @Nullable
    public String doc;
    public volatile boolean needUpdate = true;
}
