package com.nullo.voidapp.core.data.chat.domain

import com.nullo.voidapp.core.model.ImageAttachment
import io.github.vinceglb.filekit.PlatformFile

/** Stores image attachments of user prompts in the app storage. */
interface AttachmentStorage {

    /** Relative paths (as stored in the DB) of all files in the attachments directory. */
    suspend fun listFiles(): List<String>

    /** Compresses [source] if needed and saves it to the app storage. */
    suspend fun import(source: PlatformFile): ImageAttachment

    /** Deletes the file at [relativePath]; does nothing if it doesn't exist. */
    suspend fun delete(relativePath: String)
}
