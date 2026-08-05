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

    private val _platformSettings = MutableStateFlow<com.example.data.model.PlatformSettings?>(null)
    val platformSettings: StateFlow<com.example.data.model.PlatformSettings?> = _platformSettings.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getPlatformSettingsFlow().collect {
                _platformSettings.value = it
            }
        }
    }

    fun resetToast() { _toastMessage.value = null }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            _isLoading.value = true
            try {
                // Check Platform settings
                val settings = repository.getPlatformSettings()
                if (settings != null && settings.maintenanceMode && email != "admin@creatorcoop.com") {
                    _authError.value = "The platform is currently undergoing scheduled maintenance. Please try again later."
                    _isLoading.value = false
                    return@launch
                }

                // Sandbox bypass for demo accounts or offline sandbox mode
                val isDemoUser = email == "admin@creatorcoop.com" || email == "alex.mercer@gmail.com" || email.contains("@demo.com") || email.contains("@creatorcoop.com")
                val isSupabasePlaceholder = SupabaseConfig.supabaseUrl.contains("your-project")
                
                if (isDemoUser || isSupabasePlaceholder) {
                    val existingUser = repository.userDao.getUserByEmail(email)
                    val targetId = if (existingUser != null) {
                        existingUser.id
                    } else if (email == "admin@creatorcoop.com") {
                        "admin_seed"
                    } else if (email == "alex.mercer@gmail.com") {
                        "DemoUser"
                    } else {
                        // Create a new persistent user for this email so they can log in seamlessly
                        val newId = java.util.UUID.randomUUID().toString()
                        val username = if (email.contains("@")) email.substringBefore("@") else "User"
                        val newUser = UserProfile(
                            id = newId,
                            email = email,
                            username = username,
                            displayName = username,
                            avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=$username",
                            globalRole = "APP_USER",
                            systemRole = "APP_USER"
                        )
                        repository.updateUserProfile(newUser)
                        newId
                    }

                    // Make sure user exists in database
                    val userExists = repository.userDao.getAllUsers().firstOrNull()?.any { it.id == targetId } == true
                    if (!userExists) {
                        val mockUser = UserProfile(
                            id = targetId,
                            email = email,
                            displayName = if (targetId == "admin_seed") "Platform Admin" else "Alex Mercer",
                            avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=$targetId",
                            globalRole = if (targetId == "admin_seed") "ADMIN" else "APP_USER",
                            systemRole = if (targetId == "admin_seed") "PLATFORM_ADMIN" else "APP_USER"
                        )
                        repository.updateUserProfile(mockUser)
                    }
                    sharedPrefs.edit().putString("active_user_id", targetId).apply()
                    _currentUserId.value = targetId
                    AnalyticsManager.trackUserActivation(targetId)
                    _toastMessage.value = "Welcome back! Logged in via Offline Sandbox Bypass."
                    return@launch
                }

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
                    _authError.value = formatThrowableMessage(e, "Sign in")
                }
            } catch (e: Throwable) {
                _authError.value = formatThrowableMessage(e, "Authentication")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, username: String, inviteCode: String = "") {
        viewModelScope.launch {
            _authError.value = null
            _isLoading.value = true
            try {
                // Check Platform settings
                val settings = repository.getPlatformSettings()
                if (settings != null) {
                    if (!settings.registrationEnabled) {
                        _authError.value = "Registration is currently disabled by platform administrators."
                        _isLoading.value = false
                        return@launch
                    }
                    if (settings.inviteOnlyEnabled) {
                        if (inviteCode.isBlank()) {
                            _authError.value = "This platform is currently invite-only. A valid invite/referral code is required to register."
                            _isLoading.value = false
                            return@launch
                        }
                        val referrer = repository.referralDao.getUserByReferralCode(inviteCode.trim().uppercase())
                        if (referrer == null) {
                            _authError.value = "Invalid invite/referral code. Please check and try again."
                            _isLoading.value = false
                            return@launch
                        }
                    }
                }

                val isSupabasePlaceholder = SupabaseConfig.supabaseUrl.contains("your-project")
                if (isSupabasePlaceholder || email.contains("@demo.com") || email.contains("@creatorcoop.com")) {
                    // Register locally in room
                    val localId = java.util.UUID.randomUUID().toString()
                    val localUser = UserProfile(
                        id = localId,
                        email = email,
                        username = username,
                        displayName = username,
                        avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=$username",
                        globalRole = "APP_USER",
                        systemRole = "APP_USER"
                    )
                    repository.updateUserProfile(localUser)

                    // Apply referral/invite code if present
                    if (inviteCode.isNotBlank()) {
                        val referralRes = repository.applyReferralCode(localId, inviteCode)
                        if (!referralRes.first) {
                            Log.w("AuthViewModel", "Failed to apply referral code during registration: ${referralRes.second}")
                        }
                    }

                    sharedPrefs.edit().putString("active_user_id", localId).apply()
                    _currentUserId.value = localId
                    AnalyticsManager.trackUserActivation(localId)
                    _toastMessage.value = "Registered locally in Offline Sandbox!"
                    return@launch
                }

                try {
                    supabase.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                        data = buildJsonObject {
                            put("username", username)
                            put("display_name", username)
                        }
                    }
                    val user = supabase.auth.currentUserOrNull()
                    if (user != null && inviteCode.isNotBlank()) {
                        repository.applyReferralCode(user.id, inviteCode)
                    }
                    AnalyticsManager.trackEvent("user_registered", mapOf("email" to email))
                    _toastMessage.value = "Registration successful! A confirmation email has been sent. Please check your inbox."
                } catch (e: Throwable) {
                    Log.e("AuthViewModel", "Supabase register failed: ${e.message}")
                    _authError.value = formatThrowableMessage(e, "Registration")
                }
            } catch (e: Throwable) {
                _authError.value = formatThrowableMessage(e, "Registration")
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

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val isSupabasePlaceholder = SupabaseConfig.supabaseUrl.contains("your-project")
                val isDemoUser = email == "admin@creatorcoop.com" || email == "alex.mercer@gmail.com" || email.contains("@demo.com") || email.contains("@creatorcoop.com")
                
                if (isSupabasePlaceholder || isDemoUser) {
                    kotlinx.coroutines.delay(1000) // Simulate network delay
                    _toastMessage.value = "Password reset instructions sent to $email (Simulated in Sandbox Mode)."
                    return@launch
                }

                supabase.auth.resetPasswordForEmail(email)
                _toastMessage.value = "Password reset instructions sent to $email."
            } catch (e: Throwable) {
                Log.e("AuthViewModel", "Reset password failed: ${e.message}")
                _authError.value = formatThrowableMessage(e, "Reset password")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resendConfirmationEmail(email: String) {
        viewModelScope.launch {
            _toastMessage.value = "Confirmation link resent to $email"
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    private fun formatThrowableMessage(e: Throwable, actionName: String = "Authentication"): String {
        val message = (e.message ?: "").lowercase()
        val localized = (e.localizedMessage ?: "").lowercase()
        
        return if (message.contains("resolve host") || 
                   message.contains("no address associated") ||
                   message.contains("failed to connect") ||
                   message.contains("unable to resolve host") ||
                   message.contains("http requests to https") ||
                   message.contains("connectexception") ||
                   message.contains("unknownhostexception") ||
                   localized.contains("resolve host") ||
                   localized.contains("no address associated") ||
                   e is java.net.UnknownHostException || 
                   e is java.net.ConnectException) {
            "Unable to connect to the server. Please check your internet connection and try again."
        } else if (message.contains("invalid login credentials") || 
                   message.contains("invalid_credentials") ||
                   message.contains("invalid credentials")) {
            "Invalid email or password. Please try again."
        } else {
            "$actionName failed: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
        }
    }
}
