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

    private fun isSyncAllowed(context: Context): Boolean {
        return SupabaseConfig.isConfigured && SupabaseConfig.isNetworkAvailable(context)
    }

    suspend fun syncDownEverything(context: Context, repository: AppRepository) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext

        // User Profiles
        syncTable<UserProfile>("user_profiles") { repository.userDao.insertUsers(it) }

        // Workspaces
        syncTable<Workspace>("workspaces") { repository.workspaceDao.insertWorkspaces(it) }

        // Members
        syncTable<WorkspaceMember>("workspace_members") { repository.workspaceMemberDao.insertMembers(it) }

        // Tasks
        syncTable<ProductionTask>("production_tasks") { repository.productionTaskDao.insertTasks(it) }

        // Agreements
        syncTable<TeamAgreement>("team_agreements") { repository.agreementDao.insertAgreements(it) }

        // Acknowledgments
        syncTable<AgreementAcknowledgment>("agreement_acknowledgments") { repository.agreementDao.insertAcknowledgments(it) }

        // Messages
        syncTable<Message>("messages") { repository.messageDao.insertMessages(it) }

        // Endorsements
        syncTable<Endorsement>("endorsements") { repository.endorsementDao.insertEndorsements(it) }

        // Posts
        syncTable<Post>("posts") { repository.postDao.insertPosts(it) }

        // Reports (Admin only handled by policies)
        syncTable<Report>("reports") { repository.reportDao.insertReports(it) }

        // Audit Logs
        syncTable<AuditLog>("admin_audit_logs") { repository.auditLogDao.insertLogs(it) }
    }

    private suspend inline fun <reified T : Any> syncTable(
        tableName: String,
        crossinline insertBlock: suspend (List<T>) -> Unit
    ) {
        val client = SupabaseConfig.client
        val pageSize = 150L
        var offset = 0L
        var hasMore = true
        
        while (hasMore) {
            val currentOffset = offset
            try {
                val response = com.example.util.RetryWithBackoff.execute(maxAttempts = 3) {
                    client.postgrest.from(tableName).select {
                        limit(pageSize)
                        range(currentOffset, currentOffset + pageSize - 1L)
                    }.data
                }
                if (!response.isNullOrBlank()) {
                    val batch = json.decodeFromString<List<T>>(response)
                    if (batch.isNotEmpty()) {
                        insertBlock(batch)
                        offset += batch.size
                    }
                    hasMore = batch.size.toLong() == pageSize
                } else {
                    hasMore = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Paginated sync of $tableName failed at offset $currentOffset", e)
                hasMore = false // Halt current loop to prevent blocking or infinite retry loops
            }
        }
    }

    suspend fun syncUpReport(context: Context, report: Report) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("reports").upsert(report) } catch (e: Exception) { Log.e(TAG, "syncUpReport fail", e) }
    }

    suspend fun syncUpAuditLog(context: Context, log: AuditLog) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("admin_audit_logs").insert(log) } catch (e: Exception) { Log.e(TAG, "syncUpAuditLog fail", e) }
    }

    suspend fun syncDeletePost(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("posts").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeletePost fail", e) }
    }

    suspend fun syncDeleteComment(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("comments").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteComment fail", e) }
    }

    suspend fun syncUpUser(context: Context, user: UserProfile) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("user_profiles").upsert(user) } catch (e: Exception) { Log.e(TAG, "syncUpUser fail", e) }
    }

    suspend fun syncUpWorkspace(context: Context, workspace: Workspace) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("workspaces").upsert(workspace) } catch (e: Exception) { Log.e(TAG, "syncUpWorkspace fail", e) }
    }

    suspend fun syncUpWorkspaceMember(context: Context, member: WorkspaceMember) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("workspace_members").upsert(member) } catch (e: Exception) { Log.e(TAG, "syncUpWorkspaceMember fail", e) }
    }

    suspend fun syncUpProductionTask(context: Context, task: ProductionTask) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("production_tasks").upsert(task) } catch (e: Exception) { Log.e(TAG, "syncUpProductionTask fail", e) }
    }

    suspend fun syncDeleteProductionTask(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("production_tasks").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteProductionTask fail", e) }
    }

    suspend fun syncUpMessage(context: Context, message: Message) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("messages").insert(message) } catch (e: Exception) { Log.e(TAG, "syncUpMessage fail", e) }
    }

    suspend fun syncUpAgreement(context: Context, agreement: TeamAgreement) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("team_agreements").upsert(agreement) } catch (e: Exception) { Log.e(TAG, "syncUpAgreement fail", e) }
    }

    suspend fun syncUpAcknowledgment(context: Context, acknowledgment: AgreementAcknowledgment) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("agreement_acknowledgments").upsert(acknowledgment) } catch (e: Exception) { Log.e(TAG, "syncUpAcknowledgment fail", e) }
    }

    suspend fun syncUpPost(context: Context, post: Post) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("posts").upsert(post) } catch (e: Exception) { Log.e(TAG, "syncUpPost fail", e) }
    }

    suspend fun syncUpComment(context: Context, comment: Comment) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("comments").insert(comment) } catch (e: Exception) { Log.e(TAG, "syncUpComment fail", e) }
    }

    suspend fun syncUpWorkspaceAsset(context: Context, asset: WorkspaceAsset) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("workspace_assets").upsert(asset) } catch (e: Exception) { Log.e(TAG, "syncUpWorkspaceAsset fail", e) }
    }

    suspend fun syncDeleteWorkspaceAsset(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("workspace_assets").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteWorkspaceAsset fail", e) }
    }

    suspend fun syncUpDeliverable(context: Context, deliverable: Deliverable) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("deliverables").upsert(deliverable) } catch (e: Exception) { Log.e(TAG, "syncUpDeliverable fail", e) }
    }

    suspend fun syncDeleteDeliverable(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("deliverables").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteDeliverable fail", e) }
    }

    suspend fun syncUpWorkspaceEvent(context: Context, event: WorkspaceEvent) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("workspace_events").upsert(event) } catch (e: Exception) { Log.e(TAG, "syncUpWorkspaceEvent fail", e) }
    }

    suspend fun syncUpSavedSearch(context: Context, search: SavedSearch) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("saved_searches").upsert(search) } catch (e: Exception) { Log.e(TAG, "syncUpSavedSearch fail", e) }
    }

    suspend fun syncDeleteSavedSearch(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("saved_searches").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteSavedSearch fail", e) }
    }

    suspend fun syncUpLookingForWork(context: Context, listing: LookingForWork) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("looking_for_work").upsert(listing) } catch (e: Exception) { Log.e(TAG, "syncUpLookingForWork fail", e) }
    }

    suspend fun syncDeleteLookingForWork(context: Context, userId: String) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("looking_for_work").delete { filter { eq("userId", userId) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteLookingForWork fail", e) }
    }

    suspend fun syncDeleteMessage(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("messages").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteMessage fail", e) }
    }

    suspend fun syncUpSupportTicket(context: Context, ticket: SupportTicket) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("support_tickets").upsert(ticket) } catch (e: Exception) { Log.e(TAG, "syncUpSupportTicket fail", e) }
    }

    suspend fun syncDeleteSupportTicket(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("support_tickets").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteSupportTicket fail", e) }
    }

    suspend fun syncUpDisputeNote(context: Context, note: DisputeNote) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("dispute_notes").upsert(note) } catch (e: Exception) { Log.e(TAG, "syncUpDisputeNote fail", e) }
    }

    suspend fun syncUpFounderNote(context: Context, note: FounderNote) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("founder_notes").upsert(note) } catch (e: Exception) { Log.e(TAG, "syncUpFounderNote fail", e) }
    }

    suspend fun syncDeleteFounderNote(context: Context, id: String) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("founder_notes").delete { filter { eq("id", id) } } } catch (e: Exception) { Log.e(TAG, "syncDeleteFounderNote fail", e) }
    }

    suspend fun syncUpVerificationRequest(context: Context, request: VerificationRequest) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("verification_requests").upsert(request) } catch (e: Exception) { Log.e(TAG, "syncUpVerificationRequest fail", e) }
    }

    suspend fun syncUpConnectionRequest(context: Context, request: ConnectionRequest) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("connection_requests").upsert(request) } catch (e: Exception) { Log.e(TAG, "syncUpConnectionRequest fail", e) }
    }

    suspend fun syncUpEndorsement(context: Context, endorsement: Endorsement) = withContext(Dispatchers.IO) {
        if (!isSyncAllowed(context)) return@withContext
        try { SupabaseConfig.client.postgrest.from("endorsements").upsert(endorsement) } catch (e: Exception) { Log.e(TAG, "syncUpEndorsement fail", e) }
    }
}
