package no.nav.tiltakspenger.fp

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import mu.KotlinLogging
import mu.withLoggingContext
import net.logstash.logback.argument.StructuredArguments
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.tiltakspenger.fp.abakusclient.AbakusClient
import no.nav.tiltakspenger.fp.abakusclient.models.Anvisning
import no.nav.tiltakspenger.fp.abakusclient.models.Kildesystem
import no.nav.tiltakspenger.fp.abakusclient.models.Status
import no.nav.tiltakspenger.fp.abakusclient.models.YtelseV1
import no.nav.tiltakspenger.fp.abakusclient.models.Ytelser
import no.nav.tiltakspenger.libs.fp.FPResponsDTO
import no.nav.tiltakspenger.libs.fp.FPResponsDTO.AnvisningDTO
import no.nav.tiltakspenger.libs.fp.FPResponsDTO.YtelseV1DTO
import java.time.LocalDate
import no.nav.tiltakspenger.libs.fp.FPResponsDTO.Kildesystem as dtoKildesystem
import no.nav.tiltakspenger.libs.fp.FPResponsDTO.Periode as dtoPeriode
import no.nav.tiltakspenger.libs.fp.FPResponsDTO.Status as dtoStatus
import no.nav.tiltakspenger.libs.fp.FPResponsDTO.YtelserOutput as dtoYelserOutput

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class ForeldrepengerService(
    rapidsConnection: RapidsConnection,
    private val client: AbakusClient,
) : River.PacketListener {

    companion object {
        internal object BEHOV {
            const val FP_YTELSER = "fpytelser"
        }

        val objectmapper: ObjectMapper = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .setDefaultPrettyPrinter(
                DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                },
            )
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf(BEHOV.FP_YTELSER))
                it.forbid("@løsning")
                it.requireKey("@id", "@behovId")
                it.requireKey("ident")
                it.requireKey("fom")
                it.requireKey("tom")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        runCatching {
            loggVedInngang(packet)
            withLoggingContext(
                "id" to packet["@id"].asText(),
                "behovId" to packet["@behovId"].asText(),
            ) {
                val ident = packet["ident"].asText()
                val behovId = packet["@behovId"].asText()
                SECURELOG.debug { "mottok ident $ident" }
                val fom: String = packet["fom"].asText("1970-01-01")
                val tom: String = packet["tom"].asText("9999-12-31")

                val fomFixed = try {
                    val tempFom: LocalDate = LocalDate.parse(fom)
                    if (tempFom == LocalDate.MIN) {
                        LocalDate.EPOCH
                    } else {
                        tempFom
                    }
                } catch (e: Exception) {
                    LOG.warn("Klarte ikke å parse fom $fom", e)
                    LocalDate.EPOCH
                }

                val tomFixed = try {
                    val tempTom: LocalDate = LocalDate.parse(tom)
                    if (tempTom == LocalDate.MAX) {
                        LocalDate.of(9999, 12, 31)
                    } else {
                        tempTom
                    }
                } catch (e: Exception) {
                    LOG.warn("Klarte ikke å parse tom $tom", e)
                    LocalDate.of(9999, 12, 31)
                }

                val respons =
                    if ((ident == "18907299828") or (ident == "13866799568")) { // hopper over denne da den ikke finnes i dev
                        FPResponsDTO(
                            ytelser = emptyList(),
                            feil = null,
                        )
                    } else {
                        val ytelser: List<YtelseV1> = runBlocking(MDCContext()) {
                            client.hentYtelser(ident, fomFixed, tomFixed, behovId)
                        }
                        SECURELOG.info { "svar fra nytt endepunkt : $ytelser" }
                        FPResponsDTO(
                            ytelser = ytelser.map {
                                mapYtelseV1(it)
                            },
                            feil = null,
                        )
                    }

                packet["@løsning"] = mapOf(
                    BEHOV.FP_YTELSER to respons,
                )
                loggVedUtgang(packet)
                context.publish(ident, packet.toJson())
            }
        }.onFailure {
            loggVedFeil(it, packet)
        }.getOrThrow()
    }

    private fun mapYtelseV1(ytelseV1: YtelseV1): YtelseV1DTO {
        return YtelseV1DTO(
            version = ytelseV1.version,
            aktør = ytelseV1.aktør.verdi,
            vedtattTidspunkt = ytelseV1.vedtattTidspunkt,
            ytelse = when (ytelseV1.ytelse) {
                Ytelser.PLEIEPENGER_SYKT_BARN -> dtoYelserOutput.PLEIEPENGER_SYKT_BARN
                Ytelser.PLEIEPENGER_NÆRSTÅENDE -> dtoYelserOutput.PLEIEPENGER_NÆRSTÅENDE
                Ytelser.OMSORGSPENGER -> dtoYelserOutput.OMSORGSPENGER
                Ytelser.OPPLÆRINGSPENGER -> dtoYelserOutput.OPPLÆRINGSPENGER
                Ytelser.ENGANGSTØNAD -> dtoYelserOutput.ENGANGSTØNAD
                Ytelser.FORELDREPENGER -> dtoYelserOutput.FORELDREPENGER
                Ytelser.SVANGERSKAPSPENGER -> dtoYelserOutput.SVANGERSKAPSPENGER
                Ytelser.FRISINN -> dtoYelserOutput.FRISINN
            },
            saksnummer = ytelseV1.saksnummer,
            vedtakReferanse = ytelseV1.vedtakReferanse ?: "",
            ytelseStatus = when (ytelseV1.ytelseStatus) {
                Status.UNDER_BEHANDLING -> dtoStatus.UNDER_BEHANDLING
                Status.LØPENDE -> dtoStatus.LØPENDE
                Status.AVSLUTTET -> dtoStatus.AVSLUTTET
                Status.UKJENT -> dtoStatus.UKJENT
            },
            kildesystem = when (ytelseV1.kildesystem) {
                Kildesystem.FPSAK -> dtoKildesystem.FPSAK
                Kildesystem.K9SAK -> dtoKildesystem.K9SAK
                else -> when (ytelseV1.ytelse) {
                    Ytelser.PLEIEPENGER_SYKT_BARN -> dtoKildesystem.K9SAK
                    Ytelser.PLEIEPENGER_NÆRSTÅENDE -> dtoKildesystem.K9SAK
                    Ytelser.OMSORGSPENGER -> dtoKildesystem.K9SAK
                    Ytelser.OPPLÆRINGSPENGER -> dtoKildesystem.K9SAK
                    Ytelser.ENGANGSTØNAD -> dtoKildesystem.FPSAK
                    Ytelser.FORELDREPENGER -> dtoKildesystem.FPSAK
                    Ytelser.SVANGERSKAPSPENGER -> dtoKildesystem.FPSAK
                    Ytelser.FRISINN -> dtoKildesystem.FPSAK
                }
            },
            periode = dtoPeriode(
                fom = ytelseV1.periode.fom,
                tom = ytelseV1.periode.tom,
            ),
            tilleggsopplysninger = ytelseV1.tilleggsopplysninger,
            anvist = mapAnvist(ytelseV1.anvist),
        )
    }

    private fun mapAnvist(anvisninger: List<Anvisning>): List<AnvisningDTO> {
        return anvisninger.map { anvisning ->
            AnvisningDTO(
                periode = dtoPeriode(
                    fom = anvisning.periode.fom,
                    tom = anvisning.periode.tom,
                ),
                beløp = anvisning.beløp?.verdi,
                dagsats = anvisning.dagsats?.verdi,
                utbetalingsgrad = anvisning.utbetalingsgrad?.verdi,
            )
        }
    }

    fun loggVedInngang(packet: JsonMessage) {
        LOG.info(
            "løser fp-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        SECURELOG.info(
            "løser fp-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        SECURELOG.debug { "mottok melding: ${packet.toJson()}" }
    }

    private fun loggVedUtgang(packet: JsonMessage) {
        LOG.info(
            "har løst fp-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        SECURELOG.info(
            "har løst fp-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        SECURELOG.debug { "publiserer melding: ${packet.toJson()}" }
    }

    private fun loggVedFeil(ex: Throwable, packet: JsonMessage) {
        LOG.error(
            "feil ved behandling av fp-behov med {}, se securelogs for detaljer",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
        )
        SECURELOG.error(
            "feil \"${ex.message}\" ved behandling av fp-behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("packet", packet.toJson()),
            ex,
        )
    }
}
