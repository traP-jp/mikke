package jp.trap.mikke.features.ping.controller

import io.ktor.server.application.*
import io.ktor.server.response.*
import jp.trap.mikke.features.ping.controller.dto.toDto
import jp.trap.mikke.features.ping.service.PingService
import org.koin.core.annotation.Single

@Single
class PingHandler(private val pingService: PingService) {
    suspend fun handlePing(call: ApplicationCall) {
        val response = pingService.ping()
        call.respond(response.toDto())
    }
}