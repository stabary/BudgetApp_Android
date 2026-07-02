package com.simon.budgetapp.ui.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simon.budgetapp.network.RecurringRule

private val frequencyLabels = mapOf(
    "daily" to "Quotidien",
    "weekly" to "Hebdomadaire",
    "monthly" to "Mensuel",
    "yearly" to "Annuel"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    budgetId: Int,
    onBack: () -> Unit,
    viewModel: RecurringViewModel = viewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(budgetId) {
        viewModel.loadRules(budgetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Routines récurrentes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Créer une routine")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Entrées / mois", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${"%.2f".format(viewModel.monthlyIncomeTotal)} €",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Dépenses / mois", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${"%.2f".format(viewModel.monthlyExpenseTotal)} €",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    viewModel.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    viewModel.rules.isEmpty() -> {
                        Text(
                            "Aucune routine.\nAjoute un abonnement récurrent (Netflix, loyer...) !",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                            items(viewModel.rules) { rule ->
                                RecurringRuleCard(
                                    rule = rule,
                                    onDeactivate = { viewModel.deactivateRule(rule.id, budgetId) {} }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecurringRuleDialog(
            categories = viewModel.categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { categoryId, label, amount, type, frequency, dayOfMonth, startDate ->
                viewModel.createRule(budgetId, categoryId, label, amount, type, frequency, dayOfMonth, startDate) {
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun RecurringRuleCard(rule: RecurringRule, onDeactivate: () -> Unit) {
    val isExpense = rule.type == "expense"
    val amountValue = rule.amount.toDoubleOrNull() ?: 0.0

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(rule.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${frequencyLabels[rule.frequency] ?: rule.frequency} · Prochaine : ${rule.next_run_date}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isExpense) "-" else "+"}${"%.2f".format(amountValue)} €",
                    color = if (isExpense) Color(0xFFC62828) else Color(0xFF2E7D32),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onDeactivate) {
                    Text("Désactiver", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

