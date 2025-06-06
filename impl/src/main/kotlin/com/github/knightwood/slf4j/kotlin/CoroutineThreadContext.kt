package com.github.knightwood.slf4j.kotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Snapshot of the current [ContextStack] and [ContextStack].
 *
 * @param map immutable view of the current context map
 * @param stack immutable view of the current context stack
 * @see ContextStack.view
 * @see ContextStack.view
 */
data class ThreadContextData(
    val map: Map<String, String>? = ContextMap.view,
    val stack: Collection<String>? = ContextStack.view
) {
    operator fun plus(data: ThreadContextData) = ThreadContextData(
        map = this.map.orEmpty() + data.map.orEmpty(),
        stack = this.stack.orEmpty() + data.stack.orEmpty(),
    )
}

/**
 * SLF4J [MDC] element for [CoroutineContext].
 *
 * This is based on the SLF4J MDCContext maintained by Jetbrains:
 * https://github.com/Kotlin/kotlinx.coroutines/blob/master/integration/kotlinx-coroutines-slf4j/src/MDCContext.kt
 *
 * Example:
 *
 * ```
 * ContextMap["kotlin"] = "rocks" // Put a value into the Thread context
 *
 * launch(CoroutineThreadContext()) {
 *     logger.info { "..." }   // The Thread context contains the mapping here
 * }
 * ```
 *
 * Note, that you cannot update Thread context from inside of the coroutine simply
 * using [MDC.put] or [ContextStack.set]. These updates are going to be lost on the next suspension and
 * reinstalled to the Thread context that was captured or explicitly specified in
 * [contextData] when this object was created on the next resumption.
 * Use `withContext(CoroutineThreadContext()) { ... }` to capture updated map of Thread keys and values
 * for the specified block of code.
 *
 * See [loggingContext] and [additionalLoggingContext] for convenience functions that make working with a
 * [CoroutineThreadContext] simpler.
 *
 * @param contextData the value of [Thread] context map and context stack.
 * Default value is the copy of the current thread's context map that is acquired via
 * [ContextStack.view] and [ContextStack.view].
 */
class CoroutineThreadContext(
    /**
     * The value of [Thread] context map.
     */
    val contextData: ThreadContextData = ThreadContextData()
) : ThreadContextElement<ThreadContextData>, AbstractCoroutineContextElement(Key) {
    /**
     * Key of [ThreadContext] in [CoroutineContext].
     */
    companion object Key : CoroutineContext.Key<CoroutineThreadContext>

    /** @suppress */
    override fun updateThreadContext(context: CoroutineContext): ThreadContextData {
        val oldState = ThreadContextData(ContextMap.view, ContextStack.view)
        setCurrent(contextData)
        return oldState
    }

    /** @suppress */
    override fun restoreThreadContext(context: CoroutineContext, oldState: ThreadContextData) {
        setCurrent(oldState)
    }

    private fun setCurrent(contextData: ThreadContextData) {
        ContextMap.clear()
        ContextStack.clear()
        contextData.map?.let { ContextMap += it }
        contextData.stack?.let { ContextStack.set(it) }
    }
}

/**
 * Convenience function to obtain a [CoroutineThreadContext] with the given map and stack, which default
 * to no context. Any existing logging context in scope is ignored.
 *
 * Example:
 *
 * ```
 * launch(loggingContext(mapOf("kotlin" to "rocks"))) {
 *     logger.info { "..." }   // The Thread context contains the mapping here
 * }
 * ```
 */
fun loggingContext(
    map: Map<String, String>? = null,
    stack: Collection<String>? = null,
): CoroutineThreadContext = CoroutineThreadContext(ThreadContextData(map = map, stack = stack))

/**
 * Convenience function to obtain a [CoroutineThreadContext] that inherits the current context (if any), plus adds
 * the context from the given map and stack, which default to nothing.
 *
 * Example:
 *
 * ```
 * launch(additionalLoggingContext(mapOf("kotlin" to "rocks"))) {
 *     logger.info { "..." }   // The Thread context contains the mapping plus whatever context was in scope at launch
 * }
 * ```
 */
fun additionalLoggingContext(
    map: Map<String, String>? = null,
    stack: Collection<String>? = null,
): CoroutineThreadContext = CoroutineThreadContext(ThreadContextData() + ThreadContextData(map = map, stack = stack))

/**
 * Run the given block with the provided logging context, which default to no context. Any existing logging context
 * in scope is ignored.
 *
 * Example:
 *
 * ```
 * withLoggingContext(mapOf("kotlin" to "rocks")) {
 *     logger.info { "..." }   // The Thread context contains the mapping
 * }
 * ```
 */
suspend fun <R> withLoggingContext(
    map: Map<String, String>? = null,
    stack: Collection<String>? = null,
    block: suspend CoroutineScope.() -> R,
): R = withContext(loggingContext(map, stack), block)

/**
 * Run the given block with the provided additional logging context. The given context is added to any existing
 * logging context in scope.
 *
 * Example:
 *
 * ```
 * withAdditionalLoggingContext(mapOf("kotlin" to "rocks")) {
 *     logger.info { "..." }   // The Thread context contains the mapping plus whatever context was in the scope previously
 * }
 * ```
 */
suspend fun <R> withAdditionalLoggingContext(
    map: Map<String, String>? = null,
    stack: Collection<String>? = null,
    block: suspend CoroutineScope.() -> R,
): R = withContext(additionalLoggingContext(map, stack), block)
