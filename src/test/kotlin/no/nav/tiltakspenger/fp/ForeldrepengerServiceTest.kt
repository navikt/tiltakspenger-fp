package no.nav.tiltakspenger.fp

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.fp.abakusclient.AbakusClient
import no.nav.tiltakspenger.fp.abakusclient.models.Aktør
import no.nav.tiltakspenger.fp.abakusclient.models.Anvisning
import no.nav.tiltakspenger.fp.abakusclient.models.Desimaltall
import no.nav.tiltakspenger.fp.abakusclient.models.Kildesystem
import no.nav.tiltakspenger.fp.abakusclient.models.Periode
import no.nav.tiltakspenger.fp.abakusclient.models.Status
import no.nav.tiltakspenger.fp.abakusclient.models.YtelseV1
import no.nav.tiltakspenger.fp.abakusclient.models.YtelserOutput
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate
import java.time.LocalDateTime

internal class ForeldrepengerServiceTest {
    private val testRapid = TestRapid()

    private val ident = "04927799109"
    private val fom = LocalDate.of(2022, 1, 1)
    private val tom = LocalDate.of(2022, 1, 31)

    private val abakusClient = mockk<AbakusClient>()

    val service = ForeldrepengerService(
        rapidsConnection = testRapid,
        client = abakusClient,
    )

    @BeforeEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `Sjekk happy case`() {

        coEvery { abakusClient.hentYtelser(any(), any(), any(), any()) } returns listOf(
            mockYtelse,
        )
        coEvery { abakusClient.hentYtelserv2(any(), any(), any(), any()) } returns listOf(
            mockYtelse,
        )

        testRapid.sendTestMessage(behovMelding)

        with(testRapid.inspektør) {
            size shouldBe 1
            field(0, "ident").asText() shouldBe ident

            println(message(0).toPrettyString())
            JSONAssert.assertEquals(
                svar,
                message(0).toPrettyString(), JSONCompareMode.LENIENT,
            )
        }
    }

    private val beløp = 100
    private val sats = 50
    private val grad = 10

    private val mockYtelse = YtelseV1(
        version = "v1",
        aktør = Aktør(verdi = "aktørId"),
        vedtattTidspunkt = LocalDateTime.of(2022, 1, 1, 12, 0, 0, 0),
        ytelse = YtelserOutput.PLEIEPENGER_SYKT_BARN,
        saksnummer = "sakNr",
        vedtakReferanse = "Ref",
        ytelseStatus = Status.LØPENDE,
        kildesystem = Kildesystem.FPSAK,
        periode = Periode(
            fom = LocalDate.of(2022, 1, 1),
            tom = LocalDate.of(2022, 1, 31),
        ),
        tilleggsopplysninger = "Tillegg",
        anvist = listOf(
            Anvisning(
                periode = Periode(
                    fom = LocalDate.of(2022, 1, 1),
                    tom = LocalDate.of(2022, 1, 31),
                ),
                beløp = Desimaltall(beløp.toBigDecimal()),
                dagsats = Desimaltall(sats.toBigDecimal()),
                utbetalingsgrad = Desimaltall(grad.toBigDecimal()),
            ),
        ),
    )

    private val svar = """
            {
              "@løsning": {
                "fpytelser": {
                  "ytelser": [
                    {
                      "version": "v1",
                      "aktør": "aktørId",
                      "vedtattTidspunkt": "2022-01-01T12:00:00",
                      "ytelse": "PLEIEPENGER_SYKT_BARN",
                      "saksnummer": "sakNr",
                      "vedtakReferanse": "Ref",
                      "ytelseStatus": "LØPENDE",
                      "kildesystem": "FPSAK",
                      "periode": {
                        "fom": "2022-01-01",
                        "tom": "2022-01-31"
                      },
                      "tilleggsopplysninger": "Tillegg",
                      "anvist": [
                          {
                            "periode": {
                              "fom": "2022-01-01",
                              "tom": "2022-01-31"
                            },
                            "beløp": 100.0,
                            "dagsats": 50.0,
                            "utbetalingsgrad": 10.0
                          }
                        ]
                    }
                  ],
                  "feil": null
                }
              }
            }
    """.trimIndent()

    private val behovMelding = """
        {
            "@event_name": "behov",
            "@opprettet": "2023-01-17T12:50:54.875468981",
            "@id": "f51435b1-c993-4ca8-92ff-f62f3d4f2ebc",
            "@behovId": "dfe8e0cc-83ab-4182-96f8-6b5a49ce5b8b",
            "@behov": [
            "fpytelser"
            ],
            "journalpostId": "foobar3",
            "tilstandtype": "AvventerForeldrepenger",
            "ident": "$ident",
            "fom": "$fom",
            "tom": "$tom",
            "system_read_count": 0,
            "system_participating_services": [
            {
                "id": "f51435b1-c993-4ca8-92ff-f62f3d4f2ebc",
                "time": "2023-01-17T12:50:54.895176586"
            }
            ]
        }
    """.trimIndent()
}
