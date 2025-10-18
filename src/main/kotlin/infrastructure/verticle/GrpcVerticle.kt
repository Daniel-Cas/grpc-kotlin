package com.example.infrastructure.verticle

import com.example.application.service.HelloWorldService
import io.grpc.CompressorRegistry
import io.grpc.DecompressorRegistry
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle
import java.util.concurrent.TimeUnit

class GrpcVerticle : CoroutineVerticle() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val defaultPort: Int = 9090

    private lateinit var grpcServer: Server

    override suspend fun start() = try {
        val port = config.getInteger("port", defaultPort)

        grpcServer = ServerBuilder.forPort(port)
            .addService(HelloWorldService())
            .addService(ProtoReflectionService.newInstance())
            .keepAliveTime(30, TimeUnit.SECONDS)
            .keepAliveTimeout(5, TimeUnit.SECONDS)
            .permitKeepAliveWithoutCalls(true)
            .maxInboundMessageSize(4 * 1024 * 1024)
            .compressorRegistry(CompressorRegistry.getDefaultInstance())
            .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
            .build()
            .start()

        println("gRPC server started, listening on ${grpcServer.port}")

    } catch (e: Exception) {
        logger.error("[START] Error starting grpc server", e)

        throw e
    }

    override suspend fun stop() {
        try {
            logger.info("[STOP] Stopping grpc server")
            grpcServer.shutdown()

            if (!grpcServer.awaitTermination(5, TimeUnit.SECONDS)) {
                grpcServer.shutdownNow()
            }
        } catch (e: Exception) {
            logger.error("[STOP] Error stopping grpc server", e)
        }
    }
}