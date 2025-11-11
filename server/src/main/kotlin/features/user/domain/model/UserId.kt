package jp.trap.mikke.features.user.domain.model

import kotlin.uuid.Uuid

@JvmInline
value class UserId(
    val value: Uuid,
)
