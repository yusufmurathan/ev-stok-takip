package com.evstok.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AmoledDarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = OnMint,
    primaryContainer = MintContainer,
    onPrimaryContainer = MintOnContainer,
    inversePrimary = InversePrimary,
    secondary = Lavender,
    onSecondary = OnLavender,
    secondaryContainer = LavenderContainer,
    onSecondaryContainer = LavenderOnContainer,
    tertiary = AmberAccent,
    onTertiary = OnAmber,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberOnContainer,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = AmoledBlack,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceChip,
    onSurfaceVariant = TextSecondary,
    surfaceTint = Mint,
    surfaceDim = SurfaceLow,
    surfaceContainer = SurfaceRaised,
    surfaceContainerHigh = SurfaceRaisedHigh,
    surfaceContainerHighest = SurfaceRaisedHighest,
    surfaceContainerLow = SurfaceLow,
    surfaceContainerLowest = AmoledBlack,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    error = StatusRed,
    onError = Color(0xFF4A0D10),
    errorContainer = Color(0xFF3B1416),
    onErrorContainer = Color(0xFFFFD9D6),
    outline = OutlineColor,
    outlineVariant = OutlineVariantColor,
    scrim = AmoledBlack
)

@Composable
fun EvStokTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    MaterialTheme(colorScheme = AmoledDarkColors) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
        content()
    }
}
