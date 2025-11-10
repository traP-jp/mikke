package jp.trap.mikke.common

import io.ktor.client.*
import jp.trap.mikke.config.Environment
import jp.trap.mikke.traq.client.infrastructure.ApiClient

class TraqApiFactory(
    val httpClient: HttpClient,
) {
    val baseUrl = Environment.TRAQ_API_BASE_URL

    inline fun <reified T : ApiClient> createApi(
        constructor: (String, HttpClient) -> T,
        token: String,
    ): T =
        constructor(baseUrl, httpClient).apply {
            setAccessToken(token)
        }
}
