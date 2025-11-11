package jp.trap.mikke.features.file.domain.model

import jp.trap.mikke.features.user.domain.model.UserId
import kotlin.time.Instant

data class FileInfo(
    val id: FileId,
    val filename: String,
    val mimeType: String,
    val size: Long,
    val uploaderId: UserId,
    val createdAt: Instant,
)
