package io.github.linwancen.plugin.show.demo;

import io.github.linwancen.plugin.show.demo.method.Child;
import io.github.linwancen.plugin.show.demo.method.Parent;

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
