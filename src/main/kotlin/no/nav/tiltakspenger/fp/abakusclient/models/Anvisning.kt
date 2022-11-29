package no.nav.tiltakspenger.fp.abakusclient.models

data class Anvisning(
    val periode: Periode,
    val beløp: Desimaltall?,
    val dagsats: Desimaltall?,
    val utbetalingsgrad: Desimaltall?,
    val andeler: List<AnvistAndel>,
)
