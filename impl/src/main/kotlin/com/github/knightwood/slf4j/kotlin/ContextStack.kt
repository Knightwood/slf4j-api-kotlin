package com.github.knightwood.slf4j.kotlin


import org.slf4j.MDC
import java.util.*

typealias MDCStack = ContextStack

/**
 * 1. 记录多层调用的上下文信息：例如，在多层嵌套的方法调用中，每一层可以将当前方法名或关键信息推入栈中，以便在日志中显示完整的调用路径。
 * 2. 跟踪事务或请求的上下文：在一个复杂的业务流程中，可以在每个步骤中推入相关信息，以便在日志中清晰地看到整个流程的执行路径。
 *
 * Provides stack-based storage for contextual data used in logging. This
 * is provided as a facade around [MDC] operations.
 *
 * 假设你有一个多层嵌套的方法调用，每层方法调用时需要记录当前方法名以形成调用链：
 * ```
 * val logger = LoggerFactory.getLogger("MyLogger")
 *
 * fun main() {
 *     try {
 *         ContextStack.push("main")
 *         method1()
 *     } finally {
 *         ContextStack.pop()
 *     }
 * }
 *
 * fun method1() {
 *     try {
 *         ContextStack.push("method1")
 *         method2()
 *     } finally {
 *         ContextStack.pop()
 *     }
 * }
 *
 * fun method2() {
 *     try {
 *         ContextStack.push("method2")
 *         logWithContext()
 *     } finally {
 *         ContextStack.pop()
 *     }
 * }
 *
 * fun logWithContext() {
 *     val context = ContextStack.view.joinToString(" -> ")
 *     logger.debug("Executing with context: $context")
 * }
 *
 * ```
 *
 * @since 1.3.0
 */
object ContextStack {

    private const val STACK_KEY = "contextStack"

    private val stack: Deque<String>
        get() {
            val stackString = MDC.get(STACK_KEY) ?: return ArrayDeque()
            return ArrayDeque(stackString.split(","))
        }

    private fun setStack(stack: Deque<String>) {
        if (stack.isEmpty()) {
            MDC.remove(STACK_KEY)
        } else {
            MDC.put(STACK_KEY, stack.joinToString(","))
        }
    }

    /**
     * 获取栈深度
     * Returns the depth of the current context stack. If the context stack is
     * disabled, this always returns `0`.
     */
    val depth: Int
        get() = stack.size

    /**
     * Indicates whether the current context stack is empty. If the context
     * stack is disabled, this always returns `true`.
     */
    val empty: Boolean
        get() = depth == 0

    /**
     * Returns an immutable view of the current context stack. If the context
     * stack is disabled, this always returns an empty stack.
     */
    val view: List<String>
        get() = stack.toList()

    /**
     * Returns a mutable copy of the current context stack. If the context
     * stack is disabled, this always returns a new stack.
     */
    fun copy(): Deque<String> = ArrayDeque(stack)

    /**
     * Clears the current context stack of all its messages. If the context
     * stack is disabled, this does nothing.
     */
    fun clear() = setStack(ArrayDeque())

    /**
     * Overwrites the current context stack using the provided collection of
     * messages. If the context stack is disabled, this does nothing.
     */
    fun set(stack: Collection<String>) = setStack(ArrayDeque(stack))

    /**
     * Removes and returns the top-most message in the current context stack.
     * If the context stack is empty or disabled, this will always return an
     * empty string.
     */
    fun pop(): String {
        val stack = stack
        return if (stack.isEmpty()) "" else stack.removeLast().also { setStack(stack) }
    }

    /**
     * Returns without removing the top-most message in the current context
     * stack. If the context stack is empty or disabled, this will always
     * return an empty string.
     */
    fun peek(): String {
        val stack = stack
        return if (stack.isEmpty()) "" else stack.last
    }

    /**
     * Adds the provided message to the top of the current context stack. If
     * the context stack is disabled, this does nothing.
     */
    fun push(message: String) {
        val stack = stack
        stack.addLast(message)
        setStack(stack)
    }

    /**
     * Adds a formatted message to the top of the current context stack using
     * the provided parameterized message and arguments.
     */
    fun push(message: String, vararg args: Any?) {
        push(String.format(message, *args))
    }

    /**
     * Trims the current context stack to at most the given depth.
     */
    fun trim(depth: Int) {
        val stack = stack
        while (stack.size > depth) {
            stack.removeLast()
        }
        setStack(stack)
    }
}
