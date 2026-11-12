package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JciBlue
import com.example.ui.theme.JciNavy
import com.example.ui.theme.JciTextLightNavy

@Composable
fun WelcomeScreen(
  onNavigateToLogin: () -> Unit
) {
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(JciNavy)
  ) {
    val screenHeight = maxHeight
    val screenWidth = maxWidth

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Zone photo (flex: 1, min-height: 0, overflow: hidden, relative)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.TopCenter
      ) {
        // Halo lumineux derrière la tête de l'agent : cercle radial-gradient bleu #1E88E5 à 55%
        Box(
          modifier = Modifier
            .offset(y = 30.dp)
            .size(340.dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  JciBlue.copy(alpha = 0.55f),
                  JciBlue.copy(alpha = 0.25f),
                  Color.Transparent
                )
              )
            )
        )

        // Photo de l'agent : centrée horizontalement, ancrée en haut, max 350dp
        Image(
          painter = painterResource(id = R.drawable.agent_jci),
          contentDescription = "Agent JCI souriant devant son téléphone",
          modifier = Modifier
            .padding(top = 28.dp)
            .widthIn(max = 350.dp)
            .fillMaxWidth(0.92f)
            .fillMaxSize(),
          contentScale = ContentScale.Fit,
          alignment = Alignment.TopCenter
        )

        // Dégradé de fondu en bas de la zone photo pour fondre le bas du corps dans le fond marine
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Transparent,
                  JciNavy.copy(alpha = 0.5f),
                  JciNavy.copy(alpha = 0.88f),
                  JciNavy
                )
              )
            )
        )
      }

      // 2. Bloc texte centré, remonté sur le dégradé (-24dp)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .offset(y = (-24).dp)
          .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Titre sur deux lignes : « Chaque entrée, » / « en un scan. »
        Text(
          text = "Chaque entrée,\nen un scan.",
          color = Color.White,
          fontSize = 34.sp,
          fontWeight = FontWeight.ExtraBold,
          lineHeight = 38.sp,
          letterSpacing = (-0.5).sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Paragraphe descriptif
        Text(
          text = "Contrôlez les billets de vos événements, simplement et en toute fiabilité.",
          color = JciTextLightNavy,
          fontSize = 16.sp,
          fontWeight = FontWeight.Normal,
          lineHeight = 24.sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton « Se connecter » : fond blanc, texte marine
        Button(
          onClick = onNavigateToLogin,
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("welcome_login_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = JciNavy
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
          Text(
            text = "Se connecter",
            color = JciNavy,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
