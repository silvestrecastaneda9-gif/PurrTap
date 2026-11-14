package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.data.weather.WeatherConditionKind
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class AmbientParticleSpec(
  val seedX: Float,
  val seedY: Float,
  val sizeDp: Float,
  val speed: Float,
  val swayAmplitude: Float,
  val swayFrequency: Float,
  val phase: Float,
  val initialRotation: Float,
  val colorVariant: Int,
)

@Composable
fun WeatherParticlesOverlay(
  weatherCondition: WeatherConditionKind,
  isGoldenRainActive: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val specs = remember {
    val rng = Random(7741)
    List(32) {
      AmbientParticleSpec(
        seedX = rng.nextFloat(),
        seedY = rng.nextFloat(),
        sizeDp = 4f + rng.nextFloat() * 7f,
        speed = 0.55f + rng.nextFloat() * 0.95f,
        swayAmplitude = 0.015f + rng.nextFloat() * 0.04f,
        swayFrequency = 1f + rng.nextInt(3).toFloat(),
        phase = rng.nextFloat() * (2f * PI.toFloat()),
        initialRotation = rng.nextFloat() * 360f,
        colorVariant = rng.nextInt(4),
      )
    }
  }

  val goldenSpecs = remember {
    val rng = Random(9913)
    List(18) {
      AmbientParticleSpec(
        seedX = rng.nextFloat(),
        seedY = rng.nextFloat(),
        sizeDp = 5f + rng.nextFloat() * 5f,
        speed = 0.85f + rng.nextFloat() * 0.9f,
        swayAmplitude = 0.01f + rng.nextFloat() * 0.025f,
        swayFrequency = 2f,
        phase = rng.nextFloat() * (2f * PI.toFloat()),
        initialRotation = rng.nextFloat() * 360f,
        colorVariant = rng.nextInt(3),
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "weather_particles")
  val timeProgress by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 12_000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "weather_cycle",
    )

  Canvas(modifier = modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return@Canvas

    val count =
      when (weatherCondition) {
        WeatherConditionKind.RAINY,
        WeatherConditionKind.STORMY -> 32
        WeatherConditionKind.FOGGY_MIST,
        WeatherConditionKind.CLOUDY -> 18
        else -> 24
      }

    for (i in 0 until count) {
      val spec = specs[i]
      drawWeatherParticle(
        condition = weatherCondition,
        spec = spec,
        t = timeProgress,
        canvasW = w,
        canvasH = h,
      )
    }

    if (isGoldenRainActive) {
      goldenSpecs.forEach { spec ->
        val cy = ((spec.seedY + timeProgress * spec.speed) % 1.12f - 0.06f) * h
        val sway = sin(timeProgress * 2f * PI.toFloat() * spec.swayFrequency + spec.phase) * w * spec.swayAmplitude
        val cx = ((spec.seedX * w) + sway + w) % w
        val r = (spec.sizeDp * 0.65f).dp.toPx()
        val goldColor =
          when (spec.colorVariant) {
            0 -> Color(0xFFFFD54F).copy(alpha = 0.78f)
            1 -> Color(0xFFFFB300).copy(alpha = 0.72f)
            else -> Color(0xFFFFF59D).copy(alpha = 0.85f)
          }
        drawSparkle(center = Offset(cx, cy), radius = r, color = goldColor)
      }
    }
  }
}

private fun DrawScope.drawWeatherParticle(
  condition: WeatherConditionKind,
  spec: AmbientParticleSpec,
  t: Float,
  canvasW: Float,
  canvasH: Float,
) {
  val cycleAngle = t * 2f * PI.toFloat() * spec.swayFrequency + spec.phase
  val sizePx = spec.sizeDp.dp.toPx()

  when (condition) {
    WeatherConditionKind.AUTUMN_LEAVES -> {
      val cy = ((spec.seedY + t * spec.speed * 0.85f) % 1.14f - 0.07f) * canvasH
      val cx = ((spec.seedX + sin(cycleAngle) * spec.swayAmplitude * 1.4f + 1f) % 1f) * canvasW
      val color =
        when (spec.colorVariant) {
          0 -> Color(0xFFE65100).copy(alpha = 0.62f)
          1 -> Color(0xFFF57C00).copy(alpha = 0.58f)
          2 -> Color(0xFFD84315).copy(alpha = 0.60f)
          else -> Color(0xFFFFB300).copy(alpha = 0.62f)
        }
      rotate(degrees = spec.initialRotation + t * 360f * 1.5f, pivot = Offset(cx, cy)) {
        val path =
          Path().apply {
            moveTo(cx, cy - sizePx * 0.8f)
            quadraticTo(cx + sizePx * 0.9f, cy - sizePx * 0.1f, cx, cy + sizePx * 0.8f)
            quadraticTo(cx - sizePx * 0.9f, cy - sizePx * 0.1f, cx, cy - sizePx * 0.8f)
            close()
          }
        drawPath(path = path, color = color)
      }
    }

    WeatherConditionKind.CHERRY_BREEZE -> {
      val cy = ((spec.seedY + t * spec.speed * 0.75f) % 1.12f - 0.06f) * canvasH
      val cx = ((spec.seedX + t * 0.35f + sin(cycleAngle) * spec.swayAmplitude + 1f) % 1f) * canvasW
      val color =
        when (spec.colorVariant) {
          0 -> Color(0xFFF8BBD0).copy(alpha = 0.72f)
          1 -> Color(0xFFF48FB1).copy(alpha = 0.65f)
          else -> Color(0xFFFFE4EC).copy(alpha = 0.74f)
        }
      rotate(degrees = spec.initialRotation + t * 240f, pivot = Offset(cx, cy)) {
        drawOval(
          color = color,
          topLeft = Offset(cx - sizePx * 0.55f, cy - sizePx * 0.32f),
          size = Size(sizePx * 1.1f, sizePx * 0.64f),
        )
      }
    }

    WeatherConditionKind.SNOWY -> {
      val cy = ((spec.seedY + t * spec.speed * 0.6f) % 1.1f - 0.05f) * canvasH
      val cx = ((spec.seedX + sin(cycleAngle) * spec.swayAmplitude + 1f) % 1f) * canvasW
      val alpha = 0.48f + 0.25f * sin(cycleAngle).coerceIn(-1f, 1f)
      drawCircle(
        color = Color(0xFFE3F2FD).copy(alpha = alpha),
        radius = sizePx * 0.36f,
        center = Offset(cx, cy),
      )
      drawCircle(
        color = Color.White.copy(alpha = alpha * 0.9f),
        radius = sizePx * 0.18f,
        center = Offset(cx, cy),
      )
    }

    WeatherConditionKind.RAINY,
    WeatherConditionKind.STORMY -> {
      val speedMul = if (condition == WeatherConditionKind.STORMY) 2.2f else 1.5f
      val cy = ((spec.seedY + t * spec.speed * speedMul) % 1.15f - 0.08f) * canvasH
      val slantX = if (condition == WeatherConditionKind.STORMY) sizePx * 1.2f else sizePx * 0.45f
      val cx = ((spec.seedX + t * 0.25f) % 1f) * canvasW
      val rainColor =
        if (condition == WeatherConditionKind.STORMY) Color(0xFF90CAF9).copy(alpha = 0.48f)
        else Color(0xFF64B5F6).copy(alpha = 0.44f)
      drawLine(
        color = rainColor,
        start = Offset(cx, cy),
        end = Offset(cx - slantX, cy + sizePx * 2.1f),
        strokeWidth = 1.6.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }

    WeatherConditionKind.RAINBOW_SHOWER -> {
      val cy = ((spec.seedY + t * spec.speed * 1.25f) % 1.12f - 0.06f) * canvasH
      val cx = ((spec.seedX + t * 0.15f) % 1f) * canvasW
      val color =
        when (spec.colorVariant) {
          0 -> Color(0xFF81D4FA).copy(alpha = 0.55f)
          1 -> Color(0xFFF48FB1).copy(alpha = 0.52f)
          2 -> Color(0xFFFFE082).copy(alpha = 0.56f)
          else -> Color(0xFFA5D6A7).copy(alpha = 0.52f)
        }
      drawLine(
        color = color,
        start = Offset(cx, cy),
        end = Offset(cx - sizePx * 0.35f, cy + sizePx * 1.4f),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }

    WeatherConditionKind.METEOR_SHOWER -> {
      val prog = (spec.seedY + t * spec.speed * 1.4f) % 1.2f
      val cx = ((spec.seedX + prog * 0.45f) % 1.1f) * canvasW
      val cy = prog * canvasH
      val color =
        when (spec.colorVariant) {
          0 -> Color(0xFFFFF59D).copy(alpha = 0.72f)
          1 -> Color(0xFFB3E5FC).copy(alpha = 0.68f)
          else -> Color(0xFFE1BEE7).copy(alpha = 0.64f)
        }
      drawLine(
        color = color.copy(alpha = 0.38f),
        start = Offset(cx - sizePx * 2.4f, cy - sizePx * 2.4f),
        end = Offset(cx, cy),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
      )
      drawSparkle(center = Offset(cx, cy), radius = sizePx * 0.52f, color = color)
    }

    WeatherConditionKind.AURORA_BOREALIS -> {
      val cy = ((spec.seedY - t * spec.speed * 0.35f + 1f) % 1f) * canvasH
      val cx = ((spec.seedX + sin(cycleAngle) * spec.swayAmplitude * 1.3f + 1f) % 1f) * canvasW
      val alpha = 0.25f + 0.3f * ((sin(cycleAngle) + 1f) / 2f)
      val color =
        when (spec.colorVariant) {
          0 -> Color(0xFF4DD0E1).copy(alpha = alpha)
          1 -> Color(0xFF80CBC4).copy(alpha = alpha)
          2 -> Color(0xFFB388FF).copy(alpha = alpha)
          else -> Color(0xFFA7FFEB).copy(alpha = alpha)
        }
      drawCircle(color = color, radius = sizePx * 1.05f, center = Offset(cx, cy))
    }

    WeatherConditionKind.SUNSET_GAZE -> {
      val cy = ((spec.seedY - t * spec.speed * 0.45f + 1f) % 1f) * canvasH
      val cx = ((spec.seedX + sin(cycleAngle) * spec.swayAmplitude + 1f) % 1f) * canvasW
      val twinkle = 0.35f + 0.38f * ((cos(cycleAngle) + 1f) / 2f)
      val color =
        when (spec.colorVariant) {
          0 -> Color(0xFFFFAB40).copy(alpha = twinkle)
          1 -> Color(0xFFFF7043).copy(alpha = twinkle)
          else -> Color(0xFFFFE082).copy(alpha = twinkle)
        }
      drawSparkle(center = Offset(cx, cy), radius = sizePx * 0.55f, color = color)
    }

    WeatherConditionKind.FOGGY_MIST,
    WeatherConditionKind.CLOUDY -> {
      val cx = ((spec.seedX + t * spec.speed * 0.3f) % 1.2f - 0.1f) * canvasW
      val cy = ((spec.seedY + cos(cycleAngle) * 0.015f + 1f) % 1f) * canvasH
      val alpha = if (condition == WeatherConditionKind.FOGGY_MIST) 0.18f else 0.12f
      drawCircle(
        color = Color.White.copy(alpha = alpha),
        radius = sizePx * 2.2f,
        center = Offset(cx, cy),
      )
    }

    WeatherConditionKind.SUNNY -> {
      val cy = ((spec.seedY - t * spec.speed * 0.3f + 1f) % 1f) * canvasH
      val cx = ((spec.seedX + sin(cycleAngle) * spec.swayAmplitude + 1f) % 1f) * canvasW
      val alpha = 0.25f + 0.4f * ((sin(cycleAngle) + 1f) / 2f)
      drawSparkle(
        center = Offset(cx, cy),
        radius = sizePx * 0.55f,
        color = Color(0xFFFFE082).copy(alpha = alpha),
      )
    }
  }
}

private fun DrawScope.drawSparkle(
  center: Offset,
  radius: Float,
  color: Color,
) {
  drawLine(
    color = color,
    start = Offset(center.x - radius, center.y),
    end = Offset(center.x + radius, center.y),
    strokeWidth = (radius * 0.28f).coerceAtLeast(1.4f),
    cap = StrokeCap.Round,
  )
  drawLine(
    color = color,
    start = Offset(center.x, center.y - radius),
    end = Offset(center.x, center.y + radius),
    strokeWidth = (radius * 0.28f).coerceAtLeast(1.4f),
    cap = StrokeCap.Round,
  )
  drawCircle(color = color, radius = radius * 0.34f, center = center)
}
