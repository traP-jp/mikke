package jp.trap.mikke.features.file.infrastructure

import jp.trap.mikke.features.file.domain.model.FileId
import jp.trap.mikke.features.file.domain.model.FileInfo
import jp.trap.mikke.features.file.domain.repository.FileInfoRepository
import jp.trap.mikke.features.user.domain.model.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Single
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@Single(binds = [FileInfoRepository::class])
class DatabaseFileInfoRepository : FileInfoRepository {
    override suspend fun save(info: FileInfo) {
        withContext(Dispatchers.IO) {
            transaction {
                val updatedRows =
                    FileInfoTable.update({ FileInfoTable.id eq info.id.value.toJavaUuid() }) {
                        it[filename] = info.filename
                        it[mimeType] = info.mimeType
                        it[size] = info.size
                        it[uploaderId] = info.uploaderId.value.toJavaUuid()
                    }

                if (updatedRows == 0) {
                    FileInfoTable.insert {
                        it[id] = info.id.value.toJavaUuid()
                        it[filename] = info.filename
                        it[mimeType] = info.mimeType
                        it[size] = info.size
                        it[uploaderId] = info.uploaderId.value.toJavaUuid()
                        it[createdAt] = info.createdAt
                    }
                }
            }
        }
    }

    override suspend fun find(id: FileId): FileInfo? =
        withContext(Dispatchers.IO) {
            transaction {
                val result =
                    FileInfoTable
                        .selectAll()
                        .where { FileInfoTable.id eq id.value.toJavaUuid() }
                        .singleOrNull()

                result?.let {
                    FileInfo(
                        id = id,
                        filename = it[FileInfoTable.filename],
                        mimeType = it[FileInfoTable.mimeType],
                        size = it[FileInfoTable.size],
                        uploaderId =
                            UserId(
                                it[FileInfoTable.uploaderId].toKotlinUuid(),
                            ),
                        createdAt = it[FileInfoTable.createdAt],
                    )
                }
            }
        }

    override suspend fun delete(id: FileId) {
        withContext(Dispatchers.IO) {
            transaction {
                FileInfoTable.deleteWhere { FileInfoTable.id eq id.value.toJavaUuid() }
            }
        }
    }
}
