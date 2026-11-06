package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.chat.domain.AttachmentStorage
import com.nullo.voidapp.core.model.ImageAttachment
import io.github.vinceglb.filekit.PlatformFile

/** Use case responsible for saving an attachment to a user-chosen destination. */
internal class ExportAttachmentUseCase(private val attachmentStorage: AttachmentStorage) {

    suspend operator fun invoke(attachment: ImageAttachment, destination: PlatformFile) =
        attachmentStorage.export(attachment.relativePath, destination)
}
