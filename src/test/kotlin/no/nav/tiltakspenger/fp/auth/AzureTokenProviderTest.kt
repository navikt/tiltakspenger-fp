package no.nav.tiltakspenger.fp.auth

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import no.nav.tiltakspenger.fp.defaultObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AzureTokenProviderTest {

    @Test
    fun `skal gjøre to kall mot Azure og returnere token`() {
        val wellKnownUrl = "https://localhost/wellwellwell"
        val tokenEndpoint = "http://localhost/token"
        val accessToken = "abc"
        val mockEngine = MockEngine { request ->
            when (request.url.toString()) {
                wellKnownUrl -> respond(
                    content = """{ "token_endpoint": "$tokenEndpoint" }""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )

                tokenEndpoint -> respond(
                    content = """{ 
                        |"access_token": "$accessToken", 
                        |"token_type": "access_token", 
                        |"ext_expires_in": "1", 
                        |"expires_in": "1" }
                    """.trimMargin(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )

                else -> throw RuntimeException("Should not happen")
            }
        }

        val tokenProvider = AzureTokenProvider(
            objectMapper = defaultObjectMapper(),
            engine = mockEngine,
            config = AzureTokenProvider.OauthConfig(
                wellknownUrl = wellKnownUrl,
                clientSecret = "opensecret",
                clientId = "id",
                scope = "scope",
            ),
        )

        val token: String = runBlocking {
            tokenProvider.getToken()
        }
        assertEquals(accessToken, token)
    }
}
