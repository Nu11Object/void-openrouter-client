package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.session.domain.SessionRepository

/** Use case responsible for setting thinking mode state. */
internal class SetThinkingEnabledUseCase(
    private val sessionRepository: SessionRepository,
) {

    suspend operator fun invoke(enabled: Boolean) {
        sessionRepository.setThinkingEnabled(enabled)
    }
}
