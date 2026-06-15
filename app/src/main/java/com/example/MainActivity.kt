package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBackground
import com.example.ui.theme.AccentRed
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContent(viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val showSplash by viewModel.showSplash.collectAsState()
    val isOnboarded by viewModel.isOnboarded.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    // Handle Toasts reactively
    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.resetToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        AnimatedContent(
            targetState = if (showSplash) "SPLASH" else if (!isOnboarded) "ONBOARDING" else "DASHBOARD",
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "app_root_navigation"
        ) { state ->
            when (state) {
                "SPLASH" -> SplashScreen(
                    viewModel = viewModel,
                    onSplashComplete = { viewModel.completeSplash() }
                )
                "ONBOARDING" -> OnboardingScreen(viewModel = viewModel)
                "DASHBOARD" -> CreatorCoOpDashboard(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CreatorCoOpDashboard(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceColor,
                contentColor = AccentRed,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .testTag("app_navigation_bar")
            ) {
                // Tab 1: The Square Forum
                NavigationBarItem(
                    selected = currentTab == "SQUARE",
                    onClick = { viewModel.currentTab.value = "SQUARE" },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "The Square") },
                    label = { Text("The Square", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentRed,
                        selectedTextColor = AccentRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = AccentRed.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_square_tab")
                )

                // Tab 2: Syndicate Matchmaker
                NavigationBarItem(
                    selected = currentTab == "SYNDICATE",
                    onClick = { viewModel.currentTab.value = "SYNDICATE" },
                    icon = { Icon(Icons.Default.Workspaces, contentDescription = "Syndicate") },
                    label = { Text("Syndicate", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentRed,
                        selectedTextColor = AccentRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = AccentRed.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_syndicate_tab")
                )

                // Tab 3: Matched Workspaces
                NavigationBarItem(
                    selected = currentTab == "WORKSPACES",
                    onClick = { viewModel.currentTab.value = "WORKSPACES" },
                    icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "Workspaces") },
                    label = { Text("Workspaces", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentRed,
                        selectedTextColor = AccentRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = AccentRed.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_workspaces_tab")
                )

                // Tab 4: Wallet & Profile
                NavigationBarItem(
                    selected = currentTab == "PROFILE",
                    onClick = { viewModel.currentTab.value = "PROFILE" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile & Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentRed,
                        selectedTextColor = AccentRed,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = AccentRed.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_profile_tab")
                )
            }
        },
        containerColor = PrimaryBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "dashboard_sub_navigation"
            ) { tab ->
                when (tab) {
                    "SQUARE" -> SquareScreen(viewModel = viewModel)
                    "SYNDICATE" -> SyndicateScreen(viewModel = viewModel)
                    "WORKSPACES" -> WorkspaceScreen(viewModel = viewModel)
                    "PROFILE" -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}
