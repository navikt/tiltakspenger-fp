package no.nav.tiltakspenger.fp.abakusclient.models

data class Request(
    val personident: String,
    val periode: Periode,
    val ytelser: List<Ytelser>
)
