package io.github.linwancen.plugin.show.settings;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class AbstractSettingsState {

    public int lineEndCount = 2;
    public int lineEndLen = 0;
    public boolean onlySelectLine = false;

    @NotNull
    public transient Pattern lineInclude = Pattern.compile("");
    @NotNull
    public transient Pattern lineExclude = Pattern.compile("^(java)\\.");
    @NotNull
    public transient Pattern docInclude = Pattern.compile("");
    @NotNull
    public transient Pattern docExclude = Pattern.compile("");

    public boolean docGetEffect = false;
    @NotNull
    public transient Pattern docGet = Pattern.compile(".+?(?:[。\\r\\n]|\\. )");

    public boolean dirDocEffect = true;
    @NotNull
    public transient Map<String, Pattern[]> dirDoc = new LinkedHashMap<>() {{
        put("pom.xml", new Pattern[]{
                Pattern.compile("<description>([^<]++)</description>"),
                Pattern.compile("<name>\\$\\{[^}]*+\\}([^<]++)</name>"),
        });
        put("build.gradle", new Pattern[]{Pattern.compile("(?m)^description[^'\"]*+['\"]([^'\"]++)['\"]")});
        put("build.gradle.kts", new Pattern[]{Pattern.compile("(?m)^description[^'\"]*+['\"]([^'\"]++)['\"]")});
        put("README.md", new Pattern[]{Pattern.compile("(?m)^#++ (.*)")});
    }};

    public boolean fileDocEffect = true;
    @NotNull
    public transient Map<String, Pattern[]> fileDoc = new LinkedHashMap<>() {{
        put("md", new Pattern[]{Pattern.compile("(?m)^#++ (.*)")});
        put("ad", new Pattern[]{Pattern.compile("(?m)^=++ (.*)")});
        put("adoc", new Pattern[]{Pattern.compile("(?m)^=++ (.*)")});
        put("asciidoc", new Pattern[]{Pattern.compile("(?m)^=++ (.*)")});
        put("bpmn", new Pattern[]{Pattern.compile("(?m)name=\"([^\"]*)")});
        put("dmn", new Pattern[]{Pattern.compile("(?m)name=\"([^\"]*)")});
        put("sh", new Pattern[]{Pattern.compile("(?m)^#++ *([^!].*)")});
    }};


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


    @NotNull
    public String getDirDoc() {
        return PatternMapUtils.toString(dirDoc);
    }

    public void setDirDoc(@NotNull String dirDoc) {
        this.dirDoc = PatternMapUtils.toMap(dirDoc);
    }

    @NotNull
    public String getFileDoc() {
        return PatternMapUtils.toString(fileDoc);
    }

    public void setFileDoc(@NotNull String fileDoc) {
        this.fileDoc = PatternMapUtils.toMap(fileDoc);
    }
}
