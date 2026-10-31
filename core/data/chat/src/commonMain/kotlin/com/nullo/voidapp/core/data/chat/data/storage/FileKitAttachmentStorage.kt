package com.nullo.voidapp.core.data.chat.data.storage

import com.nullo.voidapp.core.data.chat.domain.AttachmentStorage
import com.nullo.voidapp.core.model.ImageAttachment
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class FileKitAttachmentStorage : AttachmentStorage {

    override suspend fun listFiles(): List<String> = withContext(Dispatchers.IO) {
        val dir = FileKit.filesDir / ATTACHMENTS_DIR
        if (!dir.exists()) return@withContext emptyList()
        dir.list().map { "$ATTACHMENTS_DIR/${it.name}" }
    }

    override suspend fun import(source: PlatformFile): ImageAttachment = withContext(Dispatchers.IO) {
        val original = source.readBytes()
        val extension = source.extension.lowercase()
        var format = if (extension == "png") ImageFormat.PNG else ImageFormat.JPEG

        // Small JPEG/PNG files are stored as is, so they are not re-encoded for nothing.
        val isAlreadyOptimal = extension in PASSTHROUGH_EXTENSIONS &&
            original.size <= PASSTHROUGH_MAX_BYTES

        var bytes = if (isAlreadyOptimal) original else compress(original, format)

        // A heavy PNG (e.g. a photo saved as PNG) is not worth keeping lossless.
        if (format == ImageFormat.PNG && bytes.size > PNG_MAX_BYTES) {
            format = ImageFormat.JPEG
            bytes = compress(original, format)
        }

        val (outputExtension, mimeType) = when (format) {
            ImageFormat.PNG -> "png" to "image/png"
            else -> "jpg" to "image/jpeg"
        }

        (FileKit.filesDir / ATTACHMENTS_DIR).createDirectories()
        val relativePath = "$ATTACHMENTS_DIR/${Uuid.random()}.$outputExtension"
        (FileKit.filesDir / relativePath).write(bytes)

        ImageAttachment(relativePath, mimeType)
    }

    override suspend fun delete(relativePath: String) {
        withContext(Dispatchers.IO) {
            (FileKit.filesDir / relativePath).delete(mustExist = false)
        }
    }

    private suspend fun compress(bytes: ByteArray, format: ImageFormat): ByteArray =
        FileKit.compressImage(
            bytes = bytes,
            imageFormat = format,
            quality = JPEG_QUALITY,
            maxWidth = MAX_SIDE_PX,
            maxHeight = MAX_SIDE_PX
        )

    private companion object {

        const val ATTACHMENTS_DIR = "attachments"
        const val MAX_SIDE_PX = 1568
        const val JPEG_QUALITY = 85
        const val PASSTHROUGH_MAX_BYTES = 400_000
        const val PNG_MAX_BYTES = 1_000_000
        val PASSTHROUGH_EXTENSIONS = setOf("jpg", "jpeg", "png")
    }
}
