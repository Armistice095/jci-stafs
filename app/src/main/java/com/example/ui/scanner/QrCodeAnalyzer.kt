package com.example.ui.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class QrCodeAnalyzer(
  private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

  private val reader = MultiFormatReader()
  private var lastAnalysisTimestamp = 0L

  @OptIn(ExperimentalGetImage::class)
  override fun analyze(imageProxy: ImageProxy) {
    val currentTimestamp = System.currentTimeMillis()
    // Limit to ~10-12 FPS to save CPU and battery as requested in Section 1
    if (currentTimestamp - lastAnalysisTimestamp < 85) {
      imageProxy.close()
      return
    }

    val mediaImage = imageProxy.image
    if (mediaImage != null) {
      try {
        val yPlane = mediaImage.planes[0]
        val yBuffer = yPlane.buffer
        val rowStride = yPlane.rowStride
        val width = imageProxy.width
        val height = imageProxy.height

        val yBytes = ByteArray(width * height)
        val originalPos = yBuffer.position()

        for (row in 0 until height) {
          yBuffer.position(originalPos + row * rowStride)
          val bytesToRead = minOf(width, yBuffer.remaining())
          if (bytesToRead > 0) {
            yBuffer.get(yBytes, row * width, bytesToRead)
          }
        }

        val source = PlanarYUVLuminanceSource(
          yBytes,
          width,
          height,
          0,
          0,
          width,
          height,
          false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val result = reader.decodeWithState(binaryBitmap)

        result?.text?.let { text ->
          if (text.isNotBlank()) {
            onQrCodeScanned(text)
          }
        }
      } catch (_: NotFoundException) {
        // Normal when no barcode is in sight
      } catch (_: Exception) {
      } finally {
        reader.reset()
        lastAnalysisTimestamp = currentTimestamp
      }
    }
    imageProxy.close()
  }
}
