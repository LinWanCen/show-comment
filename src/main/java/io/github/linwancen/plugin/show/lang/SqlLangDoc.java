package io.github.linwancen.plugin.show.lang;

import com.intellij.database.psi.DbElement;
import com.intellij.psi.PsiElement;
import com.intellij.sql.psi.SqlLanguage;
import com.intellij.sql.psi.SqlReferenceExpression;
import com.intellij.util.containers.JBIterable;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.lang.base.BaseLangDoc;
import io.github.linwancen.plugin.show.lang.base.DocSkip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

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
    protected @Nullable String refElementDoc(@NotNull LineInfo lineInfo,
                                             @NotNull PsiElement ref) {
        JBIterable<DbElement> relatedDbElements;
        Class<?> clazz;
        try {
            // new version new Class
            clazz = Class.forName("com.intellij.sql.SqlNavigationUtils");
        } catch (Throwable e) {
            try {
                // old version
                clazz = Class.forName("com.intellij.sql.SqlDocumentationProvider");
            } catch (Throwable e2) {
                return null;
            }
        }
        try {
            @NotNull Method method = clazz.getMethod("findRelatedDbElements", PsiElement.class, boolean.class);
            //noinspection unchecked
            relatedDbElements = (JBIterable<DbElement>) method.invoke(null, ref, false);
        } catch (Throwable e) {
            return null;
        }
        for (@NotNull DbElement dbElement : relatedDbElements) {
            @Nullable String refDoc = dbElement.getComment();
            if (refDoc != null && !DocSkip.skipDoc(lineInfo.appSettings, lineInfo.projectSettings, refDoc)) {
                return refDoc;
            }
        }
        return null;
    }
}
