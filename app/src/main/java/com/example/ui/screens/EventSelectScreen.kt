package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Event
import com.example.util.DateFormats
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.theme.JciBlue
import com.example.ui.theme.JciBlueLight
import com.example.ui.theme.JciNavyLight
import com.example.ui.theme.JciFieldBg
import com.example.ui.theme.JciInputBorder
import com.example.ui.theme.JciNavy
import com.example.ui.theme.JciTextSecondary

@Composable
fun EventSelectScreen(
  uiState: MainUiState,
  viewModel: MainViewModel
) {
  var showLogoutConfirm by remember { mutableStateOf(false) }

  // La liste est rechargée à chaque ouverture de l'écran (événements publiés, à venir).
  LaunchedEffect(Unit) {
    if (!uiState.isLoadingEvents) viewModel.loadEvents()
  }

  val agent = uiState.agent
  val agentFirstName = agent?.userFirstName ?: ""
  val initials = agent?.initials ?: ""

  val displayEvents = uiState.events
  val featuredEvent = displayEvents.firstOrNull()
  val upcomingEvents = if (displayEvents.size > 1) displayEvents.drop(1) else emptyList()

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = JciFieldBg
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding(),
      contentPadding = PaddingValues(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Bandeau : avatar, « Bonjour », déconnexion + question
      item {
        EventsHero(
          initials = initials,
          firstName = agentFirstName,
          onLogout = { showLogoutConfirm = true }
        )
      }

      if (uiState.isLoadingEvents && displayEvents.isEmpty()) {
        items(2) {
          Box(modifier = Modifier.padding(horizontal = 20.dp)) { EventSkeletonCard() }
        }
      } else if (displayEvents.isEmpty()) {
        item {
          Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            EventsEmptyState(
              message = uiState.eventsError ?: "Aucun événement à contrôler pour le moment.",
              onRefresh = { viewModel.loadEvents() }
            )
          }
        }
      }

      // 2. Carte du prochain événement : chevauche le bandeau
      if (featuredEvent != null) {
        item {
          FeaturedEventCard(
            event = featuredEvent,
            onStartCheckin = { viewModel.selectEvent(featuredEvent) },
            modifier = Modifier
              .padding(horizontal = 20.dp)
              .pullUp(76.dp)
          )
        }
      }

      // 3. Section « Autres événements »
      if (upcomingEvents.isNotEmpty()) {
        item {
          Text(
            text = "Autres événements",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = JciNavy,
            modifier = Modifier.padding(start = 24.dp, end = 20.dp, top = 8.dp)
          )
        }

        item {
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp)
          ) {
            upcomingEvents.forEach { event ->
              UpcomingEventCard(
                event = event,
                onSelect = { viewModel.selectEvent(event) }
              )
            }
          }
        }
      }
    }
  }

  // Logout Confirmation Dialog
  if (showLogoutConfirm) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirm = false },
      title = {
        Text(
          text = "Se déconnecter ?",
          fontWeight = FontWeight.Bold,
          color = JciNavy,
          fontSize = 18.sp
        )
      },
      text = {
        Text(
          text = "Voulez-vous vraiment vous déconnecter de JCI Stafs ?",
          color = JciTextSecondary,
          fontSize = 14.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showLogoutConfirm = false
            viewModel.logout()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Se déconnecter", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutConfirm = false }) {
          Text("Annuler", color = JciNavy)
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = Color.White
    )
  }
}

/** Remonte un élément de [amount] sans laisser de vide : il chevauche ce qui est au-dessus. */
private fun Modifier.pullUp(amount: Dp): Modifier = layout { measurable, constraints ->
  val placeable = measurable.measure(constraints)
  val up = amount.roundToPx().coerceAtMost(placeable.height)
  layout(placeable.width, placeable.height - up) { placeable.place(0, -up) }
}

