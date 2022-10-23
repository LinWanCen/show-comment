package io.github.linwancen.plugin.show.lang.base;

import io.github.linwancen.plugin.show.lang.base.DocFilter;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

/**
 * @see DocFilter
 */
class DocFilterTest {

    public static final String[] STRS = {
            "english. next",
            "China。next",
            "Class.field",
    };

    public static final String[] RESULT = {
            "english. ",
            "China。",
            "Class.field",
    };

    public static final Pattern[] PATTERNS = {
            Pattern.compile(".+?(?:[。\\r\\n]|\\. )"),
            Pattern.compile("(.+?(?:。|. ))"),
            Pattern.compile(""),
            Pattern.compile("C()"),
    };

    /**
     * @see DocFilter#filterDoc
     */
    @Test
    void testGetDoc() {
        for (int pi = 0; pi < PATTERNS.length; pi++) {
            Pattern p = PATTERNS[pi];
            for (int si = 0; si < STRS.length; si++) {
                String s = STRS[si];
                @Nullable String doc = DocFilter.filterDoc(s, p);
                String pattern = p.pattern();
                System.out.println("（" + s + ", " + pattern + "): " + doc);
                if (pi < 2) {
                    Assertions.assertEquals(RESULT[si], doc);
                }
            }
            System.out.println();
        }
    }
}