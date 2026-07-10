package com.simon.budgetapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

/**
 * Petite décoration en forme de nuage (deux ronds qui se chevauchent),
 * utilisée sur les cards du thème Douceur.
 */
@Composable
fun CloudDecoration(
    color: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Petit rond (gauche, plus haut)
        drawCircle(
            color = color,
            radius = h * 0.28f,
            center = Offset(w * 0.35f, h * 0.45f)
        )
        // Grand rond (droite, plus bas) — superposé, donne l'effet nuage
        drawCircle(
            color = color,
            radius = h * 0.38f,
            center = Offset(w * 0.62f, h * 0.58f)
        )
    }
}