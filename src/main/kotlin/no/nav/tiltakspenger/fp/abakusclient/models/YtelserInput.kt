package no.nav.tiltakspenger.fp.abakusclient.models

enum class YtelserInput {
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

enum class YtelserOutput {
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
