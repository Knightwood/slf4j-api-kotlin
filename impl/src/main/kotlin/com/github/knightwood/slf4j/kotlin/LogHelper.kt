package com.github.knightwood.slf4j.kotlin

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 扩展变量获取logger
 */
inline val <T : Any> T.logger: Logger
    get() = LoggerFactory.getLogger(this.javaClass)

/**
 * android log风格命名
 */
inline val <T : Any> T.Log: Logger
    get() = LoggerFactory.getLogger(this.javaClass)

fun logger(name: String): Logger = LoggerFactory.getLogger(name)


fun Logger.i(tag: String, msg: String, throwable: Throwable? = null) {
    this.info("tag: {}, msg: {}", tag, msg, throwable)
}

fun Logger.v(tag: String, msg: String, throwable: Throwable? = null) {
    this.trace("tag: {}, msg: {}", tag, msg, throwable)
}

fun Logger.d(tag: String, msg: String, throwable: Throwable? = null) {
    this.debug("tag: {}, msg: {}", tag, msg, throwable)
}

fun Logger.w(tag: String, msg: String, throwable: Throwable? = null) {
    this.warn("tag: {}, msg: {}", tag, msg, throwable)
}

fun Logger.e(tag: String, msg: String, throwable: Throwable? = null) {
    this.error("tag: {}, msg: {}", tag, msg, throwable)
}

fun Logger.wtf(tag: String, msg: String, throwable: Throwable? = null) {
    this.error("[wtf] tag: {}, msg: {}", tag, msg, throwable)
}
