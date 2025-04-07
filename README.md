参照于log4j api的kotlin库，对于slf4j-api使用kotlin进行了包装。
首先，使用slf4j，我们要知道两件事：
1. slf4j不鼓励直接进行字符串拼接，而是使用{}这样的占位符。
2. slf4j支持Supplier接口（需要配合fluent api），可以在日志输出时才进行必要的逻辑计算，避免在不需要打印日志时耗费资源。

## 获取logger方式

1. 在类中直接引入
```kotlin
class Main {
    fun test(){
        kLogger.info("hello")
    }
}
```

2. 继承Logging接口
```kotlin
class Main2{
    fun test(){
        kLogger.info("hello")
    }
    companion object :Logging{}
}
```

3. 调用logger方法
```kotlin
    val logger1 = logFor("test")
```

## kotlin的包装
### kotlin api
在kotlin中，字符串模版是StringBuilder的简化，所以kotlin中可以采用字符串模版来替代占位符。

 ```
 fun test2() {
     val logger = org.slf4j.LoggerFactory.getLogger("Main")
     val user = "tom"
     val marker = "SECURITY".asMarker();
     logger.info(marker = marker) { "loginUser1: $user" }
     logger.debug(marker = marker, throwable = NullPointerException("test")) { "loginUser2: $user" }
     logger.debug {
          //逻辑可以写在这，避免提前计算。
          //比如当前日志等级为Error，那么debug等级的日志输出就不会执行，
          //如果把逻辑写在外面，结果是即使不输出日志也会执行逻辑。
          val time = SimpleDateFormat("yyyy-MM-dd").format(Date())
          //最终拼接字符串
          "Now time is $time"
     }
 }
 ```


### fluent api & 占位符 &  Supplier接口
 ```
 fun test1() {
     val logger = LoggerFactory.getLogger("main")
     //普通用法，不支持Supplier参数，但使用了占位符
     logger.debug("SECURITY".asMarker(), "Now time1 is {}", time());

     //fluent-api
     //log方法时末端函数，且每个log方法都是setMessage().addArgument().log()的简化调用
     //有Supplier参数的setMessage()方法和addArgument()方法都是为了延迟计算，尽量在日志输出时才计算。
     //最终输出日志时才会根据传入的消息字符串、占位符、参数等使用StringBuilder拼接。
     //ps：在setMessage中不论你是否使用了Supplier接口，占位符都是正常可用的。
     logger.atDebug()
         .addMarker("SECURITY".asMarker())
         .addKeyValue("user", "Alice")
         .addKeyValue("age", 30)
         .addArgument { time() }// 添加参数也支持lambda延迟计算
         .addArgument(18)
 //        .setCause(RuntimeException("Temperature too high"))
         .setMessage { "Temperature set to {}. Old temperature was {}." }// lambda表达式同样支持占位，毕竟他只是为了延迟计算，实际输出时才进行拼接。
         .log()
 }
 ```



## ContextStack
 1. 记录多层调用的上下文信息：例如，在多层嵌套的方法调用中，每一层可以将当前方法名或关键信息推入栈中，以便在日志中显示完整的调用路径。
 2. 跟踪事务或请求的上下文：在一个复杂的业务流程中，可以在每个步骤中推入相关信息，以便在日志中清晰地看到整个流程的执行路径。

 Provides stack-based storage for contextual data used in logging. This
 is provided as a facade around [MDC] operations.

 假设你有一个多层嵌套的方法调用，每层方法调用时需要记录当前方法名以形成调用链：
 ```
 val logger = LoggerFactory.getLogger("MyLogger")

 fun main() {
     try {
         ContextStack.push("main")
         method1()
     } finally {
         ContextStack.pop()
     }
 }

 fun method1() {
     try {
         ContextStack.push("method1")
         method2()
     } finally {
         ContextStack.pop()
     }
 }

 fun method2() {
     try {
         ContextStack.push("method2")
         logWithContext()
     } finally {
         ContextStack.pop()
     }
 }

 fun logWithContext() {
     val context = ContextStack.view.joinToString(" -> ")
     logger.debug("Executing with context: $context")
 }

 ```

## MDCMap

 1. 记录请求或事务的上下文信息：例如，在处理 HTTP 请求时，可以将请求 ID、用户 ID 等信息放入 ContextMap 中，以便在日志中显示这些关键信息。
 2. 跨多个日志记录器共享上下文信息：在一个复杂的业务流程中，可以在不同部分的日志记录中共享相同的上下文信息。

 假设你有一个 Web 应用程序，每个请求都需要记录唯一的请求 ID 和用户 ID：
 ```
 val logger = LoggerFactory.getLogger("MyLogger")

 fun handleRequest(userId: String) {
     try {
         // 设置请求上下文
         val requestId = UUID.randomUUID().toString()
         ContextMap["requestId"] = requestId
         ContextMap["userId"] = userId

         // 处理请求逻辑
         processPayment()

         // 记录日志
         logger.info("Request processed successfully.")
     } finally {
         // 清理上下文
         ContextMap.clear()
     }
 }

 fun processPayment() {
     // 模拟支付处理逻辑
     logger.debug("Processing payment for user ${ContextMap["userId"]}")
 }
 ```
 输出示例
 如果 INFO 和 DEBUG 级别启用，上述代码的日志输出可能如下：
 ```
 INFO MyLogger - Request processed successfully.
 DEBUG MyLogger - Processing payment for user user123
 ```
  注意事项
 1. 线程安全：MDC 是线程本地的，因此 ContextMap 也是线程安全的。每个线程都有自己的 MDC 上下文。
 2. 性能影响：频繁操作上下文映射可能会对性能有一定影响，特别是在高并发场景下。确保只在必要时使用这些操作。
 3. 清理资源：确保在适当的地方清理上下文（如使用 try-finally 或 Kotlin 的 use 函数），以避免内存泄漏。

 通过合理使用 ContextMap，你可以更方便地管理和记录复杂的上下文信息，从而提高日志的可读性和调试效率。
