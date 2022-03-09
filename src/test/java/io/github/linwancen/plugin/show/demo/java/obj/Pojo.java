package io.github.linwancen.plugin.show.demo.java.obj;

import java.util.Date;
import java.util.List;

/** not doc */
public class Pojo {
    private int integer;
    private String str;
    private Date date;
    private boolean bool;
    private NestedClass nestedClass;
    private NestedClass[] nestedClassArr;
    private List<NestedClass> nestedClassList;

    public static class NestedClass {
        private NestedClass2 nestedClass2;

        public static class NestedClass2 {
            private String a;
        }
    }
}
