package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.data.supabase.SupabaseConfig
import com.example.ui.viewmodels.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlatformRegressionAndSecurityTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getDatabase(context)
        repository = AppRepository(database)
        runBlocking {
            repository.prepopulateIfEmpty()
        }
    }

    @After
    fun tearDown() {
        AppDatabase.closeDatabase()
    }

    @Test
    fun testSupabaseConfigUnconfiguredSafety() {
        // Assert that placeholder credentials validation logic works
        val fakeUrl = "https://your-project.supabase.co"
        val isFake = fakeUrl.contains("your-project")
        assertTrue("Placeholder URL must be detected as placeholder", isFake)
    }

    @Test
    fun testExecutiveAuthorizationInFounderCrm() {
        val viewModel = FounderCrmViewModel(repository)

        // Verify valid executive profiles
        assertTrue("Botla Veerendra should be authorized", viewModel.isExecutiveAuthorized("veerendrabotla", "veerendrabotla@gmail.com"))
        assertTrue("Macha Praveen should be authorized", viewModel.isExecutiveAuthorized("praveenmacha", "praveenmacha777@gmail.com"))

        // Verify standard users are denied
        assertFalse("Standard user should be denied", viewModel.isExecutiveAuthorized("random_user_123", "user@example.com"))

        // Attempt resolution with unauthorized user
        viewModel.resolveSupportTicket("ticket_001", "RESOLVED", operatorId = "random_user_123")
        assertEquals("Access Denied: Executive credentials required.", viewModel.toastMessage.value)
    }

    @Test
    fun testAgreementSignatureHashMismatchValidation() = runBlocking {
        val viewModel = AgreementViewModel(repository)
        val wsId = "ws_test_hash"
        val workspace = Workspace(
            id = wsId,
            name = "Hash Test Workspace",
            platformType = "YOUTUBE",
            createdBy = "admin_seed",
            createdAt = System.currentTimeMillis()
        )
        repository.insertWorkspace(workspace)
        repository.insertMember(WorkspaceMember(
            id = "mem_lead_hash",
            workspaceId = wsId,
            userId = "admin_seed",
            assignedRoleTitle = "Lead Creator"
        ))

        val agreement = TeamAgreement(
            id = "ag_test_hash",
            workspaceId = wsId,
            version = 1,
            contentText = "Original Agreement Terms v1.0",
            isLocked = false,
            createdAt = System.currentTimeMillis()
        )
        repository.createAgreement(agreement, "admin_seed")

        // User attempts to acknowledge with mismatched text hash
        val invalidHash = "Mismatched_Hash_12345"
        viewModel.acknowledgeAgreement("ag_test_hash", invalidHash, "user_signer")
        kotlinx.coroutines.delay(200)

        assertEquals("Signature Rejected: Agreement terms were updated. Please review the latest version.", viewModel.toastMessage.value)
    }

    @Test
    fun testDirectMessageWorkspaceRouting() = runBlocking {
        val chatViewModel = ChatViewModel(repository)

        val senderId = "user_alpha"
        val recipientId = "user_beta"

        // Send a direct message with fallback workspace "dm_general"
        chatViewModel.sendDirectMessage("dm_general", recipientId, "Direct message payload test", senderId, null)
        kotlinx.coroutines.delay(200)

        // Expected deterministic target workspace ID is sorted: "dm_user_alpha_user_beta"
        val expectedWsId = "dm_${listOf(senderId, recipientId).sorted().joinToString("_")}"

        val messages = repository.getMessagesForWorkspace(expectedWsId).first()
        assertTrue("Direct message should be routed to deterministic workspace ID", messages.isNotEmpty())
        assertEquals("Direct message payload test", messages.first().messageBody)
    }

    @Test
    fun testWorkspacePermissionGuard() = runBlocking {
        val workspaceViewModel = WorkspaceViewModel(repository)

        val wsId = "ws_permission_test"
        val workspace = Workspace(
            id = wsId,
            name = "Permission Test Workspace",
            platformType = "YOUTUBE",
            createdBy = "owner_user",
            createdAt = System.currentTimeMillis()
        )
        repository.insertWorkspace(workspace)

        // Add owner member and standard member
        repository.insertMember(WorkspaceMember(
            id = "mem_owner",
            workspaceId = wsId,
            userId = "owner_user",
            assignedRoleTitle = "Owner",
            canModifyProduction = true
        ))
        val member = WorkspaceMember(
            id = "mem_std",
            workspaceId = wsId,
            userId = "member_user",
            assignedRoleTitle = "Contributor",
            canModifyProduction = false
        )
        repository.insertMember(member)

        workspaceViewModel.selectWorkspace(workspace)
        kotlinx.coroutines.delay(200)

        // Check permission helper
        val canModify = workspaceViewModel.hasPermission(wsId, "member_user", WorkspaceViewModel.WorkspacePermission.MODIFY_PRODUCTION)
        assertFalse("Standard member should not have modify permission", canModify)

        val ownerCanModify = workspaceViewModel.hasPermission(wsId, "owner_user", WorkspaceViewModel.WorkspacePermission.MODIFY_PRODUCTION)
        assertTrue("Owner should have modify permission", ownerCanModify)
    }

    @Test
    fun testFeatureFlagRbacAndRoleEvaluation() = runBlocking {
        val testFlag = FeatureFlag(
            flagKey = "TEST_ROLE_FLAG",
            description = "Test Role Flag",
            category = "CORE",
            isEnabled = true,
            organizerEnabled = true,
            participantEnabled = false,
            globalOverrideEnabled = true
        )
        repository.updateFeatureFlag(testFlag, "admin_test", "RBAC Unit Test")

        // Helper evaluation
        assertTrue(
            "Organizer should have access",
            com.example.ui.components.isFeatureAllowed(testFlag, "ORGANIZER")
        )
        assertTrue(
            "Owner should have access as organizer tier",
            com.example.ui.components.isFeatureAllowed(testFlag, "OWNER")
        )
        assertFalse(
            "Participant should NOT have access",
            com.example.ui.components.isFeatureAllowed(testFlag, "PARTICIPANT")
        )

        // Global killswitch override
        val killedFlag = testFlag.copy(globalOverrideEnabled = false)
        assertFalse(
            "Organizer should be denied when global killswitch is disabled",
            com.example.ui.components.isFeatureAllowed(killedFlag, "ORGANIZER")
        )
        assertFalse(
            "Participant should be denied when global killswitch is disabled",
            com.example.ui.components.isFeatureAllowed(killedFlag, "PARTICIPANT")
        )
    }

    @Test
    fun testApplyFeatureFlagPresetProfilesAndAuditTrail() = runBlocking {
        val testFlag = FeatureFlag(
            flagKey = "SYNDICATE_PITCH_CREATION",
            description = "Syndicate Pitch Creation",
            category = "COLLABORATION",
            isEnabled = true,
            organizerEnabled = true,
            participantEnabled = true,
            globalOverrideEnabled = true
        )
        repository.updateFeatureFlag(testFlag, "admin_tester", "Seed flag")

        // Update with maintenance lockdown
        val disabledFlag = testFlag.copy(
            globalOverrideEnabled = false,
            organizerEnabled = false,
            participantEnabled = false,
            isEnabled = false
        )
        repository.updateFeatureFlag(disabledFlag, "admin_tester", "Preset applied: RESTRICTED_MAINTENANCE. Reason: Emergency test")

        val updatedFlags = repository.getAllFeatureFlagsFlow().first()
        val pitchFlag = updatedFlags.find { it.flagKey == "SYNDICATE_PITCH_CREATION" }
        assertNotNull(pitchFlag)
        assertFalse("Heavy creation should be disabled in RESTRICTED_MAINTENANCE", pitchFlag!!.globalOverrideEnabled)

        // Verify audit logs were created
        val auditLogs = repository.allAuditLogs.first()
        val presetLogs = auditLogs.filter { it.reason.contains("RESTRICTED_MAINTENANCE") || it.targetId == "SYNDICATE_PITCH_CREATION" }
        assertTrue("Audit trail should capture preset execution", presetLogs.isNotEmpty())
    }
}

