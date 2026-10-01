package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "users")
data class UserProfile(
    @PrimaryKey val id: String,
    val email: String,
    val username: String = "",
    val displayName: String = "",
    val globalRole: String = "CREATOR",
    val primarySpecialty: String = "Video Production",
    val bio: String = "",
    val avatarUrl: String = "",
    val websiteUrl: String = "",
    val location: String = "",
    val deviceId: String = "",
    val reliabilityBadge: String = "Silver",
    val reputationScore: Int = 100,
    val completedProjectsCount: Int = 0,
    val verificationLevel: String = "L1 Basic",
    val isVerifiedPro: Boolean = false,
    val isPremium: Boolean = false,
    val systemRole: String = "USER",
    val referralCode: String? = null,
    val signedAgreementsCount: Int = 0,
    val onTimeDeliveryRate: Float = 1.0f,
    val peerRating: Float = 5.0f,
    val premiumTrialExtensionDays: Int = 0,
    val availabilityStatus: String = "Available",
    val skillsJson: String = "[]",
    val portfolioJson: String = "[]",
    val stripeOnboardingCompleted: Boolean = false,
    val stripeConnectedAccountId: String? = null,
    val stripeVerificationStatus: String = "NONE",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "user_settings_table")
data class UserSetting(
    @PrimaryKey val id: String, // userId_key
    val userId: String,
    val key: String,
    val value: String
)

@Serializable
@Entity(tableName = "personal_notes")
data class PersonalNote(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

