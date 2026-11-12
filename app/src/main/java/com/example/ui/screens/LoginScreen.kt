package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.theme.JciBlue
import com.example.ui.theme.JciErrorRed
import com.example.ui.theme.JciFieldBg
import com.example.ui.theme.JciInputBorder
import com.example.ui.theme.InterFamily
import com.example.ui.theme.JciNavy
import com.example.ui.theme.JciPlaceholder
import com.example.ui.theme.JciTextSecondary

@Composable
fun LoginScreen(
  uiState: MainUiState,
  viewModel: MainViewModel,
  onNavigateBack: () -> Unit
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  val isButtonEnabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoggingIn
  val focusManager = LocalFocusManager.current

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .background(JciFieldBg),
    color = JciFieldBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
      // 1. Bouton retour : cercle de 48 px, bordure 1 px #D9DFEC, fond blanc, flèche marine
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .border(1.dp, JciInputBorder, CircleShape)
          .background(Color.White)
          .clickable { onNavigateBack() }
          .testTag("login_back_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Retour",
          tint = JciNavy,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      // 2. Titre h1 centré : « Bienvenue », marine, 30 px, graisse 800, letter-spacing:-0.5px
      Text(
        text = "Bienvenue",
        color = JciNavy,
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Sous-titre centré : « Connectez-vous pour contrôler les entrées. »
      Text(
        text = "Connectez-vous pour contrôler les entrées.",
        color = JciTextSecondary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(36.dp))

      // 4. Champ « Adresse e-mail » avec <label> AU-DESSUS (jamais flottant)
      Text(
        text = "Adresse e-mail",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = JciNavy,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      val emailInteractionSource = remember { MutableInteractionSource() }
      val emailIsFocused by emailInteractionSource.collectIsFocusedAsState()

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color.White)
          .border(
            width = if (emailIsFocused) 2.dp else 1.dp,
            color = if (emailIsFocused) JciBlue else JciInputBorder,
            shape = RoundedCornerShape(14.dp)
          )
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        BasicTextField(
          value = email,
          onValueChange = { email = it },
          singleLine = true,
          enabled = !uiState.isLoggingIn,
          textStyle = TextStyle(
            fontFamily = InterFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = JciNavy
          ),
          cursorBrush = SolidColor(JciBlue),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
          ),
          interactionSource = emailInteractionSource,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("email_input"),
          decorationBox = { innerTextField ->
            if (email.isEmpty()) {
              Text(
                text = "prenom.nom@jci-benin.org",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = JciPlaceholder
              )
            }
            innerTextField()
          }
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 5. Champ « Mot de passe » avec <label> AU-DESSUS
      Text(
        text = "Mot de passe",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = JciNavy,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      val passwordInteractionSource = remember { MutableInteractionSource() }
      val passwordIsFocused by passwordInteractionSource.collectIsFocusedAsState()

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color.White)
          .border(
            width = if (passwordIsFocused) 2.dp else 1.dp,
            color = if (passwordIsFocused) JciBlue else JciInputBorder,
            shape = RoundedCornerShape(14.dp)
          )
          .padding(start = 16.dp, end = 4.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
              value = password,
              onValueChange = { password = it },
              singleLine = true,
              enabled = !uiState.isLoggingIn,
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              textStyle = TextStyle(
            fontFamily = InterFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = JciNavy
              ),
              cursorBrush = SolidColor(JciBlue),
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
              ),
              keyboardActions = KeyboardActions(
                onDone = {
                  focusManager.clearFocus()
                  if (isButtonEnabled) {
                    viewModel.login(email, password)
                  }
                }
              ),
              interactionSource = passwordInteractionSource,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("password_input"),
              decorationBox = { innerTextField ->
                if (password.isEmpty()) {
                  Text(
                    text = "Votre mot de passe",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = JciPlaceholder
                  )
                }
                innerTextField()
              }
            )
          }

          // Bouton œil (48 px tactile)
          IconButton(
            onClick = { passwordVisible = !passwordVisible },
            modifier = Modifier.size(48.dp)
          ) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe",
              tint = JciTextSecondary,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // 6. Bouton « Se connecter » : 56 px, rayon 14 px, fond marine, texte blanc 17 px 700
      Button(
        onClick = {
          focusManager.clearFocus()
          viewModel.login(email, password)
        },
        enabled = isButtonEnabled,
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("login_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = JciNavy,
          contentColor = Color.White,
          disabledContainerColor = JciNavy.copy(alpha = 0.4f),
          disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
      ) {
        if (uiState.isLoggingIn) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
              color = Color.White,
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Connexion…",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        } else {
          Text(
            text = "Se connecter",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      // 7. Message d'erreur : une seule phrase en rouge #DC2626 avec petite icône
      if (uiState.loginError != null) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = JciErrorRed,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = uiState.loginError,
            color = JciErrorRed,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))
      Spacer(modifier = Modifier.height(28.dp))

      // 8. Encadré d'information en bas : fond #F4F6FB, rayon 14 px, padding 16 px, icône « i » bleue #1E88E5
      Surface(
        color = JciFieldBg,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = JciBlue,
            modifier = Modifier
              .size(20.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "Vous n’avez pas de compte ? Contactez l’administration de JCI Comé Excellence.",
            fontSize = 14.sp,
            color = JciTextSecondary,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Normal
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 9. Version tout en bas centrée : « JCI Stafs · v1.0.0 »
      Text(
        text = "JCI Stafs · v1.0.0",
        color = JciPlaceholder,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}
