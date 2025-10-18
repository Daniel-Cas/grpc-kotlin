package com.example.application.service

import hello.HelloWorld.HelloRequest
import hello.HelloWorld.HelloResponse
import hello.HelloWorld.HelloToRequest
import hello.HelloWorldServiceGrpcKt
import hello.helloResponse
import io.grpc.Status
import io.grpc.StatusException
import io.vertx.core.internal.logging.LoggerFactory
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.util.concurrent.Executors

class HelloWorldService : HelloWorldServiceGrpcKt.HelloWorldServiceCoroutineImplBase(
    coroutineContext = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher(),
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun sayHello(request: HelloRequest): HelloResponse = helloResponse {
        logger.info("[SAY_HELLO] Receiving request at ${Instant.now()} from ${request.name}")

        message = "Hello, ${request.name}! Greetings from gRPC with Vert.x"
        timestamp = Instant.now().toEpochMilli()
    }

    override suspend fun sayHelloTo(request: HelloToRequest): HelloResponse = helloResponse {
        logger.info("[SAY_HELLO_TO] Receiving request at ${Instant.now()}, from ${request.name}")

        if (request.name.isBlank()) {
            logger.warn("[SAY_HELLO_TO] Name is blank")

            throw StatusException(Status.INVALID_ARGUMENT.withDescription("Name cannot be blank"))
        }

        if (request.age !in 0..150) {
            logger.warn("[SAY_HELLO_TO] Received request with invalid age: ${request.age}")

            throw StatusException(Status.INVALID_ARGUMENT.withDescription("Age must be between 0 and 150"))
        }

        message = buildPersonalizedGreeting(request)
        timestamp = Instant.now().toEpochMilli()
    }

    override fun sayHelloStream(request: HelloRequest): Flow<HelloResponse> = flow {
        logger.info("[SAY_HELLO_STREAM] Receiving request at ${Instant.now()}, from ${request.name}")

        val greetings = listOf(
            "¡Hola, ${request.name}!",
            "Welcome, ${request.name}!",
            "Bienvenue, ${request.name}!",
        )

        greetings.forEachIndexed { index, greeting ->
            emit(
                helloResponse {
                    message = "$greeting (message ${index + 1} of ${greetings.size})"
                    timestamp = Instant.now().toEpochMilli()
                }
            )

            logger.info("[SAY_HELLO_STREAM] Sent streaming message ${index + 1} to ${request.name}")
        }

        logger.info("[SAY_HELLO_STREAM] Finished streaming request from ${request.name}")
    }

    private fun buildPersonalizedGreeting(request: HelloToRequest): String {
        val title = if (request.title.isNotBlank()) "${request.title} " else ""
        val ageMessage = when {
            request.age == 0 -> ""
            request.age in 1..17 -> " You're quite young!"
            request.age in 18..65 -> " Hope you're having a great day!"
            request.age > 65 -> " Respect for your wisdom!"

            else -> ""
        }

        val greeting = when (request.language.lowercase()) {
            "es", "spanish" -> "¡Hola"
            "fr", "french" -> "Bonjour"
            "de", "german" -> "Guten Tag"
            "it", "italian" -> "Ciao"
            else -> "Hello"
        }

        return "$greeting, $title${request.name}!$ageMessage"
    }
}
