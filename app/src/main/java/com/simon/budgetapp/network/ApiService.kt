package com.simon.budgetapp.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Streaming

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("budgets")
    suspend fun getBudgets(@Header("Authorization") token: String): Response<List<Budget>>

    @POST("budgets")
    suspend fun createBudget(
        @Header("Authorization") token: String,
        @Body request: CreateBudgetRequest
    ): Response<Budget>

    @GET("transactions/budget/{budgetId}")
    suspend fun getTransactions(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int
    ): Response<List<Transaction>>

    @GET("transactions/budget/{budgetId}/balance")
    suspend fun getBalance(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int
    ): Response<Balance>

    @POST("transactions/budget/{budgetId}")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int,
        @Body request: CreateTransactionRequest
    ): Response<Map<String, Int>>

    @GET("categories/budget/{budgetId}")
    suspend fun getCategories(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int
    ): Response<List<Category>>

    @PUT("transactions/{transactionId}")
    suspend fun updateTransaction(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: Int,
        @Body request: CreateTransactionRequest
    ): Response<Map<String, String>>

    @DELETE("transactions/{transactionId}")
    suspend fun deleteTransaction(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: Int
    ): Response<Map<String, String>>

    @GET("transactions/budget/{budgetId}/balance/current-month")
    suspend fun getCurrentMonthBalance(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int
    ): Response<MonthlyBalance>

    @GET("transactions/budget/{budgetId}/by-category")
    suspend fun getByCategory(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int,
        @Query("type") type: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<List<CategorySummary>>

    @GET("transactions/budget/{budgetId}/monthly-history")
    suspend fun getMonthlyHistory(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int,
        @Query("months") months: Int = 6
    ): Response<List<MonthlyHistory>>

    @GET("recurring-rules/budget/{budgetId}")
    suspend fun getRecurringRules(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int
    ): Response<List<RecurringRule>>

    @POST("recurring-rules/budget/{budgetId}")
    suspend fun createRecurringRule(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int,
        @Body request: CreateRecurringRuleRequest
    ): Response<Map<String, Any>>

    @PUT("recurring-rules/{ruleId}/deactivate")
    suspend fun deactivateRecurringRule(
        @Header("Authorization") token: String,
        @Path("ruleId") ruleId: Int
    ): Response<Map<String, String>>
    @GET("transactions/budget/{budgetId}/upcoming")
    suspend fun getUpcoming(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int
    ): Response<List<UpcomingRule>>
    @POST("budgets/{budgetId}/share")
    suspend fun shareBudget(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int,
        @Body request: ShareBudgetRequest
    ): Response<Map<String, String>>

    @GET("budgets/{budgetId}/members")
    suspend fun getBudgetMembers(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int
    ): Response<List<BudgetMember>>

    @PUT("budgets/{budgetId}/membership")
    suspend fun updateMembership(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int,
        @Body request: MembershipUpdateRequest
    ): Response<Map<String, String>>

    @GET("budgets/invitations/pending")
    suspend fun getPendingInvitations(
        @Header("Authorization") token: String
    ): Response<List<PendingInvitation>>

    @GET("transactions/budget/{budgetId}/export")
    @Streaming
    suspend fun exportTransactions(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int
    ): Response<okhttp3.ResponseBody>

    @GET("transactions/budget/{budgetId}/balance/account")
    suspend fun getAccountBalance(
        @Header("Authorization") token: String,
        @Path("budgetId") budgetId: Int
    ): Response<AccountBalance>

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequest): Response<Map<String, String>>
}

