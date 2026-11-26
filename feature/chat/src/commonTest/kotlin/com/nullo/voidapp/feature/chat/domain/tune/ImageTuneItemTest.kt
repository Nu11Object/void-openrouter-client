package com.nullo.voidapp.feature.chat.domain.tune

import com.nullo.voidapp.core.model.imagemodel.ImageParameter
import com.nullo.voidapp.core.model.imagemodel.ImageParameterCapability
import com.nullo.voidapp.core.model.imagemodel.ImageParameterValue
import com.nullo.voidapp.core.testing.imageModel
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private val imageTuneModel = imageModel(
    supportedParameters = mapOf(
        ImageParameter.RESOLUTION to ImageParameterCapability.Options(
            setOf("1024x1024", "512x512")
        ),
        ImageParameter.ASPECT_RATIO to ImageParameterCapability.Range(0.5, 2.0),
        ImageParameter.SIZE to ImageParameterCapability.Range(256.0, 1024.0),
        ImageParameter.QUALITY to ImageParameterCapability.Flag,
        ImageParameter.INPUT_REFERENCES to ImageParameterCapability.Range(0.0, 10.0),
        ImageParameter.OUTPUT_COMPRESSION to ImageParameterCapability.Range(0.0, 100.0),
        ImageParameter.N to ImageParameterCapability.Range(1.0, 4.0),
        ImageParameter.SEED to ImageParameterCapability.Range(0.0, 5_000_000_000.0)
    )
)

private val imageTuneItems = imageTuneModel.toTuneItems(emptyMap())

private fun imageItem(parameter: ImageParameter): ImageTuneItem =
    imageTuneItems.single { it.parameter == parameter }

private fun numericItem(parameter: ImageParameter): ImageTuneItem.Numeric =
    imageItem(parameter).shouldBeInstanceOf()

private val sizeSpec = ImageNumberSpec(256.0, 1024.0, isInteger = false, hasSlider = false)
private val nSpec = ImageNumberSpec(1.0, 4.0, isInteger = true, hasSlider = true)

private data class ImageChoiceCase(
    val value: ImageParameterValue,
    val expected: String?,
)

private data class ImageSpecCase(
    val parameter: ImageParameter,
    val min: Double,
    val max: Double,
    val isInteger: Boolean,
    val hasSlider: Boolean,
)

private data class ImageRestoreCase(
    val parameter: ImageParameter,
    val value: ImageParameterValue,
    val expectedRaw: String?,
)

private data class ImageSnapCase(
    val parameter: ImageParameter,
    val value: Double,
    val expected: Double,
)

private data class ImageParseCase(
    val parameter: ImageParameter,
    val raw: String,
    val expected: TuneInput,
)

private data class ImageSessionMapCase(
    val name: String,
    val items: List<ImageTuneItem>,
    val expected: Map<ImageParameter, ImageParameterValue>,
)

private val imageChoiceCases = listOf(
    ImageChoiceCase(ImageParameterValue.Text("512x512"), "512x512"),
    ImageChoiceCase(ImageParameterValue.Text("nope"), null),
    ImageChoiceCase(ImageParameterValue.Number(1024.0), null)
)

private val imageSpecCases = listOf(
    ImageSpecCase(ImageParameter.SIZE, 256.0, 1024.0, false, false),
    ImageSpecCase(ImageParameter.ASPECT_RATIO, 0.5, 2.0, false, false),
    ImageSpecCase(ImageParameter.OUTPUT_COMPRESSION, 0.0, 100.0, true, true),
    ImageSpecCase(ImageParameter.N, 1.0, 4.0, true, true),
    ImageSpecCase(ImageParameter.SEED, 0.0, 2_147_483_647.0, true, false)
)

