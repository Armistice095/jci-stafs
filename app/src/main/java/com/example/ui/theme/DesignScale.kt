package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/** Largeur de référence des maquettes (en dp). */
private const val DESIGN_WIDTH_DP = 300f

/**
 * Les maquettes ont été dessinées sur un téléphone de 300 de large. Ce composant agrandit
 * proportionnellement TOUT (marges, boutons, textes) selon la largeur réelle de l'écran,
 * pour retrouver exactement les proportions des maquettes sur chaque téléphone.
 * La taille de police choisie par l'utilisateur dans Android (fontScale) est conservée.
 */
@Composable
fun DesignScale(content: @Composable () -> Unit) {
  val density = LocalDensity.current
  val widthDp = LocalConfiguration.current.screenWidthDp
  val factor = (widthDp / DESIGN_WIDTH_DP).coerceIn(1f, 1.35f)
  CompositionLocalProvider(
    LocalDensity provides Density(density.density * factor, density.fontScale)
  ) {
    content()
  }
}
