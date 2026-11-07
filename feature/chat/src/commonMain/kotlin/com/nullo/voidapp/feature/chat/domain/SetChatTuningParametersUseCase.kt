package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.model.chatmodel.TuningParameter

internal class SetChatTuningParametersUseCase(private val sessionRepository: SessionRepository) {

    suspend operator fun invoke(parameters: Map<TuningParameter, Double>) =
        sessionRepository.setChatTuningParameters(parameters)
}
