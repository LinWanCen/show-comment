package io.github.linwancen.plugin.show.demo.java;

import io.github.linwancen.plugin.show.demo.java.obj.Child;
import io.github.linwancen.plugin.show.demo.java.obj.Face;
import io.github.linwancen.plugin.show.demo.java.obj.Parent;

public class Ref implements Face {
    public static void method() {
        Parent noneNewMethod = new Call();
        Face noneNewDoc = new Ref();
        Child child = new Child(true);
        Child field = child.field.field;
        boolean bool = child.field.bool;
        Face fun = Child::setFun;
        child
                .faceMethod(face1 -> Child::setFun);
    }

    @Override
    public Face faceMethod(Face face) {
        return null;
    }
}
