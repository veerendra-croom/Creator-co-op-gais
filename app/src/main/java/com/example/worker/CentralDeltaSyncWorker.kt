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
                                (filter.niche == "All" || proposal.niche.equals(filter.niche, ignoreCase = true)) &&
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
                                    filter.niche == "All" || 
                                    user.primarySpecialty.equals(filter.niche, ignoreCase = true) ||
                                    user.skillsJson.contains(filter.niche, ignoreCase = true)
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