private val imageRestoreCases = listOf(
    ImageRestoreCase(ImageParameter.SIZE, ImageParameterValue.Number(512.0), "512"),
    ImageRestoreCase(ImageParameter.SIZE, ImageParameterValue.Number(512.25), "512.25"),
    ImageRestoreCase(ImageParameter.SIZE, ImageParameterValue.Number(512.5), "512.5"),
    ImageRestoreCase(ImageParameter.N, ImageParameterValue.Number(2.0), "2"),
    ImageRestoreCase(
        ImageParameter.OUTPUT_COMPRESSION,
        ImageParameterValue.Number(50.0),
        "50"
    ),
    ImageRestoreCase(
        ImageParameter.SEED,
        ImageParameterValue.Number(2_147_483_647.0),
        "2147483647"
    ),
    ImageRestoreCase(ImageParameter.SIZE, ImageParameterValue.Number(5000.0), "5000"),
    ImageRestoreCase(ImageParameter.SIZE, ImageParameterValue.Text("x"), null)
)

private val imageSnapCases = listOf(
    ImageSnapCase(ImageParameter.SIZE, 512.4, 512.0),
    ImageSnapCase(ImageParameter.SIZE, 512.6, 513.0),
    ImageSnapCase(ImageParameter.OUTPUT_COMPRESSION, 50.4, 50.0),
    ImageSnapCase(ImageParameter.OUTPUT_COMPRESSION, 50.6, 51.0),
    ImageSnapCase(ImageParameter.N, 0.2, 1.0),
    ImageSnapCase(ImageParameter.N, 4.8, 4.0)
)

private val imageParseCases = listOf(
    ImageParseCase(ImageParameter.SIZE, "", TuneInput.Empty),
    ImageParseCase(
        ImageParameter.SIZE,
        "abc",
        TuneInput.Invalid(TuneInput.Reason.NOT_A_NUMBER)
    ),
    ImageParseCase(ImageParameter.SIZE, "512.5", TuneInput.Valid(512.5)),
    ImageParseCase(ImageParameter.SIZE, "512,5", TuneInput.Valid(512.5)),
    ImageParseCase(
        ImageParameter.SIZE,
        "100",
        TuneInput.Invalid(TuneInput.Reason.BELOW_MIN)
    ),
    ImageParseCase(
        ImageParameter.SIZE,
        "2000",
        TuneInput.Invalid(TuneInput.Reason.ABOVE_MAX)
    ),
    ImageParseCase(
        ImageParameter.N,
        "1.5",
        TuneInput.Invalid(TuneInput.Reason.NOT_AN_INTEGER)
    ),
    ImageParseCase(ImageParameter.N, "2", TuneInput.Valid(2.0))
)

private val imageSessionMapCases = listOf(
    ImageSessionMapCase(
        name = "a selected RESOLUTION, QUALITY without selection, SIZE 512 and invalid N",
        items = listOf(
            ImageTuneItem.Choice(ImageParameter.RESOLUTION, listOf("a", "b"), "a"),
            ImageTuneItem.Choice(ImageParameter.QUALITY, listOf("high"), null),
            ImageTuneItem.Numeric(ImageParameter.SIZE, sizeSpec, "512"),
            ImageTuneItem.Numeric(ImageParameter.N, nSpec, "abc")
        ),
        expected = mapOf(
            ImageParameter.RESOLUTION to ImageParameterValue.Text("a"),
            ImageParameter.SIZE to ImageParameterValue.Number(512.0)
        )
    ),
    ImageSessionMapCase(
        name = "RESOLUTION without selection, empty SIZE and invalid N",
        items = listOf(
            ImageTuneItem.Choice(ImageParameter.RESOLUTION, listOf("a"), null),
            ImageTuneItem.Numeric(ImageParameter.SIZE, sizeSpec, ""),
            ImageTuneItem.Numeric(ImageParameter.N, nSpec, "1.5")
        ),
        expected = emptyMap()
    )
)

