package com.nullo.voidapp.core.model

import com.nullo.voidapp.core.model.chatmodel.Modality
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

val ModalityTest by testSuite {

    Modality.entries.forEach { modality ->
        test("modality ${modality.name} maps to $modality") {
            Modality.fromKey(modality.name) shouldBe modality
        }
    }

    Modality.entries.forEach { modality ->
        test("lowercase modality ${modality.name.lowercase()} maps to $modality") {
            Modality.fromKey(modality.name.lowercase()) shouldBe modality
        }
    }

    test("empty modality maps to null") {
        Modality.fromKey("") shouldBe null
    }

    test("unknown modality maps to null") {
        Modality.fromKey("unknown-modality") shouldBe null
    }
}
