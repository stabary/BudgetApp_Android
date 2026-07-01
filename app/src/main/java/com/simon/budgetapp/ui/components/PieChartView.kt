package com.simon.budgetapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class PieSlice(val label: String, val value: Double, val color: Color)

@Composable
fun PieChartView(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value }

    Box(modifier = modifier.size(180.dp)) {
        Canvas(modifier = Modifier.size(180.dp)) {
            var startAngle = -90f
            val strokeWidth = 36f

            slices.forEach { slice ->
                val sweepAngle = if (total > 0) (slice.value / total * 360f).toFloat() else 0f
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
                )
                startAngle += sweepAngle
            }
        }
    }
}
