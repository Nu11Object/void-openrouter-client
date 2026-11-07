package com.nullo.voidapp.feature.chat.domain

import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue

internal class SetImageParametersUseCase(private val sessionRepository: SessionRepository) {

    suspend operator fun invoke(parameters: Map<ImageParameter, ImageParameterValue>) =
        sessionRepository.setImageParameters(parameters)
}
