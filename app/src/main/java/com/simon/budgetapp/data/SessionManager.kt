package com.simon.budgetapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.booleanPreferencesKey

private val Context.dataStore by preferencesDataStore(name = "session")

enum class AppSkin {
    CLASSIQUE,
    DOUCEUR,
    EPURE
}

class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
        private val APP_SKIN_KEY = stringPreferencesKey("app_skin")
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[BIOMETRIC_ENABLED_KEY] = enabled }
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[BIOMETRIC_ENABLED_KEY] ?: false
    }

    suspend fun setAppSkin(skin: AppSkin) {
        context.dataStore.edit { prefs -> prefs[APP_SKIN_KEY] = skin.name }
    }

    val appSkinFlow: Flow<AppSkin> = context.dataStore.data.map { prefs ->
        try {
            AppSkin.valueOf(prefs[APP_SKIN_KEY] ?: AppSkin.CLASSIQUE.name)
        } catch (e: IllegalArgumentException) {
            AppSkin.CLASSIQUE
        }
    }

    suspend fun saveSession(token: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USERNAME_KEY] = username
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val usernameFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USERNAME_KEY]
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}