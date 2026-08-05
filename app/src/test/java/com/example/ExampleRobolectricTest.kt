package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
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
class ExampleRobolectricTest {

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
    fun testPrepopulation() = runBlocking {
        val user = repository.userDao.getUserById("admin_seed").first()
        assertNotNull(user)
        assertEquals("admin@creatorcoop.com", user?.email)
        assertEquals("Platform Admin", user?.displayName)
    }

    @Test
    fun testWorkspacePrepopulation() = runBlocking {
        val workspaces = repository.allWorkspaces.first()
        assertNotNull(workspaces)
        assertTrue(workspaces.isNotEmpty())
        assertEquals("TechPulse Main Channel", workspaces.first().name)
    }

    @Test
    fun testSaveAndRetrieveUser() = runBlocking {
        val testUser = UserProfile(
            id = "test_user_id",
            email = "unittest@coop.com",
            displayName = "Unit Test User",
            avatarUrl = "https://picsum.photos/100",
            globalRole = "APP_USER",
            primarySpecialty = "Video Creation",
            isVerifiedPro = true,
            systemRole = "REGISTERED_USER"
        )
        repository.saveUser(testUser)
        val fetched = repository.userDao.getUserById("test_user_id").first()
        assertNotNull(fetched)
        assertEquals("unittest@coop.com", fetched?.email)
        assertEquals("Unit Test User", fetched?.displayName)
        assertTrue(fetched?.isVerifiedPro == true)
    }

    @Test
    fun testInsertWorkspaceAndMember() = runBlocking {
        val workspaceId = "new_ws_test"
        val ws = Workspace(
            id = workspaceId,
            name = "Collaborative Space",
            platformType = "INSTAGRAM",
            createdBy = "me",
            createdAt = System.currentTimeMillis()
        )
        repository.insertWorkspace(ws)

        val member = WorkspaceMember(
            id = UUID.randomUUID().toString(),
            workspaceId = workspaceId,
            userId = "collaborator_1",
            assignedRoleTitle = "Cinematographer",
            canModifyProduction = true
        )
        repository.insertMember(member)

        val fetchedWorkspace = repository.getWorkspaceById(workspaceId).first()
        assertNotNull(fetchedWorkspace)
        assertEquals("Collaborative Space", fetchedWorkspace?.name)

        val members = repository.getMembersForWorkspace(workspaceId).first()
        assertEquals(1, members.size)
        assertEquals("Cinematographer", members.first().assignedRoleTitle)
    }

    @Test
    fun testTaskWorkflow() = runBlocking {
        val workspaceId = "task_ws"
        val task = ProductionTask(
            id = "task_1",
            workspaceId = workspaceId,
            creatorId = "me",
            title = "Rough Audio Draft",
            contentBody = "Edit sound effects for intro.",
            stateScope = "PRODUCTION_READY",
            kanbanLane = "TODO",
            createdAt = System.currentTimeMillis()
        )
        repository.insertTask(task)

        val tasks = repository.getProductionTasks(workspaceId).first()
        assertEquals(1, tasks.size)
        assertEquals("Rough Audio Draft", tasks.first().title)

        // Move task lane
        repository.insertTask(task.copy(kanbanLane = "EDITING"))
        val updatedTasks = repository.getProductionTasks(workspaceId).first()
        assertEquals("EDITING", updatedTasks.first().kanbanLane)
    }

    @Test
    fun testMessageExchange() = runBlocking {
        val workspaceId = "chat_ws"
        val message = Message(
            id = "msg_1",
            workspaceId = workspaceId,
            senderId = "me",
            senderName = "Creator",
            messageBody = "Hey team, draft is uploaded",
            timestamp = System.currentTimeMillis()
        )
        repository.insertMessage(message)

        val messages = repository.getMessagesForWorkspace(workspaceId).first()
        assertEquals(1, messages.size)
        assertEquals("Hey team, draft is uploaded", messages.first().messageBody)
    }

