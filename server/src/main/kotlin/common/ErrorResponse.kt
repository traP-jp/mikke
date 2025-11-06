package jp.trap.mikke.common

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String)