@Composable
private fun EventsHero(
  initials: String,
  firstName: String,
  onLogout: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
      .background(Brush.verticalGradient(listOf(JciNavy, JciNavyLight)))
  ) {
    // Coin de viseur décoratif, très discret
    Canvas(modifier = Modifier.matchParentSize()) {
      val w = size.width
      val path = Path().apply {
        moveTo(w - 128.dp.toPx(), 120.dp.toPx())
        lineTo(w - 60.dp.toPx(), 120.dp.toPx())
        quadraticBezierTo(w - 38.dp.toPx(), 120.dp.toPx(), w - 38.dp.toPx(), 142.dp.toPx())
        lineTo(w - 38.dp.toPx(), 200.dp.toPx())
      }
      drawPath(
        path,
        Color.White.copy(alpha = 0.07f),
        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
      )
    }

    Column(
      modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.14f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = initials,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Bonjour",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFC9D3F7)
          )
          Text(
            text = firstName,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable { onLogout() }
            .testTag("event_select_logout_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = "Se déconnecter",
            tint = Color.White,
            modifier = Modifier.size(21.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "Quel événement\ncontrôlez-vous ?",
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 33.sp,
        letterSpacing = (-0.5).sp,
        color = Color.White
      )
    }
  }
}

@Composable
fun FeaturedEventCard(
  event: Event,
  onStartCheckin: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .shadow(
        elevation = 16.dp,
        shape = RoundedCornerShape(28.dp),
        ambientColor = JciNavy.copy(alpha = 0.25f),
        spotColor = JciNavy.copy(alpha = 0.25f)
      )
      .background(Color.White)
      .padding(24.dp)
      .testTag("featured_event_card")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      // Pastille « Prochain événement »
      Surface(
        color = JciBlueLight,
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(JciBlue)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Prochain événement",
            color = Color(0xFF1565C0),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Badge date (marine) : « NOV » / « 14 »
      Surface(
        color = JciNavy,
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .size(width = 60.dp, height = 64.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = DateFormats.monthBadge(event.startsAt),
            color = Color(0xFFC9D3F7),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Text(
            text = DateFormats.dayBadge(event.startsAt),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    Text(
      text = event.title,
      color = JciNavy,
      fontSize = 24.sp,
      fontWeight = FontWeight.ExtraBold,
      lineHeight = 29.sp,
      letterSpacing = (-0.3).sp
    )

    Spacer(modifier = Modifier.height(14.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Default.Event,
        contentDescription = null,
        tint = JciTextSecondary,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = DateFormats.fullLabel(event.startsAt),
        color = JciTextSecondary,
        fontSize = 15.sp
      )
    }

    if (event.placeLabel.isNotEmpty()) {
      Spacer(modifier = Modifier.height(10.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Place,
          contentDescription = null,
          tint = JciTextSecondary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = event.placeLabel,
          color = JciTextSecondary,
          fontSize = 15.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(Color(0xFFE1E6F3))
    )
    Spacer(modifier = Modifier.height(20.dp))

    // Bouton « Scanner »
    Button(
      onClick = onStartCheckin,
      modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .testTag("start_checkin_button"),
      shape = RoundedCornerShape(18.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = JciNavy,
        contentColor = Color.White
      ),
      elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CropFree,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Scanner",
          fontSize = 18.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White,
          maxLines = 1,
          softWrap = false
        )
      }
    }
  }
}

@Composable
fun UpcomingEventCard(
  event: Event,
  onSelect: () -> Unit
) {
  val month = DateFormats.monthBadge(event.startsAt)
  val day = DateFormats.dayBadge(event.startsAt)

  // White card, rounded 16dp
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onSelect() }
      .testTag("upcoming_event_${event.id}"),
    color = Color.White,
    shape = RoundedCornerShape(16.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Light blue date box (48x48, background #EAF2FD, month + day >= 12sp)
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFFEAF2FD)),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = month,
            color = JciBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = day,
            color = JciBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = event.title,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = JciNavy,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = listOf(DateFormats.hour(event.startsAt), event.venueName.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(" · "),
          fontSize = 12.sp,
          color = Color(0xFF6B7390),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = Color(0xFF9AA3C0),
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
private fun EventSkeletonCard() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(150.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(Color(0xFFE6EAF5))
  )
}

@Composable
private fun EventsEmptyState(
  message: String,
  onRefresh: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 40.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = message,
      fontSize = 15.sp,
      color = Color(0xFF6B7390),
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
      onClick = onRefresh,
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = JciNavy, contentColor = Color.White),
      elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
      modifier = Modifier
        .height(48.dp)
        .testTag("events_refresh_button")
    ) {
      Text(text = "Actualiser", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
  }
}
