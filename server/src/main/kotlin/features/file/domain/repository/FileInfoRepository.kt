package jp.trap.mikke.features.file.domain.repository

import jp.trap.mikke.features.file.domain.model.FileId
import jp.trap.mikke.features.file.domain.model.FileInfo

interface FileInfoRepository {
    suspend fun save(info: FileInfo)

    suspend fun find(id: FileId): FileInfo?

    suspend fun delete(id: FileId)
}