    @Test
    fun testTeamAgreement() = runBlocking {
        val workspaceId = "agreements_ws"
        val agreement = TeamAgreement(
            id = "agreement_1",
            workspaceId = workspaceId,
            version = 1,
            contentText = "We move in logic, not in law.",
            isLocked = false,
            createdAt = System.currentTimeMillis()
        )
        repository.createAgreement(agreement)

        val fetchedAgreement = repository.getLatestAgreement(workspaceId).first()
        assertNotNull(fetchedAgreement)
        assertEquals("We move in logic, not in law.", fetchedAgreement?.contentText)
        assertFalse(fetchedAgreement?.isLocked == true)
    }

    @Test
    fun testGDPRCascadingAccountDeletion() = runBlocking {
        val userId = "test_user_gdpr"
        val testUser = UserProfile(
            id = userId,
            email = "gdpr@coop.com",
            displayName = "My Real Name",
            avatarUrl = "https://picsum.photos/100",
            globalRole = "APP_USER"
        )
        repository.saveUser(testUser)

        val post = Post(
            id = "gdpr_post",
            authorId = userId,
            title = "GDPR Post Title",
            authorName = "My Real Name",
            body = "This is some post content",
            timestamp = System.currentTimeMillis()
        )
        repository.postDao.insertPost(post)

        val comment = Comment(
            id = "gdpr_comment",
            authorId = userId,
            postId = "gdpr_post",
            authorName = "My Real Name",
            text = "GDPR reply text",
            timestamp = System.currentTimeMillis()
        )
        repository.commentDao.insertComment(comment)

        val workspaceId = "gdpr_ws"
        val ws = Workspace(
            id = workspaceId,
            name = "GDPR Cooperative Team",
            platformType = "YOUTUBE",
            createdBy = userId,
            createdAt = System.currentTimeMillis()
        )
        repository.insertWorkspace(ws)

        val membership = WorkspaceMember(
            id = "gdpr_member_1",
            workspaceId = workspaceId,
            userId = userId,
            assignedRoleTitle = "Lead Creator"
        )
        repository.insertMember(membership)

        val sandboxTask = ProductionTask(
            id = "gdpr_sandbox_task",
            workspaceId = workspaceId,
            creatorId = userId,
            title = "Draft Idea",
            contentBody = "Confidential script drafts",
            stateScope = "ROUGH_SANDBOX",
            createdAt = System.currentTimeMillis()
        )
        repository.insertTask(sandboxTask)

        val sharedTask = ProductionTask(
            id = "gdpr_shared_task",
            workspaceId = workspaceId,
            creatorId = userId,
            title = "Shared Retraining Document",
            contentBody = "Published retainer SOP",
            stateScope = "PRODUCTION_READY",
            createdAt = System.currentTimeMillis()
        )
        repository.insertTask(sharedTask)

        val message = Message(
            id = "gdpr_message",
            workspaceId = workspaceId,
            senderId = userId,
            senderName = "My Real Name",
            messageBody = "Confidential team chat",
            timestamp = System.currentTimeMillis()
        )
        repository.insertMessage(message)

        // Verify initial setup is correct
        assertNotNull(repository.userDao.getUserById(userId).first())
        assertEquals(1, repository.getRoughSandboxTasks(workspaceId, userId).first().size)
        assertEquals(1, repository.getProductionTasks(workspaceId).first().size)
        assertEquals(1, repository.getMessagesForWorkspace(workspaceId).first().size)

        // Perform cascading account deletion
        val success = repository.deleteMyAccountCascade(userId)
        assertTrue(success)

        // Assert user profile is deleted from users table
        assertNull(repository.userDao.getUserById(userId).first())

        // Assert sandbox item is permanently wiped
        assertEquals(0, repository.getRoughSandboxTasks(workspaceId, userId).first().size)

        // Assert workspace memberships are completely deleted for this user
        val members = repository.workspaceMemberDao.getMembersForWorkspaceList(workspaceId)
        assertTrue(members.none { it.userId == userId })

        // Assert public forum posts are fully anonymized to generic "Deleted User" reference
        val updatedPost = repository.postDao.getPostById("gdpr_post").first()
        assertNotNull(updatedPost)
        assertEquals("Deleted User", updatedPost?.authorName)
        assertEquals("Former Member", updatedPost?.authorRole)
        assertEquals("", updatedPost?.authorAvatarUrl)

        // Assert forum comment is anonymized
        val updatedComment = repository.commentDao.getCommentById("gdpr_comment")
        assertNotNull(updatedComment)
        assertEquals("Deleted User", updatedComment?.authorName)
        assertEquals("Former Member", updatedComment?.authorRole)

        // Assert public shared tasks are anonymized (creator_id updated to 'deleted_user')
        val updatedTask = repository.getTaskById("gdpr_shared_task").first()
        assertNotNull(updatedTask)
        assertEquals("deleted_user", updatedTask?.creatorId)

        // Assert chat message is anonymized
        val updatedMessage = repository.messageDao.getMessageById("gdpr_message")
        assertNotNull(updatedMessage)
        assertEquals("Deleted User", updatedMessage?.senderName)
        assertEquals("Former Member", updatedMessage?.senderRole)
    }

