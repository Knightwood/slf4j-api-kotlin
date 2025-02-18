package com.github.knightwood.slf4j.kotlin

import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.slf4j.event.Level
import org.slf4j.event.Level.*
import java.util.function.Supplier

/**
 * 扩展方法，使用kotlin的方式写fluent api
 * * fluent api
 *
 * ```
 * fun test1() {
 *     val logger = LoggerFactory.getLogger("main")
 *     //普通用法
 *     logger.debug("SECURITY".asMarker(), "Now time1 is {}", time());
 *
 *     //fluent-api
 *     //各个log方法都是setMessage().addArgument().log()的简化
 *     //有Supplier参数的setMessage()方法和addArgument()方法都是为了延迟计算，尽量在日志输出时才计算生成字符串。
 *     //最终输出日志时才会根据传入的消息字符串、占位符、参数等使用StringBuilder拼接。
 *     //那么，实际上不论你是否使用了Supplier参数，占位符都是正常可用的。
 *     logger.atDebug()
 *         .addMarker("SECURITY".asMarker())
 *         .addKeyValue("user", "Alice")
 *         .addKeyValue("age", 30)
 *         .addArgument { time() }// 添加参数也支持lambda延迟计算
 *         .addArgument(18)
 * //        .setCause(RuntimeException("Temperature too high"))
 *         .setMessage { "Temperature set to {}. Old temperature was {}." }// lambda表达式同样支持占位，毕竟他只是为了延迟计算，实际输出时才进行拼接。
 *         .log()
 * }
 * ```
 * * kotlin api
 *
 * ```
 * fun test2() {
 *     //getLogger传入的name相当于android log中的tag
 *     val logger = org.slf4j.LoggerFactory.getLogger("Main")
 *     val user = "tom"
 *     val marker = "SECURITY".asMarker();
 *     logger.info(marker = marker) { "loginUser1: $user" }
 *     logger.debug(marker = marker, throwable = NullPointerException("test")) { "loginUser2: $user" }
 *     logger.debug {
 *          //逻辑可以写在这，避免提前计算。
 *          //比如当前日志等级为Error，那么debug等级的日志输出就不会执行，
 *          //如果把逻辑写在外面，结果是即使不输出日志也会执行逻辑。
 *          val time = SimpleDateFormat("yyyy-MM-dd").format(Date())
 *          //最终拼接字符串
 *          "Now time is $time"
 *     }
 * }
 * ```
 */
object LoggerExtensions {
    val emptyArgs1 = emptyArray<Supplier<*>>()
    val emptyArgs2 = emptyArray<Any?>()
    val emptyPairs = emptyArray<Pair<String, Any?>>()
}

//<editor-fold desc="其他">

fun Logger.levelIfEnable(level: Level, marker: Marker?): Boolean {
    val delegate = this
    return when (level) {
        TRACE -> delegate.isTraceEnabled(marker)
        DEBUG -> delegate.isDebugEnabled(marker)
        INFO -> delegate.isInfoEnabled(marker)
        WARN -> delegate.isWarnEnabled(marker)
        ERROR -> delegate.isErrorEnabled(marker)
    }
}
//</editor-fold>

//<editor-fold desc="marker">

/**
 * 将字符串作为Marker
 *
 * 如果名称相同的 Marker 已经存在，则返回已存在的实例，确保了 Marker 的唯一性和共享性。
 */
fun String.asMarker(): Marker = MarkerFactory.getMarker(this)

/**
 * 将字符串作为Marker
 *
 * 始终创建新的 Marker 实例，不会重用已有的 Marker，且不会自动附加到任何 Logger 上下文
 */
fun String.asNewMarker(): Marker = MarkerFactory.getDetachedMarker(this)

//</editor-fold>


//<editor-fold desc="日志输出">

