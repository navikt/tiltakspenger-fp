package no.nav.tiltakspenger.fp.abakusclient.models

import no.nav.tiltakspenger.fp.defaultObjectMapper
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.LocalDate

internal class RequestTest {


    @Test
    fun `skal mappes korrekt`() {

        val expectedJson = """
        {"person":{"ident":"123"},"periode":{"fom":"2022-12-01","tom":"2022-09-01"},"ytelser":["ES","FP"]}
    """.trimIndent()

        val request = Request(
            person = Person(ident = "123"),
            periode = Periode(fom = LocalDate.of(2022, 12, 1), tom = LocalDate.of(2022, 9, 1)),
            ytelser = listOf(Ytelser.ES, Ytelser.FP)
        )

        val mapper = defaultObjectMapper()

        val result = mapper.writeValueAsString(request)
        JSONAssert.assertEquals(
            expectedJson,
            result,
            JSONCompareMode.STRICT
        )
    }


}
