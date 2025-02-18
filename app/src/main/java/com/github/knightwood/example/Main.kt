package com.github.knightwood.example

import ch.qos.logback.classic.LoggerContext
import com.github.knightwood.slf4j.kotlin.asMarker
import com.github.knightwood.slf4j.kotlin.info
import org.slf4j.LoggerFactory

class Main {

}

fun main(){
    val logger = LoggerFactory.getLogger("Main")
    val tag="tag1"
    logger.info(tag.asMarker()){"Hello, World!"}

    // logback.xml配置文件中使用了异步输出，程序结束时，手动刷新日志
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    context.stop() // 确保所有日志都被刷新
}
