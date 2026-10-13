package com.nullo.voidapp.core.data.chat.data.storage

import com.nullo.voidapp.core.data.chat.domain.ImageEncoder
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64

internal class FileKitImageEncoder : ImageEncoder {

    override suspend fun encodeAsDataUrl(
        fileUri: String,
        mimeType: String
    ): String = withContext(Dispatchers.IO) {
        val bytes = (FileKit.filesDir / fileUri).readBytes()
        "data:$mimeType;base64,${Base64.encode(bytes)}"
    }
}
