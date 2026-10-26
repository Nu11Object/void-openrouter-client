package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.session.domain.SessionRepository

/** Use case responsible for setting web search mode state. */
internal class SetWebSearchEnabledUseCase(private val sessionRepository: SessionRepository) {

    suspend operator fun invoke(enabled: Boolean) {
        sessionRepository.setWebSearchEnabled(enabled)
    }
}
