package com.echoscript.voice.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val PrimaryIndigo = Color(0xFF4F46E5)
val SecondaryViolet = Color(0xFF7C3AED)
val DarkBackground = Color(0xFF0F172A)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    secondary = Color(0xFFA78BFA),
    tertiary = Color(0xFF38BDF8),
    background = DarkBackground,
    surface = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    secondary = SecondaryViolet,
    tertiary = Color(0xFF0284C7),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun EchoScriptVoiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
