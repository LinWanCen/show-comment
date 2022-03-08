package io.github.linwancen.plugin.show.demo.java;

import io.github.linwancen.plugin.show.demo.java.obj.Child;

public class Next {
    public static void method() {
        Child child = new Child(true);
        child.haveNotDoc(null).haveNotDoc(new Child()).haveNotDoc(null);
        child.haveNotDoc(null).getField().haveNotDoc(null);
    }
}
