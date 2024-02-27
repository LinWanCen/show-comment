package io.github.linwancen.plugin.show.demo

/**
 * Foo
 */
class Foo {
    /** a */
    String a;
    /**
     * fun1
     */
    def fun1(){
        a = 0;
        fun2()
    }
    /**
     * fun2
     */
    def fun2(){

    }
}

/**
 * abc
 * {@link Foo#fun2}
 * @author name
 */
static def fun3(){
    Foo.
            fun2()
}