package jp.trap.mikke.features.websocket.controller

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.inject

fun Route.webSocketRoutes() {
    val handler by inject<WebSocketHandler>()

    authenticate("cookieAuth") {
        webSocket("/ws") {
            handler.handleSession(this)
        }
    }
}
