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

    private val client by lazy { SupabaseConfig.client }

    fun subscribeToWorkspaces(repository: AppRepository, scope: CoroutineScope) {
        if (isTestEnv) return
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

        scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Exception) {} }
    }

    fun subscribeToWorkspaceMembers(repository: AppRepository, scope: CoroutineScope) {
        if (isTestEnv) return
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

        scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Exception) {} }
    }

    fun subscribeToProductionTasks(repository: AppRepository, scope: CoroutineScope) {
        if (isTestEnv) return
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

        scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Exception) {} }
    }

    fun subscribeToTeamAgreements(repository: AppRepository, scope: CoroutineScope) {
        if (isTestEnv) return
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

        scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Exception) {} }
    }

    fun subscribeToAgreementAcknowledgments(repository: AppRepository, scope: CoroutineScope) {
        if (isTestEnv) return
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

        scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Exception) {} }
    }

    fun subscribeToMessages(repository: AppRepository, scope: CoroutineScope) {
        if (isTestEnv) return
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

        scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Exception) {} }
    }

    fun subscribeToPosts(repository: AppRepository, scope: CoroutineScope) {
        if (isTestEnv) return
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

        scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Exception) {} }
    }

    fun subscribeToComments(repository: AppRepository, scope: CoroutineScope) {
        if (isTestEnv) return
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

        scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Exception) {} }
    }

    fun subscribeToUserData(repository: AppRepository, scope: CoroutineScope, userId: String) {
        if (isTestEnv) return
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

        scope.launch(Dispatchers.IO) { try { channel.subscribe() } catch (e: Exception) {} }
    }
}
