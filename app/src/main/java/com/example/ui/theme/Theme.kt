package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IvoryLightColorScheme = lightColorScheme(
    primary = MedicineGreen,
    onPrimary = Color.White,
    primaryContainer = MedicineGreenSoft,
    onPrimaryContainer = MedicineGreenDarkText,
    secondary = WaterBlue,
    onSecondary = Color.White,
    secondaryContainer = WaterCardSurface,
    onSecondaryContainer = InkPrimary,
    background = IvoryBackground,
    onBackground = InkPrimary,
    surface = IvorySurface,
    onSurface = InkPrimary,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = IvoryBorder,
)

@Composable
fun SeniorCareTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = IvoryLightColorScheme,
        typography = Typography,
        content = content,
    )
}
