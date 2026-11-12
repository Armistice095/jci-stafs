package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CropFree
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Event
import com.example.util.DateFormats
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.theme.JciBlue
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

  // 2. Clear screen background strictly #F4F6FB
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
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      // 1. Header (Avatar, Bonjour [Prénom], Déconnexion)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Circular Avatar (38x38, #0F1B4C, text white 14sp)
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(JciNavy),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = initials,
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = "Bonjour",
              fontSize = 12.sp,
              color = Color(0xFF6B7390)
            )
            Text(
              text = agentFirstName,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
              color = JciNavy
            )
          }
        }

        // Circular Logout Button
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, JciInputBorder, CircleShape)
            .background(Color.White)
            .clickable { showLogoutConfirm = true }
            .testTag("event_select_logout_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = "Se déconnecter",
            tint = JciNavy,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(26.dp))

      // 2. Title: « Quel événement contrôlez-vous ? »
      Text(
        text = "Quel événement\ncontrôlez-vous ?",
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 30.sp,
        color = JciNavy
      )

      Spacer(modifier = Modifier.height(20.dp))

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        if (uiState.isLoadingEvents && displayEvents.isEmpty()) {
          items(2) { EventSkeletonCard() }
        } else if (displayEvents.isEmpty()) {
          item {
            EventsEmptyState(
              message = uiState.eventsError ?: "Aucun événement à contrôler pour le moment.",
              onRefresh = { viewModel.loadEvents() }
            )
          }
        }

        // 3. Featured Card (Prochain événement)
        if (featuredEvent != null) {
          item {
            FeaturedEventCard(
              event = featuredEvent,
              onStartCheckin = { viewModel.selectEvent(featuredEvent) }
            )
          }
        }

        // 4. Section « À venir »: cartes blanches arrondies (16 px) avec espace de 8 px entre elles
        if (upcomingEvents.isNotEmpty()) {
          item {
            Text(
              text = "À venir",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF6B7390),
              modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
          }

          item {
            Column(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
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

@Composable
fun FeaturedEventCard(
  event: Event,
  onStartCheckin: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(JciNavy)
      .padding(18.dp)
      .testTag("featured_event_card")
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        // Pill « Prochain événement » (font size 12sp minimum)
        Surface(
          color = Color.White.copy(alpha = 0.14f),
          shape = RoundedCornerShape(20.dp)
        ) {
          Text(
            text = "Prochain événement",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }

        // Date Box (White with "NOV", "28", font size >= 12sp)
        val month = DateFormats.monthBadge(event.startsAt)
        val day = DateFormats.dayBadge(event.startsAt)
        Surface(
          color = Color.White,
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = month,
              color = JciNavy,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = day,
              color = JciNavy,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = event.title,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = DateFormats.fullLabel(event.startsAt),
        color = Color(0xFFB8C7EA),
        fontSize = 13.sp
      )

      if (event.placeLabel.isNotEmpty()) {
        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = event.placeLabel,
          color = Color(0xFFB8C7EA),
          fontSize = 13.sp
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Action Button: « Commencer le contrôle »
      Button(
        onClick = onStartCheckin,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("start_checkin_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.White,
          contentColor = JciNavy
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CropFree,
            contentDescription = null,
            tint = JciNavy,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Commencer le contrôle",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = JciNavy
          )
        }
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
