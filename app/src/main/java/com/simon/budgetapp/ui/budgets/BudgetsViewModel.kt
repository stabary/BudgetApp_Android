package com.simon.budgetapp.ui.budgets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simon.budgetapp.data.SessionManager
import com.simon.budgetapp.network.ApiClient
import com.simon.budgetapp.network.ApiService
import com.simon.budgetapp.network.Budget
import com.simon.budgetapp.network.CreateBudgetRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.simon.budgetapp.network.PendingInvitation
import com.simon.budgetapp.network.MembershipUpdateRequest

class BudgetsViewModel(application: Application) : AndroidViewModel(application) {

    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val sessionManager = SessionManager(application)

    var budgets by mutableStateOf<List<Budget>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var pendingInvitations by mutableStateOf<List<PendingInvitation>>(emptyList())
        private set
    fun loadBudgets() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val token = sessionManager.tokenFlow.first()
                if (token == null) {
                    errorMessage = "Session expirée, reconnecte-toi"
                    isLoading = false
                    return@launch
                }
                val response = api.getBudgets("Bearer $token")
                if (response.isSuccessful) {
                    budgets = response.body() ?: emptyList()
                } else {
                    errorMessage = "Erreur lors du chargement des budgets"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
            isLoading = false
        }
    }

    fun createBudget(name: String, description: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val response = api.createBudget(
                    "Bearer $token",
                    CreateBudgetRequest(name = name, description = description)
                )
                if (response.isSuccessful) {
                    loadBudgets() // recharge la liste après création
                    onSuccess()
                } else {
                    errorMessage = "Impossible de créer le budget"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
        }
    }
    fun loadPendingInvitations() {
        viewModelScope.launch {
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val response = api.getPendingInvitations("Bearer $token")
                if (response.isSuccessful) {
                    pendingInvitations = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // silencieux, non bloquant pour l'écran principal
            }
        }
    }

    fun respondToInvitation(budgetId: Int, accept: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val status = if (accept) "accepted" else "declined"
                val response = api.updateMembership("Bearer $token", budgetId, MembershipUpdateRequest(status))
                if (response.isSuccessful) {
                    loadPendingInvitations()
                    loadBudgets() // recharge la liste si acceptée
                    onDone()
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
        }
    }
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            onDone()
        }
    }
}

