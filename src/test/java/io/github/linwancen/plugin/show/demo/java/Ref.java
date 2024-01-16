package io.github.linwancen.plugin.show.demo.java;

import io.github.linwancen.plugin.show.demo.java.obj.Anno;
import io.github.linwancen.plugin.show.demo.java.obj.Child;
import io.github.linwancen.plugin.show.demo.java.obj.Face;
import io.github.linwancen.plugin.show.demo.java.obj.Parent;
import io.github.linwancen.plugin.show.demo.java.obj.YesOrNoEnum;

@Anno
public class Ref implements Face {
    public static void method() {
        YesOrNoEnum enumDoc = YesOrNoEnum.YES;
        Parent noneNewMethod = new Call();
        Face noneNewDoc = new Ref();
        Child child = new Child(true);
        Child field = child.field.field;
        Child af = child.annoField;
        Child gaf = child.getAnnoField();
        boolean bool = child.field.bool;
        Face fun = Child::setFun;
        child
                .faceMethod(face1 -> Child::setFun);
    }

    /**
     * do not show this after @Override
     */
    @Override
    public Face faceMethod(Face face) {
        return null;
    }
}
