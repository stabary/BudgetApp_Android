package com.simon.budgetapp.ui.goals

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simon.budgetapp.data.AppSkin
import com.simon.budgetapp.data.SessionManager
import com.simon.budgetapp.network.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val api = ApiClient.retrofit.create(ApiService::class.java)
    private val sessionManager = SessionManager(application)

    var goalStatus by mutableStateOf<GoalStatus?>(null)
        private set
    var pctEpargne by mutableStateOf(20f)
        private set
    var pctLoisir by mutableStateOf(30f)
        private set
    var pctFonctionnement by mutableStateOf(50f)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set

    var currentSkin by mutableStateOf(AppSkin.CLASSIQUE)
        private set

    init {
        viewModelScope.launch {
            sessionManager.appSkinFlow.collect { skin ->
                currentSkin = skin
            }
        }
    }

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

                val goalsResponse = api.getBudgetGoals(authHeader, budgetId)
                if (goalsResponse.isSuccessful) {
                    goalsResponse.body()?.let {
                        pctEpargne = it.pct_epargne.toFloat()
                        pctLoisir = it.pct_loisir.toFloat()
                        pctFonctionnement = it.pct_fonctionnement.toFloat()
                    }
                }

                val statusResponse = api.getGoalStatus(authHeader, budgetId)
                if (statusResponse.isSuccessful) {
                    goalStatus = statusResponse.body()
                } else {
                    errorMessage = "Impossible de charger l'état de l'objectif"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
            isLoading = false
        }
    }

    // Ajuste les 3 curseurs pour garder une somme de 100%, en répartissant
    // proportionnellement le reste entre les 2 curseurs non modifiés.
    fun onEpargneChanged(newValue: Float) {
        val clamped = newValue.coerceIn(0f, 100f)
        val remaining = 100f - clamped
        val otherSum = pctLoisir + pctFonctionnement
        if (otherSum <= 0f) {
            pctLoisir = remaining / 2f
            pctFonctionnement = remaining / 2f
        } else {
            pctLoisir = pctLoisir / otherSum * remaining
            pctFonctionnement = pctFonctionnement / otherSum * remaining
        }
        pctEpargne = clamped
    }

    fun onLoisirChanged(newValue: Float) {
        val clamped = newValue.coerceIn(0f, 100f)
        val remaining = 100f - clamped
        val otherSum = pctEpargne + pctFonctionnement
        if (otherSum <= 0f) {
            pctEpargne = remaining / 2f
            pctFonctionnement = remaining / 2f
        } else {
            pctEpargne = pctEpargne / otherSum * remaining
            pctFonctionnement = pctFonctionnement / otherSum * remaining
        }
        pctLoisir = clamped
    }

    fun onFonctionnementChanged(newValue: Float) {
        val clamped = newValue.coerceIn(0f, 100f)
        val remaining = 100f - clamped
        val otherSum = pctEpargne + pctLoisir
        if (otherSum <= 0f) {
            pctEpargne = remaining / 2f
            pctLoisir = remaining / 2f
        } else {
            pctEpargne = pctEpargne / otherSum * remaining
            pctLoisir = pctLoisir / otherSum * remaining
        }
        pctFonctionnement = clamped
    }

    fun applyPreset503020() {
        pctFonctionnement = 50f
        pctLoisir = 30f
        pctEpargne = 20f
    }

    fun saveGoals(budgetId: Int) {
        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            successMessage = null
            try {
                val token = sessionManager.tokenFlow.first() ?: return@launch

                // Épargne et loisir arrondis, fonctionnement déduit pour
                // garantir une somme exacte de 100 (contrainte du backend).
                val roundedEpargne = Math.round(pctEpargne).toDouble()
                val roundedLoisir = Math.round(pctLoisir).toDouble()
                val roundedFonctionnement = 100.0 - roundedEpargne - roundedLoisir

                val response = api.updateBudgetGoals(
                    "Bearer $token",
                    budgetId,
                    UpdateGoalsRequest(
                        pct_epargne = roundedEpargne,
                        pct_loisir = roundedLoisir,
                        pct_fonctionnement = roundedFonctionnement
                    )
                )
                if (response.isSuccessful) {
                    pctEpargne = roundedEpargne.toFloat()
                    pctLoisir = roundedLoisir.toFloat()
                    pctFonctionnement = roundedFonctionnement.toFloat()
                    successMessage = "Objectif enregistré"
                    loadData(budgetId)
                } else {
                    errorMessage = "Impossible d'enregistrer l'objectif"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau : ${e.message}"
            }
            isSaving = false
        }
    }
}

