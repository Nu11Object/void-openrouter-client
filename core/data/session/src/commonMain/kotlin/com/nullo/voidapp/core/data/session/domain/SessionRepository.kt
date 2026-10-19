package com.nullo.voidapp.core.data.session.domain

import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue
import kotlinx.coroutines.flow.Flow

/**
 * Manages the active model and generation settings, persisting them across app restarts.
 */
interface SessionRepository {

    val activeModel: Flow<ActiveModelRef?>
    val chatTuningParameters: Flow<Map<TuningParameter, Double>>
    val imageParameters: Flow<Map<ImageParameter, ImageParameterValue>>
    val thinkingEnabled: Flow<Boolean>
    val webSearchEnabled: Flow<Boolean>

    suspend fun setActiveModel(activeModelRef: ActiveModelRef)
    suspend fun setChatTuningParameters(parameters: Map<TuningParameter, Double>)
    suspend fun setImageParameters(parameters: Map<ImageParameter, ImageParameterValue>)
    suspend fun setThinkingEnabled(enabled: Boolean)
    suspend fun setWebSearchEnabled(enabled: Boolean)
    suspend fun clear()
}
