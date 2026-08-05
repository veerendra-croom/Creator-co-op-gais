package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ObsidianColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentBlue,
    tertiary = ColorSuccess,
    background = PrimaryBackground,
    surface = SurfaceColor,
    onPrimary = Color(0xFF090B0F),
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = ColorDivider,
    error = ColorError
)

private val CyberEmeraldColorScheme = darkColorScheme(
    primary = NeonEmerald,
    secondary = NeonEmerald,
    tertiary = AccentBlue,
    background = Color(0xFF06120E),
    surface = Color(0xFF0C1A15),
    onPrimary = Color.Black,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = Color(0xFF16382B),
    error = ColorError
)

private val AmberSunsetColorScheme = darkColorScheme(
    primary = CrispAmber,
    secondary = CrispAmber,
    tertiary = AccentRed,
    background = Color(0xFF120E08),
    surface = Color(0xFF1A140B),
    onPrimary = Color.Black,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = Color(0xFF382A16),
    error = ColorError
)

private val LightColorScheme = lightColorScheme(                
    primary = Color(0xFF0284C7),
    secondary = AccentBlue,
    tertiary = ColorSuccess,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    outline = Color(0xFFCBD5E1),
    error = ColorError
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "OBSIDIAN",
    darkTheme: Boolean = isSystemInDarkTheme(), 
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode.uppercase() == "CYBER_EMERALD" -> CyberEmeraldColorScheme
        themeMode.uppercase() == "AMBER_SUNSET" -> AmberSunsetColorScheme
        themeMode.uppercase() == "LIGHT" -> LightColorScheme
        else -> ObsidianColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

