package com.nullo.voidapp.feature.auth.domain.usecase

import com.nullo.voidapp.core.security.SecureStorageException
import com.nullo.voidapp.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing the active user authentication state.
 * Monitors the secure storage reactively to determine if a valid API key is present on the device.
 *
 * @throws SecureStorageException If the underlying platform secure storage
 * fails to verify key presence.
 */
internal class ObserveAuthStateUseCase(
    private val authRepository: AuthRepository,
) {

    operator fun invoke(): Flow<Boolean> {
        return authRepository.hasApiKey()
    }
}
