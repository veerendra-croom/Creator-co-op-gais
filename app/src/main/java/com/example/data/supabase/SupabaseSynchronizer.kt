package com.example.data.supabase

import android.content.Context
import android.util.Log
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseSynchronizer {
    private const val TAG = "SupabaseSynchronizer"

    val api: SupabaseService by lazy {
        SupabaseConfig.retrofit.create(SupabaseService::class.java)
    }

    /**
     * Pulls latest states from Supabase and overwrites local cache to keep data synchronized.
     * Wrapped in robust try-catch so incomplete network properties do not crash the app.
     */
    suspend fun syncDownEverything(context: Context, repository: AppRepository) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) {
            Log.d(TAG, "Sync down skipped: No network available")
            return@withContext
        }

        val url = SupabaseConfig.supabaseUrl
        if (url.startsWith("https://your-project")) {
            Log.d(TAG, "Sync down skipped: Supabase URL properties are still placeholders")
            return@withContext
        }

        Log.d(TAG, "Starting full Supabase sync down...")

        // Synchronize Users
        try {
            val userResponse = api.getUsers()
            if (userResponse.isSuccessful) {
                userResponse.body()?.forEach { user ->
                    try { repository.userDao.insertUser(user) } catch (e: Exception) { Log.e(TAG, "Fail saving sync user", e) }
                }
                Log.d(TAG, "Synced down users successfully")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed syncing down users", e)
        }

        // Synchronize posts
        try {
            val postsResponse = api.getPosts()
            if (postsResponse.isSuccessful) {
                postsResponse.body()?.let { posts ->
                    if (posts.isNotEmpty()) {
                        try { repository.postDao.insertPosts(posts) } catch (e: Exception) { Log.e(TAG, "Fail saving sync posts", e) }
                        Log.d(TAG, "Synced down posts successfully (${posts.size} items)")
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed syncing down posts", e)
        }

        // Synchronize comments
        try {
            val commentResponse = api.getComments()
            if (commentResponse.isSuccessful) {
                commentResponse.body()?.forEach { comment ->
                    try { repository.commentDao.insertComment(comment) } catch (e: Exception) { Log.e(TAG, "Fail saving sync comment", e) }
                }
                Log.d(TAG, "Synced down comments successfully")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed syncing down comments", e)
        }

        // Synchronize projects
        try {
            val projectsResponse = api.getProjects()
            if (projectsResponse.isSuccessful) {
                projectsResponse.body()?.let { projects ->
                    if (projects.isNotEmpty()) {
                        try { repository.projectDao.insertProjects(projects) } catch (e: Exception) { Log.e(TAG, "Fail saving sync projects", e) }
                        Log.d(TAG, "Synced down projects successfully (${projects.size} items)")
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed syncing down projects", e)
        }

        // Synchronize pitches
        try {
            val pitchesResponse = api.getPitches()
            if (pitchesResponse.isSuccessful) {
                pitchesResponse.body()?.forEach { pitch ->
                    try { repository.pitchDao.insertPitch(pitch) } catch (e: Exception) { Log.e(TAG, "Fail saving sync pitch", e) }
                }
                Log.d(TAG, "Synced down pitches successfully")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed syncing down pitches", e)
        }

        // Synchronize messages
        try {
            val messagesResponse = api.getMessages()
            if (messagesResponse.isSuccessful) {
                messagesResponse.body()?.forEach { message ->
                    try { repository.messageDao.insertMessage(message) } catch (e: Exception) { Log.e(TAG, "Fail saving sync message", e) }
                }
                Log.d(TAG, "Synced down chat messages successfully")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed syncing down chat messages", e)
        }

        // Synchronize contracts
        try {
            val contractsResponse = api.getContracts()
            if (contractsResponse.isSuccessful) {
                contractsResponse.body()?.forEach { contract ->
                    try { repository.contractDao.insertContract(contract) } catch (e: Exception) { Log.e(TAG, "Fail saving sync contract", e) }
                }
                Log.d(TAG, "Synced down contracts successfully")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed syncing down contracts", e)
        }

        Log.d(TAG, "Full Supabase sync down completed.")
    }

    // --- UPLOAD SYNCHRONIZATION WRAPPERS ---

    suspend fun syncUpUser(context: Context, user: User) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try {
            val response = api.upsertUser(user = user)
            Log.d(TAG, "syncUpUser response: ${response.code()} successful=${response.isSuccessful}")
        } catch (e: Throwable) {
            Log.e(TAG, "Network exception in syncUpUser", e)
        }
    }

    suspend fun syncUpPost(context: Context, post: Post) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try {
            val response = api.upsertPost(post = post)
            Log.d(TAG, "syncUpPost response: ${response.code()} successful=${response.isSuccessful}")
        } catch (e: Throwable) {
            Log.e(TAG, "Network exception in syncUpPost", e)
        }
    }

    suspend fun syncUpComment(context: Context, comment: Comment) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try {
            val response = api.insertComment(comment = comment)
            Log.d(TAG, "syncUpComment response: ${response.code()}")
        } catch (e: Throwable) {
            Log.e(TAG, "Network exception in syncUpComment", e)
        }
    }

    suspend fun syncUpProject(context: Context, project: Project) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try {
            val response = api.upsertProject(project = project)
            Log.d(TAG, "syncUpProject response: ${response.code()}")
        } catch (e: Throwable) {
            Log.e(TAG, "Network exception in syncUpProject", e)
        }
    }

    suspend fun syncUpPitch(context: Context, pitch: Pitch) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try {
            val response = api.upsertPitch(pitch = pitch)
            Log.d(TAG, "syncUpPitch response: ${response.code()}")
        } catch (e: Throwable) {
            Log.e(TAG, "Network exception in syncUpPitch", e)
        }
    }

    suspend fun syncUpMessage(context: Context, message: Message) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try {
            val response = api.insertMessage(message = message)
            Log.d(TAG, "syncUpMessage response: ${response.code()}")
        } catch (e: Throwable) {
            Log.e(TAG, "Network exception in syncUpMessage", e)
        }
    }

    suspend fun syncUpContract(context: Context, contract: Contract) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try {
            val response = api.upsertContract(contract = contract)
            Log.d(TAG, "syncUpContract response: ${response.code()}")
        } catch (e: Throwable) {
            Log.e(TAG, "Network exception in syncUpContract", e)
        }
    }
}
