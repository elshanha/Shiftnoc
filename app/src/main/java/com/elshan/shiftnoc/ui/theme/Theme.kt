package com.elshan.shiftnoc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA3C9FE),
    onPrimary = Color(0xFF00315B),
    secondaryContainer = Color(0xFF19284E),
    onSecondaryContainer = Color(0xFFC8E6FF),
    background = Color(0xFF111418),
    secondary = Color(0xFFE1E2E8),
    onSecondary = Color(0xFFE1E2E8).copy(alpha = .6f),
    outline = Color(0xFF8D9199),
    surfaceContainer = Color(0xFF272A2F),
    error = Color(0xFFFF0332),
    surface = Color(0xFFCEBDFE),
    onSurface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF45FF4C),
    tertiary = Color(0xFFFFC107),
    )

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF39608F),
    onPrimary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC8E6FF),
    onSecondaryContainer = Color(0xFF001E2E),
    background = Color(0xFFF8F9FF),
    secondary = Color(0xFF191C20),
    onSecondary = Color(0xFF191C20).copy(alpha = .6f),
    outline = Color(0xFF73777F),
    surfaceContainer = Color(0xFFE7E8EE),
    error = Color(0xFFFF2525),
    surface = Color(0xFF64558F),
    onSurface = Color(0xFFE1E2E8),
    onBackground = Color(0xFF45FF4C),
    tertiary = Color(0xFFBE7100)
    )


@Composable
fun ShiftnocTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                darkTheme.not()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                darkTheme.not()
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

