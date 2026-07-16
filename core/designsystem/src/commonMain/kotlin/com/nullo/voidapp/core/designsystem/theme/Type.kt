package com.nullo.voidapp.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import voidapp.core.designsystem.generated.resources.Res
import voidapp.core.designsystem.generated.resources.google_sans_flex

@Composable
fun googleSansFlexTypography(): Typography {
    val base = Typography()

    val displayFamily = googleSansFlexFamily(opsz = 57f)
    val headlineFamily = googleSansFlexFamily(opsz = 32f)
    val titleFamily = googleSansFlexFamily(opsz = 20f)
    val bodyFamily = googleSansFlexFamily(opsz = 14f)
    val labelFamily = googleSansFlexFamily(opsz = 11f)

    return remember(displayFamily, headlineFamily, titleFamily, bodyFamily, labelFamily) {
        Typography(
            displayLarge = base.displayLarge.copy(fontFamily = displayFamily),
            displayMedium = base.displayMedium.copy(fontFamily = displayFamily),
            displaySmall = base.displaySmall.copy(fontFamily = displayFamily),

            headlineLarge = base.headlineLarge.copy(fontFamily = headlineFamily),
            headlineMedium = base.headlineMedium.copy(fontFamily = headlineFamily),
            headlineSmall = base.headlineSmall.copy(fontFamily = headlineFamily),

            titleLarge = base.titleLarge.copy(fontFamily = titleFamily),
            titleMedium = base.titleMedium.copy(fontFamily = titleFamily),
            titleSmall = base.titleSmall.copy(fontFamily = titleFamily),

            bodyLarge = base.bodyLarge.copy(fontFamily = bodyFamily),
            bodyMedium = base.bodyMedium.copy(fontFamily = bodyFamily),
            bodySmall = base.bodySmall.copy(fontFamily = bodyFamily),

            labelLarge = base.labelLarge.copy(fontFamily = labelFamily),
            labelMedium = base.labelMedium.copy(fontFamily = labelFamily),
            labelSmall = base.labelSmall.copy(fontFamily = labelFamily)
        )
    }
}

private val GoogleSansWeights = listOf(
    FontWeight.Light,
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold
)

private fun FontVariation.opticalSizing(value: Float): FontVariation.Setting =
    FontVariation.Setting("opsz", value)

private fun FontVariation.grade(value: Float): FontVariation.Setting =
    FontVariation.Setting("GRAD", value)

private fun FontVariation.roundness(value: Float): FontVariation.Setting =
    FontVariation.Setting("ROND", value)

@Composable
private fun googleSansFlexFamily(
    opsz: Float,
    grad: Float = 0f,
    rond: Float = 0f,
    width: Float = 100f
): FontFamily {
    val fonts = mutableListOf<Font>()

    GoogleSansWeights.forEach { fontWeight ->
        // Normal Style
        fonts.add(
            Font(
                resource = Res.font.google_sans_flex,
                weight = fontWeight,
                style = FontStyle.Normal,
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(fontWeight.weight),
                    FontVariation.width(width),
                    FontVariation.opticalSizing(opsz),
                    FontVariation.grade(grad),
                    FontVariation.roundness(rond),
                    FontVariation.slant(0f)
                )
            )
        )

        // Italic
        fonts.add(
            Font(
                resource = Res.font.google_sans_flex,
                weight = fontWeight,
                style = FontStyle.Italic,
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(fontWeight.weight),
                    FontVariation.width(width),
                    FontVariation.opticalSizing(opsz),
                    FontVariation.grade(grad),
                    FontVariation.roundness(rond),
                )
            )
        )
    }

    return FontFamily(fonts)
}
