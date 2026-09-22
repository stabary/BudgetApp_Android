package com.simon.budgetapp.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simon.budgetapp.network.GoalBreakdownItem
import com.simon.budgetapp.ui.theme.paletteFor

private val superCategoryOrder = listOf("fonctionnement", "loisir", "epargne")
private val superCategoryLabels = mapOf(
    "fonctionnement" to "Frais de fonctionnement",
    "loisir" to "Loisirs",
    "epargne" to "Épargne"
)
private val defaultSuperCategoryColors = listOf(
    Color(0xFF42A5F5), // fonctionnement
    Color(0xFFFFA726), // loisir
    Color(0xFF66BB6A)  // epargne
)

fun smileyEmoji(smiley: String?): String = when (smiley) {
    "happy" -> "😄"
    "neutral" -> "🙂"
    "warning" -> "😐"
    "sad" -> "☹️"
    else -> "🙂"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    budgetId: Int,
    onBack: () -> Unit,
    viewModel: GoalsViewModel = viewModel()
) {
    val palette = paletteFor(viewModel.currentSkin)
    val topBarIconColor = palette.topBarIconColor ?: palette.topBarContentColor ?: MaterialTheme.colorScheme.onSurface
    val cardBackground = palette.chartCardBackground ?: CardDefaults.cardColors().containerColor
    val cardContentColor = palette.chartCardContentColor ?: Color.Unspecified
    val labelColor = palette.cardLabelColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    val colors = palette.ringSliceColors?.take(3) ?: defaultSuperCategoryColors
    val colorBySuperCategory = superCategoryOrder.zip(colors).toMap()

    LaunchedEffect(budgetId) {
        viewModel.loadData(budgetId)
    }

    Scaffold(
        containerColor = palette.screenBackground ?: MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Objectif budgétaire") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.topBarBackground ?: MaterialTheme.colorScheme.surface,
                    titleContentColor = palette.topBarContentColor ?: MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = topBarIconColor,
                    actionIconContentColor = topBarIconColor
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (viewModel.isLoading && viewModel.goalStatus == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = smileyEmoji(viewModel.goalStatus?.smiley),
                                style = MaterialTheme.typography.displayLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Revenu du mois : ${"%.2f".format(viewModel.goalStatus?.revenu_mensuel ?: 0.0)} €",
                            style = MaterialTheme.typography.bodyMedium,
                            color = labelColor,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    viewModel.goalStatus?.breakdown?.let { breakdown ->
                        val byKey = breakdown.associateBy { it.super_category }
                        items(superCategoryOrder) { key ->
                            byKey[key]?.let { item ->
                                GoalBrickCard(
                                    label = superCategoryLabels[key] ?: key,
                                    item = item,
                                    color = colorBySuperCategory[key] ?: Color.Gray,
                                    cardBackground = cardBackground,
                                    cardContentColor = cardContentColor,
                                    labelColor = labelColor
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardBackground)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Répartition cible",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = cardContentColor
                                    )
                                    TextButton(onClick = { viewModel.applyPreset503020() }) {
                                        Text("50 / 30 / 20")
                                    }
                                }

                                GoalSlider(
                                    label = "Frais de fonctionnement",
                                    value = viewModel.pctFonctionnement,
                                    color = colorBySuperCategory["fonctionnement"] ?: Color.Gray,
                                    labelColor = cardContentColor,
                                    onValueChange = { viewModel.onFonctionnementChanged(it) }
                                )
                                GoalSlider(
                                    label = "Loisirs",
                                    value = viewModel.pctLoisir,
                                    color = colorBySuperCategory["loisir"] ?: Color.Gray,
                                    labelColor = cardContentColor,
                                    onValueChange = { viewModel.onLoisirChanged(it) }
                                )
                                GoalSlider(
                                    label = "Épargne",
                                    value = viewModel.pctEpargne,
                                    color = colorBySuperCategory["epargne"] ?: Color.Gray,
                                    labelColor = cardContentColor,
                                    onValueChange = { viewModel.onEpargneChanged(it) }
                                )

                                viewModel.errorMessage?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                                viewModel.successMessage?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(it, color = MaterialTheme.colorScheme.primary)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.saveGoals(budgetId) },
                                    enabled = !viewModel.isSaving,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (viewModel.isSaving) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    } else {
                                        Text("Enregistrer l'objectif")
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun GoalBrickCard(
    label: String,
    item: GoalBreakdownItem,
    color: Color,
    cardBackground: Color,
    cardContentColor: Color,
    labelColor: Color
) {
    val progress = if (item.montant_cible > 0) (item.montant_depense / item.montant_cible).toFloat() else 0f
    val isOverBudget = item.reste_a_depenser < 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.titleMedium, color = cardContentColor)
                }
                Text(
                    "${"%.0f".format(item.pct_reel)}% (cible ${"%.0f".format(item.pct_cible)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isOverBudget) Color(0xFFC62828) else color)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${"%.2f".format(item.montant_depense)} € / ${"%.2f".format(item.montant_cible)} €",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelColor
                )
                Text(
                    text = if (isOverBudget) "Dépassement : ${"%.2f".format(-item.reste_a_depenser)} €"
                    else "Reste : ${"%.2f".format(item.reste_a_depenser)} €",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverBudget) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun GoalSlider(
    label: String,
    value: Float,
    color: Color,
    labelColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = labelColor)
            Text("${Math.round(value)}%", style = MaterialTheme.typography.bodyMedium, color = labelColor)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
    }
}

