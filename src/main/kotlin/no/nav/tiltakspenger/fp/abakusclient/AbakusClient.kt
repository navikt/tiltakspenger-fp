package no.nav.tiltakspenger.fp.abakusclient


import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.tiltakspenger.fp.Configuration
import no.nav.tiltakspenger.fp.abakusclient.models.Periode
import no.nav.tiltakspenger.fp.abakusclient.models.Person
import no.nav.tiltakspenger.fp.abakusclient.models.Request
import no.nav.tiltakspenger.fp.abakusclient.models.YtelseV1
import no.nav.tiltakspenger.fp.abakusclient.models.Ytelser
import no.nav.tiltakspenger.fp.defaultHttpClient
import no.nav.tiltakspenger.fp.defaultObjectMapper
import java.time.LocalDate


class AbakusClient(
    private val config: AbakusClientConfig = Configuration.abakusClientConfig(),
    private val objectMapper: ObjectMapper = defaultObjectMapper(),
    private val getToken: suspend () -> String,
    engine: HttpClientEngine? = null,
    private val httpClient: HttpClient = defaultHttpClient(
        objectMapper = objectMapper,
        engine = engine
    ) {}
) {
    companion object {
        const val navCallIdHeader = "Nav-Call-Id"
    }

    @Suppress("TooGenericExceptionThrown")
    suspend fun hentYtelser(ident: String, fom: LocalDate, tom: LocalDate, behovId: String): List<YtelseV1> {
        val httpResponse = httpClient.preparePost("${config.baseUrl}/ekstern/hentVedtakForPerson") {
            header(navCallIdHeader, behovId)
            bearerAuth(getToken())
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(
                Request(
                    person = Person(ident),
                    periode = Periode(fom = fom, tom = tom),
                    ytelser = listOf(Ytelser.FORELDREPENGER)
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
