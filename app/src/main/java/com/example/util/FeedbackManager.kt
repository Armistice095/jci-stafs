package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.CheckinResultStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeedbackManager(private val context: Context) {

  private var toneGenerator: ToneGenerator? = null

  init {
    try {
      toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    } catch (_: Exception) {
      // In case device audio is unavailable
    }
  }

  private val vibrator: Vibrator? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  fun notifyResult(
    status: CheckinResultStatus,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    scope: CoroutineScope
  ) {
    if (soundEnabled) {
      playSound(status, scope)
    }
    if (vibrationEnabled) {
      playVibration(status)
    }
  }

  private fun playSound(status: CheckinResultStatus, scope: CoroutineScope) {
    val tg = toneGenerator ?: return
    try {
      when (status) {
        CheckinResultStatus.ADMITTED -> {
          // 1 short high beep
          tg.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        }
        CheckinResultStatus.ALREADY_USED -> {
          // Double beep
          scope.launch(Dispatchers.Default) {
            try {
              tg.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
              delay(140)
              tg.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            } catch (_: Exception) {}
          }
        }
        CheckinResultStatus.WRONG_EVENT,
        CheckinResultStatus.CANCELLED,
        CheckinResultStatus.EVENT_NOT_ACTIVE,
        CheckinResultStatus.INVALID -> {
          // Low grave beep
          tg.startTone(ToneGenerator.TONE_CDMA_LOW_L, 350)
        }
        CheckinResultStatus.NETWORK_ERROR -> {
          // Warning tone
          tg.startTone(ToneGenerator.TONE_SUP_ERROR, 250)
        }
      }
    } catch (_: Exception) {}
  }

  private fun playVibration(status: CheckinResultStatus) {
    val vib = vibrator ?: return
    if (!vib.hasVibrator()) return

    try {
      when (status) {
        CheckinResultStatus.ADMITTED -> {
          // 1 short vibration (100ms)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
          } else {
            @Suppress("DEPRECATION")
            vib.vibrate(100)
          }
        }
        CheckinResultStatus.ALREADY_USED -> {
          // 2 vibrations
          val timings = longArrayOf(0, 100, 80, 100)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createWaveform(timings, -1))
          } else {
            @Suppress("DEPRECATION")
            vib.vibrate(timings, -1)
          }
        }
        CheckinResultStatus.WRONG_EVENT,
        CheckinResultStatus.CANCELLED,
        CheckinResultStatus.EVENT_NOT_ACTIVE,
        CheckinResultStatus.INVALID -> {
          // 1 long vibration (400ms)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
          } else {
            @Suppress("DEPRECATION")
            vib.vibrate(400)
          }
        }
        CheckinResultStatus.NETWORK_ERROR -> {
          // 1 long vibration
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
          } else {
            @Suppress("DEPRECATION")
            vib.vibrate(300)
          }
        }
      }
    } catch (_: Exception) {}
  }

  fun release() {
    try {
      toneGenerator?.release()
      toneGenerator = null
    } catch (_: Exception) {}
  }
}
