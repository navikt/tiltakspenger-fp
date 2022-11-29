package no.nav.tiltakspenger.fp


import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import mu.KotlinLogging
import java.time.Duration

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
private const val SIXTY_SECONDS = 60L

// engine skal brukes primært i test-øyemed, når man sender med MockEngine.
// Forøvrig kan man la den være null.
fun defaultHttpClient(
    objectMapper: ObjectMapper,
    engine: HttpClientEngine? = null,
    configBlock: HttpClientConfig<*>.() -> Unit = {},
    engineConfigBlock: CIOEngineConfig.() -> Unit = {}
) = engine?.let {
    HttpClient(engine) {
        apply(defaultSetup(objectMapper))
        apply(configBlock)
    }
} ?: HttpClient(CIO) {
    apply(defaultSetup(objectMapper))
    apply(configBlock)
    engine(engineConfigBlock)
}

private fun defaultSetup(objectMapper: ObjectMapper): HttpClientConfig<*>.() -> Unit = {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }
    install(HttpTimeout) {
        connectTimeoutMillis = Duration.ofSeconds(SIXTY_SECONDS).toMillis()
        requestTimeoutMillis = Duration.ofSeconds(SIXTY_SECONDS).toMillis()
        socketTimeoutMillis = Duration.ofSeconds(SIXTY_SECONDS).toMillis()
    }

    this.install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                LOG.info("HttpClient detaljer logget til securelog")
                SECURELOG.info(message)
            }
        }
        level = LogLevel.ALL
    }
    this.expectSuccess = true
}

fun defaultObjectMapper(): ObjectMapper = ObjectMapper()
    .registerModule(KotlinModule.Builder().build())
    .registerModule(JavaTimeModule())
    .setDefaultPrettyPrinter(
        DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        }
    )
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
