package com.simon.budgetapp.network

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String
)

data class LoginResponse(
    val token: String,
    val user: UserResponse
)

data class Budget(
    val id: Int,
    val name: String,
    val description: String?,
    val currency: String,
    val created_at: String
)

data class CreateBudgetRequest(
    val name: String,
    val description: String? = null,
    val currency: String = "EUR"
)
data class Transaction(
    val id: Int,
    val budget_id: Int,
    val category_id: Int?,
    val created_by: Int,
    val recurring_rule_id: Int?,
    val type: String, // "income" ou "expense"
    val amount: String, // DECIMAL renvoyé en string par MariaDB/mysql2
    val label: String,
    val description: String?,
    val transaction_date: String,
    val created_at: String,
    val updated_at: String
)

data class CreateTransactionRequest(
    val category_id: Int? = null,
    val type: String,
    val amount: Double,
    val label: String,
    val description: String? = null,
    val transaction_date: String
)

data class Balance(
    val budget_id: Int,
    val budget_name: String,
    val total_income: String,
    val total_expense: String,
    val balance: String
)

data class Category(
    val id: Int,
    val budget_id: Int,
    val name: String,
    val type: String,
    val group_name: String?,
    val color_hex: String?,
    val icon: String?
)
data class CategorySummary(
    val category_id: Int?,
    val category_name: String?,
    val group_name: String?,
    val color_hex: String?,
    val type: String,
    val total: String,
    val transaction_count: Int
)

data class MonthlyBalance(
    val total_income: Double,
    val total_expense: Double,
    val balance: Double,
    val status: String,
    val actual_income: Double,
    val actual_expense: Double,
    val projected_income: Double,
    val projected_expense: Double
)
data class MonthlyHistory(
    val month: String, // format "yyyy-MM"
    val total_income: String,
    val total_expense: String
)

data class RecurringRule(
    val id: Int,
    val budget_id: Int,
    val category_id: Int?,
    val created_by: Int,
    val label: String,
    val amount: String,
    val type: String,
    val frequency: String, // daily, weekly, monthly, yearly
    val interval_count: Int,
    val day_of_month: Int?,
    val start_date: String,
    val end_date: String?,
    val next_run_date: String,
    val is_active: Int
)

data class CreateRecurringRuleRequest(
    val category_id: Int? = null,
    val label: String,
    val amount: Double,
    val type: String,
    val frequency: String,
    val interval_count: Int = 1,
    val day_of_month: Int? = null,
    val start_date: String,
    val end_date: String? = null
)

data class UpcomingRule(
    val id: Int,
    val label: String,
    val amount: String,
    val type: String,
    val category_id: Int?,
    val next_run_date: String
)
data class BudgetMember(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val status: String
)

data class ShareBudgetRequest(
    val username: String,
    val role: String // "editor" ou "viewer"
)

data class MembershipUpdateRequest(
    val status: String // "accepted" ou "declined"
)
data class PendingInvitation(
    val budget_id: Int,
    val budget_name: String?,
    val role: String,
    val invited_by_username: String?
)

data class AccountBalance(
    val actual_income: Double,
    val actual_expense: Double,
    val future_income: Double,
    val account_balance: Double
)

