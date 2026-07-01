package com.simon.budgetapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simon.budgetapp.data.SessionManager
import com.simon.budgetapp.network.ApiClient
import com.simon.budgetapp.network.ApiService
import com.simon.budgetapp.network.LoginRequest
import com.simon.budgetapp.network.RegisterRequest
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
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
}

