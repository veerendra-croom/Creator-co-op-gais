package com.example.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Splash : Screen()
    
    @Serializable
    data object Auth : Screen()
    
    @Serializable
    data object Dashboard : Screen()
}
