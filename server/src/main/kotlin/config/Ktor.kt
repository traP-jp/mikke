package jp.trap.mikke.config

import com.codahale.metrics.Slf4jReporter
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.metrics.dropwizard.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import jp.trap.mikke.di.AppModule
import jp.trap.mikke.di.DatabaseModule
import jp.trap.mikke.di.TraqClientModule
import jp.trap.mikke.features.auth.controller.authRouting
import jp.trap.mikke.features.auth.session.RedirectSession
import jp.trap.mikke.features.auth.session.UserSession
import jp.trap.mikke.features.ping.controller.pingRouting
import jp.trap.mikke.openapi.ApplicationCompressionConfiguration
import jp.trap.mikke.openapi.ApplicationHstsConfiguration
import jp.trap.mikke.openapi.models.Error
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.util.concurrent.TimeUnit

fun Application.main() {
    install(Koin) {
        slf4jLogger()
        modules(AppModule.module, DatabaseModule.module, TraqClientModule.module)
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException ->
                    call.respond(
                        HttpStatusCode.BadRequest,
                        Error(cause.message ?: "Bad request"),
                    )

                is NoSuchElementException ->
                    call.respond(
                        HttpStatusCode.NotFound,
                        Error(cause.message ?: "Not found"),
                    )

                else ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        Error(cause.message ?: "Unknown error"),
                    )
            }
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, Error("Resource not found"))
        }
    }

    install(DefaultHeaders)
    install(DropwizardMetrics) {
        val reporter =
            Slf4jReporter
                .forRegistry(registry)
                .outputTo(this@main.log)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
        reporter.start(10, TimeUnit.SECONDS)
    }
    install(ContentNegotiation) {
        json()
    }
    install(AutoHeadResponse) // see https://ktor.io/docs/autoheadresponse.html
    install(Compression, ApplicationCompressionConfiguration()) // see https://ktor.io/docs/compression.html
    install(HSTS, ApplicationHstsConfiguration()) // see https://ktor.io/docs/hsts.html
    install(Resources)
    val httpClient = HttpClient(Apache)
    install(Authentication) {
        session<UserSession>("cookieAuth") {
            validate { session -> session }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, Error("Unauthorized"))
            }
        }

        oauth("traQAuth") {
            urlProvider = { "" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "traQ",
                    authorizeUrl = Environment.TRAQ_AUTHORIZE_URL,
                    accessTokenUrl = Environment.TRAQ_TOKEN_URL,
                    requestMethod = HttpMethod.Post,
                    clientId = Environment.TRAQ_CLIENT_ID,
                    clientSecret = Environment.TRAQ_CLIENT_SECRET,
                    defaultScopes = listOf("read", "write", "openid", "profile"),
                )
            }
            client = httpClient
        }
    }
    install(Sessions) {
        cookie<UserSession>("sid") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60 * 60 * 24 * 7
        }
        cookie<RedirectSession>("oauth_redirect") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60 * 5
        }
    }

    configureRouting()
}

fun Application.configureRouting() {
    routing {
        route("/api/v1") {
            pingRouting()
            authRouting()
            swaggerUI(path = "docs", swaggerFile = "openapi.yaml")
        }
    }
}
