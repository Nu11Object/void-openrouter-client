package com.nullo.voidapp.core.data.chat.data.storage

import com.nullo.voidapp.core.data.chat.domain.ImageEncoder
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readBytes
import kotlin.io.encoding.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class FileKitImageEncoder : ImageEncoder {

    override suspend fun encodeAsDataUrl(
        fileUri: String,
        mimeType: String,
    ): String = withContext(Dispatchers.IO) {
        val bytes = (FileKit.filesDir / fileUri).readBytes()
        val prefix = "data:$mimeType;base64,"
        val encodedLength = (bytes.size + 2) / 3 * 4

        buildString(prefix.length + encodedLength) {
            append(prefix)
            Base64.encodeToAppendable(bytes, this)
        }
    }
}
