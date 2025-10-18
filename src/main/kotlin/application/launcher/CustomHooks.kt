package com.example.application.launcher

import com.example.infrastructure.verticle.factory.CustomVerticleFactory
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.launcher.application.HookContext
import io.vertx.launcher.application.VertxApplicationHooks
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CustomHooks : VertxApplicationHooks {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @OptIn(ExperimentalTime::class)
    override fun beforeStartingVertx(context: HookContext?) {
        logger.info("[BEFORE_STARTING_VERTX] Setting Vert.x options at: ${Clock.System.now()}")

        val options = context?.vertxOptions()

        options?.let {
            it.setEventLoopPoolSize(Runtime.getRuntime().availableProcessors() * 2)
            it.setWorkerPoolSize(20)
            it.setInternalBlockingPoolSize(20)
            it.setPreferNativeTransport(true)
        }

        super.beforeStartingVertx(context)
    }

    @OptIn(ExperimentalTime::class)
    override fun afterVertxStarted(context: HookContext?) {
        logger.info("[AFTER_VERTX_STARTED] Registering Verticle factory")

        context?.vertx()?.registerVerticleFactory(CustomVerticleFactory())

        logger.info("[AFTER_VERTX_STARTED] Verticle factory registered ${Clock.System.now()}")

        super.afterVertxStarted(context)
    }

    @OptIn(ExperimentalTime::class)
    override fun beforeDeployingVerticle(context: HookContext?) {
        logger.info("[BEFORE_DEPLOYING_VERTICLE] Deploying verticle at: ${Clock.System.now()}")

        super.beforeDeployingVerticle(context)
    }

    @OptIn(ExperimentalTime::class)
    override fun afterVerticleDeployed(context: HookContext?) {
        logger.info("[AFTER_VERTICLE_DEPLOYED] Verticle deployed at: ${Clock.System.now()}",)

        super.afterVerticleDeployed(context)
    }
}
