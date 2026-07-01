package com.simon.budgetapp.ui.sharing

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simon.budgetapp.data.SessionManager
import com.simon.budgetapp.network.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SharingViewModel(application: Application) : AndroidViewModel(application) {

    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val sessionManager = SessionManager(application)

    var members by mutableStateOf<List<BudgetMember>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set

    fun loadMembers(budgetId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val response = api.getBudgetMembers("Bearer $token", budgetId)
                if (response.isSuccessful) {
                    members = response.body() ?: emptyList()
                } else {
                    errorMessage = "Impossible de charger les membres"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
            isLoading = false
        }
    }

    fun shareBudget(budgetId: Int, username: String, role: String, onResult: () -> Unit) {
        viewModelScope.launch {
            errorMessage = null
            successMessage = null
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val response = api.shareBudget("Bearer $token", budgetId, ShareBudgetRequest(username, role))
                if (response.isSuccessful) {
                    successMessage = "Invitation envoyée à $username"
                    loadMembers(budgetId)
                    onResult()
                } else {
                    errorMessage = when (response.code()) {
                        404 -> "Utilisateur introuvable"
                        else -> "Impossible d'envoyer l'invitation"
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
        }
    }
}

