package com.nullo.voidapp.core.fakes

import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSessionRepository(
    initialActiveModel: ActiveModelRef? = null,
    initialChatTuningParameters: Map<TuningParameter, Double> = emptyMap(),
    initialImageParameters: Map<ImageParameter, ImageParameterValue> = emptyMap(),
    initialThinkingEnabled: Boolean = false,
    initialWebSearchEnabled: Boolean = false,
) : SessionRepository {

    private val activeModelState = MutableStateFlow(initialActiveModel)
    private val chatTuningParametersState = MutableStateFlow(initialChatTuningParameters)
    private val imageParametersState = MutableStateFlow(initialImageParameters)
    private val thinkingEnabledState = MutableStateFlow(initialThinkingEnabled)
    private val webSearchEnabledState = MutableStateFlow(initialWebSearchEnabled)

    val calls: List<String>
        field = mutableListOf<String>()

    var setActiveModelFailure: Throwable? = null
    var setChatTuningParametersFailure: Throwable? = null
    var setImageParametersFailure: Throwable? = null
    var setThinkingEnabledFailure: Throwable? = null
    var setWebSearchEnabledFailure: Throwable? = null
    var clearFailure: Throwable? = null

    override val activeModel: Flow<ActiveModelRef?> = activeModelState
    override val chatTuningParameters: Flow<Map<TuningParameter, Double>> = chatTuningParametersState
    override val imageParameters: Flow<Map<ImageParameter, ImageParameterValue>> = imageParametersState
    override val thinkingEnabled: Flow<Boolean> = thinkingEnabledState
    override val webSearchEnabled: Flow<Boolean> = webSearchEnabledState

    override suspend fun setActiveModel(activeModelRef: ActiveModelRef) {
        calls += "setActiveModel:${activeModelRef.id}"
        setActiveModelFailure?.let { throw it }
        activeModelState.update { activeModelRef }
    }

    override suspend fun setChatTuningParameters(parameters: Map<TuningParameter, Double>) {
        calls += "setChatTuningParameters"
        setChatTuningParametersFailure?.let { throw it }
        chatTuningParametersState.update { parameters }
    }

    override suspend fun setImageParameters(parameters: Map<ImageParameter, ImageParameterValue>) {
        calls += "setImageParameters"
        setImageParametersFailure?.let { throw it }
        imageParametersState.update { parameters }
    }

    override suspend fun setThinkingEnabled(enabled: Boolean) {
        calls += "setThinkingEnabled:$enabled"
        setThinkingEnabledFailure?.let { throw it }
        thinkingEnabledState.update { enabled }
    }

    override suspend fun setWebSearchEnabled(enabled: Boolean) {
        calls += "setWebSearchEnabled:$enabled"
        setWebSearchEnabledFailure?.let { throw it }
        webSearchEnabledState.update { enabled }
    }

    override suspend fun clear() {
        calls += "clear"
        clearFailure?.let { throw it }
        activeModelState.update { null }
        chatTuningParametersState.update { emptyMap() }
        imageParametersState.update { emptyMap() }
        thinkingEnabledState.update { false }
        webSearchEnabledState.update { false }
    }
}
