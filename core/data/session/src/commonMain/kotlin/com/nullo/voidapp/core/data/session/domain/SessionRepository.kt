package com.nullo.voidapp.core.data.session.domain

import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    val activeModel: Flow<ActiveModelRef?>
    val tuningParameters: Flow<Map<TuningParameter, Double>>
    val thinkingEnabled: Flow<Boolean>
    val webSearchEnabled: Flow<Boolean>

    suspend fun setActiveModel(activeModelRef: ActiveModelRef)
    suspend fun setTuningParameters(parameters: Map<TuningParameter, Double>)
    suspend fun setThinkingEnabled(enabled: Boolean)
    suspend fun setWebSearchEnabled(enabled: Boolean)
}
