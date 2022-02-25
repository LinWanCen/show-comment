package io.github.linwancen.plugin.show.demo;

import io.github.linwancen.plugin.show.demo.method.Child;
import io.github.linwancen.plugin.show.demo.method.Face;
import io.github.linwancen.plugin.show.demo.method.Parent;

public class Method {
    public static void method() {
        Parent noneNewMethod = new Parent();
        Face noneNewDoc = new Child();

        Child child = new Child(true);
        Parent parentMethod = child.parentMethod();
        Face faceMethod = child.faceMethod(child);

        boolean is = child.isBool();
        Child field = child.field;
        Child get = child.getField();
        child.setField(new Child(true)); // field

        Face fun = Child::fun;
        child
                .faceMethod(face1 -> Child::fun);
    }
}
