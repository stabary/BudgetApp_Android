package com.simon.budgetapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.simon.budgetapp.data.SessionManager
import com.simon.budgetapp.network.ApiClient
import com.simon.budgetapp.network.ApiService
import com.simon.budgetapp.network.ErrorResponse
import com.simon.budgetapp.network.LoginRequest
import com.simon.budgetapp.network.RegisterRequest
import com.simon.budgetapp.network.ResendVerificationRequest
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
    data class EmailNotVerified(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val sessionManager = SessionManager(application)

    var authState: AuthState = AuthState.Idle

    fun login(username: String, password: String, onResult: (AuthState) -> Unit) {
        onResult(AuthState.Loading)
        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionManager.saveSession(body.token, body.user.username)
                    sessionManager.setBiometricEnabled(true)
                    onResult(AuthState.Success)
                } else if (response.code() == 403) {
                    val parsedError = parseErrorBody(response.errorBody()?.string())
                    if (parsedError?.error == "EMAIL_NOT_VERIFIED") {
                        onResult(AuthState.EmailNotVerified(parsedError.message ?: "Email non vérifié"))
                    } else {
                        onResult(AuthState.Error(parsedError?.message ?: "Identifiants invalides"))
                    }
                } else {
                    onResult(AuthState.Error("Identifiants invalides"))
                }
            } catch (e: Exception) {
                onResult(AuthState.Error("Erreur réseau : ${e.message}"))
            }
        }
    }

    fun register(username: String, email: String, password: String, onResult: (AuthState) -> Unit) {
        onResult(AuthState.Loading)
        viewModelScope.launch {
            try {
                val response = api.register(RegisterRequest(username, email, password))
                if (response.isSuccessful) {
                    onResult(AuthState.Success)
                } else {
                    onResult(AuthState.Error("Inscription impossible (nom ou email déjà utilisé ?)"))
                }
            } catch (e: Exception) {
                onResult(AuthState.Error("Erreur réseau : ${e.message}"))
            }
        }
    }

    fun resendVerification(email: String, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.resendVerification(ResendVerificationRequest(email))
                if (response.isSuccessful) {
                    onResult(true, response.body()?.get("message") ?: "Email envoyé")
                } else {
                    onResult(false, "Impossible d'envoyer le mail")
                }
            } catch (e: Exception) {
                onResult(false, "Erreur réseau : ${e.message}")
            }
        }
    }

    private fun parseErrorBody(errorBodyString: String?): ErrorResponse? {
        if (errorBodyString.isNullOrBlank()) return null
        return try {
            Gson().fromJson(errorBodyString, ErrorResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }
}