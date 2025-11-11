package jp.trap.mikke.features.file.controller

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.utils.io.jvm.javaio.*
import jp.trap.mikke.features.auth.session.UserSession
import jp.trap.mikke.features.file.domain.model.FileId
import jp.trap.mikke.features.file.service.FileService
import jp.trap.mikke.features.user.domain.model.UserId
import jp.trap.mikke.openapi.models.Error
import jp.trap.mikke.openapi.models.FileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class FileHandler(
    private val fileService: FileService,
) {
    suspend fun handleUploadFile(call: ApplicationCall) {
        val userIdRaw =
            call.sessions.get<UserSession>()?.userId
                ?: throw IllegalStateException("user not logged in")
        val multipart = call.receiveMultipart()

        val part = multipart.readPart()
        if (part !is PartData.FileItem) {
            part?.dispose()
            call.respond(HttpStatusCode.BadRequest, Error("No file part found in the request"))
            return
        }

        try {
            val originalFilename = part.originalFileName ?: "file"
            val contentType = part.contentType?.toString() ?: ContentType.Application.OctetStream.toString()
            val contentLength = part.headers["Content-Length"]?.toLongOrNull()
            val userId = UserId(userIdRaw)

            val info =
                withContext(Dispatchers.IO) {
                    fileService.uploadFile(
                        originalFilename,
                        contentType,
                        userId,
                        contentLength,
                        part.provider().toInputStream(),
                    )
                }

            call.respond(
                HttpStatusCode.Created,
                FileInfo(
                    id = info.id.value,
                    name = info.filename,
                    mimeType = info.mimeType,
                    propertySize = info.size,
                    uploaderId = info.uploaderId.value,
                    createdAt = info.createdAt,
                ),
            )
        } finally {
            part.dispose()
            multipart.forEachPart { it.dispose() }
        }
    }

    suspend fun handleDownloadFile(call: ApplicationCall) {
        val fileIdParam = call.parameters["fileId"] ?: throw IllegalArgumentException("fileId is required")
        val id = FileId(Uuid.parse(fileIdParam))

        val info =
            fileService.getFileInfo(id) ?: run {
                call.respond(HttpStatusCode.NotFound, Error("File info not found"))
                return
            }

        call.response.header(
            HttpHeaders.CacheControl,
            CacheControl
                .MaxAge(
                    maxAgeSeconds = 3600,
                    visibility = CacheControl.Visibility.Public,
                ).toString(),
        )

        fileService.useFileBody(id) { body ->
            body ?: run {
                call.respond(HttpStatusCode.NotFound, Error("File not found"))
                return@useFileBody
            }

            call.respondBytesWriter(
                contentType = ContentType.parse(info.mimeType),
                status = HttpStatusCode.OK,
                contentLength = info.size,
            ) {
                body.use { inputStream ->
                    inputStream.copyTo(this.toOutputStream())
                }
            }
        }
    }

    suspend fun handleDeleteFile(call: ApplicationCall) {
        val userIdRaw =
            call.sessions.get<UserSession>()?.userId
                ?: throw IllegalStateException("user not logged in")
        val userId = UserId(userIdRaw)
        val fileIdParam =
            call.parameters["fileId"]
                ?: throw IllegalArgumentException("fileId is required")
        val id = FileId(Uuid.parse(fileIdParam))

        val info =
            fileService.getFileInfo(id) ?: run {
                call.respond(HttpStatusCode.NotFound, Error("File not found"))
                return
            }

        if (info.uploaderId != userId) {
            call.respond(HttpStatusCode.Forbidden, Error("You do not have permission to delete this file"))
            return
        }

        fileService.deleteFile(id)
        call.respond(HttpStatusCode.NoContent)
    }

    suspend fun handleGetFileMeta(call: ApplicationCall) {
        val fileIdParam =
            call.parameters["fileId"]
                ?: throw IllegalArgumentException("fileId is required")
        val id = FileId(Uuid.parse(fileIdParam))

        val info =
            fileService.getFileInfo(id)
                ?: run {
                    call.respond(HttpStatusCode.NotFound, Error("File not found"))
                    return
                }

        call.respond(
            FileInfo(
                id = info.id.value,
                name = info.filename,
                mimeType = info.mimeType,
                propertySize = info.size,
                uploaderId = info.uploaderId.value,
                createdAt = info.createdAt,
            ),
        )
    }
}
