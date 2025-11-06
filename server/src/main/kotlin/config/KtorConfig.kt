package jp.trap.mikke.config

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import jp.trap.mikke.common.ErrorResponse
import jp.trap.mikke.di.AppModule
import jp.trap.mikke.features.ping.controller.pingRouting
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureRouting() {
    routing {
        route("/api/v1") {
            pingRouting()
        }
    }
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

    configureRouting()
}