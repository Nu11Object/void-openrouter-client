package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.AttachmentStorage
import com.nullo.voidapp.core.model.ImageAttachment
import io.github.vinceglb.filekit.PlatformFile

/** Use case responsible for importing an attachment. */
internal class ImportAttachmentUseCase(private val attachmentStorage: AttachmentStorage) {

    suspend operator fun invoke(source: PlatformFile): ImageAttachment = attachmentStorage.import(source)
}
