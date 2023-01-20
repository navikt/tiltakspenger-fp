package no.nav.tiltakspenger.fp.abakusclient.models

import java.time.LocalDateTime

data class YtelseV1(
    val version: String,
    val aktør: Aktør,
    val vedtattTidspunkt: LocalDateTime,
    val ytelse: YtelserOutput,
    val saksnummer: String?,
    val vedtakReferanse: String,
    val ytelseStatus: Status,
    val kildesystem: Kildesystem,
    val periode: Periode,
    val tilleggsopplysninger: String?,
    val anvist: List<Anvisning>,
)
