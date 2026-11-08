package com.nullo.voidapp.feature.auth.presentation.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

internal fun Modifier.ticketShadow(): Modifier = shadow(
    elevation = 32.dp,
    shape = RoundedCornerShape(32.dp),
    clip = false
)

internal fun Modifier.glowOnExit(
    progress: () -> Float,
    brush: Brush,
): Modifier = drawWithCache {
    onDrawBehind {
        val p = progress()
        if (p <= 0f) return@onDrawBehind
        withTransform(
            transformBlock = { translate(top = (1f - p) * size.height) },
            drawBlock = { drawRect(brush = brush) }
        )
    }
}
