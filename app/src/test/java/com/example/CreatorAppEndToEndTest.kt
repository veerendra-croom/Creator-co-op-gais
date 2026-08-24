package com.example

import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class CreatorAppEndToEndTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: AppRepository
    private lateinit var factory: com.example.ui.viewmodels.AppViewModelFactory
    private lateinit var globalViewModel: com.example.ui.viewmodels.GlobalViewModel
    private lateinit var authViewModel: com.example.ui.viewmodels.AuthViewModel
    private lateinit var workspaceViewModel: com.example.ui.viewmodels.WorkspaceViewModel
    private lateinit var feedViewModel: com.example.ui.viewmodels.CommunityFeedViewModel
    private lateinit var discoveryViewModel: com.example.ui.viewmodels.DiscoveryViewModel
    private lateinit var adminViewModel: com.example.ui.viewmodels.AdminViewModel

    @Before
    fun setup() {
        org.robolectric.shadows.ShadowLog.stream = System.out
        val context = RuntimeEnvironment.getApplication() as CreatorCoopApp
        context.container = com.example.di.AppContainer(context)
        context.container.sharedPreferences.edit().putString("active_user_id", "me").commit()
        
        repository = context.container.repository
        factory = com.example.ui.viewmodels.AppViewModelFactory(context)
        globalViewModel = factory.create(com.example.ui.viewmodels.GlobalViewModel::class.java)
        authViewModel = factory.create(com.example.ui.viewmodels.AuthViewModel::class.java)
        workspaceViewModel = factory.create(com.example.ui.viewmodels.WorkspaceViewModel::class.java)
        feedViewModel = factory.create(com.example.ui.viewmodels.CommunityFeedViewModel::class.java)
        discoveryViewModel = factory.create(com.example.ui.viewmodels.DiscoveryViewModel::class.java)
        adminViewModel = factory.create(com.example.ui.viewmodels.AdminViewModel::class.java)

        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                context.container.database.clearAllTables()
                repository.prepopulateIfEmpty()
            }
        }
    }

    @After
    fun tearDown() {
        AppDatabase.closeDatabase()
    }

    @Test
    fun authentication_flow_simulation() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AuthScreen(authViewModel = authViewModel)
            }
        }

        composeTestRule.waitForIdle()

        // Verify title
        composeTestRule.onNodeWithText("CREATOR CO-OP").assertExists()

        // Enter Email
        composeTestRule.onNodeWithTag("auth_email_field").performTextInput("test@creator.co")
        
        // Enter Password
        composeTestRule.onNodeWithTag("auth_password_field").performTextInput("password123")

        // Click Submit
        composeTestRule.onNodeWithTag("auth_submit_button").performClick()
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun dashboard_profile_test() {
        val userProfile = com.example.data.model.UserProfile(
            id = "me",
            email = "test@creator.co",
            displayName = "Group Creator",
            primarySpecialty = "Creative Director",
            systemRole = "ADMIN",
            isVerifiedPro = true,
            skillsJson = "[\"Video Editing\", \"Production Planning\"]",
            portfolioJson = "[\"https://youtube.com/watch?v=sample1\", \"https://behance.net/portfolio\"]"
        )
        composeTestRule.setContent {
            MyApplicationTheme {
                DashboardScreen(
                    globalViewModel = globalViewModel,
                    authViewModel = authViewModel,
                    adminViewModel = adminViewModel,
                    userProfile = userProfile
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert profile items exist
        composeTestRule.onNodeWithText("Group Creator").assertExists()
        composeTestRule.onNodeWithText("Creative Director").assertExists()
        composeTestRule.onNodeWithText("Video Editing").assertExists()
        composeTestRule.onNodeWithText("TECHNICAL ARSENAL").assertExists()
        composeTestRule.onNodeWithText("PROJECT SHOWCASE").assertExists()
        composeTestRule.onNodeWithTag("dashboard_portfolio_list").assertExists()
    }

    @Test
    fun community_forum_feed_test() {
        // Submit a post via feedViewModel to populate the feed
        val userProfile = com.example.data.model.UserProfile(
            id = "me",
            email = "test@creator.co",
            displayName = "Group Creator",
            primarySpecialty = "Creative Director",
            systemRole = "ADMIN",
            isVerifiedPro = true
        )
        feedViewModel.submitPost("Intro Guide", "Best practices for video editing simple guides", "Cinematography", userProfile)

        composeTestRule.setContent {
            MyApplicationTheme {
                CreatorCommonsScreen(
                    feedViewModel = feedViewModel,
                    adminViewModel = adminViewModel,
                    globalViewModel = globalViewModel,
                    authViewModel = authViewModel,
                    userProfile = userProfile
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify community feed header and posted contents represent simple terms
        composeTestRule.onNodeWithText("CREATOR COMMONS").assertExists()
        composeTestRule.onNodeWithText("Intro Guide").assertExists()
        composeTestRule.onNodeWithText("Best practices for video editing simple guides").assertExists()
    }

    @Test
    fun workspace_management_and_agreement_test() {
        kotlinx.coroutines.runBlocking {
            repository.userDao.insertUser(
                com.example.data.model.UserProfile(
                    id = "me",
                    email = "test@creator.co",
                    displayName = "Group Creator",
                    primarySpecialty = "Creative Director",
                    systemRole = "ADMIN",
                    isVerifiedPro = true
                )
            )
            repository.setOnboardingChecklistDismissed("me", true)
            repository.insertMember(
                com.example.data.model.WorkspaceMember(
                    id = "test_membership_me",
                    workspaceId = "ws_youtube_main",
                    userId = "me",
                    assignedRoleTitle = "Lead Creator",
                    canModifyProduction = true
                )
            )
        }

        composeTestRule.setContent {
            MyApplicationTheme {
                CreatorCoOpDashboard(
                    globalViewModel = globalViewModel,
                    authViewModel = authViewModel
                )
            }
        }

        composeTestRule.waitForIdle()

        // Navigate to the workspaces tab
        globalViewModel.navigateToTab("WORKSPACES")
        composeTestRule.waitForIdle()

        // Assert workspaces tab is open, find prepopulated project card and click it
        composeTestRule.onAllNodesWithText("TechPulse Main Channel").onFirst().assertExists()
        composeTestRule.onNodeWithText("YOUTUBE").assertExists()
        composeTestRule.onAllNodesWithText("TechPulse Main Channel").onFirst().performClick()

        composeTestRule.waitForIdle()

        // Click TechPulse Main Channel again in the workspaces list to enter details
        composeTestRule.onAllNodesWithText("TechPulse Main Channel").onFirst().performClick()
        composeTestRule.waitForIdle()

        // Verify the details screen elements (the detail view tabs) are visible with simple down-to-earth words
        composeTestRule.onNodeWithText("My Drafts").assertExists()
        composeTestRule.onNodeWithText("Tasks").assertExists()
        composeTestRule.onNodeWithText("Chat").assertExists()

        // Click "My Drafts" Tab row
        composeTestRule.onNodeWithText("My Drafts").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("PERSONAL DRAFTING PAD").assertExists()
        composeTestRule.onNodeWithText("Encrypted private drafts. Promote to the Team Space to collaborate.").assertExists()

        // Click "Chat" Tab row
        composeTestRule.onNodeWithText("Chat").performClick()
        composeTestRule.waitForIdle()
        // Verify Chat is locked pending agreement signature
        composeTestRule.onNodeWithText("Communications Locked").assertExists()
    }

    @Test
    fun syndicate_matchmaking_and_sybil_throttling_test() {
        val userProfile = com.example.data.model.UserProfile(
            id = "me",
            email = "test@creator.co",
            displayName = "Group Creator",
            primarySpecialty = "Creative Director",
            systemRole = "ADMIN",
            isVerifiedPro = true
        )
        composeTestRule.setContent {
            MyApplicationTheme {
                SyndicateScreen(
                    discoveryViewModel = discoveryViewModel,
                    globalViewModel = globalViewModel,
                    authViewModel = authViewModel,
                    userProfile = userProfile
                )
            }
        }

        composeTestRule.waitForIdle()

        // Print the tree
        try {
            composeTestRule.onRoot().printToLog("DEBUG_TREE")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 1. Assert pre-populated proposals exist using robust wait for paging async load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("TechPulse Syndicate").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("TechPulse Syndicate").assertExists()
        composeTestRule.onNodeWithTag("proposals_list").performScrollToNode(hasTestTag("pitch_button_proj_002"))
        composeTestRule.onNodeWithText("Cosmic Chronicles").assertExists()

        // 2. Click "Pitch Your Talent" on Cosmic Chronicles (which is not authored by "me")
        composeTestRule.onNodeWithTag("pitch_button_proj_002").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 3. Complete and submit the pitch dialog
        composeTestRule.onNodeWithTag("pitch_cover_input").performTextInput("Hi, I would love to write for you! Here is my stellar script portfolio.")
        composeTestRule.onNodeWithTag("pitch_portfolio_input").performTextInput("https://cosmic.portfolio.com")
        composeTestRule.onNodeWithTag("pitch_submit_button").performClick()
        composeTestRule.waitForIdle()

        // Attempt 1 was processed. Now let's directly trigger Sybil Throttling on discoveryViewModel to test the overlay!
        discoveryViewModel.securityState.value = com.example.data.model.SecurityState.SybilThrottled
        composeTestRule.waitForIdle()

        // 4. Assert Sybil warning overlay is displayed
        composeTestRule.onNodeWithTag("sybil_throttling_overlay", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("RATE LIMIT EXCEEDED", useUnmergedTree = true).assertExists()

        // 5. Dismiss the overlay
        composeTestRule.onNodeWithTag("return_to_board_button", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        // Verify overlay is closed
        composeTestRule.onNodeWithTag("sybil_throttling_overlay", useUnmergedTree = true).assertDoesNotExist()
    }
}
