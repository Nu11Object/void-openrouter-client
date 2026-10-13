package com.nullo.voidapp.core.data.chat.domain

/**
 *  Encodes file attachments as Base64 data URLs.
 *  @see [encodeAsDataUrl]
 */
fun interface ImageEncoder {

    /** Reads the attachment at [fileUri] and returns it as a base64 `data:` URL. */
    suspend fun encodeAsDataUrl(fileUri: String, mimeType: String): String
}
