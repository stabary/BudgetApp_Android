package com.simon.budgetapp.ui.categorydetail

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

data class CategoryTransactionGroup(
    val categoryId: Int?,
    val categoryName: String,
    val type: String,
    val colorHex: String?,
    val transactions: List<Transaction>,
    val total: Double
)

class CategoryDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val sessionManager = SessionManager(application)

    var categoryGroups by mutableStateOf<List<CategoryTransactionGroup>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadData(budgetId: Int) {
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
                val authHeader = "Bearer $token"

                val txResponse = api.getTransactions(authHeader, budgetId)
                val catResponse = api.getCategories(authHeader, budgetId)

                if (txResponse.isSuccessful && catResponse.isSuccessful) {
                    val allTransactions = txResponse.body() ?: emptyList()
                    val categories = catResponse.body() ?: emptyList()
                    val categoryById = categories.associateBy { it.id }

                    val currentMonth = java.text.SimpleDateFormat(
                        "yyyy-MM", java.util.Locale.getDefault()
                    ).format(java.util.Date())

                    val monthTransactions = allTransactions.filter {
                        it.transaction_date.startsWith(currentMonth)
                    }

                    categoryGroups = monthTransactions
                        .groupBy { it.category_id }
                        .map { (categoryId, txs) ->
                            val category = categoryById[categoryId]
                            CategoryTransactionGroup(
                                categoryId = categoryId,
                                categoryName = category?.name ?: "Sans catégorie",
                                type = txs.firstOrNull()?.type ?: "expense",
                                colorHex = category?.color_hex,
                                transactions = txs.sortedByDescending { it.transaction_date },
                                total = txs.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                            )
                        }
                        .sortedWith(compareBy({ it.type }, { -it.total }))
                } else {
                    errorMessage = "Impossible de charger les données"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
            isLoading = false
        }
    }
}