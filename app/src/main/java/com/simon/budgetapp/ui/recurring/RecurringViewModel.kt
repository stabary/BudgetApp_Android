package com.simon.budgetapp.ui.recurring

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

class RecurringViewModel(application: Application) : AndroidViewModel(application) {

    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val sessionManager = SessionManager(application)

    var rules by mutableStateOf<List<RecurringRule>>(emptyList())
        private set
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    val monthlyIncomeTotal: Double
        get() = rules.filter { it.type == "income" }.sumOf { toMonthlyAmount(it) }

    val monthlyExpenseTotal: Double
        get() = rules.filter { it.type == "expense" }.sumOf { toMonthlyAmount(it) }
    fun loadRules(budgetId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val authHeader = "Bearer $token"

                val rulesResponse = api.getRecurringRules(authHeader, budgetId)
                if (rulesResponse.isSuccessful) {
                    rules = (rulesResponse.body() ?: emptyList()).filter { it.is_active == 1 }
                }

                val catResponse = api.getCategories(authHeader, budgetId)
                if (catResponse.isSuccessful) {
                    categories = catResponse.body() ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
            isLoading = false
        }
    }

    fun createRule(
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
                    loadRules(budgetId)
                    onSuccess()
                } else {
                    errorMessage = "Impossible de créer la routine"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
        }
    }

    fun deactivateRule(ruleId: Int, budgetId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch
                val response = api.deactivateRecurringRule("Bearer $token", ruleId)
                if (response.isSuccessful) {
                    loadRules(budgetId)
                    onSuccess()
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
        }
    }
    private fun toMonthlyAmount(rule: RecurringRule): Double {
        val amount = rule.amount.toDoubleOrNull() ?: 0.0
        val interval = if (rule.interval_count > 0) rule.interval_count else 1

        return when (rule.frequency) {
            "daily"   -> amount * 30.44 / interval
            "weekly"  -> amount * 4.348 / interval
            "monthly" -> amount / interval
            "yearly"  -> {
                // Ne compte que si le prélèvement tombe sur le mois en cours
                if (isSameMonthAsNow(rule.next_run_date)) amount else 0.0
            }
            else -> 0.0
        }
    }

    private fun isSameMonthAsNow(dateStr: String): Boolean {
        return try {
            // dateStr au format "yyyy-MM-dd"
            val ruleMonth = dateStr.substring(5, 7).toInt()
            val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
            ruleMonth == currentMonth
        } catch (e: Exception) {
            false
        }
    }
}

