package jp.trap.mikke.features.auth.controller

import io.ktor.server.auth.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.routing.*
import jp.trap.mikke.openapi.Paths
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val authHandler by inject<AuthHandler>()

    get<Paths.login> {
        authHandler.handleLogin(call)
    }

    authenticate("traQAuth") {
        get<Paths.authorize> {
        }

        get<Paths.authCallback> {
            authHandler.handleCallback(call)
        }
    }

    authenticate("cookieAuth") {
        post<Paths.logout> {
            authHandler.handleLogout(call)
        }
    }
}
