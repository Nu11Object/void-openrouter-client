package com.nullo.voidapp.feature.settings.domain

import com.nullo.voidapp.core.auth.domain.repository.AuthRepository
import com.nullo.voidapp.core.data.catalog.domain.CatalogRepository
import com.nullo.voidapp.core.data.chat.domain.ConversationRepository
import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.data.settings.domain.SettingsRepository
import com.nullo.voidapp.core.security.SecureStorageException

/**
 * Use case responsible for signing out the user and clearing sensitive session data.
 *
 * @throws SecureStorageException If the storage fails to delete the key.
 */
internal class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val conversationRepository: ConversationRepository,
    private val catalogRepository: CatalogRepository,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke() {
        authRepository.clearApiKey()
        conversationRepository.deleteAll()
        catalogRepository.deleteAll()
        sessionRepository.clear()
        settingsRepository.clear()
    }
}
