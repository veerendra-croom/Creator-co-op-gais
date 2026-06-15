package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Project
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

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
        database.close()
    }

    @Test
    fun testPrepopulation() = runBlocking {
        val user = repository.getUserById("me").first()
        assertNotNull(user)
        assertEquals("appcroom@gmail.com", user?.email)
        assertEquals("VibeCreative Studio", user?.displayName)
    }

    @Test
    fun testProjectPrepopulation() = runBlocking {
        val projects = repository.allProjects.first()
        assertNotNull(projects)
        assertTrue(projects.size >= 2)
    }

    @Test
    fun testSaveAndRetrieveUser() = runBlocking {
        val testUser = com.example.data.model.User(
            id = "test_user_id",
            email = "unittest@coop.com",
            phone = "123456",
            displayName = "Unit Test User",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
            primaryRole = "Tester",
            secondaryRolesJson = "[\"QA\"]",
            portfolioLinksJson = "[]",
            karmaScore = 150,
            isVerifiedPro = true,
            stripeAccountId = "stripe_test_id",
            availableBalance = 1000.0,
            pendingBalance = 250.0,
            treasuryBalance = 100.0
        )
        repository.saveUser(testUser)
        val fetched = repository.getUserById("test_user_id").first()
        assertNotNull(fetched)
        assertEquals("unittest@coop.com", fetched?.email)
        assertEquals("Unit Test User", fetched?.displayName)
        assertEquals(150, fetched?.karmaScore)
    }

    @Test
    fun testInsertProject() = runBlocking {
        val proj = Project(
            id = "proj_test_id",
            managerId = "user_manager",
            title = "Test Channel Pro",
            niche = "Tech",
            contentStrategy = "Weekly cinematic teardowns",
            subscriberCount = 25000,
            status = "PUBLISHED",
            createdAt = System.currentTimeMillis(),
            hostEquity = 50,
            editorEquity = 30,
            writerEquity = 20
        )
        repository.insertProject(proj)
        val fetched = repository.getProjectById("proj_test_id").first()
        assertNotNull(fetched)
        assertEquals("Test Channel Pro", fetched?.title)
        assertEquals(50, fetched?.hostEquity)
        assertEquals(30, fetched?.editorEquity)
        assertEquals(20, fetched?.writerEquity)
    }

    @Test
    fun testSignatureWorkflowUnlocksWorkspace() = runBlocking {
        val projId = "proj_workflow_test_id"
        val proj = Project(
            id = projId,
            managerId = "user_manager",
            title = "Cinematic Flow",
            niche = "Gaming",
            contentStrategy = "Deep retro documentaries",
            subscriberCount = 50000,
            status = "PUBLISHED",
            createdAt = System.currentTimeMillis(),
            hostEquity = 40,
            editorEquity = 30,
            writerEquity = 30
        )
        repository.insertProject(proj)

        val pitch = com.example.data.model.Pitch(
            id = "pitch_workflow_id",
            projectId = projId,
            applicantId = "user_editor",
            applicantName = "Jack Editor",
            applicantRole = "Video Editor",
            applicantAvatarUrl = "",
            message = "Let's cut some frames!",
            portfolioLink = "drive.google.com/portfolio",
            status = "PENDING",
            submittedAt = System.currentTimeMillis()
        )
        repository.insertPitch(pitch)
        repository.updatePitchStatus("pitch_workflow_id", "ACCEPTED")

        // Contract should be generated automatically in ACCEPTED status of Pitch
        val contract = repository.getContractByProject(projId).first()
        assertNotNull(contract)
        assertEquals("SENT", contract?.status)

        // Sign by manager
        repository.signContractByManager(projId)
        val signedByManagerContract = repository.getContractByProject(projId).first()
        assertEquals("SIGNED_BY_MANAGER", signedByManagerContract?.status)

        // Sign by talent should change to FULLY_SIGNED and trigger project status to CONTRACTED
        repository.signContractByTalent(projId)
        val fullySignedContract = repository.getContractByProject(projId).first()
        assertEquals("FULLY_SIGNED", fullySignedContract?.status)

        val updatedProj = repository.getProjectById(projId).first()
        assertEquals("CONTRACTED", updatedProj?.status)

        // Verify that a system message has been auto-inserted in the team room
        val messages = repository.getMessagesForChannel(projId, "general").first()
        assertTrue(messages.isNotEmpty())
        assertTrue(messages.any { it.senderId == "system" })
    }
}