val ImageTuneItemTest by testSuite {

    testSuite("toTuneItems") {

        test("items follow the image order and skip flag and unknown capabilities") {
            imageTuneItems.map { it.parameter } shouldBe listOf(
                ImageParameter.RESOLUTION,
                ImageParameter.ASPECT_RATIO,
                ImageParameter.SIZE,
                ImageParameter.OUTPUT_COMPRESSION,
                ImageParameter.N,
                ImageParameter.SEED
            )
        }

        test("the RESOLUTION choice exposes its options and has no selection") {
            val choice = imageItem(ImageParameter.RESOLUTION)
                .shouldBeInstanceOf<ImageTuneItem.Choice>()

            choice.options shouldBe listOf("1024x1024", "512x512")
            choice.selected shouldBe null
        }

        imageChoiceCases.forEach { case ->
            test("RESOLUTION saved as ${case.value} has selection ${case.expected}") {
                val restored = imageTuneModel.toTuneItems(
                    mapOf(ImageParameter.RESOLUTION to case.value)
                ).single { it.parameter == ImageParameter.RESOLUTION }
                    .shouldBeInstanceOf<ImageTuneItem.Choice>()

                restored.selected shouldBe case.expected
            }
        }

        imageSpecCases.forEach { case ->
            test(
                "${case.parameter} has range ${case.min}..${case.max} " +
                    "integer=${case.isInteger} slider=${case.hasSlider}"
            ) {
                val spec = numericItem(case.parameter).spec

                spec.min shouldBe (case.min plusOrMinus 1e-9)
                spec.max shouldBe (case.max plusOrMinus 1e-9)
                spec.isInteger shouldBe case.isInteger
                spec.hasSlider shouldBe case.hasSlider
                numericItem(case.parameter).raw shouldBe null
            }
        }

        imageRestoreCases.forEach { case ->
            test(
                "${case.parameter} saved as ${case.value} restores raw ${case.expectedRaw}"
            ) {
                val restored = imageTuneModel.toTuneItems(
                    mapOf(case.parameter to case.value)
                ).single { it.parameter == case.parameter }
                    .shouldBeInstanceOf<ImageTuneItem.Numeric>()

                restored.raw shouldBe case.expectedRaw
            }
        }

        test("restoring and mapping saved values is a round trip") {
            val saved = mapOf(
                ImageParameter.RESOLUTION to ImageParameterValue.Text("1024x1024"),
                ImageParameter.SIZE to ImageParameterValue.Number(512.25),
                ImageParameter.N to ImageParameterValue.Number(2.0),
                ImageParameter.SEED to ImageParameterValue.Number(42.0)
            )

            imageTuneModel.toTuneItems(saved).toSessionMap() shouldBe saved
        }
    }

    testSuite("spec") {

        imageSnapCases.forEach { case ->
            test("${case.parameter} ${case.value} snaps to ${case.expected}") {
                numericItem(case.parameter).spec.snap(case.value) shouldBe
                    (case.expected plusOrMinus 1e-9)
            }
        }

        imageParseCases.forEach { case ->
            test("raw \"${case.raw}\" of ${case.parameter} is ${case.expected}") {
                numericItem(case.parameter).spec.parse(case.raw) shouldBe case.expected
            }
        }
    }

    testSuite("toSessionMap") {

        imageSessionMapCases.forEach { case ->
            test("${case.name} maps to the expected session map") {
                case.items.toSessionMap() shouldBe case.expected
            }
        }
    }

    testSuite("cleared") {

        test("cleared removes the selected option or the raw number") {
            val choice = ImageTuneItem.Choice(ImageParameter.RESOLUTION, listOf("a", "b"), "a")
            val clearedChoice = choice.cleared().shouldBeInstanceOf<ImageTuneItem.Choice>()

            clearedChoice.selected shouldBe null
            clearedChoice.options shouldBe listOf("a", "b")

            val numeric = ImageTuneItem.Numeric(ImageParameter.SIZE, sizeSpec, "512")
            val clearedNumeric = numeric.cleared().shouldBeInstanceOf<ImageTuneItem.Numeric>()

            clearedNumeric.raw shouldBe null
            clearedNumeric.input shouldBe TuneInput.Empty
        }

        test("a cleared list maps to an empty session map") {
            listOf(
                ImageTuneItem.Choice(ImageParameter.RESOLUTION, listOf("a"), "a"),
                ImageTuneItem.Numeric(ImageParameter.SIZE, sizeSpec, "512")
            ).map { it.cleared() }.toSessionMap() shouldBe emptyMap()
        }
    }
}
