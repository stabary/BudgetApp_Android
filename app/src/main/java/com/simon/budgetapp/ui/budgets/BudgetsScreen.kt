package com.simon.budgetapp.ui.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simon.budgetapp.network.Budget
import com.simon.budgetapp.network.PendingInvitation
import androidx.compose.material.icons.automirrored.filled.Logout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    onBudgetClick: (Int) -> Unit,
    onLogout: () -> Unit,
    viewModel: BudgetsViewModel = viewModel()
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadBudgets()
        viewModel.loadPendingInvitations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes budgets") },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Déconnexion")
                    }
                }

            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Créer un budget")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (viewModel.pendingInvitations.isNotEmpty()) {
                viewModel.pendingInvitations.forEach { invitation ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Invitation : ${invitation.budget_name ?: "Budget"}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "De ${invitation.invited_by_username ?: "?"} · Rôle : ${if (invitation.role == "editor") "Éditeur" else "Lecteur"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                Button(onClick = {
                                    viewModel.respondToInvitation(invitation.budget_id, true) {}
                                }) { Text("Accepter") }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(onClick = {
                                    viewModel.respondToInvitation(invitation.budget_id, false) {}
                                }) { Text("Refuser") }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    viewModel.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    viewModel.errorMessage != null -> {
                        Text(
                            text = viewModel.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    }
                    viewModel.budgets.isEmpty() -> {
                        Text(
                            text = "Aucun budget pour l'instant.\nCrée ton premier tableau !",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            items(viewModel.budgets) { budget ->
                                BudgetCard(budget = budget, onClick = { onBudgetClick(budget.id) })
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateBudgetDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description ->
                viewModel.createBudget(name, description) {
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
fun BudgetCard(budget: Budget, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = budget.name, style = MaterialTheme.typography.titleMedium)
            budget.description?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun CreateBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau budget") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optionnel)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) { Text("Créer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
