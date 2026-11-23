package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ImageEncodingUtil {
  fun encodeUriToCompactBase64(
    context: Context,
    uri: Uri,
    maxWidthPx: Int,
    maxHeightPx: Int,
    maxBase64Chars: Int,
  ): String? {
    val resolver = context.applicationContext.contentResolver
    val decoded =
      decodeScaledBitmap(
        context = context,
        resolver = resolver,
        uri = uri,
        maxWidthPx = maxWidthPx,
        maxHeightPx = maxHeightPx,
      ) ?: return null

    val opaqueBitmap = flattenAlphaIfNeeded(decoded)
    if (opaqueBitmap !== decoded) {
      decoded.recycle()
    }

    var working = opaqueBitmap
    return try {
      val qualities = intArrayOf(82, 70, 56, 44, 32, 22)
      var pass = 0
      while (pass < 5) {
        for (q in qualities) {
          val baos = ByteArrayOutputStream()
          working.compress(Bitmap.CompressFormat.JPEG, q, baos)
          val encoded = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
          if (encoded.length <= maxBase64Chars) {
            return encoded
          }
        }
        val nextW = (working.width * 0.72f).roundToInt()
        val nextH = (working.height * 0.72f).roundToInt()
        if (nextW < 48 || nextH < 48) break
        val smaller = Bitmap.createScaledBitmap(working, nextW, nextH, true)
        if (smaller !== working) {
          working.recycle()
          working = smaller
        } else {
          break
        }
        pass++
      }
      val finalBaos = ByteArrayOutputStream()
      working.compress(Bitmap.CompressFormat.JPEG, 18, finalBaos)
      val finalEncoded = Base64.encodeToString(finalBaos.toByteArray(), Base64.NO_WRAP)
      finalEncoded.takeIf { it.length <= maxBase64Chars }
    } catch (_: Throwable) {
      null
    } finally {
      if (!working.isRecycled) {
        working.recycle()
      }
    }
  }

  private fun decodeScaledBitmap(
    context: Context,
    resolver: android.content.ContentResolver,
    uri: Uri,
    maxWidthPx: Int,
    maxHeightPx: Int,
  ): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      val decodedModern =
        runCatching {
          val source = ImageDecoder.createSource(resolver, uri)
          ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val srcW = info.size.width.coerceAtLeast(1)
            val srcH = info.size.height.coerceAtLeast(1)
            val scale =
              min(
                maxWidthPx.toFloat() / srcW.toFloat(),
                maxHeightPx.toFloat() / srcH.toFloat(),
              ).coerceAtMost(1f)
            val targetW = max(1, (srcW * scale).roundToInt())
            val targetH = max(1, (srcH * scale).roundToInt())
            decoder.setTargetSize(targetW, targetH)
          }
        }.getOrNull()
      if (decodedModern != null) {
        return decodedModern
      }
    }

    return runCatching {
      val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      resolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
      } ?: return null

      val srcW = bounds.outWidth
      val srcH = bounds.outHeight
      if (srcW <= 0 || srcH <= 0) return null

      var sampleSize = 1
      while ((srcW / sampleSize) > maxWidthPx * 2 || (srcH / sampleSize) > maxHeightPx * 2) {
        sampleSize *= 2
      }

      val decodeOpts =
        BitmapFactory.Options().apply {
          inSampleSize = sampleSize
          inPreferredConfig = Bitmap.Config.ARGB_8888
        }
      val decoded =
        resolver.openInputStream(uri)?.use { input ->
          BitmapFactory.decodeStream(input, null, decodeOpts)
        } ?: return null

      val scale =
        min(
          maxWidthPx.toFloat() / decoded.width.coerceAtLeast(1),
          maxHeightPx.toFloat() / decoded.height.coerceAtLeast(1),
        ).coerceAtMost(1f)
      val targetW = max(1, (decoded.width * scale).roundToInt())
      val targetH = max(1, (decoded.height * scale).roundToInt())
      if (targetW < decoded.width || targetH < decoded.height) {
        Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
          if (it !== decoded) decoded.recycle()
        }
      } else {
        decoded
      }
    }.getOrNull()
  }

  private fun flattenAlphaIfNeeded(bitmap: Bitmap): Bitmap {
    if (!bitmap.hasAlpha() && bitmap.config == Bitmap.Config.ARGB_8888) {
      return bitmap
    }
    return runCatching {
      val copy = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(copy)
      canvas.drawColor(Color.WHITE)
      canvas.drawBitmap(bitmap, 0f, 0f, null)
      copy
    }.getOrDefault(bitmap)
  }

  fun decodeBase64ImageBitmap(base64: String): ImageBitmap? {
    val clean = base64.trim()
    if (clean.isBlank()) return null
    return runCatching {
      val bytes = Base64.decode(clean, Base64.DEFAULT)
      if (bytes.isEmpty()) return null
      BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()
  }
}

@Composable
fun rememberBase64ImageBitmap(base64: String): ImageBitmap? {
  return remember(base64) { ImageEncodingUtil.decodeBase64ImageBitmap(base64) }
}
