package com.github.knightwood.slf4j.kotlin

import org.slf4j.MDC

typealias MDCMap = ContextMap

/**
 * 1. 记录请求或事务的上下文信息：例如，在处理 HTTP 请求时，可以将请求 ID、用户 ID 等信息放入 ContextMap 中，以便在日志中显示这些关键信息。
 * 2. 跨多个日志记录器共享上下文信息：在一个复杂的业务流程中，可以在不同部分的日志记录中共享相同的上下文信息。
 *
 * 假设你有一个 Web 应用程序，每个请求都需要记录唯一的请求 ID 和用户 ID：
 * ```
 * val logger = LoggerFactory.getLogger("MyLogger")
 *
 * fun handleRequest(userId: String) {
 *     try {
 *         // 设置请求上下文
 *         val requestId = UUID.randomUUID().toString()
 *         ContextMap["requestId"] = requestId
 *         ContextMap["userId"] = userId
 *
 *         // 处理请求逻辑
 *         processPayment()
 *
 *         // 记录日志
 *         logger.info("Request processed successfully.")
 *     } finally {
 *         // 清理上下文
 *         ContextMap.clear()
 *     }
 * }
 *
 * fun processPayment() {
 *     // 模拟支付处理逻辑
 *     logger.debug("Processing payment for user ${ContextMap["userId"]}")
 * }
 * ```
 * 输出示例
 * 如果 INFO 和 DEBUG 级别启用，上述代码的日志输出可能如下：
 * ```
 * INFO MyLogger - Request processed successfully.
 * DEBUG MyLogger - Processing payment for user user123
 * ```
 *  注意事项
 * 1. 线程安全：MDC 是线程本地的，因此 ContextMap 也是线程安全的。每个线程都有自己的 MDC 上下文。
 * 2. 性能影响：频繁操作上下文映射可能会对性能有一定影响，特别是在高并发场景下。确保只在必要时使用这些操作。
 * 3. 清理资源：确保在适当的地方清理上下文（如使用 try-finally 或 Kotlin 的 use 函数），以避免内存泄漏。
 *
 * 通过合理使用 ContextMap，你可以更方便地管理和记录复杂的上下文信息，从而提高日志的可读性和调试效率。
 *
 * Provides map-based storage for contextual data used in logging with
 * Logback's MDC. This is provided as a facade around [MDC] map operations.
 *
 * @since 1.3.0
 */
object ContextMap {

    /**
     * Checks if the provided key is defined in the current MDC map.
     *
     * Example usage:
     * ```kotlin
     * if ("requestId" !in MDCMap) {
     *   MDCMap["requestId"] = UUID.randomUUID().toString()
     * }
     * ```
     */
    operator fun contains(key: String): Boolean = MDC.get(key) != null

    /**
     * Gets the value corresponding to the provided key from the current MDC
     * map.
     *
     * Example usage:
     * ```kotlin
     * val value = MDCMap["key"]
     * ```
     */
    operator fun get(key: String): String? = MDC.get(key)

    /**
     * Puts a value in the current MDC map for the given key. If the value is
     * null, this is effectively the same as removing the key.
     *
     * Example usage:
     * ```kotlin
     * MDCMap["key"] = "value"
     * ```
     */
    operator fun set(key: String, value: String?) {
        if (value == null) {
            MDC.remove(key)
        } else {
            MDC.put(key, value)
        }
    }

    /**
     * Puts a key/value pair into the current MDC map.
     *
     * Example usage:
     * ```kotlin
     * MDCMap += "key" to "value"
     * ```
     */
    operator fun plusAssign(pair: Pair<String, String?>) {
        val (key, value) = pair
        this[key] = value
    }

    /**
     * Puts all the entries from the provided map into the current MDC map.
     *
     * Example usage:
     * ```kotlin
     * MDCMap += mapOf("key" to "value", "otherKey" to "anotherValue")
     * ```
     */
    operator fun plusAssign(map: Map<String, String>) {
        map.forEach { (key, value) -> this[key] = value }
    }

    /**
     * Removes the provided key from the current MDC map.
     *
     * Example usage:
     * ```kotlin
     * MDCMap -= "key"
     * ```
     */
    operator fun minusAssign(key: String) {
        MDC.remove(key)
    }

    /**
     * Removes the provided keys from the current MDC map.
     *
     * Example usage:
     * ```kotlin
     * MDCMap -= listOf("key1", "key2", "key3")
     * ```
     */
    operator fun minusAssign(keys: Iterable<String>) {
        keys.forEach { key -> MDC.remove(key) }
    }

    /**
     * Indicates if the current MDC map is empty.
     */
    val empty: Boolean
        get() = MDC.getCopyOfContextMap()?.isEmpty() ?: false

    /**
     * Provides an immutable view of the current MDC map.
     */
    val view: Map<String, String>
        get() = MDC.getCopyOfContextMap() ?: emptyMap()

    /**
     * Provides a mutable copy of the current MDC map.
     */
    fun copy(): MutableMap<String, String> {
        val contextMap = MDC.getCopyOfContextMap()
        return contextMap.toMutableMap()
    }

    /**
     * Clears the current MDC map of all data.
     */
    fun clear() {
        MDC.clear()
    }
}
