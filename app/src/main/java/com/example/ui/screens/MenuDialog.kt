package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.JciBlue
import com.example.ui.theme.JciFieldBg
import com.example.ui.theme.JciNavy

@Composable
fun MenuDialog(
  uiState: MainUiState,
  viewModel: MainViewModel,
  onDismiss: () -> Unit
) {
  val agent = uiState.agent
  val agentFullName = agent?.fullDisplayName ?: ""
  val agentRole = agent?.displayRole ?: ""
  val initials = agent?.initials ?: ""

  // Dark dim backdrop: rgba(8, 12, 32, 0.62)
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0x9E080C20))
      .clickable { onDismiss() },
    contentAlignment = Alignment.BottomCenter
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = false) {}
        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
        .navigationBarsPadding(),
      color = Color.White,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 20.dp)
      ) {
        // Drag handle
        Box(
          modifier = Modifier
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFFD9DFEC))
            .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Header: Agent Info (Name + Role on ONE LINE) + Close button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Circular Avatar (44x44, #0F1B4C, text white 15sp)
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(JciNavy),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = initials,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nom complet (1 ligne) puis rôle (1 ligne)
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = agentFullName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = JciNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = agentRole,
                fontSize = 12.sp,
                color = Color(0xFF6B7390),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Close button (circle #F4F6FB)
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(JciFieldBg)
              .clickable { onDismiss() }
              .testTag("menu_close_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Fermer",
              tint = JciNavy,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Gray Card: Événement en cours (#F4F6FB)
        Surface(
          color = JciFieldBg,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
          ) {
            Text(
              text = "Événement en cours",
              fontSize = 12.sp,
              color = Color(0xFF6B7390),
              fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = uiState.selectedEvent?.title ?: "",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = JciNavy,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Changer",
                color = JciBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                  .clickable {
                    onDismiss()
                    viewModel.navigateTo(Screen.EVENT_SELECT)
                  }
                  .testTag("change_event_link")
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 1: Historique de la session
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable {
              onDismiss()
              viewModel.navigateTo(Screen.HISTORY)
            }
            .testTag("menu_history_row"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = Color(0xFF6B7390),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "Historique de la session",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = JciNavy,
            modifier = Modifier.weight(1f)
          )
          Text(
            text = if (uiState.recentScans.size == 1) "1 scan" else "${uiState.recentScans.size} scans",
            fontSize = 12.sp,
            color = Color(0xFF6B7390)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF9AA3C0),
            modifier = Modifier.size(20.dp)
          )
        }

        // Trait fin #E6EAF5
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE6EAF5))

        // Row 2: Son
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = null,
            tint = Color(0xFF6B7390),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "Son",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = JciNavy,
            modifier = Modifier.weight(1f)
          )
          Switch(
            checked = uiState.soundEnabled,
            onCheckedChange = { viewModel.toggleSound() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = JciBlue,
              uncheckedThumbColor = Color.White,
              uncheckedTrackColor = Color(0xFFD9DFEC)
            ),
            modifier = Modifier.testTag("sound_switch")
          )
        }

        // Trait fin #E6EAF5
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE6EAF5))

        // Row 3: Vibration
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Vibration,
            contentDescription = null,
            tint = Color(0xFF6B7390),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "Vibration",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = JciNavy,
            modifier = Modifier.weight(1f)
          )
          Switch(
            checked = uiState.vibrationEnabled,
            onCheckedChange = { viewModel.toggleVibration() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = JciBlue,
              uncheckedThumbColor = Color.White,
              uncheckedTrackColor = Color(0xFFD9DFEC)
            ),
            modifier = Modifier.testTag("vibration_switch")
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Red Logout Button (#FDECEC, text #DC2626)
        Button(
          onClick = {
            onDismiss()
            viewModel.logout()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFDECEC),
            contentColor = Color(0xFFDC2626)
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("menu_logout_button"),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Logout,
              contentDescription = null,
              tint = Color(0xFFDC2626),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Se déconnecter",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFDC2626)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Footer: JCI Stafs · v1.0.0 (font size >= 12sp)
        Text(
          text = "JCI Stafs · v1.0.0",
          fontSize = 12.sp,
          color = Color(0xFF6B7390),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
