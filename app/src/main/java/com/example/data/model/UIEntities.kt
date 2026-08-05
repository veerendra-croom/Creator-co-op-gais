package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "onboarding_slides")
data class OnboardingSlide(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String = "",
    val stepIndex: Int,
    val bannerImageUrl: String = ""
)

@Serializable
@Entity(tableName = "welcome_messages")
data class WelcomeMessage(
    @PrimaryKey val id: String,
    val title: String,
    val greeting: String,
    val bannerImageUrl: String = "",
    val isActive: Boolean = true
)

@Serializable
@Entity(tableName = "empty_states")
data class EmptyStateConfig(
    @PrimaryKey val id: String,
    val screenName: String = "",
    val screenContext: String = "",
    val assetKey: String = "",
    val title: String = "",
    val message: String = "",
    val imageTag: String = "",
    val suggestion: String = ""
)

@Serializable
@Entity(tableName = "help_texts")
data class HelpText(
    @PrimaryKey val id: String,
    val topicKey: String = "",
    val contextKey: String = "",
    val textContent: String = "",
    val helpContent: String = "",
    val category: String = "GENERAL"
)