    @Test
    fun testMultiUserAgreementCollaborationAndSecurityConstraints() = runBlocking {
        // --- STEP 1: Two Authenticated Users & Workspace Membership Addition ---
        val userAId = "user_a_lead"
        val userBId = "user_b_member"
        
        val userA = UserProfile(id = userAId, email = "usera@coop.com", displayName = "User A Lead", globalRole = "APP_USER")
        val userB = UserProfile(id = userBId, email = "userb@coop.com", displayName = "User B Member", globalRole = "APP_USER")
        repository.saveUser(userA)
        repository.saveUser(userB)

        // User A creates a workspace
        val workspaceId = "collab_workspace_123"
        val ws = Workspace(
            id = workspaceId,
            name = "Co-Op Video Production Studio",
            platformType = "YOUTUBE",
            createdBy = userAId,
            createdAt = System.currentTimeMillis()
        )
        repository.insertWorkspace(ws)

        // Add User A as Lead Creator
        val memberA = WorkspaceMember(
            id = "mem_a",
            workspaceId = workspaceId,
            userId = userAId,
            assignedRoleTitle = "Lead Creator"
        )
        repository.insertMember(memberA)

        // Add User B as standard Editor
        val memberB = WorkspaceMember(
            id = "mem_b",
            workspaceId = workspaceId,
            userId = userBId,
            assignedRoleTitle = "Editor"
        )
        repository.insertMember(memberB)

        // Confirm User A can see User B in the member list
        val workspaceMembers = repository.workspaceMemberDao.getMembersForWorkspaceList(workspaceId)
        val fetchedUserB = workspaceMembers.find { it.userId == userBId }
        assertNotNull("User B should exist in the workspace members list", fetchedUserB)
        assertEquals("Editor", fetchedUserB?.assignedRoleTitle)


        // --- STEP 2: Team Agreement Creation by User A and retrieval by User B ---
        val agreement = TeamAgreement(
            id = "collab_agreement",
            workspaceId = workspaceId,
            version = 1,
            contentText = "Rule 1: Content must be unique.",
            isLocked = false,
            createdAt = System.currentTimeMillis()
        )
        repository.createAgreement(agreement, userAId)

        // Confirm User B can fetch/retrieve it via a query
        val fetchedAgreement = repository.getWorkspaceById(workspaceId).firstOrNull()?.let {
            repository.getLatestAgreement(workspaceId).first()
        }
        assertNotNull("User B should be able to fetch the agreement", fetchedAgreement)
        assertEquals("Rule 1: Content must be unique.", fetchedAgreement?.contentText)


        // --- STEP 3: Agreement Acknowledgment Verification ---
        val ackB = AgreementAcknowledgment(
            id = "ack_b",
            agreementId = "collab_agreement",
            userId = userBId,
            acknowledgmentHash = "Rule 1: Content must be unique.".hashCode().toString(),
            acknowledgedAt = System.currentTimeMillis()
        )
        repository.insertAcknowledgment(ackB)

        // Confirm User A can see that User B's acknowledgment exists in the database
        val allAcks = repository.getAcknowledgmentsFlow("collab_agreement").first()
        val foundAckB = allAcks.find { it.userId == userBId }
        assertNotNull("User A must be able to verify User B's acknowledgment in the database", foundAckB)


        // --- STEP 4: Unauthorized Edit Attacks and Lock Rejection Policies ---
        // Attempt 4.1: User B (standard Editor, not Lead) attempts to update the agreement while unlocked
        val attackAgreement1 = agreement.copy(contentText = "Hacked by User B!")
        var isRejectedAsUserB = false
        try {
            repository.createAgreement(attackAgreement1, userBId)
        } catch (e: SecurityException) {
            isRejectedAsUserB = true
        }
        assertTrue("User B's update must be rejected since they are not a Lead Creator/Head", isRejectedAsUserB)

        // Lock the agreement
        val lockedAgreement = agreement.copy(isLocked = true)
        repository.createAgreement(lockedAgreement, userAId)

        // Attempt 4.2: User A (Lead Creator) attempts to update the locked agreement
        val attackAgreement2 = lockedAgreement.copy(contentText = "Even Lead updates locked agreement!")
        var isRejectedEvenAsLead = false
        try {
            repository.createAgreement(attackAgreement2, userAId)
        } catch (e: SecurityException) {
            isRejectedEvenAsLead = true
        }
        assertTrue("Even User A's update must be rejected when the agreement is locked", isRejectedEvenAsLead)
    }

