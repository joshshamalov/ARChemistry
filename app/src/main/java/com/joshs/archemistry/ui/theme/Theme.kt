package com.joshs.archemistry.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ARChemistry unified color scheme
private val ARChemistryColorScheme = darkColorScheme(
    primary = ARPrimaryBlue,
    secondary = ARSecondaryPurple,
    tertiary = ARButtonGray,
    background = ARDarkBackground,
    surface = ARCardBackground,
    onPrimary = ARTextPrimary,
    onSecondary = ARTextPrimary,
    onTertiary = ARTextPrimary,
    onBackground = ARTextPrimary,
    onSurface = ARTextPrimary,
    error = ARStatusRed,
    onError = ARTextPrimary
)

// Legacy color schemes (kept for compatibility)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun ARChemistryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled by default to use our custom theme
    content: @Composable () -> Unit
) {
    // Always use our custom ARChemistryColorScheme for a consistent look
    val colorScheme = ARChemistryColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}