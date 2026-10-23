package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class CalculatorColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val numberKey: Color,
    val numberText: Color,
    val operatorKey: Color,
    val operatorText: Color,
    val actionAccent: Color,
    val actionText: Color,
    val specialKey: Color,
    val specialText: Color,
    val errorColor: Color
)

val LocalCalculatorColors = staticCompositionLocalOf {
    CalculatorColors(
        background = DarkBackground,
        surface = DarkSurface,
        surfaceElevated = DarkSurfaceElevated,
        numberKey = DarkNumberKey,
        numberText = DarkNumberText,
        operatorKey = DarkOperatorKey,
        operatorText = DarkOperatorText,
        actionAccent = DarkActionAccent,
        actionText = DarkActionText,
        specialKey = DarkSpecialKey,
        specialText = DarkSpecialText,
        errorColor = DarkErrorColor
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkActionAccent,
    onPrimary = DarkActionText,
    secondary = DarkOperatorText,
    onSecondary = DarkNumberText,
    background = DarkBackground,
    onBackground = DarkNumberText,
    surface = DarkSurface,
    onSurface = DarkNumberText,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkSpecialText,
    error = DarkErrorColor
)

private val LightColorScheme = lightColorScheme(
    primary = LightActionAccent,
    onPrimary = LightActionText,
    secondary = LightOperatorText,
    onSecondary = LightNumberText,
    background = LightBackground,
    onBackground = LightNumberText,
    surface = LightSurface,
    onSurface = LightNumberText,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightSpecialText,
    error = LightErrorColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent tailored palette for sleek calculator style
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

    val calcColors = if (darkTheme) {
        CalculatorColors(
            background = DarkBackground,
            surface = DarkSurface,
            surfaceElevated = DarkSurfaceElevated,
            numberKey = DarkNumberKey,
            numberText = DarkNumberText,
            operatorKey = DarkOperatorKey,
            operatorText = DarkOperatorText,
            actionAccent = DarkActionAccent,
            actionText = DarkActionText,
            specialKey = DarkSpecialKey,
            specialText = DarkSpecialText,
            errorColor = DarkErrorColor
        )
    } else {
        CalculatorColors(
            background = LightBackground,
            surface = LightSurface,
            surfaceElevated = LightSurfaceElevated,
            numberKey = LightNumberKey,
            numberText = LightNumberText,
            operatorKey = LightOperatorKey,
            operatorText = LightOperatorText,
            actionAccent = LightActionAccent,
            actionText = LightActionText,
            specialKey = LightSpecialKey,
            specialText = LightSpecialText,
            errorColor = LightErrorColor
        )
    }

    CompositionLocalProvider(LocalCalculatorColors provides calcColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
