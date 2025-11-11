package jp.trap.mikke.features.auth.infrastructure

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

open class SessionTable(
    name: String,
) : Table(name) {
    val sessionId = text("session_id").uniqueIndex()
    val value = text("value")
    val expireTime = timestamp("expire_at")

    override val primaryKey = PrimaryKey(sessionId)
}
