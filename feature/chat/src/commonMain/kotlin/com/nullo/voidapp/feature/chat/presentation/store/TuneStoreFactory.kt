package com.nullo.voidapp.feature.chat.presentation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue
import com.nullo.voidapp.feature.chat.domain.ObserveActiveModelUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveChatModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveChatTuningParametersUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveImageModelByIdUseCase
import com.nullo.voidapp.feature.chat.domain.ObserveImageParametersUseCase
import com.nullo.voidapp.feature.chat.domain.SetChatTuningParametersUseCase
import com.nullo.voidapp.feature.chat.domain.SetImageParametersUseCase
import com.nullo.voidapp.feature.chat.domain.tune.ChatTuneItem
import com.nullo.voidapp.feature.chat.domain.tune.ImageTuneItem
import com.nullo.voidapp.feature.chat.domain.tune.cleared
import com.nullo.voidapp.feature.chat.domain.tune.toSessionMap
import com.nullo.voidapp.feature.chat.domain.tune.toTuneItems
import com.nullo.voidapp.feature.chat.presentation.store.TuneStore.Intent
import com.nullo.voidapp.feature.chat.presentation.store.TuneStore.State
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class TuneStoreFactory(
    private val storeFactory: StoreFactory,
    private val observeActiveModelUseCase: ObserveActiveModelUseCase,
    private val observeChatModelByIdUseCase: ObserveChatModelByIdUseCase,
    private val observeImageModelByIdUseCase: ObserveImageModelByIdUseCase,
    private val observeChatTuningParametersUseCase: ObserveChatTuningParametersUseCase,
    private val observeImageParametersUseCase: ObserveImageParametersUseCase,
    private val setChatTuningParametersUseCase: SetChatTuningParametersUseCase,
    private val setImageParametersUseCase: SetImageParametersUseCase,
) : StoreFactory by storeFactory {

    fun create(): TuneStore = object :
        TuneStore,
        Store<Intent, State, Nothing> by storeFactory.create(
            name = "TuneStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl
        ) {}

    private sealed interface LoadedModel {

        data object None : LoadedModel

        data class Chat(
            val model: ChatModel,
            val saved: Map<TuningParameter, Double>,
        ) : LoadedModel

        data class Image(
            val model: ImageModel,
            val saved: Map<ImageParameter, ImageParameterValue>,
        ) : LoadedModel
    }

    private sealed interface Action {

        data class ModelLoaded(val model: LoadedModel) : Action
    }

    private sealed interface Msg {

        data object PanelOpened : Msg
        data object PanelClosed : Msg

        data class HintsVisibilityChanged(val visible: Boolean) : Msg
        data class ModelLoaded(
            val modelName: String?,
            val content: State.Content,
        ) : Msg

        data class ChatItemChanged(val item: ChatTuneItem) : Msg
        data class ImageItemChanged(val item: ImageTuneItem) : Msg
        data object AllReset : Msg
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {

        override fun invoke() {
            observeActiveModelUseCase()
                .distinctUntilChanged()
                .flatMapLatest(::observeLoadedModel)
                .onEach { dispatch(Action.ModelLoaded(it)) }
                .launchIn(scope)
        }

        private fun observeLoadedModel(ref: ActiveModelRef?): Flow<LoadedModel> = when (ref) {
            null -> flowOf(LoadedModel.None)

            is ActiveModelRef.Chat -> observeChatModelByIdUseCase(ref.id)
                .distinctUntilChanged()
                .map { model ->
                    if (model == null) {
                        LoadedModel.None
                    } else {
                        LoadedModel.Chat(model, observeChatTuningParametersUseCase().first())
                    }
                }

            is ActiveModelRef.Image -> observeImageModelByIdUseCase(ref.id)
                .distinctUntilChanged()
                .map { model ->
                    if (model == null) {
                        LoadedModel.None
                    } else {
                        LoadedModel.Image(model, observeImageParametersUseCase().first())
                    }
                }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Nothing>() {

        private var lastSyncedChat: Map<TuningParameter, Double> = emptyMap()
        private var lastSyncedImage: Map<ImageParameter, ImageParameterValue> = emptyMap()

        override fun executeAction(action: Action) {
            when (action) {
                is Action.ModelLoaded -> showModel(action.model)
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.OpenPanel -> dispatch(Msg.PanelOpened)

                Intent.ClosePanel -> dispatch(Msg.PanelClosed)

                is Intent.ChangeHintsVisibility -> dispatch(Msg.HintsVisibilityChanged(intent.visible))

                Intent.ResetAll -> {
                    dispatch(Msg.AllReset)
                    syncSession()
                }

                is Intent.ChangeChatValue -> changeChat(intent.parameter) {
                    it.copy(raw = intent.raw)
                }

                is Intent.ChangeChatSlider -> changeChat(intent.parameter) {
                    it.copy(raw = it.spec.format(it.spec.snap(intent.value)))
                }

                Intent.SetMaxTokensToLimit -> {
                    val limit = (state().content as? State.Content.Chat)?.maxTokensLimit ?: return
                    changeChat(TuningParameter.MAX_TOKENS) { it.copy(raw = limit.toString()) }
                }

                Intent.RandomizeChatSeed -> changeChat(TuningParameter.SEED) {
                    it.copy(raw = randomInRange(it.spec.min, it.spec.max))
                }

                is Intent.SelectImageOption -> changeImage(intent.parameter) { item ->
                    (item as? ImageTuneItem.Choice)?.let {
                        it.copy(selected = intent.option.takeIf { option -> option != it.selected })
                    }
                }

                is Intent.ChangeImageValue -> changeImage(intent.parameter) { item ->
                    (item as? ImageTuneItem.Numeric)?.copy(raw = intent.raw)
                }

                is Intent.ChangeImageSlider -> changeImage(intent.parameter) { item ->
                    (item as? ImageTuneItem.Numeric)?.let {
                        it.copy(raw = it.spec.format(it.spec.snap(intent.value)))
                    }
                }

                Intent.RandomizeImageSeed -> changeImage(ImageParameter.SEED) { item ->
                    (item as? ImageTuneItem.Numeric)?.copy(
                        raw = randomInRange(item.spec.min, item.spec.max)
                    )
                }
            }
        }

        private fun showModel(loaded: LoadedModel) {
            when (loaded) {
                LoadedModel.None -> dispatch(Msg.ModelLoaded(null, State.Content.NoModel))

                is LoadedModel.Chat -> {
                    val items = loaded.model.toTuneItems(loaded.saved)
                    val content = if (items.isEmpty()) {
                        State.Content.Unsupported
                    } else {
                        State.Content.Chat(
                            items = items,
                            maxTokensLimit = loaded.model.maxCompletionTokens?.takeIf { it > 0 }
                        )
                    }
                    dispatch(Msg.ModelLoaded(loaded.model.name, content))
                    lastSyncedChat = loaded.saved
                    syncSession()
                }

                is LoadedModel.Image -> {
                    val items = loaded.model.toTuneItems(loaded.saved)
                    val content = if (items.isEmpty()) {
                        State.Content.Unsupported
                    } else {
                        State.Content.Image(items)
                    }
                    dispatch(Msg.ModelLoaded(loaded.model.name, content))
                    lastSyncedImage = loaded.saved
                    syncSession()
                }
            }
        }

        private fun changeChat(
            parameter: TuningParameter,
            transform: (ChatTuneItem) -> ChatTuneItem,
        ) {
            val item = (state().content as? State.Content.Chat)
                ?.items
                ?.firstOrNull { it.spec.parameter == parameter }
                ?: return
            dispatch(Msg.ChatItemChanged(transform(item)))
            syncSession()
        }

        private fun changeImage(
            parameter: ImageParameter,
            transform: (ImageTuneItem) -> ImageTuneItem?,
        ) {
            val item = (state().content as? State.Content.Image)
                ?.items
                ?.firstOrNull { it.parameter == parameter }
                ?: return
            dispatch(Msg.ImageItemChanged(transform(item) ?: return))
            syncSession()
        }

        private fun syncSession() {
            when (val content = state().content) {
                is State.Content.Chat -> {
                    val parameters = content.items.toSessionMap()
                    if (parameters == lastSyncedChat) return
                    lastSyncedChat = parameters
                    scope.launch {
                        try {
                            setChatTuningParametersUseCase(parameters)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                        }
                    }
                }

                is State.Content.Image -> {
                    val parameters = content.items.toSessionMap()
                    if (parameters == lastSyncedImage) return
                    lastSyncedImage = parameters
                    scope.launch {
                        try {
                            setImageParametersUseCase(parameters)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                        }
                    }
                }

                State.Content.NoModel,
                State.Content.Unsupported,
                -> return
            }
        }

        private fun randomInRange(min: Double, max: Double): String =
            Random.nextLong(min.toLong(), max.toLong() + 1).toString()
    }

    private object ReducerImpl : Reducer<State, Msg> {

        override fun State.reduce(msg: Msg): State = when (msg) {
            Msg.PanelOpened -> copy(isPanelOpen = true)

            Msg.PanelClosed -> copy(isPanelOpen = false)

            is Msg.HintsVisibilityChanged -> copy(hintsVisible = msg.visible)

            is Msg.ModelLoaded -> copy(modelName = msg.modelName, content = msg.content)

            is Msg.ChatItemChanged -> (content as? State.Content.Chat)?.let { chat ->
                copy(
                    content = chat.copy(
                        items = chat.items.map {
                            if (it.spec.parameter == msg.item.spec.parameter) msg.item else it
                        }
                    )
                )
            } ?: this

            is Msg.ImageItemChanged -> (content as? State.Content.Image)?.let { image ->
                copy(
                    content = image.copy(
                        items = image.items.map {
                            if (it.parameter == msg.item.parameter) msg.item else it
                        }
                    )
                )
            } ?: this

            Msg.AllReset -> when (val current = content) {
                is State.Content.Chat -> copy(
                    content = current.copy(items = current.items.map { it.copy(raw = null) })
                )

                is State.Content.Image -> copy(
                    content = current.copy(items = current.items.map { it.cleared() })
                )

                State.Content.NoModel,
                State.Content.Unsupported,
                -> this
            }
        }
    }
}
