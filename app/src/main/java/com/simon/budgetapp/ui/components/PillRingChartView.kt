package com.simon.budgetapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class PillRingSlice(val label: String, val value: Double, val color: Color)

/**
 * Anneau façon "pilules" arrondies (style Douceur), avec un libellé centré.
 * Un petit espace en degrés sépare chaque tranche pour l'effet visuel de pilule.
 */
@Composable
fun PillRingChartView(
    slices: List<PillRingSlice>,
    centerLabel: String,
    modifier: Modifier = Modifier,
    ringSize: androidx.compose.ui.unit.Dp = 220.dp,
    strokeWidth: Float = 46f,
    gapDegrees: Float = 10f
) {
    val total = slices.sumOf { it.value }

    Box(modifier = modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(ringSize)) {
            if (total <= 0.0 || slices.isEmpty()) return@Canvas

            var startAngle = -90f
            val slicesCount = slices.size
            // Espace total retiré du cercle pour créer les gaps entre pilules
            val totalGap = gapDegrees * slicesCount
            val availableDegrees = 360f - totalGap

            slices.forEach { slice ->
                val sweep = (slice.value / total * availableDegrees).toFloat()
                if (sweep > 0f) {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    )
                }
                startAngle += sweep + gapDegrees
            }
        }

        Text(
            text = centerLabel,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = Color.Black

        )
    }
}

