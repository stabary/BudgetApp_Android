package com.simon.budgetapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class BarGroup(val label: String, val income: Double, val expense: Double)

@Composable
fun BarChartView(
    groups: List<BarGroup>,
    modifier: Modifier = Modifier
) {
    val maxValue = groups.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0
    val safeMax = if (maxValue == 0.0) 1.0 else maxValue

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            if (groups.isEmpty()) return@Canvas

            val groupWidth = size.width / groups.size
            val barWidth = groupWidth / 3.5f

            groups.forEachIndexed { index, group ->
                val centerX = groupWidth * index + groupWidth / 2

                val incomeHeight = (group.income / safeMax * size.height).toFloat()
                val expenseHeight = (group.expense / safeMax * size.height).toFloat()

                // Barre revenu (vert)
                drawRect(
                    color = Color(0xFF2E7D32),
                    topLeft = androidx.compose.ui.geometry.Offset(centerX - barWidth - 2, size.height - incomeHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, incomeHeight)
                )
                // Barre dépense (rouge)
                drawRect(
                    color = Color(0xFFC62828),
                    topLeft = androidx.compose.ui.geometry.Offset(centerX + 2, size.height - expenseHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, expenseHeight)
                )
            }
        }

        androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
            groups.forEach { group ->
                Text(
                    text = group.label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f).padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

