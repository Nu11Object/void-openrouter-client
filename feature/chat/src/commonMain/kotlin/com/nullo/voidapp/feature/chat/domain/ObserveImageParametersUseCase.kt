package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue
import kotlinx.coroutines.flow.Flow

internal class ObserveImageParametersUseCase(private val sessionRepository: SessionRepository) {

    operator fun invoke(): Flow<Map<ImageParameter, ImageParameterValue>> = sessionRepository.imageParameters
}
