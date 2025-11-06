package jp.trap.mikke.features.ping.controller.dto

import jp.trap.mikke.features.ping.domain.model.PingResult
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(val message: String)

fun PingResult.toDto(): MessageDto {
    return MessageDto(message = this.message)
}