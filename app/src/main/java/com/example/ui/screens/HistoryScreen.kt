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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.model.CheckinResultStatus
import com.example.data.model.ScanHistoryItem
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.InterFamily
import com.example.ui.theme.JciBlue
import com.example.ui.theme.JciFieldBg
import com.example.ui.theme.JciInputBorder
import com.example.ui.theme.JciNavy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
  uiState: MainUiState,
  viewModel: MainViewModel
) {
  var searchQuery by remember { mutableStateOf("") }
  val focusManager = LocalFocusManager.current
  val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.FRANCE) }

  val baseList = uiState.recentScans

  val filteredList = if (searchQuery.isBlank()) {
    baseList
  } else {
    baseList.filter {
      it.holderName.contains(searchQuery, ignoreCase = true) ||
        it.ticketNumber.contains(searchQuery, ignoreCase = true)
    }
  }

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
        .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
      // 1. Header (Back button + Title + Subtitle)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, JciInputBorder, CircleShape)
            .background(Color.White)
            .clickable { viewModel.navigateTo(Screen.SCANNER) }
            .testTag("history_back_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Retour",
            tint = JciNavy,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = "Historique",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = JciNavy
          )
          Text(
            text = when {
              baseList.isEmpty() -> "Aucun scan pour le moment"
              baseList.size == 1 -> "1 scan · cet appareil"
              baseList.size >= 50 -> "50 derniers scans · cet appareil"
              else -> "${baseList.size} scans · cet appareil"
            },
            fontSize = 12.sp,
            color = Color(0xFF6B7390)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 2. Search Field (Height 46dp, background #FFFFFF, radius 14dp)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color.White)
          .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFF9AA3C0),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              singleLine = true,
              textStyle = TextStyle(
                fontFamily = InterFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = JciNavy
              ),
              cursorBrush = SolidColor(JciBlue),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
              keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
              decorationBox = { innerTextField ->
                if (searchQuery.isEmpty()) {
                  Text(
                    text = "Rechercher un nom",
                    fontSize = 14.sp,
                    color = Color(0xFF9AA3C0),
                    fontWeight = FontWeight.Normal
                  )
                }
                innerTextField()
              }
            )
          }
          if (searchQuery.isNotEmpty()) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Effacer la recherche",
              tint = Color(0xFF9AA3C0),
              modifier = Modifier
                .size(18.dp)
                .clickable { searchQuery = "" }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. History Card Container (White with rounded 20dp)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        color = Color.White,
        shape = RoundedCornerShape(20.dp)
      ) {
        if (baseList.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color(0xFF9AA3C0),
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Aucun scan pour le moment",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = JciNavy,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Les scans faits sur cet appareil apparaîtront ici.",
                fontSize = 13.sp,
                color = Color(0xFF6B7390),
                textAlign = TextAlign.Center
              )
            }
          }
        } else if (filteredList.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Aucun scan trouvé pour « $searchQuery »",
              fontSize = 14.sp,
              color = Color(0xFF6B7390),
              textAlign = TextAlign.Center
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 14.dp)
          ) {
            itemsIndexed(filteredList, key = { _, item -> item.id }) { index, item ->
              HistoryRowItem(item = item, timeStr = timeFormat.format(Date(item.timestamp)))
              if (index < filteredList.size - 1) {
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE6EAF5))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun HistoryRowItem(
  item: ScanHistoryItem,
  timeStr: String
) {
  val (badgeBg, iconColor, iconVector) = when (item.status) {
    CheckinResultStatus.ADMITTED -> Triple(
      Color(0xFFDCFCE7),
      Color(0xFF15803D),
      Icons.Default.Check
    )
    CheckinResultStatus.ALREADY_USED -> Triple(
      Color(0xFFFEF3C7),
      Color(0xFFB45309),
      Icons.Default.AccessTime
    )
    CheckinResultStatus.CANCELLED -> Triple(
      Color(0xFFFEE2E2),
      Color(0xFFDC2626),
      Icons.Default.Close
    )
    CheckinResultStatus.INVALID, CheckinResultStatus.WRONG_EVENT, CheckinResultStatus.EVENT_NOT_ACTIVE -> Triple(
      Color(0xFFFEE2E2),
      Color(0xFFDC2626),
      Icons.Default.QuestionMark
    )
    CheckinResultStatus.NETWORK_ERROR -> Triple(
      Color(0xFFF3F4F6),
      Color(0xFF4B5563),
      Icons.AutoMirrored.Filled.HelpOutline
    )
  }

  val subColor = when (item.status) {
    CheckinResultStatus.ALREADY_USED -> Color(0xFFB45309)
    CheckinResultStatus.CANCELLED, CheckinResultStatus.INVALID, CheckinResultStatus.WRONG_EVENT, CheckinResultStatus.EVENT_NOT_ACTIVE -> Color(0xFFDC2626)
    else -> Color(0xFF6B7390)
  }

  val subtitleText = item.note ?: when (item.status) {
    CheckinResultStatus.ADMITTED -> listOf(item.category, item.ticketNumber).filter { it.isNotBlank() }.joinToString(" · ")
    CheckinResultStatus.ALREADY_USED -> "Déjà scanné"
    CheckinResultStatus.CANCELLED -> "Billet annulé"
    CheckinResultStatus.INVALID -> "Code non reconnu"
    CheckinResultStatus.WRONG_EVENT -> "Autre événement"
    CheckinResultStatus.EVENT_NOT_ACTIVE -> "Événement non actif"
    CheckinResultStatus.NETWORK_ERROR -> "Échec réseau"
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(64.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left Circular Badge (38x38)
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(badgeBg),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = iconVector,
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(20.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Middle info
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = item.holderName,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = JciNavy,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitleText,
        fontSize = 12.sp,
        color = subColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    // Right scan time
    Text(
      text = timeStr,
      fontSize = 12.sp,
      color = Color(0xFF6B7390)
    )
  }
}
