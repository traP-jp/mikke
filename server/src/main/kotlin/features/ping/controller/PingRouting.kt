package jp.trap.mikke.features.ping.controller

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.pingRouting() {
    val pingHandler by inject<PingHandler>()

    route("/ping") {
        get {
            pingHandler.handlePing(call)
        }
    }
}