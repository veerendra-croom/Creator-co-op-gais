package com.example.worker

import android.content.Context
import androidx.work.*
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository
import com.example.data.supabase.SupabaseSynchronizer
import android.util.Log
import com.example.ui.util.NotificationHelper
import com.example.data.model.SavedSearchFilter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object SyncLock {
    val mutex = Mutex()
}

/**
 * Manages automatic background sync cycles using WorkManager.
 */
class CentralDeltaSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("CentralDeltaSyncWorker", "Executing scheduled background sync loop...")
        
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(db, applicationContext)

        return SyncLock.mutex.withLock {
            try {
                repository.syncUpPendingEvents()
                SupabaseSynchronizer.syncDownEverything(applicationContext, repository)

                // Perform periodic background check for saved searches
                val savedSearches = repository.savedSearchDao.getAllSavedSearchesList()
                if (savedSearches.isNotEmpty()) {
                    val proposals = repository.projectProposalDao.getAllProjectProposalsList(System.currentTimeMillis()).first()
                    val activeListings = repository.lookingForWorkDao.getAllActiveListings().first()
                    val allUsers = repository.userDao.getAllUsers().first().associateBy { it.id }

                    val json = Json { 
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                    
                    savedSearches.forEach { search ->
                        val filter = try {
                            json.decodeFromString<SavedSearchFilter>(search.filterJson)
                        } catch (e: Exception) {
                            SavedSearchFilter()
                        }
                        
                        var hasNewMatch = false
                        
                        if (filter.type == "OPEN_ROLES") {
                            val matches = proposals.filter { data ->
                                val proposal = data.proposal
                                proposal.createdAt > search.lastNotifiedAt &&
                                ((filter.niche ?: "All") == "All" || proposal.niche.equals(filter.niche, ignoreCase = true)) &&
                                (filter.searchQuery.isBlank() || proposal.title.contains(filter.searchQuery, ignoreCase = true) || proposal.brief.contains(filter.searchQuery, ignoreCase = true))
                            }
                            if (matches.isNotEmpty()) {
                                hasNewMatch = true
                            }
                        } else if (filter.type == "AVAILABLE_TALENT") {
                            val matches = activeListings.filter { listing ->
                                listing.createdAt > search.lastNotifiedAt
                            }.filter { listing ->
                                val user = allUsers[listing.userId]
                                val userMatch = user != null && (
                                    (filter.niche ?: "All") == "All" || 
                                    user.primarySpecialty.equals(filter.niche, ignoreCase = true) ||
                                    user.skillsJson.contains(filter.niche ?: "", ignoreCase = true)
                                )
                                val detailsMatch = filter.searchQuery.isBlank() || listing.detailsJson.contains(filter.searchQuery, ignoreCase = true)
                                userMatch && detailsMatch
                            }
                            if (matches.isNotEmpty()) {
                                hasNewMatch = true
                            }
                        }
                        
                        if (hasNewMatch) {
                            NotificationHelper.showNotification(
                                applicationContext,
                                "New match for your saved search: ${search.name}",
                                "Open Discovery to check matching talent or roles."
                            )
                            repository.savedSearchDao.insertSavedSearch(
                                search.copy(lastNotifiedAt = System.currentTimeMillis())
                            )
                        }
                    }
                }

                // --- MOCK WEEKLY DIGEST GENERATION ---
                try {
                    val allFeatureFlags = repository.getAllFeatureFlagsFlow().first()
                    val isWeeklyDigestFlagEnabled = allFeatureFlags.find { it.flagKey == "weekly_digest_enabled" }?.isEnabled ?: true
                    
                    if (isWeeklyDigestFlagEnabled) {
                        val allUsersList = repository.userDao.getAllUsers().first()
                        allUsersList.forEach { user ->
                            val isDigestEnabled = repository.getWeeklyDigestPreference(user.id)
                            if (isDigestEnabled) {
                                // Find matching open roles (proposals)
                                val nowTime = System.currentTimeMillis()
                                val proposalsList = repository.projectProposalDao.getAllProjectProposalsList(nowTime).first()
                                val userSpecialty = user.primarySpecialty
                                val matchingRoles = proposalsList.filter { data ->
                                    data.proposal.niche.equals(userSpecialty, ignoreCase = true) ||
                                    data.proposal.title.contains(userSpecialty, ignoreCase = true)
                                }
                                
                                // Find unread workspace activity (notifications)
                                val notifications = repository.getNotificationsForUser(user.id).first()
                                val unreadCount = notifications.count { !it.isRead }
                                
                                // If there are matching roles or unread workspace activities, send digest notification
                                if (matchingRoles.isNotEmpty() || unreadCount > 0) {
                                    val roleText = if (matchingRoles.isNotEmpty()) {
                                        "${matchingRoles.size} matched open role(s)"
                                    } else {
                                        "no new matching roles"
                                    }
                                    val unreadText = if (unreadCount > 0) {
                                        "${unreadCount} unread activity alert(s)"
                                    } else {
                                        "no unread updates"
                                    }
                                    
                                    NotificationHelper.showNotification(
                                        applicationContext,
                                        "Weekly Co-Op Digest for ${user.displayName}",
                                        "Weekly Digest: $roleText and $unreadText compiled for you."
                                    )
                                }
                            }
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("CentralDeltaSyncWorker", "Weekly digest compilation failed: ${ex.message}")
                }

                Result.success()
            } catch (e: Exception) {
                Log.e("CentralDeltaSyncWorker", "Delta sync failed: ${e.message}")
                Result.retry()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "CentralDeltaSyncWorker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<CentralDeltaSyncWorker>(
                15, java.util.concurrent.TimeUnit.MINUTES
            )
            .setConstraints(constraints)
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
