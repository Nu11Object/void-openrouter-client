package com.nullo.voidapp.feature.catalog.domain

import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.model.ActiveModelRef

/** Use case responsible for setting the session's active model. */
internal class UseModelUseCase(
    private val sessionRepository: SessionRepository,
) {

    suspend operator fun invoke(modelRef: ActiveModelRef) {
        sessionRepository.setActiveModel(modelRef)
    }
}
