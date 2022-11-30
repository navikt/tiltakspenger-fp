package no.nav.tiltakspenger.fp

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.fp.abakusclient.AbakusClient
import no.nav.tiltakspenger.fp.auth.AzureTokenProvider

fun main() {
    System.setProperty("logback.configurationFile", "egenLogback.xml")
    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }

    val tokenProviderClient = AzureTokenProvider()
    val abakusClient = AbakusClient(
        getToken = tokenProviderClient::getToken
    )

    RapidApplication.create(Configuration.rapidsAndRivers).apply {
        ForeldrepengerService(
            rapidsConnection = this,
            client = abakusClient
        )

        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                log.info { "Starting tiltakspenger-fp" }
            }

            override fun onShutdown(rapidsConnection: RapidsConnection) {
                log.info { "Stopping tiltakspenger-fp" }
                super.onShutdown(rapidsConnection)
            }
        })
    }.start()
}
