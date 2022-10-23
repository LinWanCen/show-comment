package io.github.linwancen.plugin.show.lang;

import com.intellij.database.psi.DbElement;
import com.intellij.psi.PsiElement;
import com.intellij.sql.SqlDocumentationProvider;
import com.intellij.sql.psi.SqlLanguage;
import com.intellij.sql.psi.SqlReferenceExpression;
import com.intellij.util.containers.JBIterable;
import io.github.linwancen.plugin.show.bean.SettingsInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.lang.base.DocSkip;
import io.github.linwancen.plugin.show.bean.LineInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SqlLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(SqlLanguage.INSTANCE.getID(), new SqlLangDoc());
    }

    @Override
    public @NotNull Class<? extends PsiElement> getRefClass() {
        return SqlReferenceExpression.class;
    }

    @Override
    public boolean show(@NotNull LineInfo lineInfo) {
        return lineInfo.appSettings.showLineEndCommentSql;
    }


    @Override
    protected @Nullable <T extends SettingsInfo> String refElementDoc(@NotNull T lineInfo,
                                                                      @NotNull PsiElement ref) {
        JBIterable<DbElement> relatedDbElements;
        try {
            relatedDbElements = SqlDocumentationProvider.findRelatedDbElements(ref, false);
        } catch (Throwable e) {
            return null;
        }
        for (DbElement dbElement : relatedDbElements) {
            String refDoc = dbElement.getComment();
            if (refDoc != null && !DocSkip.skipDoc(lineInfo.appSettings, lineInfo.projectSettings, refDoc)) {
                return refDoc;
            }
        }
        return null;
    }
}
