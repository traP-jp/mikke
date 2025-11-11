package jp.trap.mikke.features.file.infrastructure

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object FileInfoTable : UUIDTable("file_info") {
    val filename = varchar("file_name", 255)
    val mimeType = varchar("content_type", 100)
    val size = long("file_size")
    val uploaderId = uuid("uploader_id")
    val createdAt = timestamp("upload_date")
}
