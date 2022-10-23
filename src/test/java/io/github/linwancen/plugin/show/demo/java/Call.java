package io.github.linwancen.plugin.show.demo.java;

import io.github.linwancen.plugin.show.demo.java.obj.Child;
import io.github.linwancen.plugin.show.demo.java.obj.Face;
import io.github.linwancen.plugin.show.demo.java.obj.Parent;

/**
 * @author lin
 */
public class Call extends Parent {
    public static void call() {
        new Call();
        Child child = new Child(true);
        Parent parentMethod = child.parentMethod();
        Face faceMethod = child.faceMethod(child);
        boolean is = child.isBool();
        Child get = child.getField();
        child.setField(new Child(true));
    }
}
