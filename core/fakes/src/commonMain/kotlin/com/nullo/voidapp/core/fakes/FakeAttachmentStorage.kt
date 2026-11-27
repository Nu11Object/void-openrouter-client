package com.nullo.voidapp.core.fakes

import com.nullo.voidapp.core.data.chat.domain.AttachmentStorage
import com.nullo.voidapp.core.model.ImageAttachment
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import kotlin.time.Duration
import kotlinx.coroutines.delay

class FakeAttachmentStorage : AttachmentStorage {

    val calls: List<String>
        field = mutableListOf<String>()

    var importFailureNames: Set<String> = emptySet()
    var deleteFailure: Throwable? = null
    var exportFailure: Throwable? = null
    var importDelay: Duration = Duration.ZERO

    override suspend fun listFiles(): List<String> = emptyList()

    override suspend fun import(source: PlatformFile): ImageAttachment {
        calls += "import:${source.name}"
        delay(importDelay)
        if (source.name in importFailureNames) {
            throw Exception("Cannot import ${source.name}")
        }
        return ImageAttachment(relativePath = source.name, mimeType = "image/png")
    }

    override suspend fun export(relativePath: String, destination: PlatformFile) {
        calls += "export:$relativePath"
        exportFailure?.let { throw it }
    }

    override suspend fun saveGenerated(bytes: ByteArray, mimeType: String): ImageAttachment {
        calls += "saveGenerated:${bytes.size}:$mimeType"
        return ImageAttachment(relativePath = "generated/${bytes.size}", mimeType = mimeType)
    }

    override suspend fun delete(relativePath: String) {
        calls += "delete:$relativePath"
        deleteFailure?.let { throw it }
    }
}
