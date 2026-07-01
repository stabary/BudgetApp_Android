package com.simon.budgetapp.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.simon.budgetapp.data.SessionManager
import com.simon.budgetapp.network.ApiClient
import com.simon.budgetapp.network.ApiService
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


@Composable
fun SplashScreen(
    onSessionValid: () -> Unit,
    onSessionInvalid: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.tokenFlow.first()

        if (token == null) {
            onSessionInvalid()
            return@LaunchedEffect
        }

        try {
            val api = ApiClient.retrofit.create(ApiService::class.java)
            val response = api.getBudgets("Bearer $token")
            if (response.isSuccessful) {
                onSessionValid()
            } else {
                sessionManager.clearSession()
                onSessionInvalid()
            }
        } catch (e: Exception) {
            onSessionValid()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}




private suspend fun validateSession(
    sessionManager: SessionManager,
    token: String,
    onSessionValid: () -> Unit,
    onSessionInvalid: () -> Unit
) {
    try {
        val api = ApiClient.retrofit.create(ApiService::class.java)
        val response = api.getBudgets("Bearer $token")
        if (response.isSuccessful) {
            onSessionValid()
        } else {
            sessionManager.clearSession()
            onSessionInvalid()
        }
    } catch (e: Exception) {
        onSessionValid() // erreur réseau : on laisse passer plutôt que déconnecter
    }
}