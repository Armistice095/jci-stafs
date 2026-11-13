package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.theme.InterFamily
import com.example.ui.theme.JciBlue
import com.example.ui.theme.JciBlueLight
import com.example.ui.theme.JciFieldBg
import com.example.ui.theme.JciInputBorder
import com.example.ui.theme.JciNavy
import com.example.ui.theme.JciNavyLight
import com.example.ui.theme.JciPlaceholder
import com.example.ui.theme.JciTextSecondary

private val FieldIdleBg = Color(0xFFF8FAFD)
private val ErrorBg = Color(0xFFFEF2F2)
private val ErrorText = Color(0xFFB91C1C)
private val HeroSubtitle = Color(0xFFC9D3F7)

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
    modifier = Modifier.fillMaxSize(),
    color = JciFieldBg
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .verticalScroll(rememberScrollState())
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {

        // 1. Bandeau de marque (dégradé marine, coins bas arrondis)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(Brush.linearGradient(listOf(JciNavy, JciNavyLight)))
        ) {
          // Coins de viseur décoratifs, très discrets
          Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            val color = Color.White.copy(alpha = 0.08f)
            val w = size.width
            val tr = Path().apply {
              moveTo(w - 140.dp.toPx(), 40.dp.toPx())
              lineTo(w - 60.dp.toPx(), 40.dp.toPx())
              quadraticBezierTo(w - 38.dp.toPx(), 40.dp.toPx(), w - 38.dp.toPx(), 62.dp.toPx())
              lineTo(w - 38.dp.toPx(), 120.dp.toPx())
            }
            val bl = Path().apply {
              moveTo(30.dp.toPx(), 190.dp.toPx())
              lineTo(30.dp.toPx(), 250.dp.toPx())
              quadraticBezierTo(30.dp.toPx(), 272.dp.toPx(), 52.dp.toPx(), 272.dp.toPx())
              lineTo(110.dp.toPx(), 272.dp.toPx())
            }
            drawPath(tr, color, style = stroke)
            drawPath(bl, color, style = stroke)
          }
        }

        // Bouton retour
        Box(
          modifier = Modifier
            .padding(start = 20.dp, top = 20.dp)
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
            .clickable { onNavigateBack() }
            .testTag("login_back_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Retour",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }

        // Logo + nom
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Image(
            painter = painterResource(id = R.drawable.ic_jci_logo),
            contentDescription = "Logo JCI Stafs",
            modifier = Modifier.size(76.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "JCI Stafs",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.2).sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "JCI Comé Excellence",
            color = HeroSubtitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }

        // 2. Carte blanche qui chevauche le bandeau
        Column(
          modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 236.dp)
            .fillMaxWidth()
            .shadow(
              elevation = 16.dp,
              shape = RoundedCornerShape(28.dp),
              ambientColor = JciNavy.copy(alpha = 0.25f),
              spotColor = JciNavy.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 24.dp)
        ) {
          Text(
            text = "Bienvenue",
            color = JciNavy,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Connectez-vous pour contrôler les entrées.",
            color = JciTextSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp
          )

          Spacer(modifier = Modifier.height(24.dp))

          // Champ e-mail
          Text(
            text = "Adresse e-mail",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = JciNavy,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          val emailInteractionSource = remember { MutableInteractionSource() }
          val emailIsFocused by emailInteractionSource.collectIsFocusedAsState()

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (emailIsFocused) Color.White else FieldIdleBg)
              .border(
                width = if (emailIsFocused) 2.dp else 1.5.dp,
                color = if (emailIsFocused) JciBlue else JciInputBorder,
                shape = RoundedCornerShape(16.dp)
              )
              .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.MailOutline,
                contentDescription = null,
                tint = JciTextSecondary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
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
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Champ mot de passe
          Text(
            text = "Mot de passe",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = JciNavy,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          val passwordInteractionSource = remember { MutableInteractionSource() }
          val passwordIsFocused by passwordInteractionSource.collectIsFocusedAsState()

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (passwordIsFocused) Color.White else FieldIdleBg)
              .border(
                width = if (passwordIsFocused) 2.dp else 1.5.dp,
                color = if (passwordIsFocused) JciBlue else JciInputBorder,
                shape = RoundedCornerShape(16.dp)
              )
              .padding(start = 16.dp, end = 4.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = JciTextSecondary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
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

          Spacer(modifier = Modifier.height(24.dp))

          // Bouton « Se connecter »
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
            shape = RoundedCornerShape(16.dp),
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Se connecter",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }

          // Message d'erreur
          if (uiState.loginError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ErrorBg)
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Start
            ) {
              Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = ErrorText,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = uiState.loginError,
                color = ErrorText,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // 3. Aide : pas de compte
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 36.dp),
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(JciBlueLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF1565C0),
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Pas de compte ? Contactez l’administration de JCI Comé Excellence.",
          fontSize = 14.sp,
          color = JciTextSecondary,
          lineHeight = 21.sp,
          modifier = Modifier.padding(top = 5.dp)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      // 4. Version
      Text(
        text = "JCI Stafs · v1.0.0",
        color = JciPlaceholder,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
