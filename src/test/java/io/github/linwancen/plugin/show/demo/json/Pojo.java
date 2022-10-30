package io.github.linwancen.plugin.show.demo.json;

import java.util.Date;
import java.util.List;

@SuppressWarnings("all")
public class Pojo {
    /** integer */
    private int integer;
    /** str */
    private String str;
    /** date */
    private Date date;
    /** bool */
    private boolean bool;
    /** nestedClass */
    private NestedClass nestedClass;
    /** nestedClassArr */
    private NestedClass[][] nestedClassArr;
    /** nestedClassList */
    private List<NestedClass> nestedClassList;

    /** NestedClass */
    public static class NestedClass {
        /** nestedClass2 */
        private NestedClass2 nestedClass2;

        /** NestedClass2 */
        public static class NestedClass2 {
            /** a */
            private String a;
        }
    }
}
