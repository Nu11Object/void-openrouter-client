package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.session.domain.SessionRepository
import kotlinx.coroutines.flow.Flow

/** Use case responsible for observing thinking mode state. */
internal class ObserveThinkingEnabledUseCase(
    private val sessionRepository: SessionRepository,
) {

    operator fun invoke(): Flow<Boolean> {
        return sessionRepository.thinkingEnabled
    }
}
