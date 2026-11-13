package com.nullo.voidapp.core.model

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.nullo.voidapp.core.model.chatmodel.ChatModel
import com.nullo.voidapp.core.model.chatmodel.DefaultTuningParameters
import com.nullo.voidapp.core.model.chatmodel.Modality
import com.nullo.voidapp.core.model.chatmodel.ModelFeature
import com.nullo.voidapp.core.model.chatmodel.ModelPricing
import com.nullo.voidapp.core.model.chatmodel.TuningParameter
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

private fun chatModel(
    inputModalities: Set<Modality> = emptySet(),
    supportedTuningParameters: Set<TuningParameter> = emptySet(),
    supportedFeatures: Set<ModelFeature> = emptySet(),
) = ChatModel(
    id = "test/chat-model",
    name = "Test Model",
    description = "Chat model for testing",
    createdAtTimestamp = 987654321,
    contextLength = 1_000_000,
    maxCompletionTokens = 256_000,
    inputModalities = inputModalities,
    outputModalities = emptySet(),
    supportedTuningParameters = supportedTuningParameters,
    defaultTuningParameters = DefaultTuningParameters(),
    supportedFeatures = supportedFeatures,
    pricing = ModelPricing(promptPerMillion = 2.toBigDecimal(), completionPerMillion = 10.toBigDecimal()),
    pinned = false
)

val ChatModelTest by testSuite {
    test("supportsReasoning is true when thinking feature is declared") {
        val model = chatModel(supportedFeatures = setOf(ModelFeature.THINKING))
        model.supportsReasoning shouldBe true
    }

    test("supportsReasoning is false when thinking feature is undeclared") {
        chatModel().supportsReasoning shouldBe false
    }

    test("supportsVision is true when IMAGE input modality is declared") {
        val model = chatModel(
            inputModalities = setOf(Modality.IMAGE)
        )
        model.supportsVision shouldBe true
    }

    test("supportsVision is false when IMAGE input modality is undeclared") {
        chatModel().supportsVision shouldBe false
    }

    test("supports() returns true for declared tuning parameter") {
        val model = chatModel(
            supportedTuningParameters = setOf(TuningParameter.TEMPERATURE)
        )
        model.supports(TuningParameter.TEMPERATURE) shouldBe true
    }

    test("supports() returns false for undeclared tuning parameter") {
        val model = chatModel(
            supportedTuningParameters = setOf(TuningParameter.TEMPERATURE)
        )
        model.supports(TuningParameter.TOP_P) shouldBe false
    }
}
