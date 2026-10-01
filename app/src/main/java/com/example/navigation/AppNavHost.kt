package com.example.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.screens.*
import com.example.CreatorCoOpDashboard

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodels.*
import androidx.compose.ui.platform.LocalContext
import com.example.CreatorCoopApp

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    globalViewModel: GlobalViewModel? = null
) {
    val application = LocalContext.current.applicationContext as CreatorCoopApp
    val factory = AppViewModelFactory(application)
    val globalViewModel: GlobalViewModel = globalViewModel ?: viewModel(factory = factory)
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val showSplash by globalViewModel.showSplash.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState(initial = null)
    var showStandaloneOnboarding by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(currentUserId, showSplash) {
        if (!showSplash) {
            val currentDestination = navController.currentBackStackEntry?.destination?.route
            if (currentUserId != null) {
                if (currentDestination != Screen.Dashboard::class.qualifiedName) {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                // If logged out or starting fresh and not on Landing or Auth, navigate to Landing
                if (currentDestination != Screen.Landing::class.qualifiedName && 
                    currentDestination?.contains("Auth") != true) {
                    navController.navigate(Screen.Landing) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        enterTransition = { fadeIn(animationSpec = tween(500)) },
        exitTransition = { fadeOut(animationSpec = tween(500)) },
        popEnterTransition = { fadeIn(animationSpec = tween(500)) },
        popExitTransition = { fadeOut(animationSpec = tween(500)) }
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                globalViewModel = globalViewModel,
                onSplashComplete = {
                    globalViewModel.completeSplash()
                }
            )
        }

        composable<Screen.Landing> {
            if (showStandaloneOnboarding) {
                OnboardingScreen(
                    globalViewModel = globalViewModel,
                    onComplete = { showStandaloneOnboarding = false }
                )
            } else {
                LandingScreen(
                    globalViewModel = globalViewModel,
                    authViewModel = authViewModel,
                    onNavigateToSignIn = {
                        navController.navigate(Screen.Auth("LOGIN"))
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.Auth("REGISTER"))
                    },
                    onViewOnboarding = {
                        showStandaloneOnboarding = true
                    }
                )
            }
        }

        composable<Screen.Auth> { backStackEntry ->
            val authArgs = backStackEntry.toRoute<Screen.Auth>()
            AuthScreen(
                authViewModel = authViewModel,
                initialMode = authArgs.initialMode,
                onNavigateToLanding = {
                    navController.navigate(Screen.Landing) {
                        popUpTo(Screen.Landing) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Screen.Dashboard> {
            CreatorCoOpDashboard(
                globalViewModel = globalViewModel,
                authViewModel = authViewModel
            )
        }
    }
}
