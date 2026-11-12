package com.example.ui.screens

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckinResultStatus
import com.example.ui.ScanResultUiState

private val ResultGreen = Color(0xFF16A34A)
private val ResultAmber = Color(0xFFF59E0B)
private val ResultRed = Color(0xFFDC2626)
private val ResultGray = Color(0xFF6B7280)
private val AmberText = Color(0xFF3B1D00)
private val AmberSub = Color(0xFF5A3000)

/** Couleurs et icône d'un résultat, selon les maquettes validées. */
private data class ResultStyle(
  val background: Color,
  val title: Color,
  val sub: Color,
  val card: Color,
  val icon: ImageVector,
  val iconTint: Color,
  val pillBackground: Color,
  val pillText: Color
)

private fun styleFor(status: CheckinResultStatus): ResultStyle = when (status) {
  CheckinResultStatus.ADMITTED -> ResultStyle(
    ResultGreen, Color.White, Color(0xFFDCFCE7), Color(0x29FFFFFF),
    Icons.Default.Check, ResultGreen, Color.White, Color(0xFF15803D)
  )
  CheckinResultStatus.ALREADY_USED -> ResultStyle(
    ResultAmber, AmberText, AmberSub, Color(0x80FFFFFF),
    Icons.Default.AccessTime, Color(0xFFB45309), AmberText, Color(0xFFFDE68A)
  )
  CheckinResultStatus.WRONG_EVENT -> ResultStyle(
    ResultRed, Color.White, Color(0xFFFEE2E2), Color(0x29FFFFFF),
    Icons.Default.EventBusy, ResultRed, Color.White, Color(0xFFB91C1C)
  )
  CheckinResultStatus.CANCELLED -> ResultStyle(
    ResultRed, Color.White, Color(0xFFFEE2E2), Color(0x29FFFFFF),
    Icons.Default.Close, ResultRed, Color.White, Color(0xFFB91C1C)
  )
  CheckinResultStatus.EVENT_NOT_ACTIVE -> ResultStyle(
    ResultRed, Color.White, Color(0xFFFEE2E2), Color(0x29FFFFFF),
    Icons.Default.EventBusy, ResultRed, Color.White, Color(0xFFB91C1C)
  )
  CheckinResultStatus.INVALID -> ResultStyle(
    ResultRed, Color.White, Color(0xFFFEE2E2), Color(0x29FFFFFF),
    Icons.Default.QuestionMark, ResultRed, Color.White, Color(0xFFB91C1C)
  )
  CheckinResultStatus.NETWORK_ERROR -> ResultStyle(
    ResultGray, Color.White, Color(0xFFE5E7EB), Color(0x29FFFFFF),
    Icons.Default.WifiOff, Color(0xFF4B5563), Color.White, Color(0xFF374151)
  )
}

