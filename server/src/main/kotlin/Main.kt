package jp.trap.mikke

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import jp.trap.mikke.config.module

fun main() {
    embeddedServer(
        factory = Netty,
        host = "0.0.0.0",
        port = 8080,
        module = Application::module,
    ).start(wait = true)
}
