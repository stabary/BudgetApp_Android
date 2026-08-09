package com.simon.budgetapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EmailVerificationScreen(
    email: String,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var isSending by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MarkEmailRead,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Vérifie ta boîte mail",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Un email de vérification a été envoyé à :",
            textAlign = TextAlign.Center
        )
        Text(
            text = email,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Clique sur le lien reçu pour activer ton compte, puis reviens te connecter.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        feedbackMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                isSending = true
                feedbackMessage = null
                viewModel.resendVerification(email) { success, message ->
                    isSending = false
                    feedbackMessage = message
                }
            },
            enabled = !isSending,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Renvoyer le mail")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aller à la connexion")
        }
    }
}