package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.CreatorCoopApp

class AppViewModelFactory(private val application: CreatorCoopApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val container = application.container
        return when {
            modelClass.isAssignableFrom(GlobalViewModel::class.java) -> {
                GlobalViewModel(container.repository, application) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(container.repository, container.sharedPreferences) as T
            }
            modelClass.isAssignableFrom(WorkspaceViewModel::class.java) -> {
                WorkspaceViewModel(container.repository) as T
            }
            modelClass.isAssignableFrom(CommunityFeedViewModel::class.java) -> {
                CommunityFeedViewModel(container.repository) as T
            }
            modelClass.isAssignableFrom(DiscoveryViewModel::class.java) -> {
                DiscoveryViewModel(container.repository) as T
            }
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                AdminViewModel(container.repository) as T
            }

            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(container.repository) as T
            }
            modelClass.isAssignableFrom(AgreementViewModel::class.java) -> {
                AgreementViewModel(container.repository) as T
            }
            modelClass.isAssignableFrom(AnalyticsViewModel::class.java) -> {
                AnalyticsViewModel(container.repository) as T
            }
            modelClass.isAssignableFrom(SupportViewModel::class.java) -> {
                SupportViewModel(container.repository) as T
            }
            modelClass.isAssignableFrom(FounderCrmViewModel::class.java) -> {
                FounderCrmViewModel(container.repository) as T
            }
            modelClass.isAssignableFrom(CommunicationViewModel::class.java) -> {
                CommunicationViewModel(container.repository) as T
            }
            modelClass.isAssignableFrom(PlatformControlViewModel::class.java) -> {
                PlatformControlViewModel(container.repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
