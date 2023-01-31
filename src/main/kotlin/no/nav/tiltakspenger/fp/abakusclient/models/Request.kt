package no.nav.tiltakspenger.fp.abakusclient.models

data class Request(
    val ident: Ident,
    val periode: Periode,
    val ytelser: List<Ytelser>,
)
