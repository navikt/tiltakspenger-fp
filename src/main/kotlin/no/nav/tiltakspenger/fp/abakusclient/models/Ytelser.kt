package no.nav.tiltakspenger.fp.abakusclient.models

enum class Ytelser {
    /** Folketrygdloven K9 ytelser.  */
    PLEIEPENGER_SYKT_BARN,
    PLEIEPENGER_NÆRSTÅENDE,
    OMSORGSPENGER,
    OPPLÆRINGSPENGER,

    /** Folketrygdloven K14 ytelser.  */
    ENGANGSTØNAD,
    FORELDREPENGER,
    SVANGERSKAPSPENGER,

    /** Midlertidig ytelse for Selvstendig næringsdrivende og Frilansere (Anmodning 10).  */
    FRISINN
}
