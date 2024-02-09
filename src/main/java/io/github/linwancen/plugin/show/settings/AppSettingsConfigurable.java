package io.github.linwancen.plugin.show.settings;

import com.google.common.base.Splitter;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AppSettingsConfigurable implements Configurable {

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NotNull
    private AppSettingsComponent mySettingsComponent;

    @NotNull
    @Override
    public String getDisplayName() {
        return "// Show Comment App";
    }

    @NotNull
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        @NotNull AppSettingsState settings = AppSettingsState.getInstance();
        boolean modified = mySettingsComponent.getShowTreeComment() != settings.showTreeComment;
        modified |= mySettingsComponent.getCompact() != settings.compact;
        modified |= mySettingsComponent.getTreeCache() != settings.treeCache;

        modified |= mySettingsComponent.getShowLineEndComment() != settings.showLineEndComment;
        modified |= mySettingsComponent.getLineEndCache() != settings.lineEndCache;
        modified |= mySettingsComponent.getShowLineEndCommentJava() != settings.showLineEndCommentJava;
        modified |= mySettingsComponent.getShowLineEndCommentKotlin() != settings.showLineEndCommentKotlin;
        modified |= mySettingsComponent.getShowLineEndCommentJs() != settings.showLineEndCommentJs;
        modified |= mySettingsComponent.getShowLineEndCommentPhp() != settings.showLineEndCommentPhp;
        modified |= mySettingsComponent.getShowLineEndCommentPy() != settings.showLineEndCommentPy;
        modified |= mySettingsComponent.getShowLineEndCommentGo() != settings.showLineEndCommentGo;
        modified |= mySettingsComponent.getShowLineEndCommentJavaBase() != settings.showLineEndCommentJavaBase;
        modified |= mySettingsComponent.getShowLineEndCommentKotlinBase() != settings.showLineEndCommentKotlinBase;
        modified |= mySettingsComponent.getShowLineEndCommentJsBase() != settings.showLineEndCommentJsBase;
        modified |= mySettingsComponent.getShowLineEndCommentPhpBase() != settings.showLineEndCommentPhpBase;
        modified |= mySettingsComponent.getShowLineEndCommentPyBase() != settings.showLineEndCommentPyBase;
        modified |= mySettingsComponent.getShowLineEndCommentGoBase() != settings.showLineEndCommentGoBase;
        modified |= mySettingsComponent.getShowLineEndCommentRustBase() != settings.showLineEndCommentRustBase;
        modified |= mySettingsComponent.getShowLineEndCommentCBase() != settings.showLineEndCommentCBase;
        modified |= mySettingsComponent.getShowLineEndCommentSql() != settings.showLineEndCommentSql;
        modified |= mySettingsComponent.getShowLineEndCommentJson() != settings.showLineEndCommentJson;

        modified |= !mySettingsComponent.getTreeTags().equals(String.join("|", settings.treeTags));
        modified |= !mySettingsComponent.getLineTags().equals(String.join("|", settings.lineTags));

        modified |= !settings.lineEndTextAttr.getForegroundColor().equals(mySettingsComponent.getLineEndColor());
        modified |= !settings.lineEndJsonTextAttr.getForegroundColor().equals(mySettingsComponent.getLineEndJsonColor());
        modified |= !mySettingsComponent.getLineEndPrefix().equals(settings.lineEndPrefix);

        modified |= mySettingsComponent.getGetToSet() != settings.getToSet;
        modified |= mySettingsComponent.getFromNew() != settings.fromNew;
        modified |= mySettingsComponent.getFromParam() != settings.fromParam;
        modified |= mySettingsComponent.getEnumDoc() != settings.enumDoc;
        modified |= mySettingsComponent.getSkipAnnotation() != settings.skipAnnotation;
        modified |= mySettingsComponent.getSkipAscii() != settings.skipAscii;
        modified |= mySettingsComponent.getSkipBlank() != settings.skipBlank;
        return modified;
    }

    @Override
    public void apply() {
        @NotNull AppSettingsState settings = AppSettingsState.getInstance();
        settings.showTreeComment = mySettingsComponent.getShowTreeComment();
        settings.compact = mySettingsComponent.getCompact();
        settings.treeCache = mySettingsComponent.getTreeCache();

        settings.showLineEndComment = mySettingsComponent.getShowLineEndComment();
        settings.lineEndCache = mySettingsComponent.getLineEndCache();
        settings.showLineEndCommentJava = mySettingsComponent.getShowLineEndCommentJava();
        settings.showLineEndCommentKotlin = mySettingsComponent.getShowLineEndCommentKotlin();
        settings.showLineEndCommentJs = mySettingsComponent.getShowLineEndCommentJs();
        settings.showLineEndCommentPhp = mySettingsComponent.getShowLineEndCommentPhp();
        settings.showLineEndCommentPy = mySettingsComponent.getShowLineEndCommentPy();
        settings.showLineEndCommentGo = mySettingsComponent.getShowLineEndCommentGo();
        settings.showLineEndCommentJavaBase = mySettingsComponent.getShowLineEndCommentJavaBase();
        settings.showLineEndCommentKotlinBase = mySettingsComponent.getShowLineEndCommentKotlinBase();
        settings.showLineEndCommentJsBase = mySettingsComponent.getShowLineEndCommentJsBase();
        settings.showLineEndCommentPhpBase = mySettingsComponent.getShowLineEndCommentPhpBase();
        settings.showLineEndCommentPyBase = mySettingsComponent.getShowLineEndCommentPyBase();
        settings.showLineEndCommentGoBase = mySettingsComponent.getShowLineEndCommentGoBase();
        settings.showLineEndCommentRustBase = mySettingsComponent.getShowLineEndCommentRustBase();
        settings.showLineEndCommentCBase = mySettingsComponent.getShowLineEndCommentCBase();
        settings.showLineEndCommentSql = mySettingsComponent.getShowLineEndCommentSql();
        settings.showLineEndCommentJson = mySettingsComponent.getShowLineEndCommentJson();

        settings.treeTags = Splitter.on('|').splitToList(mySettingsComponent.getTreeTags()).toArray(new String[0]);
        settings.lineTags = Splitter.on('|').splitToList(mySettingsComponent.getLineTags()).toArray(new String[0]);

        settings.lineEndTextAttr.setForegroundColor(mySettingsComponent.getLineEndColor());
        settings.lineEndJsonTextAttr.setForegroundColor(mySettingsComponent.getLineEndJsonColor());
        settings.lineEndPrefix = mySettingsComponent.getLineEndPrefix();

        settings.getToSet = mySettingsComponent.getGetToSet();
        settings.fromNew = mySettingsComponent.getFromNew();
        settings.fromParam = mySettingsComponent.getFromParam();
        settings.enumDoc = mySettingsComponent.getEnumDoc();
        settings.skipAnnotation = mySettingsComponent.getSkipAnnotation();
        settings.skipAscii = mySettingsComponent.getSkipAscii();
        settings.skipBlank = mySettingsComponent.getSkipBlank();
    }

    @Override
    public void reset() {
        @NotNull AppSettingsState settings = AppSettingsState.getInstance();
        reset(settings, mySettingsComponent);
    }

    static void reset(@NotNull AppSettingsState settings, @NotNull AppSettingsComponent mySettingsComponent) {
        mySettingsComponent.setShowTreeComment(settings.showTreeComment);
        mySettingsComponent.setCompact(settings.compact);
        mySettingsComponent.setTreeCache(settings.treeCache);

        mySettingsComponent.setShowLineEndComment(settings.showLineEndComment);
        mySettingsComponent.setLineEndCache(settings.lineEndCache);
        mySettingsComponent.setShowLineEndCommentJava(settings.showLineEndCommentJava);
        mySettingsComponent.setShowLineEndCommentKotlin(settings.showLineEndCommentKotlin);
        mySettingsComponent.setShowLineEndCommentJs(settings.showLineEndCommentJs);
        mySettingsComponent.setShowLineEndCommentPhp(settings.showLineEndCommentPhp);
        mySettingsComponent.setShowLineEndCommentPy(settings.showLineEndCommentPy);
        mySettingsComponent.setShowLineEndCommentGo(settings.showLineEndCommentGo);
        mySettingsComponent.setShowLineEndCommentJavaBase(settings.showLineEndCommentJavaBase);
        mySettingsComponent.setShowLineEndCommentKotlinBase(settings.showLineEndCommentKotlinBase);
        mySettingsComponent.setShowLineEndCommentJsBase(settings.showLineEndCommentJsBase);
        mySettingsComponent.setShowLineEndCommentPhpBase(settings.showLineEndCommentPhpBase);
        mySettingsComponent.setShowLineEndCommentPyBase(settings.showLineEndCommentPyBase);
        mySettingsComponent.setShowLineEndCommentGoBase(settings.showLineEndCommentGoBase);
        mySettingsComponent.setShowLineEndCommentRustBase(settings.showLineEndCommentRustBase);
        mySettingsComponent.setShowLineEndCommentCBase(settings.showLineEndCommentCBase);
        mySettingsComponent.setShowLineEndCommentSql(settings.showLineEndCommentSql);
        mySettingsComponent.setShowLineEndCommentJson(settings.showLineEndCommentJson);

        mySettingsComponent.setTreeTags(String.join("|", settings.treeTags));
        mySettingsComponent.setLineTags(String.join("|", settings.lineTags));

        mySettingsComponent.setLineEndColor(settings.lineEndTextAttr.getForegroundColor());
        mySettingsComponent.setLineEndJsonColor(settings.lineEndJsonTextAttr.getForegroundColor());
        mySettingsComponent.setLineEndPrefix(settings.lineEndPrefix);

        mySettingsComponent.setGetToSet(settings.getToSet);
        mySettingsComponent.setFromNew(settings.fromNew);
        mySettingsComponent.setFromParam(settings.fromParam);
        mySettingsComponent.setEnumDoc(settings.enumDoc);
        mySettingsComponent.setSkipAnnotation(settings.skipAnnotation);
        mySettingsComponent.setSkipAscii(settings.skipAscii);
        mySettingsComponent.setSkipBlank(settings.skipBlank);
    }

    @Override
    public void disposeUIResources() {
        //noinspection ConstantConditions
        mySettingsComponent = null;
    }

}
