package com.example.ui.screens

import android.Manifest
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.provider.Settings
import android.net.Uri
import android.content.Intent
import android.app.Activity
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.scanner.QrCodeAnalyzer
import com.example.ui.theme.JciBlue
import com.example.ui.theme.JciFieldBg
import com.example.ui.theme.JciNavy
import com.example.util.DateFormats
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
  uiState: MainUiState,
  viewModel: MainViewModel
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // Keep screen on during scan session
  DisposableEffect(Unit) {
    val activity = context as? Activity
    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    onDispose {
      activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }

  // Camera Permission state
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
  }

  // Au retour des paramètres Android, on revérifie l'autorisation.
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  var cameraInstance by remember { mutableStateOf<Camera?>(null) }

  // Sync torch state with camera
  LaunchedEffect(uiState.isTorchOn, cameraInstance) {
    cameraInstance?.let { cam ->
      if (cam.cameraInfo.hasFlashUnit()) {
        cam.cameraControl.enableTorch(uiState.isTorchOn)
      }
    }
  }

  // 5. Opacity 0.85 for top bar: rgba(15,27,76, 0.85)
  val topBarBgColor = Color(0xD90F1B4C)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF1A1E26))
  ) {
    // 1. Camera View or Permission Prompt
    if (hasCameraPermission) {
      AndroidView(
        factory = { ctx ->
          val previewView = PreviewView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
          }

          val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
          val executor = Executors.newSingleThreadExecutor()

          cameraProviderFuture.addListener({
            try {
              val cameraProvider = cameraProviderFuture.get()

              val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
              }

              val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                  analysis.setAnalyzer(
                    executor,
                    QrCodeAnalyzer { rawQr ->
                      viewModel.onQrCodeDetected(rawQr)
                    }
                  )
                }

              val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

              cameraProvider.unbindAll()
              val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
              )
              cameraInstance = camera
            } catch (_: Exception) {
            }
          }, ContextCompat.getMainExecutor(ctx))

          previewView
        },
        modifier = Modifier.fillMaxSize()
      )
    } else {
      CameraPermissionDenied(
        onRetry = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        onOpenSettings = {
          val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
          )
          context.startActivity(intent)
        }
      )
    }

    // 4. Cadre de visée : quatre coins en L (bordure 4 px blanche sur 2 côtés seulement, longueur 36 px,
    // un seul coin arrondi à l'extérieur de 16 px) aux 4 angles d'un carré de 220 px centré,
    // sans bordure sur les côtés entiers. Le texte « Placez le QR code dans le cadre » est centré dessous.
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 60.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Canvas(
          modifier = Modifier
            .size(220.dp)
            .testTag("scanner_reticle_frame")
        ) {
          val strokeWidthPx = 4.dp.toPx()
          val armLengthPx = 36.dp.toPx()
          val cornerRadiusPx = 16.dp.toPx()
          val halfStroke = strokeWidthPx / 2f
          val w = size.width
          val h = size.height

          val strokeStyle = Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
          )

          // Coin 1 : Haut-Gauche (Top-Left)
          val pathTopLeft = Path().apply {
            moveTo(halfStroke, armLengthPx)
            lineTo(halfStroke, cornerRadiusPx + halfStroke)
            arcTo(
              rect = Rect(halfStroke, halfStroke, halfStroke + 2 * cornerRadiusPx, halfStroke + 2 * cornerRadiusPx),
              startAngleDegrees = 180f,
              sweepAngleDegrees = 90f,
              forceMoveTo = false
            )
            lineTo(armLengthPx, halfStroke)
          }
          drawPath(pathTopLeft, color = Color.White, style = strokeStyle)

          // Coin 2 : Haut-Droite (Top-Right)
          val pathTopRight = Path().apply {
            moveTo(w - armLengthPx, halfStroke)
            lineTo(w - cornerRadiusPx - halfStroke, halfStroke)
            arcTo(
              rect = Rect(w - 2 * cornerRadiusPx - halfStroke, halfStroke, w - halfStroke, halfStroke + 2 * cornerRadiusPx),
              startAngleDegrees = 270f,
              sweepAngleDegrees = 90f,
              forceMoveTo = false
            )
            lineTo(w - halfStroke, armLengthPx)
          }
          drawPath(pathTopRight, color = Color.White, style = strokeStyle)

          // Coin 3 : Bas-Gauche (Bottom-Left)
          val pathBottomLeft = Path().apply {
            moveTo(halfStroke, h - armLengthPx)
            lineTo(halfStroke, h - cornerRadiusPx - halfStroke)
            arcTo(
              rect = Rect(halfStroke, h - 2 * cornerRadiusPx - halfStroke, halfStroke + 2 * cornerRadiusPx, h - halfStroke),
              startAngleDegrees = 180f,
              sweepAngleDegrees = -90f,
              forceMoveTo = false
            )
            lineTo(armLengthPx, h - halfStroke)
          }
          drawPath(pathBottomLeft, color = Color.White, style = strokeStyle)

          // Coin 4 : Bas-Droite (Bottom-Right)
          val pathBottomRight = Path().apply {
            moveTo(w - armLengthPx, h - halfStroke)
            lineTo(w - cornerRadiusPx - halfStroke, h - halfStroke)
            arcTo(
              rect = Rect(w - 2 * cornerRadiusPx - halfStroke, h - 2 * cornerRadiusPx - halfStroke, w - halfStroke, h - halfStroke),
              startAngleDegrees = 90f,
              sweepAngleDegrees = -90f,
              forceMoveTo = false
            )
            lineTo(w - halfStroke, h - armLengthPx)
          }
          drawPath(pathBottomRight, color = Color.White, style = strokeStyle)
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "Placez le QR code dans le cadre",
          color = Color(0xFFE3E8F7),
          fontSize = 13.sp,
          fontWeight = FontWeight.Normal,
          textAlign = TextAlign.Center
        )
      }
    }

    // 5. Barre du haut du scanner : fond rgba(15,27,76,0.85), nom sur une seule ligne tronqué
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left Translucent Pill: Back arrow + Event title (1 line truncated) + Online status
        Surface(
          color = topBarBgColor,
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(start = 6.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { viewModel.navigateTo(Screen.EVENT_SELECT) },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.Center
            ) {
              Text(
                text = uiState.selectedEvent?.title ?: "",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (uiState.isOnline) Color(0xFF7BE0A0) else Color(0xFFDC2626))
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = if (uiState.isOnline) "En ligne" else "Hors ligne",
                  fontSize = 12.sp,
                  color = if (uiState.isOnline) Color(0xFF7BE0A0) else Color(0xFFFCA5A5)
                )
              }
            }
          }
        }

        // Circular Torch Toggle Button (44x44, rgba(15,27,76, 0.85))
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (uiState.isTorchOn) JciBlue else topBarBgColor)
            .clickable { viewModel.toggleTorch() }
            .testTag("torch_toggle_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (uiState.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
            contentDescription = "Lampe torche",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        // Circular Menu Button (44x44, rgba(15,27,76, 0.85))
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(topBarBgColor)
            .clickable { viewModel.setMenuDialogVisible(true) }
            .testTag("menu_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MoreHoriz,
            contentDescription = "Menu",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Offline Alert Banner
      if (!uiState.isOnline) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          color = Color(0xFFDC2626),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.WifiOff,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Hors ligne — les scans sont suspendus",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }

    // 4. Bottom Panel: Entrées, Counter, Progress bar, Saisir le numéro
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
      Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          val hasCounters = uiState.counters != null
          val admitted = uiState.counters?.admitted ?: 0
          val capacity = uiState.counters?.capacity ?: 0
          val percentage = if (capacity > 0) ((admitted.toFloat() / capacity.toFloat()) * 100).toInt() else 0
          val progress = (admitted.toFloat() / capacity.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

          // "Entrées" row with percentage on right
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Entrées",
              fontSize = 12.sp,
              color = Color(0xFF6B7390),
              fontWeight = FontWeight.Normal
            )
            Text(
              text = if (hasCounters) "$percentage %" else "",
              fontSize = 12.sp,
              color = Color(0xFF6B7390),
              fontWeight = FontWeight.Normal
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Counter: Big 342 + / 500
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = if (hasCounters) "$admitted" else "—",
              fontSize = 28.sp,
              fontWeight = FontWeight.SemiBold,
              color = JciNavy
            )
            Spacer(modifier = Modifier.width(6.dp))
            if (hasCounters) {
              Text(
                text = "/ $capacity",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6B7390)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Slim blue progress bar (6dp height)
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = JciBlue,
            trackColor = Color(0xFFE6EAF5),
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Button: « Saisir le numéro » (height 48dp, background #F4F6FB)
          Button(
            onClick = { viewModel.setManualInputDialogVisible(true) },
            colors = ButtonDefaults.buttonColors(
              containerColor = JciFieldBg,
              contentColor = JciNavy
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("manual_entry_button"),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = null,
                tint = JciNavy,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Saisir le numéro",
                color = JciNavy,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }
      }
    }

    // 5. Fullscreen Result Overlays (Screens 8-13)
    ScanResultOverlay(
      result = uiState.activeResult,
      selectedEventTitle = uiState.selectedEvent?.title ?: "",
      isOnline = uiState.isOnline,
      onDismiss = { viewModel.dismissActiveResult() },
      onRetry = { viewModel.retryPendingScan() }
    )

    // 6. Manual Ticket Entry Sheet (Screen 6)
    if (uiState.showManualInputDialog) {
      ManualTicketDialog(
        ticketPrefix = "JCI-${DateFormats.year(uiState.selectedEvent?.startsAt)}-",
        onDismiss = { viewModel.setManualInputDialogVisible(false) },
        onSubmit = { ticketNumber ->
          viewModel.onManualTicketSubmitted(ticketNumber)
        }
      )
    }

    // 7. Menu Bottom Sheet (Screen 5)
    if (uiState.showMenuDialog) {
      MenuDialog(
        uiState = uiState,
        viewModel = viewModel,
        onDismiss = { viewModel.setMenuDialogVisible(false) }
      )
    }
  }
}

@Composable
private fun CameraPermissionDenied(
  onRetry: () -> Unit,
  onOpenSettings: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(JciFieldBg)
      .statusBarsPadding()
      .padding(start = 24.dp, end = 24.dp, top = 84.dp, bottom = 200.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(Color(0xFFEAF2FD)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.NoPhotography,
          contentDescription = null,
          tint = JciBlue,
          modifier = Modifier.size(36.dp)
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Accès à la caméra refusé",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = JciNavy,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Pour scanner les billets, autorisez l'appareil photo.",
        fontSize = 14.sp,
        color = Color(0xFF6B7390),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(16.dp))
      listOf(
        "Touchez « Ouvrir les paramètres ».",
        "Choisissez « Autorisations » puis « Caméra ».",
        "Sélectionnez « Autoriser » et revenez ici."
      ).forEachIndexed { index, step ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(Color(0xFFEAF2FD)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${index + 1}",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = JciBlue
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = step, fontSize = 14.sp, color = JciNavy)
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = onOpenSettings,
        colors = ButtonDefaults.buttonColors(containerColor = JciNavy, contentColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("open_settings_button")
      ) {
        Text(text = "Ouvrir les paramètres", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
      }
      Spacer(modifier = Modifier.height(4.dp))
      TextButton(onClick = onRetry) {
        Text(text = "Réessayer", fontSize = 14.sp, color = JciNavy)
      }
    }
  }
}
