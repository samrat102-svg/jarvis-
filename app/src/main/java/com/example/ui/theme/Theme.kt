package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = ArcCyan,
    onPrimary = Color(0xFF001F29),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = ArcCyanGlow,
    
    secondary = StarkGold,
    onSecondary = Color(0xFF261A00),
    secondaryContainer = Color(0xFF4D3800),
    onSecondaryContainer = StarkGold,
    
    tertiary = ArcCyanGlow,
    onTertiary = Color(0xFF002022),
    
    background = JarvisBackground,
    onBackground = TextPrimary,
    
    surface = JarvisSurface,
    onSurface = TextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    error = JarvisRedAlert,
    onError = Color.White,
    
    outline = BorderCyan,
    outlineVariant = Color(0xFF14284B)
)

@Composable
fun JarvisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    JarvisTheme(content = content)
}
