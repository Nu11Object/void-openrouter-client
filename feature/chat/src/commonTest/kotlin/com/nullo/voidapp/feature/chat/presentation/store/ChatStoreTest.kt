package com.nullo.voidapp.feature.chat.presentation.store

import com.nullo.voidapp.core.fakes.FakeAttachmentStorage
import com.nullo.voidapp.core.fakes.FakeCatalogRepository
import com.nullo.voidapp.core.fakes.FakeChatRepository
import com.nullo.voidapp.core.fakes.FakeConversationRepository
import com.nullo.voidapp.core.fakes.FakeSessionRepository
import com.nullo.voidapp.core.model.ActiveModelRef
import com.nullo.voidapp.core.model.Conversation
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.imagemodel.ImageModel
import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import com.nullo.voidapp.core.testing.chatModel
import com.nullo.voidapp.core.testing.imageModel
import com.nullo.voidapp.core.utils.resources.UiText
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore.Intent
import de.infix.testBalloon.framework.core.testSuite
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.PlatformFileSerializer
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import voidapp.core.utils.generated.resources.Res as CoreRes
import voidapp.core.utils.generated.resources.error_unknown
import voidapp.feature.chat.generated.resources.Res
import voidapp.feature.chat.generated.resources.error_empty_prompt_on_image
import voidapp.feature.chat.generated.resources.error_empty_request
import voidapp.feature.chat.generated.resources.error_image_save_failed
import voidapp.feature.chat.generated.resources.error_model_no_image_input
import voidapp.feature.chat.generated.resources.error_model_not_selected
import voidapp.feature.chat.generated.resources.error_no_active_conversation
import voidapp.feature.chat.generated.resources.error_some_attachments_import_failed
import voidapp.feature.chat.generated.resources.template_error_max_attachments_reached
import voidapp.feature.chat.generated.resources.template_error_model_max_images

private val visionChatModel = chatModel(
    id = "vision-chat",
    name = "Vision Chat",
    inputModalities = setOf(Modality.TEXT, Modality.IMAGE)
)

private val textChatModel = chatModel(
    id = "text-chat",
    name = "Text Chat"
)

private fun imageModelWithReferences(id: String, references: Int) = imageModel(
    id = id,
    name = "Image $id",
    supportedParameters = mapOf(
        ImageParameter.INPUT_REFERENCES to ImageParameterCapability.Range(0.0, references.toDouble())
    )
)

private fun conversation(id: Long) = Conversation(
    id = id,
    title = "Conversation $id",
    createdAt = 0L,
    updatedAt = 0L
)

private fun prompt(id: Long, query: String = "hello") = Message.Prompt(id = id, query = query)

private fun completion(id: Long) = Message.Completion(id = id, text = "text")

private fun imageAttachment(name: String) = ImageAttachment(relativePath = name, mimeType = "image/png")

private fun platformFile(name: String): PlatformFile = Json.decodeFromString(PlatformFileSerializer, "\"$name\"")

private fun holder(id: Long?) = ActiveConversationHolder().apply { set(id) }

private fun catalog(
    chatModels: List<ChatModel> = emptyList(),
    imageModels: List<ImageModel> = emptyList(),
) = FakeCatalogRepository(initialChatModels = chatModels, initialImageModels = imageModels)

private fun chatSession(model: ChatModel) =
    FakeSessionRepository(initialActiveModel = ActiveModelRef.Chat(model.id))

private fun imageSession(model: ImageModel) =
    FakeSessionRepository(initialActiveModel = ActiveModelRef.Image(model.id))

private fun ChatStoreTestEnv.attachImages(vararg names: String) {
    store.accept(Intent.AttachImages(names.map(::platformFile)))
    idle()
}

private data class ImageLimitCase(
    val references: Int,
    val expectedLimit: Int,
)

private val imageLimitCases = listOf(
    ImageLimitCase(references = 0, expectedLimit = 0),
    ImageLimitCase(references = 4, expectedLimit = 4),
    ImageLimitCase(references = 20, expectedLimit = 8)
)

