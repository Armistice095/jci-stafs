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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JciBlue
import com.example.ui.theme.JciFieldBg
import com.example.ui.theme.JciInputBorder
import com.example.ui.theme.JciNavy

@Composable
fun ManualTicketDialog(
  ticketPrefix: String,
  onDismiss: () -> Unit,
  onSubmit: (String) -> Unit
) {
  var digits by remember { mutableStateOf("") }
  var showHint by remember { mutableStateOf(false) }
  val isComplete = digits.length == 6

  // Dark dim backdrop: rgba(8, 12, 32, 0.62)
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0x9E080C20))
      .clickable { onDismiss() },
    contentAlignment = Alignment.BottomCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = false) {}
        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
        .background(Color.White)
    ) {
      // Top Input Form Section
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 18.dp)
      ) {
        // Drag Handle
        Box(
          modifier = Modifier
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFFD9DFEC))
            .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column {
            Text(
              text = "Numéro de billet",
              fontSize = 18.sp,
              fontWeight = FontWeight.SemiBold,
              color = JciNavy
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Imprimé sous le QR code",
              fontSize = 12.sp,
              color = Color(0xFF6B7390)
            )
          }

          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(JciFieldBg)
              .clickable { onDismiss() }
              .testTag("manual_dialog_close_button"),
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

        Spacer(modifier = Modifier.height(18.dp))

        // Prefix + Digits Input Box (Height 56dp, border 1.5dp)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
              width = 1.5.dp,
              color = if (digits.isNotEmpty()) JciBlue else JciInputBorder,
              shape = RoundedCornerShape(16.dp)
            )
            .background(Color.White)
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Fixed Prefix Container
            Box(
              modifier = Modifier
                .height(56.dp)
                .background(Color(0xFFEAF2FD))
                .padding(horizontal = 14.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = ticketPrefix,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7390)
              )
            }

            // Typed Digits Display
            Box(
              modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (digits.isEmpty()) {
                Text(
                  text = "000000",
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Medium,
                  letterSpacing = 2.sp,
                  color = Color(0xFF9AA3C0)
                )
              } else {
                Text(
                  text = digits,
                  fontSize = 22.sp,
                  fontWeight = FontWeight.SemiBold,
                  letterSpacing = 2.sp,
                  color = JciNavy
                )
              }
            }

            // Checkmark Icon when 6 digits are typed
            if (isComplete) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Numéro complet",
                tint = Color(0xFF16A34A),
                modifier = Modifier
                  .padding(end = 14.dp)
                  .size(22.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Button: « Vérifier le billet »
        Button(
          onClick = {
            if (isComplete) {
              onSubmit("$ticketPrefix$digits")
            } else {
              showHint = true
            }
          },
          enabled = true,
          colors = ButtonDefaults.buttonColors(
            containerColor = JciNavy,
            contentColor = Color.White,
            disabledContainerColor = JciNavy.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("verify_manual_ticket_button"),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
          Text(
            text = "Vérifier le billet",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        }

        if (showHint && !isComplete) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Saisissez les 6 chiffres",
            fontSize = 13.sp,
            color = Color(0xFFDC2626),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
          )
        }
      }

      // Virtual Numeric Keypad (Background #D9DCE3)
      Surface(
        color = Color(0xFFD9DCE3),
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Row 1: 1, 2, 3
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            KeypadKey("1", Modifier.weight(1f)) { if (digits.length < 6) digits += "1" }
            KeypadKey("2", Modifier.weight(1f)) { if (digits.length < 6) digits += "2" }
            KeypadKey("3", Modifier.weight(1f)) { if (digits.length < 6) digits += "3" }
          }

          // Row 2: 4, 5, 6
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            KeypadKey("4", Modifier.weight(1f)) { if (digits.length < 6) digits += "4" }
            KeypadKey("5", Modifier.weight(1f)) { if (digits.length < 6) digits += "5" }
            KeypadKey("6", Modifier.weight(1f)) { if (digits.length < 6) digits += "6" }
          }

          // Row 3: 7, 8, 9
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            KeypadKey("7", Modifier.weight(1f)) { if (digits.length < 6) digits += "7" }
            KeypadKey("8", Modifier.weight(1f)) { if (digits.length < 6) digits += "8" }
            KeypadKey("9", Modifier.weight(1f)) { if (digits.length < 6) digits += "9" }
          }

          // Row 4: Empty, 0, Backspace
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(modifier = Modifier.weight(1f).height(48.dp))
            KeypadKey("0", Modifier.weight(1f)) { if (digits.length < 6) digits += "0" }
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { if (digits.isNotEmpty()) digits = digits.dropLast(1) }
                .testTag("keypad_backspace"),
              color = Color.White,
              shape = RoundedCornerShape(8.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Backspace,
                  contentDescription = "Effacer",
                  tint = JciNavy,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun KeypadKey(
  label: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    modifier = modifier
      .height(48.dp)
      .clip(RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .testTag("keypad_key_$label"),
    color = Color.White,
    shape = RoundedCornerShape(8.dp)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal,
        color = JciNavy,
        textAlign = TextAlign.Center
      )
    }
  }
}
