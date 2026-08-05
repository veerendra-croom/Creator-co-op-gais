package com.example.data.supabase

import android.content.Context
import android.util.Log
import com.example.data.model.*
import com.example.data.repository.AppRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseSynchronizer {
    private val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    private const val TAG = "SupabaseSynchronizer"

    suspend fun syncDownEverything(context: Context, repository: AppRepository) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        val client = SupabaseConfig.client

        // User Profiles
        try {
            val response = com.example.util.RetryWithBackoff.execute(maxAttempts = 3) {
                client.postgrest.from("user_profiles").select().data
            }
            if (response != null) {
                val data = json.decodeFromString<List<UserProfile>>(response)
                data.forEach { repository.userDao.insertUser(it) }
            }
        } catch (e: Exception) { Log.e(TAG, "Sync user_profiles fail", e) }

        // Workspaces
        try {
            val response = com.example.util.RetryWithBackoff.execute(maxAttempts = 3) {
                client.postgrest.from("workspaces").select().data
            }
            if (response != null) {
                val data = json.decodeFromString<List<Workspace>>(response)
                data.forEach { repository.workspaceDao.insertWorkspace(it) }
            }
        } catch (e: Exception) { Log.e(TAG, "Sync workspaces fail", e) }

        // Members
        try {
            val response = com.example.util.RetryWithBackoff.execute(maxAttempts = 3) {
                client.postgrest.from("workspace_members").select().data
            }
            if (response != null) {
                val data = json.decodeFromString<List<WorkspaceMember>>(response)
                data.forEach { repository.workspaceMemberDao.insertMember(it) }
            }
        } catch (e: Exception) { Log.e(TAG, "Sync workspace_members fail", e) }

        // Tasks
        try {
            val response = com.example.util.RetryWithBackoff.execute(maxAttempts = 3) {
                client.postgrest.from("production_tasks").select().data
            }
            if (response != null) {
                val data = json.decodeFromString<List<ProductionTask>>(response)
                data.forEach { repository.productionTaskDao.insertTask(it) }
            }
        } catch (e: Exception) { Log.e(TAG, "Sync production_tasks fail", e) }

        // Agreements
        try {
            val response = com.example.util.RetryWithBackoff.execute(maxAttempts = 3) {
                client.postgrest.from("team_agreements").select().data
            }
            if (response != null) {
                val data = json.decodeFromString<List<TeamAgreement>>(response)
                data.forEach { repository.agreementDao.insertAgreement(it) }
            }
        } catch (e: Exception) { Log.e(TAG, "Sync team_agreements fail", e) }

        // Acknowledgments
        try {
            val response = client.postgrest.from("agreement_acknowledgments").select().data
            val data = json.decodeFromString<List<AgreementAcknowledgment>>(response)
            data.forEach { repository.agreementDao.insertAcknowledgment(it) }
        } catch (e: Exception) { Log.e(TAG, "Sync acknowledgments fail", e) }

        // Messages
        try {
            val response = client.postgrest.from("messages").select().data
            val data = json.decodeFromString<List<Message>>(response)
            data.forEach { repository.messageDao.insertMessage(it) }
        } catch (e: Exception) { Log.e(TAG, "Sync messages fail", e) }

        // Endorsements
        try {
            val response = client.postgrest.from("endorsements").select().data
            val data = json.decodeFromString<List<Endorsement>>(response)
            repository.endorsementDao.insertEndorsements(data)
        } catch (e: Exception) { Log.e(TAG, "Sync endorsements fail", e) }

        // Posts
        try {
            val response = client.postgrest.from("posts").select().data
            val data = json.decodeFromString<List<Post>>(response)
            repository.postDao.insertPosts(data)
        } catch (e: Exception) { Log.e(TAG, "Sync posts fail", e) }

        // Reports (Admin only handled by policies)
        try {
            val response = client.postgrest.from("reports").select().data
            val data = json.decodeFromString<List<Report>>(response)
            data.forEach { repository.reportDao.insertReport(it) }
        } catch (e: Exception) { Log.e(TAG, "Sync reports fail", e) }

        // Audit Logs
        try {
            val response = client.postgrest.from("admin_audit_logs").select().data
            val data = json.decodeFromString<List<AuditLog>>(response)
            data.forEach { repository.auditLogDao.insertLog(it) }
        } catch (e: Exception) { Log.e(TAG, "Sync logs fail", e) }
    }

    suspend fun syncUpReport(context: Context, report: Report) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("reports").upsert(report) } catch (e: Exception) { Log.e(TAG, "syncUpReport fail", e) }
    }

    suspend fun syncUpAuditLog(context: Context, log: AuditLog) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("admin_audit_logs").insert(log) } catch (e: Exception) { Log.e(TAG, "syncUpAuditLog fail", e) }
    }

    suspend fun syncDeletePost(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("forum_posts_table").delete { filter { eq("post_id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeletePost fail", e) }
    }

    suspend fun syncDeleteComment(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        // Assuming there is a comments table in Supabase
        try { SupabaseConfig.client.postgrest.from("forum_comments_table").delete { filter { eq("comment_id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteComment fail", e) }
    }

    suspend fun syncUpUser(context: Context, user: UserProfile) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("user_profiles").upsert(user) } catch (e: Exception) { Log.e(TAG, "syncUpUser fail", e) }
    }

    suspend fun syncUpWorkspace(context: Context, workspace: Workspace) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("workspaces").upsert(workspace) } catch (e: Exception) { Log.e(TAG, "syncUpWorkspace fail", e) }
    }

    suspend fun syncUpWorkspaceMember(context: Context, member: WorkspaceMember) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("workspace_members").upsert(member) } catch (e: Exception) { Log.e(TAG, "syncUpWorkspaceMember fail", e) }
    }

    suspend fun syncUpProductionTask(context: Context, task: ProductionTask) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("production_tasks").upsert(task) } catch (e: Exception) { Log.e(TAG, "syncUpProductionTask fail", e) }
    }

    suspend fun syncDeleteProductionTask(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("production_tasks").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteProductionTask fail", e) }
    }

    suspend fun syncUpMessage(context: Context, message: Message) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("messages").insert(message) } catch (e: Exception) { Log.e(TAG, "syncUpMessage fail", e) }
    }

    suspend fun syncUpAgreement(context: Context, agreement: TeamAgreement) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("team_agreements").upsert(agreement) } catch (e: Exception) { Log.e(TAG, "syncUpAgreement fail", e) }
    }

    suspend fun syncUpAcknowledgment(context: Context, acknowledgment: AgreementAcknowledgment) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("agreement_acknowledgments").upsert(acknowledgment) } catch (e: Exception) { Log.e(TAG, "syncUpAcknowledgment fail", e) }
    }

    suspend fun syncUpPost(context: Context, post: Post) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("posts").upsert(post) } catch (e: Exception) { Log.e(TAG, "syncUpPost fail", e) }
    }

    suspend fun syncUpComment(context: Context, comment: Comment) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("comments").insert(comment) } catch (e: Exception) { Log.e(TAG, "syncUpComment fail", e) }
    }

    suspend fun syncUpWorkspaceAsset(context: Context, asset: WorkspaceAsset) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("workspace_assets").upsert(asset) } catch (e: Exception) { Log.e(TAG, "syncUpWorkspaceAsset fail", e) }
    }

    suspend fun syncDeleteWorkspaceAsset(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("workspace_assets").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteWorkspaceAsset fail", e) }
    }

    suspend fun syncUpDeliverable(context: Context, deliverable: Deliverable) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("deliverables").upsert(deliverable) } catch (e: Exception) { Log.e(TAG, "syncUpDeliverable fail", e) }
    }

    suspend fun syncDeleteDeliverable(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("deliverables").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteDeliverable fail", e) }
    }

    suspend fun syncUpWorkspaceEvent(context: Context, event: WorkspaceEvent) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("workspace_events").upsert(event) } catch (e: Exception) { Log.e(TAG, "syncUpWorkspaceEvent fail", e) }
    }

    suspend fun syncUpSavedSearch(context: Context, search: SavedSearch) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("saved_searches").upsert(search) } catch (e: Exception) { Log.e(TAG, "syncUpSavedSearch fail", e) }
    }

    suspend fun syncDeleteSavedSearch(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("saved_searches").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteSavedSearch fail", e) }
    }

    suspend fun syncUpLookingForWork(context: Context, listing: LookingForWork) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("looking_for_work").upsert(listing) } catch (e: Exception) { Log.e(TAG, "syncUpLookingForWork fail", e) }
    }

    suspend fun syncDeleteLookingForWork(context: Context, userId: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("looking_for_work").delete { filter { eq("userId", userId) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteLookingForWork fail", e) }
    }

    suspend fun syncDeleteMessage(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("messages").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteMessage fail", e) }
    }

    suspend fun syncUpSupportTicket(context: Context, ticket: SupportTicket) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("support_tickets").upsert(ticket) } catch (e: Exception) { Log.e(TAG, "syncUpSupportTicket fail", e) }
    }

    suspend fun syncDeleteSupportTicket(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("support_tickets").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteSupportTicket fail", e) }
    }

    suspend fun syncUpDisputeNote(context: Context, note: DisputeNote) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("dispute_notes").upsert(note) } catch (e: Exception) { Log.e(TAG, "syncUpDisputeNote fail", e) }
    }

    suspend fun syncUpFounderNote(context: Context, note: FounderNote) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("founder_notes").upsert(note) } catch (e: Exception) { Log.e(TAG, "syncUpFounderNote fail", e) }
    }

    suspend fun syncDeleteFounderNote(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("founder_notes").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteFounderNote fail", e) }
    }

    suspend fun syncUpVerificationRequest(context: Context, request: VerificationRequest) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("verification_requests").upsert(request) } catch (e: Exception) { Log.e(TAG, "syncUpVerificationRequest fail", e) }
    }

    suspend fun syncUpConnectionRequest(context: Context, request: ConnectionRequest) = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isNetworkAvailable(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("connection_requests").upsert(request) } catch (e: Exception) { Log.e(TAG, "syncUpConnectionRequest fail", e) }
    }
}
