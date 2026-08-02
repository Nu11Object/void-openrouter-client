package com.nullo.voidapp.feature.settings.domain

import com.nullo.voidapp.core.auth.domain.repository.AuthRepository
import com.nullo.voidapp.core.security.SecureStorageException

/**
 * Use case responsible for signing out the user and clearing sensitive session data.
 *
 * @throws SecureStorageException If the storage fails to delete the key.
 */
internal class SignOutUseCase(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke() {
        // todo: clear chat and models db
        authRepository.clearApiKey()
    }
}
