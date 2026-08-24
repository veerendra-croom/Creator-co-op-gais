package com.example.data.supabase

import android.util.Log
import com.example.data.model.*
import com.example.data.repository.AppRepository
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

object SupabaseRealtimeManager {
    private const val TAG = "SupabaseRealtime"
    
    private val isTestEnv = try {
        Class.forName("org.robolectric.Robolectric") != null
    } catch (e: Throwable) {
        false
    }

    val isRealtimeConfigured: Boolean
        get() {
            if (isTestEnv) return false
            if (!SupabaseConfig.isRealtimeEnabled) return false
            return try {
                val url = SupabaseConfig.supabaseUrl
                val key = SupabaseConfig.supabaseKey
                url.isNotBlank() && key.isNotBlank() &&
                        !url.contains("your-project") && !url.contains("example.com") &&
                        !key.contains("your-supabase") && !key.contains("placeholder")
            } catch (e: Exception) {
                false
            }
        }

    private val client by lazy { SupabaseConfig.client }
    private val subscribedChannels = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    fun subscribeToWorkspaces(repository: AppRepository, scope: CoroutineScope) {
        if (!isRealtimeConfigured || !subscribedChannels.add("workspaces_channel")) return
        try {
            val channel = client.channel("workspaces_channel")
            val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "workspaces"
            }

            flow.onEach { action ->
                try {
                    val record = when (action) {
                        is PostgresAction.Insert -> action.decodeRecord<Workspace>()
                        is PostgresAction.Update -> action.decodeRecord<Workspace>()
                        else -> null
                    }
                    record?.let { repository.workspaceDao.insertWorkspace(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding workspace", e)
                }
            }.launchIn(scope)

            scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Throwable) { Log.w(TAG, "Workspaces channel subscription notice: ${e.message}") } }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to initialize workspaces realtime: ${e.message}")
        }
    }

    fun subscribeToWorkspaceMembers(repository: AppRepository, scope: CoroutineScope) {
        if (!isRealtimeConfigured || !subscribedChannels.add("members_channel")) return
        try {
            val channel = client.channel("members_channel")
            val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "workspace_members"
            }

            flow.onEach { action ->
                try {
                    val record = when (action) {
                        is PostgresAction.Insert -> action.decodeRecord<WorkspaceMember>()
                        is PostgresAction.Update -> action.decodeRecord<WorkspaceMember>()
                        else -> null
                    }
                    record?.let { repository.workspaceMemberDao.insertMember(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding workspace member", e)
                }
            }.launchIn(scope)

            scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Throwable) { Log.w(TAG, "Members channel subscription notice: ${e.message}") } }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to initialize members realtime: ${e.message}")
        }
    }

    fun subscribeToProductionTasks(repository: AppRepository, scope: CoroutineScope) {
        if (!isRealtimeConfigured || !subscribedChannels.add("tasks_channel")) return
        try {
            val channel = client.channel("tasks_channel")
            val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "production_tasks"
            }

            flow.onEach { action ->
                try {
                    val record = when (action) {
                        is PostgresAction.Insert -> action.decodeRecord<ProductionTask>()
                        is PostgresAction.Update -> action.decodeRecord<ProductionTask>()
                        else -> null
                    }
                    record?.let { repository.productionTaskDao.insertTask(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding task", e)
                }
            }.launchIn(scope)

            scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Throwable) { Log.w(TAG, "Tasks channel subscription notice: ${e.message}") } }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to initialize tasks realtime: ${e.message}")
        }
    }

    fun subscribeToTeamAgreements(repository: AppRepository, scope: CoroutineScope) {
        if (!isRealtimeConfigured || !subscribedChannels.add("agreements_channel")) return
        try {
            val channel = client.channel("agreements_channel")
            val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "team_agreements_table"
            }

            flow.onEach { action ->
                try {
                    val record = when (action) {
                        is PostgresAction.Insert -> action.decodeRecord<TeamAgreement>()
                        is PostgresAction.Update -> action.decodeRecord<TeamAgreement>()
                        else -> null
                    }
                    record?.let { repository.agreementDao.insertAgreement(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding agreement", e)
                }
            }.launchIn(scope)

            scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Throwable) { Log.w(TAG, "Agreements channel subscription notice: ${e.message}") } }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to initialize agreements realtime: ${e.message}")
        }
    }

    fun subscribeToAgreementAcknowledgments(repository: AppRepository, scope: CoroutineScope) {
        if (!isRealtimeConfigured || !subscribedChannels.add("acknowledgments_channel")) return
        try {
            val channel = client.channel("acknowledgments_channel")
            val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "agreement_acknowledgments_table"
            }

            flow.onEach { action ->
                try {
                    val record = when (action) {
                        is PostgresAction.Insert -> action.decodeRecord<AgreementAcknowledgment>()
                        is PostgresAction.Update -> action.decodeRecord<AgreementAcknowledgment>()
                        else -> null
                    }
                    record?.let { repository.agreementDao.insertAcknowledgment(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding acknowledgment", e)
                }
            }.launchIn(scope)

            scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Throwable) { Log.w(TAG, "Acknowledgments channel subscription notice: ${e.message}") } }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to initialize acknowledgments realtime: ${e.message}")
        }
    }

    fun subscribeToMessages(repository: AppRepository, scope: CoroutineScope) {
        if (!isRealtimeConfigured || !subscribedChannels.add("messages_channel")) return
        try {
            val channel = client.channel("messages_channel")
            val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "messages"
            }

            flow.onEach { action ->
                try {
                    val newMessage = action.decodeRecord<Message>()
                    repository.messageDao.insertMessage(newMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding message", e)
                }
            }.launchIn(scope)

            scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Throwable) { Log.w(TAG, "Messages channel subscription notice: ${e.message}") } }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to initialize messages realtime: ${e.message}")
        }
    }

    fun subscribeToPosts(repository: AppRepository, scope: CoroutineScope) {
        if (!isRealtimeConfigured || !subscribedChannels.add("posts_channel")) return
        try {
            val channel = client.channel("posts_channel")
            val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "posts"
            }

            flow.onEach { action ->
                try {
                    val newPost = action.decodeRecord<Post>()
                    repository.postDao.insertPost(newPost)
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding post", e)
                }
            }.launchIn(scope)

            scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Throwable) { Log.w(TAG, "Posts channel subscription notice: ${e.message}") } }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to initialize posts realtime: ${e.message}")
        }
    }

    fun subscribeToComments(repository: AppRepository, scope: CoroutineScope) {
        if (!isRealtimeConfigured || !subscribedChannels.add("comments_channel")) return
        try {
            val channel = client.channel("comments_channel")
            val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "comments"
            }

            flow.onEach { action ->
                try {
                    val newComment = action.decodeRecord<Comment>()
                    repository.commentDao.insertComment(newComment)
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding comment", e)
                }
            }.launchIn(scope)

            scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Throwable) { Log.w(TAG, "Comments channel subscription notice: ${e.message}") } }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to initialize comments realtime: ${e.message}")
        }
    }

    fun subscribeToUserData(repository: AppRepository, scope: CoroutineScope, userId: String) {
        if (!isRealtimeConfigured || !subscribedChannels.add("user_data_$userId")) return
        try {
            val channel = client.channel("user_data_$userId")
            val flow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "user_profiles"
            }

            flow.onEach { action ->
                try {
                    val updatedUser = action.decodeRecord<UserProfile>()
                    if (updatedUser.id == userId) {
                        repository.userDao.insertUser(updatedUser)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding user update", e)
                }
            }.launchIn(scope)

            scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Throwable) { Log.w(TAG, "UserData channel subscription notice: ${e.message}") } }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to initialize user data realtime: ${e.message}")
        }
    }
}
