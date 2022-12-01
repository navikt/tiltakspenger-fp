package no.nav.tiltakspenger.fp.abakusclient.models

enum class Ytelser {
    /** Folketrygdloven K9 ytelser.  */
    PSB, // PLEIEPENGER_SYKT_BARN,
    PPN, // PLEIEPENGER_NÆRSTÅENDE,
    OMP, // OMSORGSPENGER,
    OLP, // OPPLÆRINGSPENGER,

    /** Folketrygdloven K14 ytelser.  */
    ES, // ENGANGSTØNAD,
    FP, // FORELDREPENGER,
    SVP // SVANGERSKAPSPENGER,

    /** Midlertidig ytelse for Selvstendig næringsdrivende og Frilansere (Anmodning 10).  */
    // FRISINN
}
