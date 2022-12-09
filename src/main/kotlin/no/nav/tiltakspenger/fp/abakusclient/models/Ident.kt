package no.nav.tiltakspenger.fp.abakusclient.models

data class Ident(val identType: IdentType, val ident: String)

enum class IdentType {
    ORGNUMMER, AKTØRID, FNR
}
