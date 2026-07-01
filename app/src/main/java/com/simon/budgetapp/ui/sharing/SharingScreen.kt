package com.simon.budgetapp.ui.sharing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simon.budgetapp.network.BudgetMember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharingScreen(
    budgetId: Int,
    onBack: () -> Unit,
    viewModel: SharingViewModel = viewModel()
) {
    var showInviteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(budgetId) {
        viewModel.loadMembers(budgetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partage du budget") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showInviteDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Inviter quelqu'un")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {

            viewModel.successMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            viewModel.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    viewModel.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    viewModel.members.isEmpty() -> {
                        Text(
                            "Ce budget n'est partagé avec personne.\nInvite quelqu'un avec le bouton +",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(viewModel.members) { member ->
                                MemberRow(member)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        InviteMemberDialog(
            onDismiss = { showInviteDialog = false },
            onConfirm = { username, role ->
                viewModel.shareBudget(budgetId, username, role) {
                    showInviteDialog = false
                }
            }
        )
    }
}

@Composable
fun MemberRow(member: BudgetMember) {
    val statusLabel = when (member.status) {
        "accepted" -> "Accepté"
        "pending" -> "En attente"
        "declined" -> "Refusé"
        else -> member.status
    }
    val roleLabel = when (member.role) {
        "editor" -> "Éditeur"
        "viewer" -> "Lecteur"
        else -> member.role
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(member.username, style = MaterialTheme.typography.titleMedium)
                Text(member.email, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(roleLabel, style = MaterialTheme.typography.bodyMedium)
                Text(statusLabel, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}