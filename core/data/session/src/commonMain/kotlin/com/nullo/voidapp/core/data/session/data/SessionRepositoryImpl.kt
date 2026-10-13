package com.nullo.voidapp.core.data.session.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nullo.voidapp.core.data.session.domain.SessionRepository
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal class SessionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : SessionRepository {

    private val imageParametersSerializer = MapSerializer(
        keySerializer = String.serializer(),
        valueSerializer = ImageParameterValue.serializer()
    )

    private object Keys {
        val ACTIVE_MODEL_TYPE = stringPreferencesKey("active_model_type")
        val ACTIVE_MODEL_ID = stringPreferencesKey("active_model_id")
        val CHAT_TUNING_PARAMETERS_JSON = stringPreferencesKey("chat_tuning_parameters_json")
        val IMAGE_PARAMETERS_JSON = stringPreferencesKey("image_parameters_json")
        val THINKING_ENABLED = booleanPreferencesKey("thinking_enabled")
        val WEB_SEARCH_ENABLED = booleanPreferencesKey("web_search_enabled")
    }

    override val activeModel: Flow<ActiveModelRef?>
        get() = dataStore.data.map { prefs ->
            val id = prefs[Keys.ACTIVE_MODEL_ID] ?: return@map null
            when (prefs[Keys.ACTIVE_MODEL_TYPE]) {
                MODEL_TYPE_CHAT -> ActiveModelRef.Chat(id)
                MODEL_TYPE_IMAGE -> ActiveModelRef.Image(id)
                else -> null
            }
        }

    override val chatTuningParameters: Flow<Map<TuningParameter, Double>>
        get() = dataStore.data.map { prefs ->
            val raw = prefs[Keys.CHAT_TUNING_PARAMETERS_JSON] ?: return@map emptyMap()
            runCatching {
                json.decodeFromString<Map<String, Double>>(raw)
                    .mapNotNull { (key, value) ->
                        TuningParameter.fromKey(key)?.let { it to value }
                    }
                    .toMap()
            }.getOrDefault(emptyMap())
        }

    override val imageParameters: Flow<Map<ImageParameter, ImageParameterValue>>
        get() = dataStore.data.map { prefs ->
            val raw = prefs[Keys.IMAGE_PARAMETERS_JSON] ?: return@map emptyMap()
            runCatching {
                json.decodeFromString(imageParametersSerializer, raw)
                    .mapNotNull { (key, value) ->
                        ImageParameter.fromKey(key)?.let { it to value }
                    }
                    .toMap()
            }.getOrDefault(emptyMap())
        }

    override val thinkingEnabled: Flow<Boolean>
        get() = dataStore.data.map { it[Keys.THINKING_ENABLED] ?: false }

    override val webSearchEnabled: Flow<Boolean>
        get() = dataStore.data.map { it[Keys.WEB_SEARCH_ENABLED] ?: false }

    override suspend fun setActiveModel(activeModelRef: ActiveModelRef) {
        dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_MODEL_ID] = activeModelRef.id
            prefs[Keys.ACTIVE_MODEL_TYPE] = when (activeModelRef) {
                is ActiveModelRef.Chat -> MODEL_TYPE_CHAT
                is ActiveModelRef.Image -> MODEL_TYPE_IMAGE
            }
        }
    }

    override suspend fun setChatTuningParameters(parameters: Map<TuningParameter, Double>) {
        val serializable = parameters.mapKeys { (param, _) -> param.name }
        dataStore.edit { prefs ->
            prefs[Keys.CHAT_TUNING_PARAMETERS_JSON] = json.encodeToString(serializable)
        }
    }

    override suspend fun setImageParameters(parameters: Map<ImageParameter, ImageParameterValue>) {
        val serializable = parameters.mapKeys { (param, _) -> param.name }
        dataStore.edit { prefs ->
            prefs[Keys.IMAGE_PARAMETERS_JSON] =
                json.encodeToString(imageParametersSerializer, serializable)
        }
    }

    override suspend fun setThinkingEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.THINKING_ENABLED] = enabled }
    }

    override suspend fun setWebSearchEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.WEB_SEARCH_ENABLED] = enabled }
    }

    companion object {

        private const val MODEL_TYPE_CHAT = "CHAT"
        private const val MODEL_TYPE_IMAGE = "IMAGE"
    }
}
