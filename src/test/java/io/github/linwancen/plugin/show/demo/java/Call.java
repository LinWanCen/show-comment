package io.github.linwancen.plugin.show.demo.java;

import io.github.linwancen.plugin.show.demo.java.obj.Child;
import io.github.linwancen.plugin.show.demo.java.obj.Face;
import io.github.linwancen.plugin.show.demo.java.obj.Parent;

public class Call extends Parent {
    public static void call() {
        Child child = new Child(true);
        Parent parentMethod = child.parentMethod();
        Face faceMethod = child.faceMethod(child);
        boolean is = child.isBool();
        Child get = child.getField();
        child.setField(new Child(true)); // field
    }
}
