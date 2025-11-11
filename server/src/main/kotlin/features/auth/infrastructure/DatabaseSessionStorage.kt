package jp.trap.mikke.features.auth.infrastructure

import io.ktor.server.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class DatabaseSessionStorage(
    private val table: SessionTable,
    private val maxAgeInSeconds: Long,
) : SessionStorage {
    override suspend fun invalidate(id: String) {
        withContext(Dispatchers.IO) {
            transaction {
                table.deleteWhere { table.sessionId eq id }
            }
        }
    }

    override suspend fun read(id: String): String =
        withContext(Dispatchers.IO) {
            transaction {
                val now = Clock.System.now()

                val result =
                    table.selectAll().where { table.sessionId eq id }.singleOrNull()
                        ?: throw NoSuchElementException("Session $id not found in database")

                val expiry = result[table.expireTime]
                if (expiry < now) {
                    table.deleteWhere { table.sessionId eq id }
                    throw NoSuchElementException("Session $id expired and deleted")
                }

                result[table.value]
            }
        }

    override suspend fun write(
        id: String,
        value: String,
    ) {
        withContext(Dispatchers.IO) {
            transaction {
                val now = Clock.System.now()
                val expireTime = now + maxAgeInSeconds.seconds

                val updatedRows =
                    table.update({ table.sessionId eq id }) {
                        it[table.value] = value
                        it[table.expireTime] = expireTime
                    }

                if (updatedRows == 0) {
                    table.insert {
                        it[sessionId] = id
                        it[this.value] = value
                        it[this.expireTime] = expireTime
                    }
                }
            }
        }
    }
}
