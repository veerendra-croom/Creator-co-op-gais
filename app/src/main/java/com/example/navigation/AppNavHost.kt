package com.example.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.CreatorCoOpDashboard

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodels.*
import androidx.compose.ui.platform.LocalContext
import com.example.CreatorCoopApp

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val application = LocalContext.current.applicationContext as CreatorCoopApp
    val factory = AppViewModelFactory(application)
    val globalViewModel: GlobalViewModel = viewModel(factory = factory)
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val showSplash by globalViewModel.showSplash.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState(initial = null)

    androidx.compose.runtime.LaunchedEffect(currentUserId, showSplash) {
        if (!showSplash) {
            val currentDestination = navController.currentBackStackEntry?.destination?.route
            val targetRoute = if (currentUserId != null) Screen.Dashboard::class.qualifiedName else Screen.Auth::class.qualifiedName
            
            if (currentDestination != targetRoute) {
                val route = if (currentUserId != null) Screen.Dashboard else Screen.Auth
                navController.navigate(route) {
                    // Pop up to the start destination (Splash) and remove it
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
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

        composable<Screen.Auth> {
            AuthScreen(authViewModel = authViewModel)
        }

        composable<Screen.Dashboard> {
            CreatorCoOpDashboard(
                globalViewModel = globalViewModel,
                authViewModel = authViewModel
            )
        }
    }
}