@Composable
fun ScanResultOverlay(
  result: ScanResultUiState?,
  selectedEventTitle: String,
  isOnline: Boolean,
  onDismiss: () -> Unit,
  onRetry: () -> Unit
) {
  AnimatedVisibility(
    visible = result != null,
    enter = fadeIn(animationSpec = tween(150)),
    exit = fadeOut(animationSpec = tween(150))
  ) {
    if (result == null) return@AnimatedVisibility

    val status = result.status
    val style = styleFor(status)
    val isError = status == CheckinResultStatus.NETWORK_ERROR

    // Animations coupées si l'utilisateur les a désactivées dans Android (équivalent de prefers-reduced-motion).
    val context = LocalContext.current
    val reduceMotion = remember {
      Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    // Entrée : l'icône « pop » avec un léger rebond, puis le texte monte en fondu.
    var shown by remember(result) { mutableStateOf(reduceMotion) }
    LaunchedEffect(result) { shown = true }
    val iconScale by animateFloatAsState(
      targetValue = if (shown) 1f else 0.6f,
      animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
      label = "result_icon_scale"
    )
    val contentAlpha by animateFloatAsState(
      targetValue = if (shown) 1f else 0f,
      animationSpec = tween(durationMillis = 220, delayMillis = 70),
      label = "result_content_alpha"
    )

    // Barre qui se vide pendant les 1,5 s d'affichage
    var progressTarget by remember(result) { mutableFloatStateOf(1f) }
    val animatedProgress by animateFloatAsState(
      targetValue = progressTarget,
      animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
      label = "overlay_timer"
    )
    LaunchedEffect(result) {
      if (!isError) progressTarget = 0f
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(style.background)
        .clickable { onDismiss() }
        .testTag("scan_result_overlay")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Ligne du haut : événement (1 ligne, tronqué) et état de connexion (1 ligne, sans rétrécir)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = selectedEventTitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = style.sub,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                  when {
                    isError -> Color(0xFFFBBF24)
                    status == CheckinResultStatus.ALREADY_USED -> AmberText
                    else -> Color.White
                  }
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isError) "Connexion instable" else "En ligne",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = style.sub,
              maxLines = 1
            )
          }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Grande icône dans un cercle blanc
        Box(
          modifier = Modifier
            .size(112.dp)
            .graphicsLayer {
              scaleX = iconScale
              scaleY = iconScale
            }
            .clip(CircleShape)
            .background(Color.White),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = style.iconTint,
            modifier = Modifier.size(62.dp)
          )
        }

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
              alpha = contentAlpha
              translationY = (1f - contentAlpha) * 24.dp.toPx()
            },
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = result.title,
            fontSize = 34.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 39.sp,
            color = style.title,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(28.dp))

          Surface(
            color = style.card,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              when (status) {
                CheckinResultStatus.ADMITTED -> IdentityBlock(result, style)

                CheckinResultStatus.ALREADY_USED -> {
                  IdentityBlock(result, style)
                  Spacer(modifier = Modifier.height(12.dp))
                  HorizontalDivider(thickness = 0.5.dp, color = Color(0x4D3B1D00))
                  Spacer(modifier = Modifier.height(10.dp))
                  Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Premier passage", fontSize = 12.sp, color = style.sub)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!result.firstCheckedInAt.isNullOrBlank()) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                          imageVector = Icons.Default.AccessTime,
                          contentDescription = null,
                          tint = style.title,
                          modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                          Text(
                            text = result.firstCheckedInAt,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = style.title
                          )
                          if (!result.elapsedLabel.isNullOrBlank()) {
                            Text(text = result.elapsedLabel, fontSize = 12.sp, color = style.sub)
                          }
                        }
                      }
                      Spacer(modifier = Modifier.height(10.dp))
                    }
                    if (!result.firstCheckedInBy.isNullOrBlank()) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                          imageVector = Icons.Default.Person,
                          contentDescription = null,
                          tint = style.title,
                          modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                          text = "Scanné par ${result.firstCheckedInBy}",
                          fontSize = 15.sp,
                          color = style.title
                        )
                      }
                    }
                  }
                }

                CheckinResultStatus.WRONG_EVENT -> {
                  Text(text = "Ce billet est pour", fontSize = 13.sp, color = style.sub)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = result.otherEventTitle ?: "un autre événement",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = style.title,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                  )
                  if (!result.otherEventDate.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = result.otherEventDate, fontSize = 15.sp, color = style.sub)
                  }
                }

                CheckinResultStatus.CANCELLED -> {
                  IdentityBlock(result, style)
                  if (!result.reason.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0x59FFFFFF))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                      text = "Motif : ${result.reason}",
                      fontSize = 15.sp,
                      color = style.sub,
                      textAlign = TextAlign.Center
                    )
                  }
                }

                CheckinResultStatus.EVENT_NOT_ACTIVE,
                CheckinResultStatus.INVALID -> {
                  Text(
                    text = result.subtitle ?: "Ce code n'est pas un billet JCI valide",
                    fontSize = 16.sp,
                    color = style.title,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                  )
                }

                CheckinResultStatus.NETWORK_ERROR -> {
                  Text(
                    text = result.subtitle ?: "Vérifiez la connexion et rescannez",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = style.title,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "Ce billet n'a pas été validé.",
                    fontSize = 13.sp,
                    color = style.sub,
                    textAlign = TextAlign.Center
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isError) {
          Button(
            onClick = {
              onDismiss()
              onRetry()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.White,
              contentColor = Color(0xFF374151)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("retry_scan_button"),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
          ) {
            Text(
              text = "Rescanner",
              color = Color(0xFF374151),
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
        } else {
          Box(
            modifier = Modifier
              .width(120.dp)
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(style.title.copy(alpha = 0.25f))
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(style.title)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

/** Nom du participant, catégorie en pastille et numéro du billet : seules les données reçues s'affichent. */
@Composable
private fun IdentityBlock(result: ScanResultUiState, style: ResultStyle) {
  if (!result.attendeeName.isNullOrBlank()) {
    Text(
      text = result.attendeeName,
      fontSize = 22.sp,
      fontWeight = FontWeight.SemiBold,
      color = style.title,
      lineHeight = 27.sp,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(10.dp))
  }
  Row(verticalAlignment = Alignment.CenterVertically) {
    if (!result.category.isNullOrBlank()) {
      Surface(
        color = style.pillBackground,
        shape = RoundedCornerShape(20.dp)
      ) {
        Text(
          text = result.category,
          color = style.pillText,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
    }
    if (!result.ticketNumber.isNullOrBlank()) {
      Text(
        text = result.ticketNumber,
        fontSize = 14.sp,
        color = style.sub
      )
    }
  }
}
