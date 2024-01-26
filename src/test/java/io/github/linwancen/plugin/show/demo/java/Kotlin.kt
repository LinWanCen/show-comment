package io.github.linwancen.plugin.show.demo.java

import io.github.linwancen.plugin.show.demo.java.obj.Child
import io.github.linwancen.plugin.show.demo.java.obj.Parent

/**
 * Kotlin
 *
 * [call]
 * [InDoc.field]
 * [InDoc.method1] [InDoc.method2]
 *
 * @author l
 */
object Kotlin : Parent() {
    /** call  */
    fun call() {
        Kotlin()
        abc()
        val child = Child(true)
        val parentMethod = child.parentMethod()
        val faceMethod = child.faceMethod(child)
        val `is` = child.isBool // NotAutoComment
        val get = child.getField()
        child.setField(Child(true))
    }

    private operator fun invoke() {
        TODO("Not yet implemented")
    }

    /**
     * abc
     */
    private fun abc() {
        TODO("Not yet implemented")
    }
}