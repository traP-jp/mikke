package jp.trap.mikke

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jp.trap.mikke.common.ErrorResponse
import jp.trap.mikke.di.AppModule
import jp.trap.mikke.features.ping.controller.pingRouting
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(Koin) {
        slf4jLogger()
        modules(AppModule.module)
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(cause.message ?: "Bad request")
                )

                is NoSuchElementException -> call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(cause.message ?: "Not found")
                )

                else -> call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(cause.message ?: "Unknown error")
                )
            }
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ErrorResponse("Resource not found"))
        }
    }

    routing {
        route("/api/v1") {
            pingRouting()
        }
    }
}
