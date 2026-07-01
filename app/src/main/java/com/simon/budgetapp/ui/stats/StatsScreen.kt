package com.simon.budgetapp.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simon.budgetapp.network.CategorySummary

private val chartColors = listOf(
    Color(0xFFEF5350), Color(0xFFFFA726), Color(0xFFFFEE58),
    Color(0xFF66BB6A), Color(0xFF26C6DA), Color(0xFF42A5F5),
    Color(0xFF7E57C2), Color(0xFFEC407A), Color(0xFF8D6E63),
    Color(0xFF78909C)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    budgetId: Int,
    onBack: () -> Unit,
    viewModel: StatsViewModel = viewModel()
) {
    LaunchedEffect(budgetId) {
        viewModel.loadStats(budgetId)
    }

    val total = viewModel.expensesByCategory.sumOf { it.total.toDoubleOrNull() ?: 0.0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dépenses par catégorie") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                viewModel.expensesByCategory.isEmpty() -> {
                    Text(
                        "Pas encore de dépenses catégorisées.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(viewModel.expensesByCategory) { item ->
                            val index = viewModel.expensesByCategory.indexOf(item)
                            CategorySummaryRow(
                                item = item,
                                color = chartColors[index % chartColors.size],
                                percentage = if (total > 0) (item.total.toDoubleOrNull() ?: 0.0) / total else 0.0
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySummaryRow(item: CategorySummary, color: Color, percentage: Double) {
    val amount = item.total.toDoubleOrNull() ?: 0.0

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.category_name ?: "Sans catégorie", style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                "${"%.2f".format(amount)} € (${(percentage * 100).toInt()}%)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Barre de proportion visuelle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage.toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}