package no.nav.tiltakspenger.fp.abakusclient.models

data class AnvistAndel(
    val arbeidsgiver: Aktør?,
    val arbeidsforholdId: String,
    val dagsats: Desimaltall,
    val utbetalingsgrad: Desimaltall?,
    val refusjonsgrad: Desimaltall?,
    val inntektklasse: Inntektklasse?,
)
