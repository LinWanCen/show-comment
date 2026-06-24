package io.github.linwancen.plugin.show.lang;

import com.intellij.database.model.DasColumn;
import com.intellij.database.model.basic.BasicIndex;
import com.intellij.database.model.basic.BasicTableOrView;
import com.intellij.database.model.families.Family;
import com.intellij.database.psi.DbColumn;
import com.intellij.database.psi.DbElement;
import com.intellij.database.psi.DbTable;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SqlLangDoc extends BaseLangDoc {

    static {
        LANG_DOC_MAP.put(SqlLanguage.INSTANCE.getID(), new SqlLangDoc());
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(SqlReferenceExpression.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentSql;
    }


    @Override
    protected @Nullable String refElementDoc(@NotNull LineInfo info,
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
            StringBuilder sb = new StringBuilder();
            if (dbElement instanceof DbColumn) {
                DbColumn column = (DbColumn) dbElement;
                DbTable table = column.getTable();
                if (table != null) {
                    Set<DasColumn.Attribute> attrs = table.getColumnAttrs(column);
                    for (DasColumn.Attribute attr : attrs) {
                        String str = Emoji.ATTR_TO_EMOJI.get(attr);
                        if (str == null) {
                            str = attr.name();
                        }
                        sb.append(str).append(" ");
                    }
                }
            }
            @Nullable String refDoc = dbElement.getComment();
            if (refDoc != null && !DocSkip.skipDoc(info, refDoc)) {
                sb.append(refDoc);
            }
            if (dbElement instanceof DbTable) {
                DbTable table = (DbTable) dbElement;
                Object delegate = table.getDelegate();
                if (delegate instanceof BasicTableOrView) {
                    BasicTableOrView tableOrView = (BasicTableOrView) delegate;
                    Family<? extends BasicIndex> indices = tableOrView.getIndices();
                    if (!indices.isEmpty()) {
                        sb.append(indices.stream()
                                .map(i -> (i.isUnique() ? "U" : "")
                                        + "(" + String.join(", ", i.getColNames()) + ")")
                                .collect(Collectors.joining(", ", " [", "]")));
                    }
                }
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
        }
        return null;
    }

    static class Emoji {
        private static final EnumMap<DasColumn.Attribute, String> ATTR_TO_EMOJI =
                new EnumMap<>(DasColumn.Attribute.class);

        static {
            ATTR_TO_EMOJI.put(DasColumn.Attribute.PRIMARY_KEY, "\uD83D\uDD11"); // 🔑
            ATTR_TO_EMOJI.put(DasColumn.Attribute.CANDIDATE_KEY, "\uD83D\uDDDD️"); // 🗝️
            ATTR_TO_EMOJI.put(DasColumn.Attribute.FOREIGN_KEY, "\uD83D\uDD17"); // 🔗
            ATTR_TO_EMOJI.put(DasColumn.Attribute.INDEX, "\uD83D\uDD0D"); // 🔍
            ATTR_TO_EMOJI.put(DasColumn.Attribute.AUTO_GENERATED, "⏫"); // ⏫
            ATTR_TO_EMOJI.put(DasColumn.Attribute.COMPUTED, "\uD83D\uDCBB"); // 💻
            ATTR_TO_EMOJI.put(DasColumn.Attribute.VERSION, "\uD83D\uDCCC"); // 📌
        }
    }
}
