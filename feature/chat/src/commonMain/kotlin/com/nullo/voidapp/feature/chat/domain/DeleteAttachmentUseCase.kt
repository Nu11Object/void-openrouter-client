package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.AttachmentStorage
import com.nullo.voidapp.core.model.ImageAttachment

/** Use case responsible for deleting an attachment. */
internal class DeleteAttachmentUseCase(private val attachmentStorage: AttachmentStorage) {

    suspend operator fun invoke(attachment: ImageAttachment) = attachmentStorage.delete(attachment.relativePath)
}
