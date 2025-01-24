package io.github.linwancen.plugin.show.lang;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.javascript.psi.JSPsiReferenceElement;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.model.psi.PsiSymbolReferenceService;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import io.github.linwancen.plugin.show.bean.LineInfo;
import io.github.linwancen.plugin.show.lang.base.DocFilter;
import io.github.linwancen.plugin.show.lang.base.DocSkip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HtmlLangDoc extends JsLangDoc {
    private static final Logger LOG = LoggerFactory.getLogger(HtmlLangDoc.class);

    public static final Method WEB_DOC_METHOD;
    public static final Method REF_METHOD;

    static {
        LANG_DOC_MAP.put(HTMLLanguage.INSTANCE.getID(), new HtmlLangDoc());
        Method method = null;
        Method refMethod = null;
        try {
            Class<?> clazz = Class.forName("com.intellij.webSymbols.WebSymbol");
            method = clazz.getMethod("getDescription");
            // noinspection UnstableApiUsage
            refMethod = PsiSymbolReferenceService.class.getMethod("getReferences", PsiElement.class);
        } catch (Exception e) {
            LOG.warn("Web Tag Attr Doc is support since 2022.3, {}", e.getLocalizedMessage());
        }
        WEB_DOC_METHOD = method;
        REF_METHOD = refMethod;
    }

    @Override
    public @NotNull List<Class<? extends PsiElement>> getRefClass() {
        return List.of(JSPsiReferenceElement.class, XmlAttribute.class, XmlTag.class);
    }

    @Override
    public boolean show(@NotNull LineInfo info) {
        return info.appSettings.showLineEndCommentJs || info.appSettings.showLineEndCommentHtml;
    }

    /**
     * Override like Java/Json/Html
     */
    @SuppressWarnings("UnstableApiUsage")
    @Nullable
    protected String refDoc(@NotNull LineInfo info, @NotNull PsiElement ref) {
        if (DocSkip.skipTagAttr(info, ref)) {
            return null;
        }
        if (WEB_DOC_METHOD == null || !info.appSettings.showLineEndCommentHtml) {
            return super.refDoc(info, ref);
        }
        PsiSymbolReferenceService service = PsiSymbolReferenceService.getService();
        ArrayList<PsiSymbolReference> references = new ArrayList<>();
        try {
            Object object = REF_METHOD.invoke(service, ref);
            if (object instanceof Iterable) {
                Iterable<?> objects = (Iterable<?>) object;
                for (Object o : objects) {
                    if (o instanceof PsiSymbolReference) {
                        references.add(((PsiSymbolReference) o));
                    }
                }
            }
        } catch (ProcessCanceledException ignored) {
            return super.refDoc(info, ref);
        } catch (Exception e) {
            LOG.warn("Web Tag Attr getReferences fail: ", e);
        }
        if (references.isEmpty()) {
            return super.refDoc(info, ref);
        }
        try {
            // v-model:visible should only visible
            for (Object symbol : references.get(references.size() - 1).resolveReference()) {
                try {
                    Object doc = WEB_DOC_METHOD.invoke(symbol);
                    if (doc instanceof String) {
                        return DocFilter.html2Text((String) doc);
                    }
                } catch (ProcessCanceledException ignored) {
                } catch (Exception e) {
                    LOG.warn("Get Web Tag Attr Doc fail: ", e);
                }
            }
        } catch (ProcessCanceledException ignored) {
        } catch (Throwable e) {
            LOG.warn("Web Tag Attr Doc resolveReference fail: ", e);
        }
        return super.refDoc(info, ref);
    }
}
