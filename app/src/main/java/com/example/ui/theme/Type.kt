package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

/** Police de la charte JCI Stafs : Inter (licence SIL OFL), embarquée dans l'application. */
val InterFamily = FontFamily(
  Font(R.font.inter_regular, FontWeight.Normal),
  Font(R.font.inter_medium, FontWeight.Medium),
  Font(R.font.inter_semibold, FontWeight.SemiBold),
  Font(R.font.inter_bold, FontWeight.Bold)
)

private val base = Typography()

// Tous les styles passent en Inter, sans espacement de lettres ajouté (les maquettes n'en ont pas).
val Typography = Typography(
  displayLarge = base.displayLarge.copy(fontFamily = InterFamily),
  displayMedium = base.displayMedium.copy(fontFamily = InterFamily),
  displaySmall = base.displaySmall.copy(fontFamily = InterFamily),
  headlineLarge = base.headlineLarge.copy(fontFamily = InterFamily),
  headlineMedium = base.headlineMedium.copy(fontFamily = InterFamily),
  headlineSmall = base.headlineSmall.copy(fontFamily = InterFamily),
  titleLarge = base.titleLarge.copy(fontFamily = InterFamily),
  titleMedium = base.titleMedium.copy(fontFamily = InterFamily),
  titleSmall = base.titleSmall.copy(fontFamily = InterFamily),
  bodyLarge = base.bodyLarge.copy(fontFamily = InterFamily, letterSpacing = 0.sp),
  bodyMedium = base.bodyMedium.copy(fontFamily = InterFamily, letterSpacing = 0.sp),
  bodySmall = base.bodySmall.copy(fontFamily = InterFamily, letterSpacing = 0.sp),
  labelLarge = base.labelLarge.copy(fontFamily = InterFamily, letterSpacing = 0.sp),
  labelMedium = base.labelMedium.copy(fontFamily = InterFamily, letterSpacing = 0.sp),
  labelSmall = base.labelSmall.copy(fontFamily = InterFamily, letterSpacing = 0.sp)
)
