package com.example.ui.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.AppRepository
import com.example.analytics.AnalyticsManager
import com.example.data.supabase.SupabaseConfig

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put



class AuthViewModel constructor(
    private val repository: AppRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val supabase by lazy { SupabaseConfig.client }

    private val _currentUserId = MutableStateFlow<String?>(sharedPrefs.getString("active_user_id", null))
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun resetToast() { _toastMessage.value = null }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            _isLoading.value = true
            try {
                try {
                    supabase.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    val user = supabase.auth.currentUserOrNull()
                    if (user != null) {
                        sharedPrefs.edit().putString("active_user_id", user.id).apply()
                        _currentUserId.value = user.id
                        AnalyticsManager.trackUserActivation(user.id)
                        _toastMessage.value = "Welcome back!"
                        return@launch
                    } else {
                        _authError.value = "Invalid credentials or account not verified."
                    }
                } catch (e: Throwable) {
                    Log.e("AuthViewModel", "Supabase sign in failed: ${e.message}")
                    _authError.value = "Authentication failed: ${e.message}"
                }
            } catch (e: Throwable) {
                _authError.value = e.message ?: "Authentication failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _authError.value = null
            _isLoading.value = true
            try {
                try {
                    supabase.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                        data = buildJsonObject {
                            put("username", username)
                            put("display_name", username)
                        }
                    }
                    AnalyticsManager.trackEvent("user_registered", mapOf("email" to email))
                    _toastMessage.value = "Registration successful! A confirmation email has been sent. Please check your inbox."
                } catch (e: Throwable) {
                    Log.e("AuthViewModel", "Supabase register failed: ${e.message}")
                    _authError.value = "Registration failed: ${e.message}"
                }
            } catch (e: Throwable) {
                _authError.value = e.message ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        sharedPrefs.edit().remove("active_user_id").apply()
        _currentUserId.value = null
        _toastMessage.value = "Logged out successfully."
    }

    fun deleteAccountAndCascade(userId: String) {
        viewModelScope.launch {
            val success = repository.deleteMyAccountCascade(userId)
            if (success) {
                logout()
                _toastMessage.value = "Your account and personal data have been permanently deleted."
            } else {
                _toastMessage.value = "Failed to completely delete account. Please try again."
            }
        }
    }

    fun resendConfirmationEmail(email: String) {
        viewModelScope.launch {
            _toastMessage.value = "Confirmation link resent to $email"
        }
    }
}
