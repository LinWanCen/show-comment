package io.github.linwancen.plugin.show.demo.method;

import java.util.Date;

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

    /** NestedClass */
    public static class NestedClass{
        /** nestedClass2 */
        private NestedClass2 nestedClass2;

        /** NestedClass2 */
        public static class NestedClass2{
            /** a */
            private String a;
        }
    }

    public int getInteger() {
        return integer;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }
}
