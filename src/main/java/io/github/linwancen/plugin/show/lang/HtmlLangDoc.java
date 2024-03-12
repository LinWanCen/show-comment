package io.github.linwancen.plugin.show.lang;

import com.intellij.lang.html.HTMLLanguage;
import io.github.linwancen.plugin.show.bean.LineInfo;
import org.jetbrains.annotations.NotNull;

public class HtmlLangDoc extends JsLangDoc {

    static {
        LANG_DOC_MAP.put(HTMLLanguage.INSTANCE.getID(), new HtmlLangDoc());
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentJs;
    }
}