val ChatStoreTest by testSuite {

    testSuite("model") {

        test("without an active model the name, type and attachment limit are empty") {
            withChatStore {
                store.state.activeModelName shouldBe null
                store.state.modelType shouldBe null
                store.state.maxAttachments shouldBe 0
            }
        }

        test("a vision chat model is CHAT with the full attachment limit") {
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel)
            ) {
                store.state.activeModelName shouldBe "Vision Chat"
                store.state.modelType shouldBe ModelType.CHAT
                store.state.maxAttachments shouldBe 8
            }
        }

        test("a chat model without vision has no attachment limit") {
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(textChatModel)),
                sessionRepository = chatSession(textChatModel)
            ) {
                store.state.activeModelName shouldBe "Text Chat"
                store.state.modelType shouldBe ModelType.CHAT
                store.state.maxAttachments shouldBe 0
            }
        }

        imageLimitCases.forEach { case ->
            test(
                "an image model with ${case.references} input references has limit " +
                    "${case.expectedLimit}"
            ) {
                val model = imageModelWithReferences("image-${case.references}", case.references)
                withChatStore(
                    catalogRepository = catalog(imageModels = listOf(model)),
                    sessionRepository = imageSession(model)
                ) {
                    store.state.activeModelName shouldBe model.name
                    store.state.modelType shouldBe ModelType.IMAGE
                    store.state.maxAttachments shouldBe case.expectedLimit
                }
            }
        }

        test("switching the active model updates the name, type and limit") {
            val imageModel = imageModelWithReferences("image-4", 4)
            withChatStore(
                catalogRepository = catalog(
                    chatModels = listOf(visionChatModel),
                    imageModels = listOf(imageModel)
                ),
                sessionRepository = chatSession(visionChatModel)
            ) {
                sessionRepository.setActiveModel(ActiveModelRef.Image(imageModel.id))
                idle()

                store.state.activeModelName shouldBe imageModel.name
                store.state.modelType shouldBe ModelType.IMAGE
                store.state.maxAttachments shouldBe 4
            }
        }

        test("clearing the session keeps the last name, type and limit in State") {
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel)
            ) {
                sessionRepository.clear()
                idle()

                store.state.activeModelName shouldBe "Vision Chat"
                store.state.modelType shouldBe ModelType.CHAT
                store.state.maxAttachments shouldBe 8
            }
        }
    }

    testSuite("flags") {

        test("thinking and web search follow the session") {
            withChatStore(
                sessionRepository = FakeSessionRepository(
                    initialThinkingEnabled = true,
                    initialWebSearchEnabled = true
                )
            ) {
                store.state.thinkingEnabled shouldBe true
                store.state.webSearchEnabled shouldBe true
            }
        }

        test("ToggleThinking writes the negated value") {
            withChatStore {
                store.accept(Intent.ToggleThinking)
                idle()

                sessionRepository.calls shouldBe listOf("setThinkingEnabled:true")
                store.state.thinkingEnabled shouldBe true

                store.accept(Intent.ToggleThinking)
                idle()

                sessionRepository.calls shouldBe listOf(
                    "setThinkingEnabled:true",
                    "setThinkingEnabled:false"
                )
                store.state.thinkingEnabled shouldBe false
            }
        }

        test("ToggleWebSearch writes the negated value") {
            withChatStore {
                store.accept(Intent.ToggleWebSearch)
                idle()

                sessionRepository.calls shouldBe listOf("setWebSearchEnabled:true")
                store.state.webSearchEnabled shouldBe true

                store.accept(Intent.ToggleWebSearch)
                idle()

                sessionRepository.calls shouldBe listOf(
                    "setWebSearchEnabled:true",
                    "setWebSearchEnabled:false"
                )
                store.state.webSearchEnabled shouldBe false
            }
        }

        test("a failed thinking write shows an unknown error") {
            val sessionRepository = FakeSessionRepository()
            sessionRepository.setThinkingEnabledFailure = Exception("x")
            withChatStore(sessionRepository = sessionRepository) {
                store.accept(Intent.ToggleThinking)
                idle()

                store.state.errorDialogText shouldBe UiText(CoreRes.string.error_unknown)
            }
        }

        test("a failed web search write shows an unknown error") {
            val sessionRepository = FakeSessionRepository()
            sessionRepository.setWebSearchEnabledFailure = Exception("x")
            withChatStore(sessionRepository = sessionRepository) {
                store.accept(Intent.ToggleWebSearch)
                idle()

                store.state.errorDialogText shouldBe UiText(CoreRes.string.error_unknown)
            }
        }
    }

    testSuite("conversation") {

        test("messages of the held conversation appear") {
            val messages = listOf(prompt(id = 1), completion(id = 2))
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1)),
                    initialMessages = mapOf(1L to messages)
                ),
                activeConversation = holder(1)
            ) {
                store.state.messages shouldBe messages
            }
        }

        test("changing the held conversation switches the messages") {
            val firstMessages = listOf(prompt(id = 1))
            val secondMessages = listOf(prompt(id = 2), completion(id = 3))
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1), conversation(2)),
                    initialMessages = mapOf(1L to firstMessages, 2L to secondMessages)
                )
            ) {
                activeConversation.set(1)
                idle()

                store.state.messages shouldBe firstMessages

                activeConversation.set(2)
                idle()

                store.state.messages shouldBe secondMessages
            }
        }

        test("a null holder shows no messages") {
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1)),
                    initialMessages = mapOf(1L to listOf(prompt(id = 1)))
                )
            ) {
                store.state.messages shouldBe emptyList()
            }
        }

        test("a message added later appears") {
            val added = listOf(prompt(id = 1), completion(id = 2))
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1))
                ),
                activeConversation = holder(1)
            ) {
                conversationRepository.changeMessagesExternally(1, added)
                idle()

                store.state.messages shouldBe added
            }
        }

        test("deleting the open conversation resets the holder and clears the messages") {
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1)),
                    initialMessages = mapOf(1L to listOf(prompt(id = 1)))
                ),
                activeConversation = holder(1)
            ) {
                conversationRepository.delete(1)
                idle()

                activeConversation.id.value shouldBe null
                store.state.messages shouldBe emptyList()
            }
        }

        test("a holder id of a missing conversation is reset to null") {
            withChatStore(activeConversation = holder(99)) {
                activeConversation.id.value shouldBe null
                store.state.messages shouldBe emptyList()
            }
        }
    }

    testSuite("prompt") {

        test("ChangePrompt stores the prompt") {
            withChatStore {
                store.accept(Intent.ChangePrompt("hello"))

                store.state.prompt shouldBe "hello"
            }
        }

        test("DismissErrorDialog clears the error") {
            withChatStore {
                store.accept(Intent.SendPrompt)
                idle()

                store.state.errorDialogText shouldBe UiText(Res.string.error_model_not_selected)

                store.accept(Intent.DismissErrorDialog)
                idle()

                store.state.errorDialogText shouldBe null
            }
        }
    }

    testSuite("sendPrompt guards") {

        test("SendPrompt without an active model shows model not selected") {
            withChatStore {
                store.accept(Intent.ChangePrompt("hello"))
                store.accept(Intent.SendPrompt)
                idle()

                store.state.errorDialogText shouldBe UiText(Res.string.error_model_not_selected)
                store.state.prompt shouldBe "hello"
                store.state.promptImages shouldBe emptyList()
                conversationRepository.calls shouldBe emptyList()
                chatRepository.calls shouldBe emptyList()
            }
        }

        test("SendPrompt with a blank prompt and no images shows empty request") {
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel)
            ) {
                store.accept(Intent.SendPrompt)
                idle()

                store.state.errorDialogText shouldBe UiText(Res.string.error_empty_request)
                store.state.prompt shouldBe ""
                store.state.promptImages shouldBe emptyList()
                conversationRepository.calls shouldBe emptyList()
                chatRepository.calls shouldBe emptyList()
            }
        }

        test("SendPrompt with images and a blank prompt on an image model shows empty prompt") {
            val imageModel = imageModelWithReferences("image-4", 4)
            withChatStore(
                catalogRepository = catalog(imageModels = listOf(imageModel)),
                sessionRepository = imageSession(imageModel)
            ) {
                attachImages("a.png")

                store.accept(Intent.SendPrompt)
                idle()

                store.state.errorDialogText shouldBe UiText(Res.string.error_empty_prompt_on_image)
                store.state.prompt shouldBe ""
                store.state.promptImages shouldBe listOf(imageAttachment("a.png"))
                conversationRepository.calls shouldBe emptyList()
                chatRepository.calls shouldBe emptyList()
            }
        }

        test("SendPrompt with images after switching to a model without vision shows no image input") {
            withChatStore(
                catalogRepository = catalog(
                    chatModels = listOf(visionChatModel, textChatModel)
                ),
                sessionRepository = chatSession(visionChatModel)
            ) {
                attachImages("a.png")

                sessionRepository.setActiveModel(ActiveModelRef.Chat(textChatModel.id))
                idle()

                store.accept(Intent.ChangePrompt("hello"))
                store.accept(Intent.SendPrompt)
                idle()

                store.state.errorDialogText shouldBe UiText(Res.string.error_model_no_image_input)
                store.state.prompt shouldBe "hello"
                store.state.promptImages shouldBe listOf(imageAttachment("a.png"))
                conversationRepository.calls shouldBe emptyList()
                chatRepository.calls shouldBe emptyList()
            }
        }

        test("SendPrompt with more images than the new model limit shows the limit") {
            val limitThree = imageModelWithReferences("image-3", 3)
            val limitOne = imageModelWithReferences("image-1", 1)
            withChatStore(
                catalogRepository = catalog(imageModels = listOf(limitThree, limitOne)),
                sessionRepository = imageSession(limitThree)
            ) {
                attachImages("a.png", "b.png", "c.png")

                sessionRepository.setActiveModel(ActiveModelRef.Image(limitOne.id))
                idle()

                store.accept(Intent.ChangePrompt("hello"))
                store.accept(Intent.SendPrompt)
                idle()

                store.state.errorDialogText shouldBe
                    UiText(Res.string.template_error_model_max_images, 1)
                store.state.prompt shouldBe "hello"
                store.state.promptImages shouldBe listOf(
                    imageAttachment("a.png"),
                    imageAttachment("b.png"),
                    imageAttachment("c.png")
                )
                conversationRepository.calls shouldBe emptyList()
                chatRepository.calls shouldBe emptyList()
            }
        }
    }

    testSuite("sendPrompt") {

        test("SendPrompt in a new conversation creates it and clears the composer immediately") {
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel)
            ) {
                attachImages("a.png")

                store.accept(Intent.ChangePrompt("  hello  "))
                store.accept(Intent.SendPrompt)

                store.state.prompt shouldBe ""
                store.state.promptImages shouldBe emptyList()

                idle()

                conversationRepository.calls shouldBe listOf("createConversation:hello")
                activeConversation.id.value shouldBe 1L
                chatRepository.calls shouldBe
                    listOf("sendPrompt:1:hello:1:vision-chat")
                store.state.prompt shouldBe ""
                store.state.promptImages shouldBe emptyList()
            }
        }

        test("SendPrompt in an existing conversation does not create a conversation") {
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(7))
                ),
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                activeConversation = holder(7)
            ) {
                store.accept(Intent.ChangePrompt("hello"))
                store.accept(Intent.SendPrompt)
                idle()

                conversationRepository.calls shouldBe emptyList()
                chatRepository.calls shouldBe
                    listOf("sendPrompt:7:hello:0:vision-chat")
            }
        }

        test("SendPrompt is ignored while a generation runs and works after it finishes") {
            val chatRepository = FakeChatRepository()
            chatRepository.sendPromptDelay = 1.seconds
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                chatRepository = chatRepository
            ) {
                store.accept(Intent.ChangePrompt("first"))
                store.accept(Intent.SendPrompt)
                idle()

                store.accept(Intent.ChangePrompt("second"))
                store.accept(Intent.SendPrompt)
                idle()

                chatRepository.calls shouldBe
                    listOf("sendPrompt:1:first:0:vision-chat")

                advanceBy(1.seconds)

                store.accept(Intent.ChangePrompt("third"))
                store.accept(Intent.SendPrompt)
                idle()

                chatRepository.calls shouldBe listOf(
                    "sendPrompt:1:first:0:vision-chat",
                    "sendPrompt:1:third:0:vision-chat"
                )
            }
        }

        test("SendPrompt is ignored while attachments are importing") {
            val attachmentStorage = FakeAttachmentStorage()
            attachmentStorage.importDelay = 1.seconds
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                attachmentStorage = attachmentStorage
            ) {
                store.accept(Intent.AttachImages(listOf(platformFile("a.png"))))
                idle()

                store.state.isImportingAttachments shouldBe true

                store.accept(Intent.ChangePrompt("hello"))
                store.accept(Intent.SendPrompt)
                idle()

                chatRepository.calls shouldBe emptyList()

                advanceBy(1.seconds)

                store.accept(Intent.SendPrompt)
                idle()

                chatRepository.calls shouldBe
                    listOf("sendPrompt:1:hello:1:vision-chat")
            }
        }
    }

    testSuite("stop") {

        test("StopGeneration cancels the running send and a new send works immediately") {
            val chatRepository = FakeChatRepository()
            chatRepository.sendPromptDelay = 1.seconds
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1))
                ),
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                chatRepository = chatRepository,
                activeConversation = holder(1)
            ) {
                store.accept(Intent.ChangePrompt("first"))
                store.accept(Intent.SendPrompt)
                idle()

                store.accept(Intent.StopGeneration)
                idle()

                chatRepository.sendPromptCancellations shouldBe 1

                store.accept(Intent.ChangePrompt("second"))
                store.accept(Intent.SendPrompt)
                idle()

                chatRepository.calls shouldBe listOf(
                    "sendPrompt:1:first:0:vision-chat",
                    "sendPrompt:1:second:0:vision-chat"
                )
            }
        }

        test("changing the held conversation cancels the running send") {
            val chatRepository = FakeChatRepository()
            chatRepository.sendPromptDelay = 1.seconds
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1), conversation(2))
                ),
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                chatRepository = chatRepository,
                activeConversation = holder(1)
            ) {
                store.accept(Intent.ChangePrompt("hello"))
                store.accept(Intent.SendPrompt)
                idle()

                activeConversation.set(2)
                idle()

                chatRepository.sendPromptCancellations shouldBe 1
            }
        }

        test("the conversation id set by SendPrompt does not cancel its own send") {
            val chatRepository = FakeChatRepository()
            chatRepository.sendPromptDelay = 1.seconds
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                chatRepository = chatRepository
            ) {
                store.accept(Intent.ChangePrompt("hello"))
                store.accept(Intent.SendPrompt)
                idle()

                activeConversation.id.value shouldBe 1L
                chatRepository.sendPromptCancellations shouldBe 0

                advanceBy(1.seconds)

                chatRepository.sendPromptCompletions shouldBe 1
            }
        }
    }

    testSuite("failure") {

        test("a failed send shows an unknown error and restores the prompt and images") {
            val chatRepository = FakeChatRepository()
            chatRepository.sendPromptFailure = Exception("x")
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                chatRepository = chatRepository
            ) {
                attachImages("a.png")

                store.accept(Intent.ChangePrompt("  hello  "))
                store.accept(Intent.SendPrompt)
                idle()

                store.state.errorDialogText shouldBe UiText(CoreRes.string.error_unknown)
                store.state.prompt shouldBe "hello"
                store.state.promptImages shouldBe listOf(imageAttachment("a.png"))
                chatRepository.calls shouldBe listOf("sendPrompt:1:hello:1:vision-chat")
            }
        }
    }

    testSuite("regenerate") {

        test("RegenerateLastResponse without an open conversation shows no active conversation") {
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel)
            ) {
                store.accept(Intent.RegenerateLastResponse)
                idle()

                store.state.errorDialogText shouldBe
                    UiText(Res.string.error_no_active_conversation)
                chatRepository.calls shouldBe emptyList()
            }
        }

        test("RegenerateLastResponse without a model shows model not selected") {
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1))
                ),
                activeConversation = holder(1)
            ) {
                store.accept(Intent.RegenerateLastResponse)
                idle()

                store.state.errorDialogText shouldBe UiText(Res.string.error_model_not_selected)
                chatRepository.calls shouldBe emptyList()
            }
        }

        test("RegenerateLastResponse records the conversation and model") {
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1))
                ),
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                activeConversation = holder(1)
            ) {
                store.accept(Intent.RegenerateLastResponse)
                idle()

                chatRepository.calls shouldBe
                    listOf("regenerateLastResponse:1:vision-chat")
            }
        }

        test("RegenerateLastResponse is ignored while a generation runs") {
            val chatRepository = FakeChatRepository()
            chatRepository.sendPromptDelay = 1.seconds
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1))
                ),
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                chatRepository = chatRepository,
                activeConversation = holder(1)
            ) {
                store.accept(Intent.ChangePrompt("hello"))
                store.accept(Intent.SendPrompt)
                idle()

                store.accept(Intent.RegenerateLastResponse)
                idle()

                chatRepository.calls shouldBe
                    listOf("sendPrompt:1:hello:0:vision-chat")

                advanceBy(1.seconds)

                chatRepository.calls shouldBe
                    listOf("sendPrompt:1:hello:0:vision-chat")
            }
        }

        test("a failed regeneration shows an unknown error") {
            val chatRepository = FakeChatRepository()
            chatRepository.regenerateFailure = Exception("x")
            withChatStore(
                conversationRepository = FakeConversationRepository(
                    initialConversations = listOf(conversation(1))
                ),
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                chatRepository = chatRepository,
                activeConversation = holder(1)
            ) {
                store.accept(Intent.RegenerateLastResponse)
                idle()

                store.state.errorDialogText shouldBe UiText(CoreRes.string.error_unknown)
            }
        }
    }

    testSuite("dismissError") {

        test("DismissChatError records the message id") {
            withChatStore {
                store.accept(Intent.DismissChatError(42))
                idle()

                chatRepository.calls shouldBe listOf("dismissError:42")
            }
        }

        test("a failed dismissal shows an unknown error") {
            val chatRepository = FakeChatRepository()
            chatRepository.dismissErrorFailure = Exception("x")
            withChatStore(chatRepository = chatRepository) {
                store.accept(Intent.DismissChatError(42))
                idle()

                store.state.errorDialogText shouldBe UiText(CoreRes.string.error_unknown)
            }
        }
    }

    testSuite("attachments") {

        test("AttachImages adds the imported attachments in order") {
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel)
            ) {
                attachImages("a.png", "b.png", "c.png")

                store.state.promptImages shouldBe listOf(
                    imageAttachment("a.png"),
                    imageAttachment("b.png"),
                    imageAttachment("c.png")
                )
                attachmentStorage.calls shouldBe listOf(
                    "import:a.png",
                    "import:b.png",
                    "import:c.png"
                )
            }
        }

        test("isImportingAttachments is true during the import and false after it") {
            val attachmentStorage = FakeAttachmentStorage()
            attachmentStorage.importDelay = 1.seconds
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                attachmentStorage = attachmentStorage
            ) {
                store.accept(Intent.AttachImages(listOf(platformFile("a.png"))))
                idle()

                store.state.isImportingAttachments shouldBe true

                advanceBy(1.seconds)

                store.state.isImportingAttachments shouldBe false
                store.state.promptImages shouldBe listOf(imageAttachment("a.png"))
            }
        }

        test("more files than the remaining room shows the limit and imports the rest") {
            val imageModel = imageModelWithReferences("image-2", 2)
            withChatStore(
                catalogRepository = catalog(imageModels = listOf(imageModel)),
                sessionRepository = imageSession(imageModel)
            ) {
                attachImages("a.png", "b.png", "c.png")

                store.state.errorDialogText shouldBe
                    UiText(Res.string.template_error_max_attachments_reached, 2)
                store.state.promptImages shouldBe listOf(
                    imageAttachment("a.png"),
                    imageAttachment("b.png")
                )
                attachmentStorage.calls shouldBe listOf("import:a.png", "import:b.png")
            }
        }

        test("no room left shows the limit and imports nothing") {
            val imageModel = imageModelWithReferences("image-1", 1)
            withChatStore(
                catalogRepository = catalog(imageModels = listOf(imageModel)),
                sessionRepository = imageSession(imageModel)
            ) {
                attachImages("a.png")

                store.accept(Intent.AttachImages(listOf(platformFile("b.png"))))
                idle()

                store.state.errorDialogText shouldBe
                    UiText(Res.string.template_error_max_attachments_reached, 1)
                store.state.promptImages shouldBe listOf(imageAttachment("a.png"))
                store.state.isImportingAttachments shouldBe false
                attachmentStorage.calls shouldBe listOf("import:a.png")
            }
        }

        test("one failing file shows an import error and keeps the other attachments") {
            val attachmentStorage = FakeAttachmentStorage()
            attachmentStorage.importFailureNames = setOf("b.png")
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                attachmentStorage = attachmentStorage
            ) {
                attachImages("a.png", "b.png", "c.png")

                store.state.errorDialogText shouldBe
                    UiText(Res.string.error_some_attachments_import_failed)
                store.state.promptImages shouldBe listOf(
                    imageAttachment("a.png"),
                    imageAttachment("c.png")
                )
                attachmentStorage.calls shouldBe listOf(
                    "import:a.png",
                    "import:b.png",
                    "import:c.png"
                )
            }
        }

        test("RemoveImageAttachment removes it from State and deletes the file") {
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel)
            ) {
                attachImages("a.png")

                store.accept(Intent.RemoveImageAttachment(imageAttachment("a.png")))
                idle()

                store.state.promptImages shouldBe emptyList()
                attachmentStorage.calls shouldBe listOf("import:a.png", "delete:a.png")
            }
        }

        test("a failed delete still removes the attachment and shows no error") {
            val attachmentStorage = FakeAttachmentStorage()
            attachmentStorage.deleteFailure = Exception("x")
            withChatStore(
                catalogRepository = catalog(chatModels = listOf(visionChatModel)),
                sessionRepository = chatSession(visionChatModel),
                attachmentStorage = attachmentStorage
            ) {
                attachImages("a.png")

                store.accept(Intent.RemoveImageAttachment(imageAttachment("a.png")))
                idle()

                store.state.promptImages shouldBe emptyList()
                store.state.errorDialogText shouldBe null
            }
        }
    }

    testSuite("saveImage") {

        test("SaveImage exports the image") {
            withChatStore {
                store.accept(
                    Intent.SaveImage(
                        image = imageAttachment("a.png"),
                        destination = platformFile("out.png")
                    )
                )
                idle()

                attachmentStorage.calls shouldBe listOf("export:a.png")
            }
        }

        test("a failed export shows an image save error") {
            val attachmentStorage = FakeAttachmentStorage()
            attachmentStorage.exportFailure = Exception("x")
            withChatStore(attachmentStorage = attachmentStorage) {
                store.accept(
                    Intent.SaveImage(
                        image = imageAttachment("a.png"),
                        destination = platformFile("out.png")
                    )
                )
                idle()

                store.state.errorDialogText shouldBe UiText(Res.string.error_image_save_failed)
            }
        }
    }
}
