package io.github.linwancen.plugin.show.settings;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class AbstractSettingsState {

    @NotNull
    public transient Pattern lineInclude = Pattern.compile("");
    @NotNull
    public transient Pattern lineExclude = Pattern.compile("^java");
    @NotNull
    public transient Pattern docInclude = Pattern.compile("");
    @NotNull
    public transient Pattern docExclude = Pattern.compile("");
    public boolean docGetEffect = false;
    @NotNull
    public transient Pattern docGet = Pattern.compile(".+?(?:[。\\r\\n]|\\. )");

    public String getLineInclude() {
        return lineInclude.pattern();
    }

    public void setLineInclude(@NotNull String lineInclude) {
        this.lineInclude = Pattern.compile(lineInclude);
    }

    public String getLineExclude() {
        return lineExclude.pattern();
    }

    public void setLineExclude(@NotNull String lineExclude) {
        this.lineExclude = Pattern.compile(lineExclude);
    }


    public String getDocInclude() {
        return docInclude.pattern();
    }

    public void setDocInclude(@NotNull String docInclude) {
        this.docInclude = Pattern.compile(docInclude);
    }

    public String getDocExclude() {
        return docExclude.pattern();
    }

    public void setDocExclude(@NotNull String docExclude) {
        this.docExclude = Pattern.compile(docExclude);
    }


    public String getDocGet() {
        return docGet.pattern();
    }

    public void setDocGet(@NotNull String docExclude) {
        this.docGet = Pattern.compile(docExclude);
    }
}