//<editor-fold desc="internal方法">
/**
 * slf4j的普通api中使用占位符输出、fluent-api中addArgument方法配合setMessage占位符，
 * 都是为了尽量少用字符串拼接，避免java中字符串直接拼接带来的性能问题
 * 以及addArgument和setMessage方法都引入Supplier，可以在当前日志等级需要输出日志时，延迟计算生成字符串。
 *
 * 这一系列都是为了提升性能，尽量在确定要输出日志时才计算生成字符串（Supplier），并使用StringBuilder拼接（占位符）。
 *
 * 在kotlin的字符串模版本质上是StringBuilder的append拼接，slf4j的占位符也是StringBuilder的append拼接
 * 字符串模版性能很好、易用，slf4j又可以使用Supplier延迟计算，那么，
 * 我们可以抛弃slf4j的占位符、addArgument方法，直接使用setMessage(Supplier:supplier)，
 * 传递kotlin的字符串模版，效果是一样的。
 */
internal fun Logger.log(
    level: Level,
    marker: Marker? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    throwable: Throwable? = null,
    msgProvider: Supplier<String>,
) {
    this.atLevel(level)
        .apply {
            pairs.forEach {
                addKeyValue(it.first, it.second)
            }
            if (marker != null) {
                addMarker(marker)
            }
            if (throwable != null) {
                setCause(throwable)
            }
        }
        .setMessage(msgProvider)
        .log()
}

//</editor-fold>


//<editor-fold desc="Info">

fun Logger.info(
    marker: Marker,
    throwable: Throwable? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    msgProvider: Supplier<String>
) = log(level = Level.INFO, marker = marker, throwable = throwable, pairs = pairs, msgProvider = msgProvider)

fun Logger.info(
    throwable: Throwable? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    msgProvider: Supplier<String>
) = log(level = Level.INFO, marker = null, throwable = throwable, pairs = pairs, msgProvider = msgProvider)

//</editor-fold>

//<editor-fold desc="Debug">
fun Logger.debug(
    marker: Marker,
    throwable: Throwable? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    msgProvider: Supplier<String>
) = log(level = Level.DEBUG, marker = marker, throwable = throwable, pairs = pairs, msgProvider = msgProvider)

fun Logger.debug(
    throwable: Throwable? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    msgProvider: Supplier<String>
) = log(level = Level.DEBUG, marker = null, throwable = throwable, pairs = pairs, msgProvider = msgProvider)

//</editor-fold>

//<editor-fold desc="Warn">
fun Logger.warn(
    marker: Marker,
    throwable: Throwable? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    msgProvider: Supplier<String>
) = log(level = Level.WARN, marker = marker, throwable = throwable, pairs = pairs, msgProvider = msgProvider)

fun Logger.warn(
    throwable: Throwable? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    msgProvider: Supplier<String>
) = log(level = Level.WARN, marker = null, throwable = throwable, pairs = pairs, msgProvider = msgProvider)
//</editor-fold>

//<editor-fold desc="Error">
fun Logger.error(
    marker: Marker,
    throwable: Throwable? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    msgProvider: Supplier<String>
) = log(level = Level.ERROR, marker = marker, throwable = throwable, pairs = pairs, msgProvider = msgProvider)

fun Logger.error(
    throwable: Throwable? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    msgProvider: Supplier<String>
) = log(level = Level.ERROR, marker = null, throwable = throwable, pairs = pairs, msgProvider = msgProvider)
//</editor-fold>

//<editor-fold desc="TRACE">
fun Logger.trace(
    marker: Marker,
    throwable: Throwable? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    msgProvider: Supplier<String>
) = log(level = Level.TRACE, marker = marker, throwable = throwable, pairs = pairs, msgProvider = msgProvider)

fun Logger.trace(
    throwable: Throwable? = null,
    pairs: Array<out Pair<String, Any?>> = LoggerExtensions.emptyPairs,
    msgProvider: Supplier<String>
) = log(level = Level.TRACE, marker = null, throwable = throwable, pairs = pairs, msgProvider = msgProvider)
//</editor-fold>

//</editor-fold>
