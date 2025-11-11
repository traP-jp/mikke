package jp.trap.mikke.features.file.service

import jp.trap.mikke.features.file.domain.model.FileId
import jp.trap.mikke.features.file.domain.model.FileInfo
import jp.trap.mikke.features.user.domain.model.UserId
import java.io.InputStream

data class FileHeader(
    val id: FileId,
    val name: String,
    val mimeType: String,
    val length: Long?,
    val uploaderId: UserId,
)

interface FileStorage {
    suspend fun writeFile(
        header: FileHeader,
        data: InputStream,
    ): FileInfo

    suspend fun <R> useFile(
        fileId: FileId,
        block: suspend (stream: InputStream?) -> R,
    ): R

    suspend fun deleteFile(fileId: FileId)
}
