package com.nullo.voidapp.feature.chat.presentation.store

import com.nullo.voidapp.core.model.ErrorReason
import com.nullo.voidapp.core.model.ImageAttachment
import com.nullo.voidapp.core.model.Message
import com.nullo.voidapp.core.model.ModelType
import com.nullo.voidapp.feature.chat.presentation.store.ChatStore.State
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private val attachment = ImageAttachment("a.png", "image/png")

private fun prompt(id: Long = 1) = Message.Prompt(id = id, query = "hello")

private fun completion(id: Long = 1, streaming: Boolean) =
    Message.Completion(id = id, text = "text", isStreaming = streaming)

private data class GeneratingCase(
    val description: String,
    val messages: List<Message>,
    val expected: Boolean,
)

private data class LimitCase(
    val maxAttachments: Int,
    val expected: Boolean,
)

private data class AttachCase(
    val description: String,
    val state: State,
    val expected: Boolean,
)

private data class SendCase(
    val description: String,
    val state: State,
    val expected: Boolean,
)

private val generatingCases = listOf(
    GeneratingCase(
        description = "a streaming completion",
        messages = listOf(completion(streaming = true)),
        expected = true
    ),
    GeneratingCase(
        description = "a finished completion",
        messages = listOf(completion(streaming = false)),
        expected = false
    ),
    GeneratingCase(
        description = "an image loading message",
        messages = listOf(Message.ImageLoading(id = 1)),
        expected = true
    ),
    GeneratingCase(
        description = "an images message",
        messages = listOf(Message.Images(id = 1, images = listOf(attachment))),
        expected = false
    ),
    GeneratingCase(
        description = "an error message",
        messages = listOf(Message.Error(id = 1, reason = ErrorReason.NETWORK, rawMessage = null)),
        expected = false
    ),
    GeneratingCase(
        description = "an interrupted message",
        messages = listOf(Message.Interrupted(id = 1, text = null)),
        expected = false
    ),
    GeneratingCase(
        description = "a prompt message",
        messages = listOf(prompt()),
        expected = false
    ),
    GeneratingCase(
        description = "no messages",
        messages = emptyList(),
        expected = false
    ),
    GeneratingCase(
        description = "a streaming completion followed by a prompt",
        messages = listOf(completion(streaming = true), prompt()),
        expected = false
    ),
    GeneratingCase(
        description = "a prompt followed by a streaming completion",
        messages = listOf(prompt(), completion(streaming = true)),
        expected = true
    )
)

private val supportsImageInputCases = listOf(
    LimitCase(maxAttachments = 0, expected = false),
    LimitCase(maxAttachments = 1, expected = true),
    LimitCase(maxAttachments = 8, expected = true)
)

private val canAttachImagesCases = listOf(
    AttachCase(
        description = "importing attachments with free room",
        state = State(isImportingAttachments = true, maxAttachments = 8),
        expected = false
    ),
    AttachCase(
        description = "at the attachment limit",
        state = State(maxAttachments = 2, promptImages = listOf(attachment, attachment)),
        expected = false
    ),
    AttachCase(
        description = "below the attachment limit",
        state = State(maxAttachments = 2, promptImages = listOf(attachment)),
        expected = true
    ),
    AttachCase(
        description = "without an attachment limit",
        state = State(maxAttachments = 0),
        expected = false
    )
)

private val canSendPromptCases = listOf(
    SendCase(
        description = "a non-blank prompt",
        state = State(prompt = "hello", modelType = ModelType.CHAT),
        expected = true
    ),
    SendCase(
        description = "a blank prompt without images",
        state = State(prompt = "   ", modelType = ModelType.CHAT),
        expected = false
    ),
    SendCase(
        description = "images only on a chat model",
        state = State(prompt = "", promptImages = listOf(attachment), modelType = ModelType.CHAT),
        expected = true
    ),
    SendCase(
        description = "images only on an image model",
        state = State(prompt = "", promptImages = listOf(attachment), modelType = ModelType.IMAGE),
        expected = false
    ),
    SendCase(
        description = "a prompt while importing attachments",
        state = State(prompt = "hello", isImportingAttachments = true),
        expected = false
    )
)

val ChatStateTest by testSuite {

    testSuite("isGenerating") {

        generatingCases.forEach { case ->
            test("${case.description} is generating=${case.expected}") {
                State(messages = case.messages).isGenerating shouldBe case.expected
            }
        }
    }

    testSuite("supportsImageInput") {

        supportsImageInputCases.forEach { case ->
            test("maxAttachments ${case.maxAttachments} supports images=${case.expected}") {
                State(maxAttachments = case.maxAttachments).supportsImageInput shouldBe case.expected
            }
        }
    }

    testSuite("canAttachImages") {

        canAttachImagesCases.forEach { case ->
            test("${case.description} can attach=${case.expected}") {
                case.state.canAttachImages shouldBe case.expected
            }
        }
    }

    testSuite("canSendPrompt") {

        canSendPromptCases.forEach { case ->
            test("${case.description} can send=${case.expected}") {
                case.state.canSendPrompt shouldBe case.expected
            }
        }
    }
}
