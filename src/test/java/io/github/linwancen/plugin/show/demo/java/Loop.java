package io.github.linwancen.plugin.show.demo.java;

import io.github.linwancen.plugin.show.demo.java.obj.Child;
import io.github.linwancen.plugin.show.demo.java.obj.Parent;

@SuppressWarnings("all")
public class Loop {
    public static void method() {
        Child child = new Child();
        while (child.bool) {
            if (child.isBool()) {
                return;
            }
        }

        Parent[] fors = new Child[]{};
        for (Parent f : fors) {
        }
    }
}
