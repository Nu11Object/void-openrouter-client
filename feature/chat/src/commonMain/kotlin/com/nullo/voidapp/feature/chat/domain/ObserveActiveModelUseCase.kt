package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.model.ActiveModelRef
import kotlinx.coroutines.flow.Flow

/** Use case responsible for observing active model. */
internal class ObserveActiveModelUseCase(private val sessionRepository: SessionRepository) {

    operator fun invoke(): Flow<ActiveModelRef?> = sessionRepository.activeModel
}
