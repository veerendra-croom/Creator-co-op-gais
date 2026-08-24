package com.example.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Splash : Screen()
    
    @Serializable
    data object Landing : Screen()
    
    @Serializable
    data class Auth(val initialMode: String = "LOGIN") : Screen()
    
    @Serializable
    data object Dashboard : Screen()
}
