package jp.trap.mikke.features.file.controller

import io.ktor.server.auth.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.routing.*
import jp.trap.mikke.openapi.Paths
import org.koin.ktor.ext.inject

fun Route.fileRoutes() {
    val handler by inject<FileHandler>()

    authenticate("cookieAuth") {
        get<Paths.getFile> {
            handler.handleDownloadFile(call)
        }

        get<Paths.getFileMetadata> {
            handler.handleGetFileMeta(call)
        }

        post<Paths.uploadFile> {
            handler.handleUploadFile(call)
        }

        delete<Paths.deleteFile> {
            handler.handleDeleteFile(call)
        }
    }
}
