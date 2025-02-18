package com.github.knightwood.slf4j.kotlin

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Logging {
    val kLogger: Logger
        get() = LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass))

    /**
     * 作用是检查传入的类是否是一个伴生对象的类，如果是伴生对象，则返回其外部类。
     *
     * 这样可以确保日志记录器的名称始终是外部类的名称，而不是伴生对象的名称。
     *
     * 实际上，这要求伴生对象不能有名称，如果指定名称，则返回的是外部类+伴生对象名称
     *
     * 例如：
     *
     * ```
     * class Foo {
     *      companion object : Logging
     * }
     * ```
     *
     * 得到`Foo`
     *
     * ```
     * class Foo {
     *      companion object ABC: Logging
     * }
     * ```
     *
     * 得到`ABC`
     */
    private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
        val className = ofClass.name
        val companionSuffix = "\$Companion"
        return if (className.endsWith(companionSuffix)) {
            val outerClassName = className.substring(0, className.length - companionSuffix.length)
            try {
                Class.forName(outerClassName)
            } catch (e: ClassNotFoundException) {
                ofClass // 如果找不到外层类，则返回原始类
            }
        } else {
            ofClass
        }
    }


}
/**
 * Returns normalized context name.
 * * Execution within a class/object will return the full qualified
 *   class/object name, in case of nested classes/objects the most outer
 *   class/object is used.
 * * Execution outside of any class/object will return the full qualified
 *   file name without `.kt suffix.
 *
 * Usage: `val LOG = logger(contextName {})`
 *
 * @param context should always be `{}`
 * @return normalized context name
 */
fun contextName(context: () -> Unit): String = with(context::class.java.name) {
    when {
        contains("Kt$") -> substringBefore("Kt$")
        contains("$") -> substringBefore("$")
        else -> this
    }
}

/**
 * 作用是检查传入的类是否是一个伴生对象的类，并如果是，则返回其外部类。这样可以确保日志记录器的名称始终是外部类的名称，而不是伴生对象的名称。
 */
//    fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
//        return if (ofClass.enclosingClass?.kotlin?.companionObject?.java == ofClass) {
//            ofClass.enclosingClass!!
//        } else {
//            ofClass
//        }
//    }
