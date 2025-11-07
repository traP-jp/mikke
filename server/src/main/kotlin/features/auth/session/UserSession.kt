package jp.trap.mikke.features.auth.session

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserSession(
    val userId: Uuid,
    val name: String,
)
