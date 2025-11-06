package jp.trap.mikke.features.ping.controller

import io.ktor.server.resources.*
import io.ktor.server.routing.*
import jp.trap.mikke.openapi.Paths
import org.koin.ktor.ext.inject

fun Route.pingRouting() {
    val pingHandler by inject<PingHandler>()

    get<Paths.ping> {
        pingHandler.handlePing(call)
    }
}
