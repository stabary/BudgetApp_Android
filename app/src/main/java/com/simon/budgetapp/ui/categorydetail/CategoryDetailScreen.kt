package com.simon.budgetapp.ui.categorydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    budgetId: Int,
    onBack: () -> Unit,
    viewModel: CategoryDetailViewModel = viewModel()
) {
    LaunchedEffect(budgetId) {
        viewModel.loadData(budgetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du mois") },
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
                viewModel.errorMessage != null -> {
                    Text(
                        viewModel.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                viewModel.categoryGroups.isEmpty() -> {
                    Text(
                        "Aucune transaction ce mois-ci.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(viewModel.categoryGroups) { group ->
                            CategoryGroupCard(group)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryGroupCard(group: CategoryTransactionGroup) {
    var expanded by remember { mutableStateOf(true) }
    val isExpense = group.type == "expense"

    val dotColor = try {
        Color(android.graphics.Color.parseColor(group.colorHex))
    } catch (e: Exception) {
        if (isExpense) Color(0xFFC62828) else Color(0xFF2E7D32)
    }

    Card(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(dotColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(group.categoryName, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    "${if (isExpense) "-" else "+"}${"%.2f".format(group.total)} €",
                    color = if (isExpense) Color(0xFFC62828) else Color(0xFF2E7D32),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                group.transactions.forEach { tx ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(tx.label, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                tx.transaction_date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${"%.2f".format(tx.amount.toDoubleOrNull() ?: 0.0)} €",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}