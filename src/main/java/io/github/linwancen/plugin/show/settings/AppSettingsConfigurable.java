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
        return "// Show Comment Global";
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

        modified |= mySettingsComponent.getShowLineEndComment() != settings.showLineEndComment;
        modified |= mySettingsComponent.getShowLineEndCommentJava() != settings.showLineEndCommentJava;
        modified |= mySettingsComponent.getShowLineEndCommentSql() != settings.showLineEndCommentSql;
        modified |= mySettingsComponent.getShowLineEndCommentJson() != settings.showLineEndCommentJson;
        modified |= mySettingsComponent.getShowLineEndCommentJs() != settings.showLineEndCommentJs;
        modified |= mySettingsComponent.getJsdoc() != settings.jsDoc;
        modified |= mySettingsComponent.getShowLineEndCommentPy() != settings.showLineEndCommentPy;
        modified |= mySettingsComponent.getShowLineEndCommentGo() != settings.showLineEndCommentGo;
        modified |= mySettingsComponent.getShowLineEndCommentKotlin() != settings.showLineEndCommentKotlin;

        modified |= !mySettingsComponent.getTreeTags().equals(String.join("|", settings.treeTags));
        modified |= !mySettingsComponent.getLineTags().equals(String.join("|", settings.lineTags));

        modified |= !mySettingsComponent.getLineEndCount().equals(String.valueOf(settings.lineEndCount));
        modified |= !settings.lineEndTextAttr.getForegroundColor().equals(mySettingsComponent.getLineEndColor());
        modified |= !settings.lineEndJsonTextAttr.getForegroundColor().equals(mySettingsComponent.getLineEndJsonColor());
        modified |= !mySettingsComponent.getLineEndPrefix().equals(settings.lineEndPrefix);

        modified |= mySettingsComponent.getGetToSet() != settings.getToSet;
        modified |= mySettingsComponent.getFromNew() != settings.fromNew;
        modified |= mySettingsComponent.getFromParam() != settings.fromParam;
        modified |= mySettingsComponent.getSkipAnnotation() != settings.skipAnnotation;
        modified |= mySettingsComponent.getSkipAscii() != settings.skipAscii;
        modified |= mySettingsComponent.getSkipBlank() != settings.skipBlank;

        modified = AbstractSettingsConfigurable.isModified(settings, mySettingsComponent, modified);

        return modified;
    }

    @Override
    public void apply() {
        @NotNull AppSettingsState settings = AppSettingsState.getInstance();
        settings.showTreeComment = mySettingsComponent.getShowTreeComment();
        settings.compact = mySettingsComponent.getCompact();

        settings.showLineEndComment = mySettingsComponent.getShowLineEndComment();
        settings.showLineEndCommentJava = mySettingsComponent.getShowLineEndCommentJava();
        settings.showLineEndCommentSql = mySettingsComponent.getShowLineEndCommentSql();
        settings.showLineEndCommentJson = mySettingsComponent.getShowLineEndCommentJson();
        settings.showLineEndCommentJs = mySettingsComponent.getShowLineEndCommentJs();
        settings.jsDoc = mySettingsComponent.getJsdoc();
        settings.showLineEndCommentPy = mySettingsComponent.getShowLineEndCommentPy();
        settings.showLineEndCommentGo = mySettingsComponent.getShowLineEndCommentGo();
        settings.showLineEndCommentKotlin = mySettingsComponent.getShowLineEndCommentKotlin();

        settings.treeTags = Splitter.on('|').splitToList(mySettingsComponent.getTreeTags()).toArray(new String[0]);
        settings.lineTags = Splitter.on('|').splitToList(mySettingsComponent.getLineTags()).toArray(new String[0]);

        try {
            settings.lineEndCount = Integer.parseInt(mySettingsComponent.getLineEndCount());
        } catch (NumberFormatException e) {
            mySettingsComponent.setLineEndCount(String.valueOf(settings.lineEndCount));
        }
        settings.lineEndTextAttr.setForegroundColor(mySettingsComponent.getLineEndColor());
        settings.lineEndJsonTextAttr.setForegroundColor(mySettingsComponent.getLineEndJsonColor());
        settings.lineEndPrefix = mySettingsComponent.getLineEndPrefix();

        settings.getToSet = mySettingsComponent.getGetToSet();
        settings.fromNew = mySettingsComponent.getFromNew();
        settings.fromParam = mySettingsComponent.getFromParam();
        settings.skipAnnotation = mySettingsComponent.getSkipAnnotation();
        settings.skipAscii = mySettingsComponent.getSkipAscii();
        settings.skipBlank = mySettingsComponent.getSkipBlank();

        AbstractSettingsConfigurable.apply(settings, mySettingsComponent);
    }

    @Override
    public void reset() {
        @NotNull AppSettingsState settings = AppSettingsState.getInstance();
        reset(settings, mySettingsComponent);
    }

    static void reset(@NotNull AppSettingsState settings, AppSettingsComponent mySettingsComponent) {
        mySettingsComponent.setShowTreeComment(settings.showTreeComment);
        mySettingsComponent.setCompact(settings.compact);

        mySettingsComponent.setShowLineEndComment(settings.showLineEndComment);
        mySettingsComponent.setShowLineEndCommentJava(settings.showLineEndCommentJava);
        mySettingsComponent.setShowLineEndCommentSql(settings.showLineEndCommentSql);
        mySettingsComponent.setShowLineEndCommentJson(settings.showLineEndCommentJson);
        mySettingsComponent.setShowLineEndCommentJs(settings.showLineEndCommentJs);
        mySettingsComponent.setJsdoc(settings.jsDoc);
        mySettingsComponent.setShowLineEndCommentPy(settings.showLineEndCommentPy);
        mySettingsComponent.setShowLineEndCommentGo(settings.showLineEndCommentGo);
        mySettingsComponent.setShowLineEndCommentKotlin(settings.showLineEndCommentKotlin);

        mySettingsComponent.setTreeTags(String.join("|", settings.treeTags));
        mySettingsComponent.setLineTags(String.join("|", settings.lineTags));

        mySettingsComponent.setLineEndCount(String.valueOf(settings.lineEndCount));
        mySettingsComponent.setLineEndColor(settings.lineEndTextAttr.getForegroundColor());
        mySettingsComponent.setLineEndJsonColor(settings.lineEndJsonTextAttr.getForegroundColor());
        mySettingsComponent.setLineEndPrefix(settings.lineEndPrefix);

        mySettingsComponent.setGetToSet(settings.getToSet);
        mySettingsComponent.setFromNew(settings.fromNew);
        mySettingsComponent.setFromParam(settings.fromParam);
        mySettingsComponent.setSkipAnnotation(settings.skipAnnotation);
        mySettingsComponent.setSkipAscii(settings.skipAscii);
        mySettingsComponent.setSkipBlank(settings.skipBlank);

        AbstractSettingsConfigurable.reset(settings, mySettingsComponent);
    }

    @Override
    public void disposeUIResources() {
        //noinspection ConstantConditions
        mySettingsComponent = null;
    }

}
