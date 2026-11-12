package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
  primary = JciNavy,
  onPrimary = Color.White,
  primaryContainer = JciNavyLight,
  onPrimaryContainer = Color.White,
  secondary = JciBlue,
  onSecondary = Color.White,
  secondaryContainer = JciBlueLight,
  onSecondaryContainer = JciNavyDark,
  tertiary = JciBlueCyan,
  background = JciBackground,
  onBackground = JciTextPrimary,
  surface = JciSurface,
  onSurface = JciTextPrimary,
  surfaceVariant = JciSurfaceVariant,
  onSurfaceVariant = JciTextSecondary,
  outline = JciBorder
)

private val DarkColorScheme = darkColorScheme(
  primary = JciBlueCyan,
  onPrimary = JciNavyDark,
  primaryContainer = JciNavy,
  onPrimaryContainer = Color.White,
  secondary = JciBlue,
  onSecondary = Color.White,
  background = JciNavyDark,
  onBackground = Color.White,
  surface = Color(0xFF131D3B),
  onSurface = Color.White,
  surfaceVariant = Color(0xFF1C284E),
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Color(0xFF334155)
)

@Composable
fun MyApplicationTheme(
  // Les maquettes n'existent qu'en thème clair : on ne suit pas le mode sombre du téléphone.
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false, // Preserve branded JCI colors
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
