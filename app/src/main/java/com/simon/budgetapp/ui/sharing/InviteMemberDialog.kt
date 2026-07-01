package com.simon.budgetapp.ui.sharing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (username: String, role: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("editor") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inviter un utilisateur") },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nom d'utilisateur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Droits accordés", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = role == "editor",
                        onClick = { role = "editor" },
                        label = { Text("Éditeur") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = role == "viewer",
                        onClick = { role = "viewer" },
                        label = { Text("Lecteur") }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (role == "editor") "Peut ajouter/modifier des transactions"
                    else "Peut seulement consulter",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(username, role) },
                enabled = username.isNotBlank()
            ) { Text("Inviter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

