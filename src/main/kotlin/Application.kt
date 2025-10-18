package com.example

import com.example.application.launcher.CustomHooks
import com.example.application.launcher.CustomLauncher
import io.vertx.core.internal.logging.LoggerFactory

private val logger = LoggerFactory.getLogger("com.example.Application")

fun main(args: Array<String>) {
    val launcher = CustomLauncher(args, CustomHooks())

    val code = launcher.launch()

    logger.info("Application finished with exit code: $code")
}
