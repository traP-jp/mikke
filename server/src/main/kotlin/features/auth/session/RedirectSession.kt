package jp.trap.mikke.features.auth.session

import kotlinx.serialization.Serializable

@Serializable
data class RedirectSession(
    val target: String,
)
