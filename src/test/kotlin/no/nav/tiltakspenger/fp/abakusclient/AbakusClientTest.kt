package no.nav.tiltakspenger.fp.abakusclient

import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import no.nav.tiltakspenger.fp.abakusclient.models.YtelseV1
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class AbakusClientTest {

    @Test
    fun `skal klare å deserialisere bodyen som returneres`() {
        val mockEngine = MockEngine { request ->
            respond(
                content = enkeltSvar,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val client = AbakusClient(
            getToken = { "whatever" },
            engine = mockEngine,
        )

        val response: List<YtelseV1> = runBlocking {
            client.hentYtelser(
                ident = "x",
                fom = LocalDate.MIN,
                tom = LocalDate.MAX,
                behovId = "y",
            )
        }
        response.size shouldBe 1
    }

    @Test
    fun `skal klare å deserialisere body med liste som returneres`() {
        val mockEngine = MockEngine { request ->
            respond(
                content = svarMedListe,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val client = AbakusClient(
            getToken = { "whatever" },
            engine = mockEngine,
        )

        val response: List<YtelseV1> = runBlocking {
            client.hentYtelser(
                ident = "x",
                fom = LocalDate.MIN,
                tom = LocalDate.MAX,
                behovId = "y",
            )
        }
        response.size shouldBe 2
    }

    private val enkeltSvar = """
                        [{
                            "version": "1.0",
                            "aktør": {"verdi": "2785133818346"},
                            "vedtattTidspunkt": "2022-12-09T15:58:56.46",
                            "ytelse": "FORELDREPENGER",
                            "saksnummer": "352010537",
                            "vedtakReferanse": "bda969a9-0005-4f86-856a-7d0b5cd73375",
                            "ytelseStatus": "LØPENDE",
                            "kildesystem": "FPSAK",
                            "periode": {"fom": "2022-12-08", "tom": "2022-12-21"},
                            "tilleggsopplysninger": null,
                            "anvist": [{
                                "periode": {"fom": "2022-12-08", "tom": "2022-12-21"},
                                "beløp": null,
                                "dagsats": {"verdi": 2077.00},
                                "utbetalingsgrad": {"verdi": 100.00},
                                "andeler": [{
                                    "arbeidsgiver": {"identType": "ORGNUMMER", "ident": "947064649"},
                                    "dagsats": {"verdi": 2077.00},
                                    "utbetalingsgrad": {"verdi": 100.00},
                                    "refusjonsgrad": {"verdi": 0.00},
                                    "inntektklasse": "ARBEIDSTAKER"
                                }]
                            }]
                        }]
    """.trimIndent()

    private val svarMedListe = """
        [
          {
            "version": "1.0",
            "aktør": {
                "verdi": "2785133818346"
            },
            "vedtattTidspunkt": "2019-06-03T00:00:00",
            "ytelse": "OPPLÆRINGSPENGER",
            "saksnummer": null,
            "vedtakReferanse": null,
            "ytelseStatus": "UNDER_BEHANDLING",
            "kildesystem": null,
            "periode": {
              "fom": "2019-06-03",
              "tom": "9999-12-31"
            },
            "tilleggsopplysninger": null,
            "anvist": []
          },
          {
            "version": "1.0",
            "aktør": {
              "verdi": "2785133818346"
            },
            "vedtattTidspunkt": "2019-06-11T00:00:00",
            "ytelse": "OPPLÆRINGSPENGER",
            "saksnummer": null,
            "vedtakReferanse": null,
            "ytelseStatus": "UNDER_BEHANDLING",
            "kildesystem": null,
            "periode": {
              "fom": "2019-06-11",
              "tom": "9999-12-31"
            },
            "tilleggsopplysninger": null,
            "anvist": []
          }
        ]
    """.trimIndent()
}
