package no.nav.tiltakspenger.fp.abakusclient

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import mu.KotlinLogging
import no.nav.tiltakspenger.fp.Configuration
import no.nav.tiltakspenger.fp.abakusclient.models.Periode
import no.nav.tiltakspenger.fp.abakusclient.models.Person
import no.nav.tiltakspenger.fp.abakusclient.models.Request
import no.nav.tiltakspenger.fp.abakusclient.models.Request2
import no.nav.tiltakspenger.fp.abakusclient.models.YtelseV1
import no.nav.tiltakspenger.fp.abakusclient.models.Ytelser
import no.nav.tiltakspenger.fp.abakusclient.models.YtelserInput
import no.nav.tiltakspenger.fp.defaultHttpClient
import no.nav.tiltakspenger.fp.defaultObjectMapper
import java.time.LocalDate

private val SECURELOG = KotlinLogging.logger("tjenestekall")

class AbakusClient(
    private val config: AbakusClientConfig = Configuration.abakusClientConfig(),
    private val objectMapper: ObjectMapper = defaultObjectMapper(),
    private val getToken: suspend () -> String,
    engine: HttpClientEngine? = null,
    private val httpClient: HttpClient = defaultHttpClient(
        objectMapper = objectMapper,
        engine = engine
    )
) {
    companion object {
        const val navCallIdHeader = "Nav-Call-Id"
    }

    suspend fun hentYtelser(ident: String, fom: LocalDate, tom: LocalDate, behovId: String): List<YtelseV1> {
        val httpResponse =
            httpClient.preparePost("${config.baseUrl}/fpabakus/ekstern/api/ytelse/v1/hent-vedtatte/for-ident") {
                header(navCallIdHeader, behovId)
                bearerAuth(getToken())
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(
                    Request(
                        person = Person(ident = ident),
                        periode = Periode(fom = fom, tom = tom),
                        ytelser = YtelserInput.values().toList()
                    )
                )
            }.execute()
        return when (httpResponse.status) {
            HttpStatusCode.OK -> httpResponse.call.response.body()
            else -> throw RuntimeException("error (responseCode=${httpResponse.status.value}) from Abakus")
        }
    }

    suspend fun hentYtelserv2(ident: String, fom: LocalDate, tom: LocalDate, behovId: String): List<YtelseV1> {
        SECURELOG.info { getToken }
        val httpResponse =
            httpClient.preparePost("${config.baseUrl}/fpabakus/ekstern/api/ytelse/v1/hent-vedtatte/hent-ytelse-vedtak") {
                header(navCallIdHeader, behovId)
                bearerAuth(getToken())
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(
                    Request2(
                        person = Person(ident = ident),
                        periode = Periode(fom = fom, tom = tom),
                        ytelser = Ytelser.values().toList()
                    )
                )
            }.execute()
        return when (httpResponse.status) {
            HttpStatusCode.OK -> httpResponse.call.response.body()
            else -> throw RuntimeException("error (responseCode=${httpResponse.status.value}) from Abakus")
        }
    }

    data class AbakusClientConfig(
        val baseUrl: String,
    )
}
