package jp.trap.mikke.features.file.service

import com.github.f4b6a3.uuid.UuidCreator
import jp.trap.mikke.features.file.domain.model.FileId
import jp.trap.mikke.features.file.domain.model.FileInfo
import jp.trap.mikke.features.file.domain.repository.FileInfoRepository
import jp.trap.mikke.features.user.domain.model.UserId
import org.koin.core.annotation.Single
import java.io.InputStream
import kotlin.uuid.toKotlinUuid

@Single
class FileService(
    private val fileStorage: FileStorage,
    private val fileInfoRepository: FileInfoRepository,
) {
    suspend fun uploadFile(
        filename: String,
        mimeType: String,
        uploaderId: UserId,
        length: Long?,
        data: InputStream,
    ): FileInfo {
        val meta =
            FileHeader(
                id = FileId(UuidCreator.getTimeOrderedEpoch().toKotlinUuid()),
                name = filename,
                mimeType = mimeType,
                length = length,
                uploaderId = uploaderId,
            )

        val info = fileStorage.writeFile(meta, data)
        fileInfoRepository.save(info)

        return info
    }

    suspend fun getFileBody(id: FileId): InputStream? = fileStorage.readFile(id)

    suspend fun getFileInfo(id: FileId): FileInfo? = fileInfoRepository.find(id)

    suspend fun deleteFile(id: FileId) {
        fileStorage.deleteFile(id)
        fileInfoRepository.delete(id)
    }
}
