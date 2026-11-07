package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import kotlinx.coroutines.flow.Flow

internal class ObserveChatTuningParametersUseCase(private val sessionRepository: SessionRepository) {

    operator fun invoke(): Flow<Map<TuningParameter, Double>> = sessionRepository.chatTuningParameters
}
