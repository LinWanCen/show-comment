package io.github.linwancen.plugin.show.settings;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public boolean projectDocEffect = true;
    @NotNull
    public transient Pattern[][] projectDoc = {
            {
                    Pattern.compile("pom.xml"),
                    Pattern.compile("<description>([^<]++)</description>"),
                    Pattern.compile("<name>\\$\\{[^}]*+\\}([^<]++)</name>"),
            },
            {Pattern.compile("build.gradle"), Pattern.compile("^description[^'\"]*+['\"]([^'\"]++)['\"]")},
            {Pattern.compile("build.gradle.kts"), Pattern.compile("^description[^'\"]*+['\"]([^'\"]++)['\"]")},
            {Pattern.compile("README.md"), Pattern.compile("^# (.*)")},
    };

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


    public static final Function<Pattern[], String> TO_LINE = patterns -> Stream.of(patterns)
            .map(Pattern::pattern)
            .collect(Collectors.joining("||"));

    public String getProjectDoc() {
        return Stream.of(projectDoc)
                .map(TO_LINE)
                .collect(Collectors.joining("\n"));
    }

    private static final Pattern LINE_PATTERN = Pattern.compile("[\\r\\n]++");

    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\|\\|");

    public void setProjectDoc(@NotNull String projectDoc) {
        String[] projectDocLines = LINE_PATTERN.split(projectDoc);
        @NotNull Pattern[][] patterns = new Pattern[projectDocLines.length][];
        for (int i = 0; i < projectDocLines.length; i++) {
            String[] projectDocs = SPLIT_PATTERN.split(projectDocLines[i]);
            patterns[i] = new Pattern[projectDocs.length];
            for (int j = 0; j < projectDocs.length; j++) {
                patterns[i][j] = Pattern.compile(projectDocs[j]);
            }
        }
        this.projectDoc = patterns;
    }
}
