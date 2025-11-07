package jp.trap.mikke.features.auth.controller

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import jp.trap.mikke.config.Environment
import jp.trap.mikke.features.auth.session.RedirectSession
import jp.trap.mikke.features.auth.session.UserSession
import jp.trap.mikke.traq.client.apis.MeApi
import org.koin.core.annotation.Single

@Single
class AuthHandler(
    private val apiHttpClient: HttpClient,
) {
    suspend fun handleLogin(call: ApplicationCall) {
        val redirectUrl = call.request.queryParameters["redirect_to"]
        if (redirectUrl != null) {
            call.sessions.set(RedirectSession(redirectUrl))
        }
        call.respondRedirect("/api/v1/auth/authorize")
    }

    suspend fun handleCallback(call: ApplicationCall) {
        val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()

        if (principal != null) {
            val token = principal.accessToken
            val meApi =
                MeApi(Environment.TRAQ_API_BASE_URL, apiHttpClient).apply {
                    setAccessToken(token)
                }
            val userInfo = meApi.getMe().body()
            call.sessions.set(UserSession(userInfo.id, userInfo.name))
            val redirectSession = call.sessions.get<RedirectSession>()
            call.sessions.clear<RedirectSession>()
            call.respondRedirect(redirectSession?.target ?: "/")
        } else {
            call.respond(HttpStatusCode.BadRequest, Error("OAuth failed"))
        }
    }
}
