package no.nav.tiltakspenger.fp.abakusclient.models

data class Request(
    val person: Person,
    val periode: Periode,
    val ytelser: List<YtelserInput>
)

data class Request2(
    val person: Person,
    val periode: Periode,
    val ytelser: List<Ytelser>
)