    @Test
    fun testSecureDisputeNotesRLS() = runBlocking {
        // Create an archived workspace
        val workspaceId = "archived_project"
        val ws = Workspace(
            id = workspaceId,
            name = "Coop Archive",
            isArchived = true,
            createdBy = "creator_lead"
        )
        repository.insertWorkspace(ws)

        // Setup members: creator_lead, editor_b
        val m1 = WorkspaceMember(id = "m1", workspaceId = workspaceId, userId = "creator_lead", assignedRoleTitle = "Lead Creator")
        val m2 = WorkspaceMember(id = "m2", workspaceId = workspaceId, userId = "editor_b", assignedRoleTitle = "Video Editor")
        repository.insertMember(m1)
        repository.insertMember(m2)

        // Add a dispute note authored by editor_b about creator_lead
        val disputeNote = DisputeNote(
            id = "note_1",
            workspaceId = workspaceId,
            authorId = "editor_b",
            targetUserId = "creator_lead",
            content = "Dispute over edits deliverable",
            noteText = "Dispute over edits deliverable"
        )
        repository.insertDisputeNote(disputeNote)

        // 1. Author (editor_b) queries notes for this workspace - should see it
        val notesForAuthor = repository.getDisputeNotesForWorkspace(workspaceId, "editor_b").first()
        assertEquals(1, notesForAuthor.size)
        assertEquals("Dispute over edits deliverable", notesForAuthor.first().noteText)

        // 2. Co-member (creator_lead) queries notes about creator_lead - should see it because they share the workspace
        val notesForLead = repository.getDisputeNotesAboutUser("creator_lead", "creator_lead").first()
        assertEquals(1, notesForLead.size)

        // 3. Unauthorized bystander (some other user, e.g., "bystander") queries notes about creator_lead - should return EMPTY list (RLS enforcement!)
        val notesForBystander = repository.getDisputeNotesAboutUser("creator_lead", "bystander").first()
        assertTrue("Bystander must be filtered out securely via SQLite RLS", notesForBystander.isEmpty())
    }
}
