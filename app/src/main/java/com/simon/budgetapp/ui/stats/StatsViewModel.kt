package com.simon.budgetapp.ui.stats

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simon.budgetapp.data.SessionManager
import com.simon.budgetapp.network.ApiClient
import com.simon.budgetapp.network.ApiService
import com.simon.budgetapp.network.CategorySummary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val sessionManager = SessionManager(application)

    var expensesByCategory by mutableStateOf<List<CategorySummary>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadStats(budgetId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val token = sessionManager.tokenFlow.first()
                if (token == null) {
                    errorMessage = "Session expirée"
                    isLoading = false
                    return@launch
                }
                val response = api.getByCategory("Bearer $token", budgetId, "expense")
                if (response.isSuccessful) {
                    expensesByCategory = (response.body() ?: emptyList())
                        .filter { it.category_name != null } // ignore les transactions sans catégorie
                } else {
                    errorMessage = "Erreur lors du chargement des statistiques"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
            isLoading = false
        }
    }
}