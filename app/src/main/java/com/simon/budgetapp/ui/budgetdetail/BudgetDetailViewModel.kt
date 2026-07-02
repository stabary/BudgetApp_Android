package com.simon.budgetapp.ui.budgetdetail

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
import android.net.Uri

class BudgetDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val sessionManager = SessionManager(application)

    var transactions by mutableStateOf<List<Transaction>>(emptyList())
        private set
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var balance by mutableStateOf<Balance?>(null)
        private set
    var monthlyBalance by mutableStateOf<MonthlyBalance?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var currentMonthCategories by mutableStateOf<List<CategorySummary>>(emptyList())
        private set
    var monthlyHistory by mutableStateOf<List<MonthlyHistory>>(emptyList())
        private set
    var upcomingRules by mutableStateOf<List<UpcomingRule>>(emptyList())
        private set
    var accountBalance by mutableStateOf<AccountBalance?>(null)
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
                if (txResponse.isSuccessful) {
                    transactions = txResponse.body() ?: emptyList()
                }

                val balanceResponse = api.getBalance(authHeader, budgetId)
                if (balanceResponse.isSuccessful) {
                    balance = balanceResponse.body()
                }
                // Calcule le premier et dernier jour du mois en cours
                val calendar = java.util.Calendar.getInstance()
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                val firstDay = sdf.format(calendar.time)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                val lastDay = sdf.format(calendar.time)

                val categoryResponse = api.getByCategory(authHeader, budgetId, "expense", firstDay, lastDay)
                if (categoryResponse.isSuccessful) {
                    currentMonthCategories = (categoryResponse.body() ?: emptyList())
                        .filter { it.category_name != null }
                }
                val monthlyResponse = api.getCurrentMonthBalance(authHeader, budgetId)
                if (monthlyResponse.isSuccessful) {
                    monthlyBalance = monthlyResponse.body()
                }
                val accountBalanceResponse = api.getAccountBalance(authHeader, budgetId)
                if (accountBalanceResponse.isSuccessful) {
                    accountBalance = accountBalanceResponse.body()
                }
                val catResponse = api.getCategories(authHeader, budgetId)
                if (catResponse.isSuccessful) {
                    categories = catResponse.body() ?: emptyList()
                }
                val historyResponse = api.getMonthlyHistory(authHeader, budgetId, 6)
                if (historyResponse.isSuccessful) {
                    monthlyHistory = historyResponse.body() ?: emptyList()
                }
                val upcomingResponse = api.getUpcoming(authHeader, budgetId)
                if (upcomingResponse.isSuccessful) {
                    upcomingRules = upcomingResponse.body() ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
            isLoading = false
        }
    }

    fun addTransaction(
        budgetId: Int,
        categoryId: Int?,
        type: String,
        amount: Double,
        label: String,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val response = api.createTransaction(
                    "Bearer $token",
                    budgetId,
                    CreateTransactionRequest(
                        category_id = categoryId,
                        type = type,
                        amount = amount,
                        label = label,
                        transaction_date = date
                    )
                )
                if (response.isSuccessful) {
                    loadData(budgetId) // recharge tout après ajout
                    onSuccess()
                } else {
                    errorMessage = "Impossible d'ajouter la transaction"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
        }
    }

    fun addRecurringTransaction(
        budgetId: Int,
        categoryId: Int?,
        label: String,
        amount: Double,
        type: String,
        frequency: String,
        dayOfMonth: Int?,
        startDate: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val response = api.createRecurringRule(
                    "Bearer $token",
                    budgetId,
                    CreateRecurringRuleRequest(
                        category_id = categoryId,
                        label = label,
                        amount = amount,
                        type = type,
                        frequency = frequency,
                        day_of_month = dayOfMonth,
                        start_date = startDate
                    )
                )
                if (response.isSuccessful) {
                    loadData(budgetId) // recharge tout (transactions + à venir) après création
                    onSuccess()
                } else {
                    errorMessage = "Impossible de créer la routine"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
        }
    }

    fun updateTransaction(
        transactionId: Int,
        budgetId: Int,
        categoryId: Int?,
        type: String,
        amount: Double,
        label: String,
        date: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val response = api.updateTransaction(
                    "Bearer $token",
                    transactionId,
                    CreateTransactionRequest(
                        category_id = categoryId,
                        type = type,
                        amount = amount,
                        label = label,
                        transaction_date = date
                    )
                )
                if (response.isSuccessful) {
                    loadData(budgetId)
                    onSuccess()
                } else {
                    errorMessage = "Impossible de modifier la transaction"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
        }
    }

    fun deleteTransaction(transactionId: Int, budgetId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val response = api.deleteTransaction("Bearer $token", transactionId)
                if (response.isSuccessful) {
                    loadData(budgetId)
                    onSuccess()
                } else {
                    errorMessage = "Impossible de supprimer la transaction"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
        }
    }

    fun exportToCSV(budgetId: Int, onResult: (Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val response = api.exportTransactions("Bearer $token", budgetId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val context = getApplication<Application>()
                        val fileName = "export_budget_$budgetId.csv"
                        val file = java.io.File(context.cacheDir, fileName)
                        body.byteStream().use { input ->
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        onResult(uri)
                    } else {
                        errorMessage = "Export vide"
                        onResult(null)
                    }
                } else {
                    errorMessage = "Impossible d'exporter"
                    onResult(null)
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
                onResult(null)
            }
        }
    }

}