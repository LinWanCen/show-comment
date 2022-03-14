package io.github.linwancen.plugin.show.demo.java.obj;

/** Child */
public class Child extends Parent implements Face {

    public Child() {
    }

    /** Child(boolean bool) */
    public Child(boolean bool) {
        this.bool = bool;
    }


    @Override
    public Parent parentMethod() {
        return null;
    }

    @Override
    public Face faceMethod(Face face) {
        return null;
    }

    /** String fun */
    public String fun;

    public static Face setFun(Face face) {
        return null;
    }

    public Child haveNotDoc(Face face) {
        return null;
    }

    /** bool */
    public boolean bool;

    public boolean isBool() {
        return bool;
    }


    /** field */
    public Child field;

    public Child getField() {
        return field;
    }

    public void setField(Child field) {
        this.field = field;
    }
}
