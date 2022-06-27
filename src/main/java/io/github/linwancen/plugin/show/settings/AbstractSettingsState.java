package io.github.linwancen.plugin.show.settings;

import java.util.regex.Pattern;

public abstract class AbstractSettingsState {

    public transient Pattern lineInclude = Pattern.compile("");
    public transient Pattern lineExclude = Pattern.compile("^java");
    public transient Pattern docInclude = Pattern.compile("");
    public transient Pattern docExclude = Pattern.compile("");
    public transient boolean docGetEffect = false;
    public transient Pattern docGet = Pattern.compile(".+?(?:[。\\r\\n]|\\. )");

    public String getLineInclude() {
        return lineInclude.pattern();
    }

    public void setLineInclude(String lineInclude) {
        this.lineInclude = Pattern.compile(lineInclude);
    }

    public String getLineExclude() {
        return lineExclude.pattern();
    }

    public void setLineExclude(String lineExclude) {
        this.lineExclude = Pattern.compile(lineExclude);
    }


    public String getDocInclude() {
        return docInclude.pattern();
    }

    public void setDocInclude(String docInclude) {
        this.docInclude = Pattern.compile(docInclude);
    }

    public String getDocExclude() {
        return docExclude.pattern();
    }

    public void setDocExclude(String docExclude) {
        this.docExclude = Pattern.compile(docExclude);
    }


    public String getDocGet() {
        return docGet.pattern();
    }

    public void setDocGet(String docExclude) {
        this.docGet = Pattern.compile(docExclude);
    }
}
