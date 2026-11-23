package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.data.model.MoonlightPhase
import com.example.data.weather.WeatherConditionKind
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

private data class CatPalette(
  val bodyMain: Color,
  val bodySecondary: Color,
  val stripeOrPatch: Color,
  val earInner: Color,
  val cheek: Color,
  val nose: Color,
  val eye: Color,
  val outline: Color,
  val isCosmic: Boolean = false,
)

private fun paletteForPattern(patternId: String): CatPalette {
  return when (patternId) {
    "tuxedo" ->
      CatPalette(
        bodyMain = Color(0xFF282C37),
        bodySecondary = Color(0xFFFDFBF7),
        stripeOrPatch = Color(0xFF1A1D24),
        earInner = Color(0xFFF2A6B3),
        cheek = Color(0xFFFF96A8),
        nose = Color(0xFFF28B9C),
        eye = Color(0xFF5CD6A9),
        outline = Color(0xFF161820),
      )
    "calico" ->
      CatPalette(
        bodyMain = Color(0xFFFFF8EE),
        bodySecondary = Color(0xFFEA8C44),
        stripeOrPatch = Color(0xFF4B3832),
        earInner = Color(0xFFF6AEAE),
        cheek = Color(0xFFFF9B9B),
        nose = Color(0xFFE87A88),
        eye = Color(0xFF3E7858),
        outline = Color(0xFF3D2B24),
      )
    "siamese" ->
      CatPalette(
        bodyMain = Color(0xFFF6EAD7),
        bodySecondary = Color(0xFF6B4E3D),
        stripeOrPatch = Color(0xFF533B2E),
        earInner = Color(0xFFE6A5A5),
        cheek = Color(0xFFFFA5AC),
        nose = Color(0xFF4A3327),
        eye = Color(0xFF4CA8E0),
        outline = Color(0xFF422F26),
      )
    "sakura" ->
      CatPalette(
        bodyMain = Color(0xFFFFDCE6),
        bodySecondary = Color(0xFFFFF5F8),
        stripeOrPatch = Color(0xFFF59AB4),
        earInner = Color(0xFFFF9EBB),
        cheek = Color(0xFFFF7597),
        nose = Color(0xFFE0527B),
        eye = Color(0xFF6B3E5E),
        outline = Color(0xFF7D455E),
      )
    "cosmic" ->
      CatPalette(
        bodyMain = Color(0xFF2E295E),
        bodySecondary = Color(0xFF55479A),
        stripeOrPatch = Color(0xFFFFD86B),
        earInner = Color(0xFFBA9BFF),
        cheek = Color(0xFFE38EFF),
        nose = Color(0xFFFFCC66),
        eye = Color(0xFF88F3FF),
        outline = Color(0xFF1B173B),
        isCosmic = true,
      )
    "lantern_koi" ->
      CatPalette(
        bodyMain = Color(0xFFFFFAF3),
        bodySecondary = Color(0xFFE53935),
        stripeOrPatch = Color(0xFFFFB300),
        earInner = Color(0xFFFFA5B0),
        cheek = Color(0xFFFF8095),
        nose = Color(0xFFE5505C),
        eye = Color(0xFF00897B),
        outline = Color(0xFF4E2723),
      )
    "golden_taiyaki" ->
      CatPalette(
        bodyMain = Color(0xFFF6B94D),
        bodySecondary = Color(0xFFFFF2C4),
        stripeOrPatch = Color(0xFFA8551B),
        earInner = Color(0xFFFFBD9B),
        cheek = Color(0xFFFF8F73),
        nose = Color(0xFFD65E36),
        eye = Color(0xFF4E2A14),
        outline = Color(0xFF592D12),
      )
    "solar_sphinx" ->
      CatPalette(
        bodyMain = Color(0xFFFFF6DE),
        bodySecondary = Color(0xFFFFA726),
        stripeOrPatch = Color(0xFFFF7043),
        earInner = Color(0xFFFFC09F),
        cheek = Color(0xFFFF9671),
        nose = Color(0xFFE6673A),
        eye = Color(0xFF1E88E5),
        outline = Color(0xFF52351E),
      )
    "lunar_eclipse" ->
      CatPalette(
        bodyMain = Color(0xFF1D1B2A),
        bodySecondary = Color(0xFF69182D),
        stripeOrPatch = Color(0xFFFF5252),
        earInner = Color(0xFFB33951),
        cheek = Color(0xFFE56B7A),
        nose = Color(0xFFFF6E6E),
        eye = Color(0xFFFFD54F),
        outline = Color(0xFF0C0A14),
        isCosmic = true,
      )
    "cinnamon_swirl" ->
      CatPalette(
        bodyMain = Color(0xFFC97E4B),
        bodySecondary = Color(0xFFFFF1DE),
        stripeOrPatch = Color(0xFF7B3F1D),
        earInner = Color(0xFFF2AA9C),
        cheek = Color(0xFFFF9688),
        nose = Color(0xFFA64A32),
        eye = Color(0xFF422C1D),
        outline = Color(0xFF4A2512),
      )
    "aurora_tabby" ->
      CatPalette(
        bodyMain = Color(0xFF6AD0CA),
        bodySecondary = Color(0xFFE0FBFF),
        stripeOrPatch = Color(0xFF7C4DFF),
        earInner = Color(0xFFB39DDB),
        cheek = Color(0xFFFF9EC7),
        nose = Color(0xFF6D5BD0),
        eye = Color(0xFFFFEE58),
        outline = Color(0xFF1A3A47),
        isCosmic = true,
      )
    "november_maple_tabby" ->
      CatPalette(
        bodyMain = Color(0xFFD96B27),
        bodySecondary = Color(0xFFFFE5BD),
        stripeOrPatch = Color(0xFF9C2D19),
        earInner = Color(0xFFFFB299),
        cheek = Color(0xFFFF867C),
        nose = Color(0xFFB73A22),
        eye = Color(0xFF2E4E3F),
        outline = Color(0xFF4A1E0C),
      )
    "golden_touch_cat" ->
      CatPalette(
        bodyMain = Color(0xFFFFD54F),
        bodySecondary = Color(0xFFFFF8D6),
        stripeOrPatch = Color(0xFFE59800),
        earInner = Color(0xFFFFE29A),
        cheek = Color(0xFFFFAB40),
        nose = Color(0xFFD47600),
        eye = Color(0xFF00B8D4),
        outline = Color(0xFF7A4E00),
        isCosmic = true,
      )
    "prism_galaxy" ->
      CatPalette(
        bodyMain = Color(0xFF1E2D5A),
        bodySecondary = Color(0xFF80F5FF),
        stripeOrPatch = Color(0xFFB388FF),
        earInner = Color(0xFF9CFFFF),
        cheek = Color(0xFFFF80AB),
        nose = Color(0xFF80F5FF),
        eye = Color(0xFFFFF176),
        outline = Color(0xFF0E1838),
        isCosmic = true,
      )
    "pumpkin_pie_tabby" ->
      CatPalette(
        bodyMain = Color(0xFFEA7C24),
        bodySecondary = Color(0xFFFFF4E0),
        stripeOrPatch = Color(0xFF9C3D0C),
        earInner = Color(0xFFFFBC8A),
        cheek = Color(0xFFFF906E),
        nose = Color(0xFFB8421B),
        eye = Color(0xFF3E2411),
        outline = Color(0xFF4F1D06),
      )
    "cranberry_calico" ->
      CatPalette(
        bodyMain = Color(0xFFFFF5EA),
        bodySecondary = Color(0xFF9E1F38),
        stripeOrPatch = Color(0xFFD68C22),
        earInner = Color(0xFFF699AA),
        cheek = Color(0xFFFF758F),
        nose = Color(0xFF9B2242),
        eye = Color(0xFF2B5B3F),
        outline = Color(0xFF42101F),
      )
    "pilgrim_pheasant" ->
      CatPalette(
        bodyMain = Color(0xFFA6602A),
        bodySecondary = Color(0xFFFAD390),
        stripeOrPatch = Color(0xFF8A1C1C),
        earInner = Color(0xFFE5A27B),
        cheek = Color(0xFFFF8C69),
        nose = Color(0xFF6B2D16),
        eye = Color(0xFFFFC107),
        outline = Color(0xFF3B1A08),
      )
    "roasted_crown_calico" ->
      CatPalette(
        bodyMain = Color(0xFFFFF4E4),
        bodySecondary = Color(0xFF8C3B14),
        stripeOrPatch = Color(0xFFE5A823),
        earInner = Color(0xFFF7AE9E),
        cheek = Color(0xFFFF8F85),
        nose = Color(0xFFA6402B),
        eye = Color(0xFF2E6B4E),
        outline = Color(0xFF3E1806),
        isCosmic = true,
      )
    "velvet_gravy_tabby" ->
      CatPalette(
        bodyMain = Color(0xFFB55A16),
        bodySecondary = Color(0xFFFFE59E),
        stripeOrPatch = Color(0xFFFFC436),
        earInner = Color(0xFFF2A47E),
        cheek = Color(0xFFFF906E),
        nose = Color(0xFF7D2E0C),
        eye = Color(0xFF40E0D0),
        outline = Color(0xFF381604),
        isCosmic = true,
      )
    else -> // "tabby"
      CatPalette(
        bodyMain = Color(0xFFF49A48),
        bodySecondary = Color(0xFFFFE9CF),
        stripeOrPatch = Color(0xFFC8621E),
        earInner = Color(0xFFFFB2B8),
        cheek = Color(0xFFFF8C94),
        nose = Color(0xFFE56B7A),
        eye = Color(0xFF362820),
        outline = Color(0xFF4B2B18),
      )
  }
}

@Composable
fun InteractivePurrCat(
  catName: String,
  patternId: String,
  accessoryId: String,
  weatherCondition: WeatherConditionKind,
  moonlightPhase: MoonlightPhase,
  comboCharge: Float,
  onPetAt: (xFraction: Float, yFraction: Float) -> Unit,
  modifier: Modifier = Modifier,
  isGoldenRainActive: Boolean = false,
) {
  var lastPetMillis by remember { mutableLongStateOf(0L) }
  var isHappyPurring by remember { mutableLongStateOf(0L) }

  LaunchedEffect(lastPetMillis) {
    if (lastPetMillis > 0L) {
      isHappyPurring = lastPetMillis
      delay(380L)
      if (isHappyPurring == lastPetMillis) {
        isHappyPurring = 0L
      }
    }
  }

  val isPurring = isHappyPurring > 0L || comboCharge > 0.72f

  val squishScale by
    animateFloatAsState(
      targetValue = if (isHappyPurring > 0L) 1.07f else 1f,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMedium,
        ),
      label = "catSquish",
    )

  val infinite = rememberInfiniteTransition(label = "catIdle")
  val breathe by
    infinite.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "breathe",
    )

  val tailSwing by
    infinite.animateFloat(
      initialValue = -16f,
      targetValue = 16f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = if (isPurring) 700 else 1600, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "tailSwing",
    )

  val weatherPhase by
    infinite.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "weatherPhase",
    )

  Box(
    modifier =
      modifier
        .semantics { contentDescription = "Interactive pettable cat named $catName" }
        .pointerInput(Unit) {
          detectTapGestures { offset ->
            lastPetMillis = System.currentTimeMillis()
            val xFrac = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
            val yFrac = (offset.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
            onPetAt(xFrac, yFrac)
          }
        }
  ) {
    Canvas(
      modifier =
        Modifier.fillMaxSize().graphicsLayer {
          scaleX = squishScale
          scaleY = if (isHappyPurring > 0L) 0.96f else 1f + breathe * 0.015f
        }
    ) {
      drawWeatherAtmosphere(weatherCondition, moonlightPhase, weatherPhase)
      if (isGoldenRainActive) {
        drawGoldenRainAtmosphere(weatherPhase)
      }
      drawCatCharacter(
        patternId = patternId,
        accessoryId = accessoryId,
        tailAngle = tailSwing,
        breatheOffset = breathe,
        isHappyEyes = isPurring,
        showFullBody = true,
      )
    }
  }
}

@Composable
fun CatAvatarMini(
  patternId: String,
  accessoryId: String,
  modifier: Modifier = Modifier.size(48.dp),
) {
  Canvas(modifier = modifier) {
    drawCatCharacter(
      patternId = patternId,
      accessoryId = accessoryId,
      tailAngle = 0f,
      breatheOffset = 0f,
      isHappyEyes = true,
      showFullBody = false,
    )
  }
}

private fun DrawScope.drawWeatherAtmosphere(
  condition: WeatherConditionKind,
  moonlightPhase: MoonlightPhase,
  phase: Float,
) {
  val w = size.width
  val h = size.height

  // Moonlight / Bloodmoon Night Sky Overlay (Midnight until Morning)
  when (moonlightPhase) {
    MoonlightPhase.MOONLIGHT -> {
      drawRect(
        brush =
          Brush.verticalGradient(
            colors = listOf(Color(0x55181C4A), Color(0x222D3575), Color.Transparent)
          )
      )
      // Silver full moon with crescent shadow
      val moonCenter = Offset(w * 0.80f, h * 0.19f)
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(Color(0x66DCE8FF), Color.Transparent),
            center = moonCenter,
            radius = w * 0.22f,
          ),
        radius = w * 0.22f,
        center = moonCenter,
      )
      drawCircle(color = Color(0xFFEAF1FF), radius = w * 0.068f, center = moonCenter)
      // Twinkling stars
      for (i in 0..7) {
        val sx = w * (0.10f + i * 0.11f)
        val sy = h * (0.10f + (i % 3) * 0.08f)
        val twinkle = 2.2f + (sin(phase * 2 * PI + i) + 1f).toFloat() * 1.3f
        drawCircle(color = Color(0xCCFFF8D6), radius = twinkle, center = Offset(sx, sy))
      }
    }
    MoonlightPhase.BLOODMOON -> {
      drawRect(
        brush =
          Brush.verticalGradient(
            colors = listOf(Color(0x66450914), Color(0x337A1222), Color.Transparent)
          )
      )
      // Crimson Bloodmoon
      val moonCenter = Offset(w * 0.80f, h * 0.19f)
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(Color(0x88E53935), Color.Transparent),
            center = moonCenter,
            radius = w * 0.28f,
          ),
        radius = w * 0.28f,
        center = moonCenter,
      )
      drawCircle(color = Color(0xFFD32F2F), radius = w * 0.075f, center = moonCenter)
      drawCircle(color = Color(0x557F0000), radius = w * 0.032f, center = Offset(moonCenter.x - w * 0.02f, moonCenter.y + h * 0.01f))
      // Crimson embers
      for (i in 0..9) {
        val ex = w * (0.08f + i * 0.095f) + sin(phase * 2 * PI + i).toFloat() * 8f
        val ey = h * (0.85f - ((phase + i * 0.19f) % 0.8f))
        drawCircle(color = Color(0xAAFF6E6E), radius = 3.2f, center = Offset(ex, ey))
      }
    }
    MoonlightPhase.INACTIVE -> Unit
  }

  // Soft floor shadow cushion
  drawOval(
    brush =
      Brush.radialGradient(
        colors = listOf(Color.Black.copy(alpha = 0.14f), Color.Transparent),
        center = Offset(w * 0.5f, h * 0.85f),
        radius = w * 0.36f,
      ),
    topLeft = Offset(w * 0.16f, h * 0.77f),
    size = Size(w * 0.68f, h * 0.16f),
  )

  when (condition) {
    WeatherConditionKind.SUNNY -> {
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(Color(0x55FFD66B), Color(0x00FFD66B)),
            center = Offset(w * 0.82f, h * 0.18f),
            radius = w * 0.32f,
          ),
        radius = w * 0.32f,
        center = Offset(w * 0.82f, h * 0.18f),
      )
    }
    WeatherConditionKind.SUNSET_GAZE -> {
      drawRect(
        brush =
          Brush.verticalGradient(
            colors = listOf(Color(0x4D7B3FE4), Color(0x55FF6B4A), Color(0x44FFC95C), Color.Transparent)
          )
      )
      val sunCenter = Offset(w * 0.22f, h * 0.28f)
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(Color(0x99FFB74D), Color(0x44FF5722), Color.Transparent),
            center = sunCenter,
            radius = w * 0.34f,
          ),
        radius = w * 0.34f,
        center = sunCenter,
      )
      drawCircle(color = Color(0xFFFFCA6B), radius = w * 0.068f, center = sunCenter)
    }
    WeatherConditionKind.RAINY, WeatherConditionKind.STORMY -> {
      val dropColor =
        if (condition == WeatherConditionKind.STORMY) Color(0x705875A8) else Color(0x6674A9E8)
      for (i in 0..10) {
        val x = w * (0.08f + (i * 0.085f))
        val y = ((phase + i * 0.17f) % 1f) * h * 0.9f
        drawLine(
          color = dropColor,
          start = Offset(x, y),
          end = Offset(x - 6f, y + 18f),
          strokeWidth = 3f,
          cap = StrokeCap.Round,
        )
      }
    }
    WeatherConditionKind.SNOWY -> {
      for (i in 0..11) {
        val x = w * (0.06f + (i * 0.08f)) + sin(phase * 2 * PI + i).toFloat() * 10f
        val y = ((phase + i * 0.13f) % 1f) * h * 0.9f
        drawCircle(
          color = Color(0xBBFFFFFF),
          radius = 4f + (i % 3) * 1.5f,
          center = Offset(x, y),
        )
      }
    }
    WeatherConditionKind.CLOUDY -> {
      val drift = cos(phase * 2 * PI).toFloat() * 10f
      drawCircle(color = Color(0x40FFFFFF), radius = w * 0.09f, center = Offset(w * 0.18f + drift, h * 0.18f))
      drawCircle(color = Color(0x40FFFFFF), radius = w * 0.07f, center = Offset(w * 0.25f + drift, h * 0.19f))
      drawCircle(color = Color(0x40FFFFFF), radius = w * 0.08f, center = Offset(w * 0.78f - drift, h * 0.20f))
    }
    WeatherConditionKind.CHERRY_BREEZE -> {
      for (i in 0..11) {
        val px = ((w * (0.08f + i * 0.09f) + phase * w * 0.35f) % w)
        val py = ((phase + i * 0.14f) % 1f) * h * 0.88f
        rotate(degrees = (phase * 360f + i * 35f), pivot = Offset(px, py)) {
          drawOval(
            color = if (i % 2 == 0) Color(0xCCFFB7C5) else Color(0xCCFF8FA3),
            topLeft = Offset(px - 6f, py - 3.5f),
            size = Size(12f, 7f),
          )
        }
      }
    }
    WeatherConditionKind.FOGGY_MIST -> {
      val drift = sin(phase * 2 * PI).toFloat() * 18f
      drawOval(
        color = Color(0x44E6EEF4),
        topLeft = Offset(-w * 0.10f + drift, h * 0.56f),
        size = Size(w * 0.75f, h * 0.18f),
      )
      drawOval(
        color = Color(0x3CDFE9F2),
        topLeft = Offset(w * 0.30f - drift, h * 0.68f),
        size = Size(w * 0.80f, h * 0.20f),
      )
      drawOval(
        color = Color(0x33FFFFFF),
        topLeft = Offset(w * 0.08f + drift * 0.6f, h * 0.18f),
        size = Size(w * 0.65f, h * 0.14f),
      )
    }
    WeatherConditionKind.RAINBOW_SHOWER -> {
      val arcColors =
        listOf(
          Color(0x44FF6B6B),
          Color(0x44FFD93D),
          Color(0x446BCB77),
          Color(0x444D96FF),
          Color(0x449B72CF),
        )
      arcColors.forEachIndexed { idx, color ->
        val inset = idx * 8f
        drawArc(
          color = color,
          startAngle = 195f,
          sweepAngle = 150f,
          useCenter = false,
          topLeft = Offset(w * 0.08f + inset, h * 0.08f + inset),
          size = Size(w * 0.84f - inset * 2f, h * 0.76f - inset * 2f),
          style = Stroke(width = 7f, cap = StrokeCap.Round),
        )
      }
      for (i in 0..7) {
        val rx = w * (0.12f + i * 0.11f)
        val ry = ((phase + i * 0.19f) % 1f) * h * 0.88f
        drawLine(
          color = Color(0x5574B8F0),
          start = Offset(rx, ry),
          end = Offset(rx - 4f, ry + 14f),
          strokeWidth = 2.5f,
          cap = StrokeCap.Round,
        )
      }
    }
    WeatherConditionKind.METEOR_SHOWER -> {
      drawRect(
        brush =
          Brush.verticalGradient(
            colors = listOf(Color(0x441B1E4B), Color(0x222D2A6E), Color.Transparent)
          )
      )
      for (i in 0..6) {
        val sx = w * (0.12f + i * 0.13f)
        val sy = h * (0.09f + (i % 3) * 0.09f)
        val r = 2f + (sin(phase * 2 * PI + i) + 1f).toFloat() * 1.4f
        drawCircle(color = Color(0xDDFFF7C2), radius = r, center = Offset(sx, sy))
      }
      for (m in 0..2) {
        val progress = (phase + m * 0.33f) % 1f
        val startX = w * (0.25f + m * 0.25f) + progress * w * 0.35f
        val startY = h * 0.06f + progress * h * 0.36f
        drawLine(
          brush = Brush.linearGradient(listOf(Color.Transparent, Color(0xEEFFF59D))),
          start = Offset(startX - 36f, startY - 22f),
          end = Offset(startX, startY),
          strokeWidth = 3.2f,
          cap = StrokeCap.Round,
        )
      }
    }
    WeatherConditionKind.AURORA_BOREALIS -> {
      val wave = sin(phase * 2 * PI).toFloat() * h * 0.04f
      drawRect(
        brush =
          Brush.verticalGradient(
            colors = listOf(Color(0x330F293A), Color(0x1A184C5C), Color.Transparent)
          )
      )
      val aurora1 =
        Path().apply {
          moveTo(0f, h * 0.24f + wave)
          cubicTo(w * 0.30f, h * 0.10f - wave, w * 0.65f, h * 0.32f + wave, w, h * 0.16f - wave)
        }
      val aurora2 =
        Path().apply {
          moveTo(0f, h * 0.30f - wave)
          cubicTo(w * 0.35f, h * 0.18f + wave, w * 0.72f, h * 0.38f - wave, w, h * 0.24f + wave)
        }
      drawPath(
        path = aurora1,
        brush = Brush.horizontalGradient(listOf(Color(0x664DF0B8), Color(0x6644C8FF), Color(0x66B378FF))),
        style = Stroke(width = h * 0.09f, cap = StrokeCap.Round),
      )
      drawPath(
        path = aurora2,
        brush = Brush.horizontalGradient(listOf(Color(0x447C4DFF), Color(0x444DF0B8), Color(0x4444C8FF))),
        style = Stroke(width = h * 0.07f, cap = StrokeCap.Round),
      )
    }
    WeatherConditionKind.AUTUMN_LEAVES -> {
      val leafPalette = listOf(Color(0xDDF57C00), Color(0xDDD84315), Color(0xDDFBC02D))
      for (i in 0..10) {
        val lx = w * (0.08f + i * 0.085f) + sin(phase * 2 * PI + i).toFloat() * 14f
        val ly = ((phase + i * 0.15f) % 1f) * h * 0.90f
        rotate(degrees = phase * 240f + i * 42f, pivot = Offset(lx, ly)) {
          drawOval(
            color = leafPalette[i % leafPalette.size],
            topLeft = Offset(lx - 7f, ly - 4f),
            size = Size(14f, 8f),
          )
        }
      }
    }
  }
}

private fun DrawScope.drawGoldenRainAtmosphere(phase: Float) {
  val w = size.width
  val h = size.height
  drawRect(
    brush =
      Brush.verticalGradient(
        colors = listOf(Color(0x55FFD54F), Color(0x28FFB300), Color.Transparent)
      )
  )
  for (i in 0..14) {
    val gx = w * (0.05f + i * 0.065f)
    val gy = ((phase * 1.4f + i * 0.13f) % 1f) * h * 0.94f
    drawLine(
      brush = Brush.verticalGradient(listOf(Color(0x00FFF59D), Color(0xEEFFD54F))),
      start = Offset(gx + 4f, gy - 20f),
      end = Offset(gx, gy),
      strokeWidth = 3.5f,
      cap = StrokeCap.Round,
    )
    drawCircle(
      color = Color(0xEEFFF8C6),
      radius = if (i % 3 == 0) 3.8f else 2.5f,
      center = Offset(gx, gy),
    )
  }
}

private fun DrawScope.drawCatCharacter(
  patternId: String,
  accessoryId: String,
  tailAngle: Float,
  breatheOffset: Float,
  isHappyEyes: Boolean,
  showFullBody: Boolean,
) {
  val palette = paletteForPattern(patternId)
  val w = size.width
  val h = size.height
  val strokeW = (w * 0.018f).coerceIn(2f, 5.5f)

  val headCenterY = if (showFullBody) h * 0.39f + breatheOffset * 3f else h * 0.56f
  val headRadius = if (showFullBody) w * 0.23f else w * 0.34f
  val cx = w * 0.5f

  if (showFullBody) {
    // Wagging Tail behind body
    rotate(degrees = tailAngle, pivot = Offset(cx + headRadius * 0.7f, h * 0.72f)) {
      val tailPath =
        Path().apply {
          moveTo(cx + headRadius * 0.65f, h * 0.72f)
          cubicTo(
            cx + headRadius * 1.55f,
            h * 0.68f,
            cx + headRadius * 1.65f,
            h * 0.44f,
            cx + headRadius * 1.25f,
            h * 0.42f,
          )
        }
      drawPath(
        path = tailPath,
        color = if (patternId == "siamese") palette.bodySecondary else palette.bodyMain,
        style = Stroke(width = headRadius * 0.38f, cap = StrokeCap.Round),
      )
      drawPath(
        path = tailPath,
        color = palette.outline,
        style = Stroke(width = strokeW, cap = StrokeCap.Round),
      )
    }

    // Chubby Sitting Body
    val bodyTop = headCenterY + headRadius * 0.55f
    val bodyW = headRadius * 1.9f
    val bodyH = h * 0.36f
    drawRoundRect(
      color = palette.bodyMain,
      topLeft = Offset(cx - bodyW / 2f, bodyTop),
      size = Size(bodyW, bodyH),
      cornerRadius = CornerRadius(bodyW * 0.48f, bodyW * 0.48f),
    )

    // Pattern markings on body
    when (patternId) {
      "tuxedo" -> {
        drawOval(
          color = palette.bodySecondary,
          topLeft = Offset(cx - bodyW * 0.26f, bodyTop + bodyH * 0.14f),
          size = Size(bodyW * 0.52f, bodyH * 0.68f),
        )
      }
      "calico" -> {
        drawCircle(
          color = palette.bodySecondary,
          radius = bodyW * 0.20f,
          center = Offset(cx - bodyW * 0.25f, bodyTop + bodyH * 0.42f),
        )
        drawCircle(
          color = palette.stripeOrPatch,
          radius = bodyW * 0.17f,
          center = Offset(cx + bodyW * 0.24f, bodyTop + bodyH * 0.55f),
        )
      }
      "cosmic" -> {
        drawCircle(color = palette.stripeOrPatch, radius = 3.5f, center = Offset(cx - bodyW * 0.2f, bodyTop + bodyH * 0.45f))
        drawCircle(color = Color.White, radius = 2.5f, center = Offset(cx + bodyW * 0.18f, bodyTop + bodyH * 0.35f))
        drawCircle(color = palette.stripeOrPatch, radius = 2.5f, center = Offset(cx + bodyW * 0.05f, bodyTop + bodyH * 0.65f))
      }
      "tabby", "cinnamon_swirl" -> {
        drawOval(
          color = palette.bodySecondary,
          topLeft = Offset(cx - bodyW * 0.24f, bodyTop + bodyH * 0.2f),
          size = Size(bodyW * 0.48f, bodyH * 0.62f),
        )
      }
      "lantern_koi" -> {
        drawCircle(
          color = palette.bodySecondary,
          radius = bodyW * 0.19f,
          center = Offset(cx - bodyW * 0.20f, bodyTop + bodyH * 0.36f),
        )
        drawCircle(
          color = palette.stripeOrPatch,
          radius = bodyW * 0.15f,
          center = Offset(cx + bodyW * 0.22f, bodyTop + bodyH * 0.56f),
        )
      }
      "golden_taiyaki", "solar_sphinx" -> {
        drawOval(
          color = palette.bodySecondary,
          topLeft = Offset(cx - bodyW * 0.26f, bodyTop + bodyH * 0.16f),
          size = Size(bodyW * 0.52f, bodyH * 0.66f),
        )
      }
      "lunar_eclipse", "aurora_tabby" -> {
        drawOval(
          color = palette.bodySecondary.copy(alpha = 0.85f),
          topLeft = Offset(cx - bodyW * 0.22f, bodyTop + bodyH * 0.18f),
          size = Size(bodyW * 0.44f, bodyH * 0.62f),
        )
        drawCircle(color = palette.stripeOrPatch, radius = 3.2f, center = Offset(cx, bodyTop + bodyH * 0.48f))
      }
      "november_maple_tabby" -> {
        drawOval(
          color = palette.bodySecondary,
          topLeft = Offset(cx - bodyW * 0.25f, bodyTop + bodyH * 0.16f),
          size = Size(bodyW * 0.50f, bodyH * 0.64f),
        )
        drawCircle(color = palette.stripeOrPatch, radius = bodyW * 0.12f, center = Offset(cx - bodyW * 0.25f, bodyTop + bodyH * 0.45f))
      }
      "golden_touch_cat" -> {
        drawOval(
          color = palette.bodySecondary,
          topLeft = Offset(cx - bodyW * 0.26f, bodyTop + bodyH * 0.15f),
          size = Size(bodyW * 0.52f, bodyH * 0.66f),
        )
        drawCircle(color = Color.White, radius = 4f, center = Offset(cx - bodyW * 0.18f, bodyTop + bodyH * 0.38f))
        drawCircle(color = Color.White, radius = 3f, center = Offset(cx + bodyW * 0.20f, bodyTop + bodyH * 0.55f))
      }
      "pumpkin_pie_tabby", "pilgrim_pheasant", "velvet_gravy_tabby" -> {
        drawOval(
          color = palette.bodySecondary,
          topLeft = Offset(cx - bodyW * 0.26f, bodyTop + bodyH * 0.16f),
          size = Size(bodyW * 0.52f, bodyH * 0.64f),
        )
      }
      "cranberry_calico", "roasted_crown_calico" -> {
        drawCircle(
          color = palette.bodySecondary,
          radius = bodyW * 0.21f,
          center = Offset(cx - bodyW * 0.24f, bodyTop + bodyH * 0.40f),
        )
        drawCircle(
          color = palette.stripeOrPatch,
          radius = bodyW * 0.18f,
          center = Offset(cx + bodyW * 0.22f, bodyTop + bodyH * 0.56f),
        )
      }
    }

    drawRoundRect(
      color = palette.outline,
      topLeft = Offset(cx - bodyW / 2f, bodyTop),
      size = Size(bodyW, bodyH),
      cornerRadius = CornerRadius(bodyW * 0.48f, bodyW * 0.48f),
      style = Stroke(width = strokeW),
    )

    // Front Paws
    val pawColor =
      when (patternId) {
        "tuxedo", "tabby", "sakura", "november_maple_tabby", "golden_touch_cat" -> palette.bodySecondary
        "siamese" -> palette.bodySecondary
        else -> palette.bodyMain
      }
    val pawRadius = headRadius * 0.24f
    listOf(-1f, 1f).forEach { side ->
      val pawCenter = Offset(cx + side * bodyW * 0.22f, bodyTop + bodyH - pawRadius * 0.4f)
      drawCircle(color = pawColor, radius = pawRadius, center = pawCenter)
      drawCircle(
        color = palette.outline,
        radius = pawRadius,
        center = pawCenter,
        style = Stroke(width = strokeW),
      )
    }
  }

  // Ears
  val earBaseY = headCenterY - headRadius * 0.58f
  val earPeakY = headCenterY - headRadius * 1.42f
  val earFill = if (patternId == "siamese") palette.bodySecondary else palette.bodyMain

  listOf(-1f, 1f).forEach { side ->
    val outerEar =
      Path().apply {
        moveTo(cx + side * headRadius * 0.26f, earBaseY)
        lineTo(cx + side * headRadius * 0.88f, earPeakY)
        lineTo(cx + side * headRadius * 0.94f, headCenterY - headRadius * 0.08f)
        close()
      }
    val earColor =
      if (patternId == "calico") {
        if (side < 0) palette.bodySecondary else palette.stripeOrPatch
      } else {
        earFill
      }
    drawPath(path = outerEar, color = earColor)
    drawPath(
      path = outerEar,
      color = palette.outline,
      style = Stroke(width = strokeW, join = StrokeJoin.Round),
    )

    val innerEar =
      Path().apply {
        moveTo(cx + side * headRadius * 0.40f, earBaseY + headRadius * 0.04f)
        lineTo(cx + side * headRadius * 0.80f, earPeakY + headRadius * 0.22f)
        lineTo(cx + side * headRadius * 0.84f, headCenterY - headRadius * 0.14f)
        close()
      }
    drawPath(path = innerEar, color = palette.earInner)
  }

  // Head
  drawOval(
    color = palette.bodyMain,
    topLeft = Offset(cx - headRadius * 1.08f, headCenterY - headRadius * 0.92f),
    size = Size(headRadius * 2.16f, headRadius * 1.84f),
  )

  // Pattern markings on head
  when (patternId) {
    "tabby" -> {
      for (i in -1..1) {
        val x = cx + i * headRadius * 0.22f
        drawLine(
          color = palette.stripeOrPatch,
          start = Offset(x, headCenterY - headRadius * 0.82f),
          end = Offset(x, headCenterY - headRadius * 0.52f),
          strokeWidth = strokeW * 1.4f,
          cap = StrokeCap.Round,
        )
      }
    }
    "siamese" -> {
      drawOval(
        color = palette.bodySecondary.copy(alpha = 0.85f),
        topLeft = Offset(cx - headRadius * 0.52f, headCenterY - headRadius * 0.26f),
        size = Size(headRadius * 1.04f, headRadius * 0.72f),
      )
    }
    "tuxedo" -> {
      drawOval(
        color = palette.bodySecondary,
        topLeft = Offset(cx - headRadius * 0.45f, headCenterY + headRadius * 0.14f),
        size = Size(headRadius * 0.90f, headRadius * 0.65f),
      )
    }
    "calico" -> {
      drawCircle(
        color = palette.bodySecondary,
        radius = headRadius * 0.36f,
        center = Offset(cx - headRadius * 0.52f, headCenterY - headRadius * 0.38f),
      )
    }
    "sakura" -> {
      drawCircle(
        color = palette.stripeOrPatch,
        radius = headRadius * 0.11f,
        center = Offset(cx - headRadius * 0.46f, headCenterY - headRadius * 0.48f),
      )
      drawCircle(
        color = palette.stripeOrPatch,
        radius = headRadius * 0.09f,
        center = Offset(cx + headRadius * 0.50f, headCenterY - headRadius * 0.42f),
      )
    }
    "cosmic" -> {
      drawCircle(
        color = palette.stripeOrPatch,
        radius = headRadius * 0.08f,
        center = Offset(cx, headCenterY - headRadius * 0.56f),
      )
    }
    "lantern_koi" -> {
      drawCircle(
        color = palette.bodySecondary,
        radius = headRadius * 0.34f,
        center = Offset(cx - headRadius * 0.48f, headCenterY - headRadius * 0.44f),
      )
      drawCircle(
        color = palette.stripeOrPatch,
        radius = headRadius * 0.20f,
        center = Offset(cx + headRadius * 0.46f, headCenterY - headRadius * 0.46f),
      )
    }
    "golden_taiyaki", "cinnamon_swirl", "aurora_tabby", "november_maple_tabby", "golden_touch_cat", "pumpkin_pie_tabby", "pilgrim_pheasant", "velvet_gravy_tabby" -> {
      for (i in -1..1) {
        val x = cx + i * headRadius * 0.22f
        drawLine(
          color = palette.stripeOrPatch,
          start = Offset(x, headCenterY - headRadius * 0.82f),
          end = Offset(x, headCenterY - headRadius * 0.50f),
          strokeWidth = strokeW * 1.4f,
          cap = StrokeCap.Round,
        )
      }
    }
    "cranberry_calico", "roasted_crown_calico" -> {
      drawCircle(
        color = palette.bodySecondary,
        radius = headRadius * 0.34f,
        center = Offset(cx - headRadius * 0.48f, headCenterY - headRadius * 0.40f),
      )
      drawCircle(
        color = palette.stripeOrPatch,
        radius = headRadius * 0.22f,
        center = Offset(cx + headRadius * 0.48f, headCenterY - headRadius * 0.44f),
      )
    }
    "solar_sphinx" -> {
      drawCircle(
        color = palette.stripeOrPatch,
        radius = headRadius * 0.14f,
        center = Offset(cx, headCenterY - headRadius * 0.54f),
      )
      drawCircle(
        color = palette.bodySecondary,
        radius = headRadius * 0.07f,
        center = Offset(cx, headCenterY - headRadius * 0.54f),
      )
    }
    "lunar_eclipse" -> {
      drawCircle(
        color = palette.stripeOrPatch,
        radius = headRadius * 0.15f,
        center = Offset(cx, headCenterY - headRadius * 0.54f),
      )
      drawCircle(
        color = palette.bodyMain,
        radius = headRadius * 0.11f,
        center = Offset(cx + headRadius * 0.04f, headCenterY - headRadius * 0.55f),
      )
    }
  }

  drawOval(
    color = palette.outline,
    topLeft = Offset(cx - headRadius * 1.08f, headCenterY - headRadius * 0.92f),
    size = Size(headRadius * 2.16f, headRadius * 1.84f),
    style = Stroke(width = strokeW),
  )

  // Cheeks
  listOf(-1f, 1f).forEach { side ->
    drawOval(
      color = palette.cheek.copy(alpha = 0.65f),
      topLeft = Offset(cx + side * headRadius * 0.58f - headRadius * 0.18f, headCenterY + headRadius * 0.12f),
      size = Size(headRadius * 0.36f, headRadius * 0.20f),
    )
  }

  // Eyes
  val eyeY = headCenterY - headRadius * 0.04f
  val eyeOffsetX = headRadius * 0.42f
  if (isHappyEyes) {
    listOf(-1f, 1f).forEach { side ->
      val ex = cx + side * eyeOffsetX
      val happyArc =
        Path().apply {
          moveTo(ex - headRadius * 0.15f, eyeY + headRadius * 0.04f)
          quadraticTo(ex, eyeY - headRadius * 0.16f, ex + headRadius * 0.15f, eyeY + headRadius * 0.04f)
        }
      drawPath(
        path = happyArc,
        color = palette.outline,
        style = Stroke(width = strokeW * 1.25f, cap = StrokeCap.Round),
      )
    }
  } else {
    listOf(-1f, 1f).forEach { side ->
      val ex = cx + side * eyeOffsetX
      drawCircle(color = palette.eye, radius = headRadius * 0.14f, center = Offset(ex, eyeY))
      drawCircle(
        color = Color.White,
        radius = headRadius * 0.055f,
        center = Offset(ex - headRadius * 0.04f, eyeY - headRadius * 0.045f),
      )
    }
  }

  // Nose + ":3" Smile
  val noseY = headCenterY + headRadius * 0.16f
  val nosePath =
    Path().apply {
      moveTo(cx - headRadius * 0.09f, noseY - headRadius * 0.04f)
      lineTo(cx + headRadius * 0.09f, noseY - headRadius * 0.04f)
      lineTo(cx, noseY + headRadius * 0.07f)
      close()
    }
  drawPath(path = nosePath, color = palette.nose)

  val mouthLeft =
    Path().apply {
      moveTo(cx, noseY + headRadius * 0.07f)
      quadraticTo(cx - headRadius * 0.10f, noseY + headRadius * 0.22f, cx - headRadius * 0.20f, noseY + headRadius * 0.14f)
    }
  val mouthRight =
    Path().apply {
      moveTo(cx, noseY + headRadius * 0.07f)
      quadraticTo(cx + headRadius * 0.10f, noseY + headRadius * 0.22f, cx + headRadius * 0.20f, noseY + headRadius * 0.14f)
    }
  drawPath(mouthLeft, color = palette.outline, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round))
  drawPath(mouthRight, color = palette.outline, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round))

  // Whiskers
  listOf(-1f, 1f).forEach { side ->
    val wxStart = cx + side * headRadius * 0.64f
    val wxEnd = cx + side * headRadius * 1.15f
    drawLine(
      color = palette.outline,
      start = Offset(wxStart, headCenterY + headRadius * 0.06f),
      end = Offset(wxEnd, headCenterY - headRadius * 0.04f),
      strokeWidth = strokeW * 0.75f,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = palette.outline,
      start = Offset(wxStart, headCenterY + headRadius * 0.19f),
      end = Offset(wxEnd, headCenterY + headRadius * 0.22f),
      strokeWidth = strokeW * 0.75f,
      cap = StrokeCap.Round,
    )
  }

  // Accessories
  val neckY = headCenterY + headRadius * 0.88f
  when (accessoryId) {
    "bell_collar" -> {
      drawLine(
        color = Color(0xFFD93829),
        start = Offset(cx - headRadius * 0.62f, neckY),
        end = Offset(cx + headRadius * 0.62f, neckY),
        strokeWidth = headRadius * 0.16f,
        cap = StrokeCap.Round,
      )
      drawCircle(
        color = Color(0xFFFFC93C),
        radius = headRadius * 0.16f,
        center = Offset(cx, neckY + headRadius * 0.12f),
      )
      drawCircle(
        color = palette.outline,
        radius = headRadius * 0.16f,
        center = Offset(cx, neckY + headRadius * 0.12f),
        style = Stroke(width = strokeW * 0.8f),
      )
    }
    "scarf" -> {
      drawRoundRect(
        color = Color(0xFF3E7C64),
        topLeft = Offset(cx - headRadius * 0.75f, neckY - headRadius * 0.08f),
        size = Size(headRadius * 1.50f, headRadius * 0.28f),
        cornerRadius = CornerRadius(headRadius * 0.14f, headRadius * 0.14f),
      )
      drawRoundRect(
        color = Color(0xFFD94F30),
        topLeft = Offset(cx + headRadius * 0.15f, neckY + headRadius * 0.06f),
        size = Size(headRadius * 0.32f, headRadius * 0.62f),
        cornerRadius = CornerRadius(headRadius * 0.10f, headRadius * 0.10f),
      )
    }
    "sunglasses" -> {
      listOf(-1f, 1f).forEach { side ->
        val gx = cx + side * eyeOffsetX
        drawRoundRect(
          color = Color(0xFF1D222B),
          topLeft = Offset(gx - headRadius * 0.25f, eyeY - headRadius * 0.15f),
          size = Size(headRadius * 0.50f, headRadius * 0.30f),
          cornerRadius = CornerRadius(headRadius * 0.08f, headRadius * 0.08f),
        )
        drawLine(
          color = Color(0x66FFFFFF),
          start = Offset(gx - headRadius * 0.14f, eyeY - headRadius * 0.08f),
          end = Offset(gx - headRadius * 0.02f, eyeY + headRadius * 0.06f),
          strokeWidth = strokeW,
          cap = StrokeCap.Round,
        )
      }
      drawLine(
        color = Color(0xFF1D222B),
        start = Offset(cx - headRadius * 0.18f, eyeY - headRadius * 0.03f),
        end = Offset(cx + headRadius * 0.18f, eyeY - headRadius * 0.03f),
        strokeWidth = strokeW * 1.4f,
      )
    }
    "wizard_hat" -> {
      val brimY = headCenterY - headRadius * 0.80f
      val cone =
        Path().apply {
          moveTo(cx - headRadius * 0.62f, brimY)
          lineTo(cx + headRadius * 0.06f, brimY - headRadius * 1.15f)
          lineTo(cx + headRadius * 0.62f, brimY)
          close()
        }
      drawPath(cone, color = Color(0xFF3E3480))
      drawPath(cone, color = palette.outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))
      drawOval(
        color = Color(0xFF3E3480),
        topLeft = Offset(cx - headRadius * 0.92f, brimY - headRadius * 0.12f),
        size = Size(headRadius * 1.84f, headRadius * 0.28f),
      )
      drawOval(
        color = palette.outline,
        topLeft = Offset(cx - headRadius * 0.92f, brimY - headRadius * 0.12f),
        size = Size(headRadius * 1.84f, headRadius * 0.28f),
        style = Stroke(width = strokeW),
      )
      drawCircle(color = Color(0xFFFFD86B), radius = headRadius * 0.09f, center = Offset(cx, brimY - headRadius * 0.52f))
    }
    "crown" -> {
      val baseY = headCenterY - headRadius * 0.80f
      val crownPath =
        Path().apply {
          moveTo(cx - headRadius * 0.45f, baseY)
          lineTo(cx - headRadius * 0.52f, baseY - headRadius * 0.46f)
          lineTo(cx - headRadius * 0.22f, baseY - headRadius * 0.22f)
          lineTo(cx, baseY - headRadius * 0.56f)
          lineTo(cx + headRadius * 0.22f, baseY - headRadius * 0.22f)
          lineTo(cx + headRadius * 0.52f, baseY - headRadius * 0.46f)
          lineTo(cx + headRadius * 0.45f, baseY)
          close()
        }
      drawPath(crownPath, color = Color(0xFFFFC93C))
      drawPath(crownPath, color = palette.outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))
      drawCircle(color = Color(0xFFE04848), radius = headRadius * 0.07f, center = Offset(cx, baseY - headRadius * 0.16f))
    }
    "paper_lantern" -> {
      val lx = cx - headRadius * 1.15f
      val ly = headCenterY - headRadius * 0.35f
      drawLine(
        color = Color(0xFFFFD54F),
        start = Offset(lx, ly - headRadius * 0.45f),
        end = Offset(lx, ly + headRadius * 0.42f),
        strokeWidth = strokeW,
      )
      drawCircle(
        color = Color(0x44FF8A65),
        radius = headRadius * 0.44f,
        center = Offset(lx, ly),
      )
      drawOval(
        color = Color(0xFFE53935),
        topLeft = Offset(lx - headRadius * 0.26f, ly - headRadius * 0.32f),
        size = Size(headRadius * 0.52f, headRadius * 0.64f),
      )
      drawOval(
        color = Color(0xFFFFCA28),
        topLeft = Offset(lx - headRadius * 0.16f, ly - headRadius * 0.18f),
        size = Size(headRadius * 0.32f, headRadius * 0.36f),
      )
    }
    "taiyaki_plush" -> {
      val tx = cx + headRadius * 0.85f
      val ty = neckY + headRadius * 0.05f
      drawOval(
        color = Color(0xFFF6AE42),
        topLeft = Offset(tx - headRadius * 0.28f, ty - headRadius * 0.18f),
        size = Size(headRadius * 0.56f, headRadius * 0.36f),
      )
      drawOval(
        color = palette.outline,
        topLeft = Offset(tx - headRadius * 0.28f, ty - headRadius * 0.18f),
        size = Size(headRadius * 0.56f, headRadius * 0.36f),
        style = Stroke(width = strokeW * 0.75f),
      )
      drawCircle(color = palette.outline, radius = headRadius * 0.04f, center = Offset(tx - headRadius * 0.10f, ty - headRadius * 0.03f))
    }
    "sunflower_halo" -> {
      val flowerCenters =
        listOf(
          Offset(cx - headRadius * 0.42f, headCenterY - headRadius * 0.84f),
          Offset(cx, headCenterY - headRadius * 0.95f),
          Offset(cx + headRadius * 0.42f, headCenterY - headRadius * 0.84f),
        )
      flowerCenters.forEach { center ->
        drawCircle(color = Color(0xFFFFCA28), radius = headRadius * 0.22f, center = center)
        drawCircle(color = Color(0xFF6D4C41), radius = headRadius * 0.10f, center = center)
      }
    }
    "crescent_amulet" -> {
      drawLine(
        color = Color(0xFF4A148C),
        start = Offset(cx - headRadius * 0.60f, neckY),
        end = Offset(cx + headRadius * 0.60f, neckY),
        strokeWidth = headRadius * 0.14f,
        cap = StrokeCap.Round,
      )
      val pendantCenter = Offset(cx, neckY + headRadius * 0.15f)
      drawCircle(color = Color(0xFFFFD54F), radius = headRadius * 0.17f, center = pendantCenter)
      drawCircle(color = Color(0xFF4A148C), radius = headRadius * 0.12f, center = Offset(pendantCenter.x + headRadius * 0.06f, pendantCenter.y - headRadius * 0.02f))
    }
    "bow_tie" -> {
      val leftWing =
        Path().apply {
          moveTo(cx, neckY + headRadius * 0.06f)
          lineTo(cx - headRadius * 0.34f, neckY - headRadius * 0.10f)
          lineTo(cx - headRadius * 0.34f, neckY + headRadius * 0.22f)
          close()
        }
      val rightWing =
        Path().apply {
          moveTo(cx, neckY + headRadius * 0.06f)
          lineTo(cx + headRadius * 0.34f, neckY - headRadius * 0.10f)
          lineTo(cx + headRadius * 0.34f, neckY + headRadius * 0.22f)
          close()
        }
      drawPath(leftWing, color = Color(0xFFD81B60))
      drawPath(rightWing, color = Color(0xFFD81B60))
      drawPath(leftWing, color = palette.outline, style = Stroke(width = strokeW * 0.7f, join = StrokeJoin.Round))
      drawPath(rightWing, color = palette.outline, style = Stroke(width = strokeW * 0.7f, join = StrokeJoin.Round))
      drawCircle(color = Color(0xFFFFC107), radius = headRadius * 0.08f, center = Offset(cx, neckY + headRadius * 0.06f))
    }
    "tiny_beret" -> {
      val beretCenter = Offset(cx - headRadius * 0.18f, headCenterY - headRadius * 0.92f)
      rotate(degrees = -14f, pivot = beretCenter) {
        drawOval(
          color = Color(0xFF8E2448),
          topLeft = Offset(beretCenter.x - headRadius * 0.56f, beretCenter.y - headRadius * 0.22f),
          size = Size(headRadius * 1.12f, headRadius * 0.44f),
        )
        drawOval(
          color = palette.outline,
          topLeft = Offset(beretCenter.x - headRadius * 0.56f, beretCenter.y - headRadius * 0.22f),
          size = Size(headRadius * 1.12f, headRadius * 0.44f),
          style = Stroke(width = strokeW * 0.85f),
        )
        drawLine(
          color = Color(0xFF560027),
          start = Offset(beretCenter.x, beretCenter.y - headRadius * 0.20f),
          end = Offset(beretCenter.x + headRadius * 0.04f, beretCenter.y - headRadius * 0.34f),
          strokeWidth = strokeW,
          cap = StrokeCap.Round,
        )
      }
    }
    "harvest_beret" -> {
      val beretCenter = Offset(cx + headRadius * 0.14f, headCenterY - headRadius * 0.92f)
      rotate(degrees = 12f, pivot = beretCenter) {
        drawOval(
          color = Color(0xFFD85A18),
          topLeft = Offset(beretCenter.x - headRadius * 0.58f, beretCenter.y - headRadius * 0.22f),
          size = Size(headRadius * 1.16f, headRadius * 0.44f),
        )
        drawOval(
          color = palette.outline,
          topLeft = Offset(beretCenter.x - headRadius * 0.58f, beretCenter.y - headRadius * 0.22f),
          size = Size(headRadius * 1.16f, headRadius * 0.44f),
          style = Stroke(width = strokeW * 0.85f),
        )
        drawCircle(
          color = Color(0xFFFFD54F),
          radius = headRadius * 0.10f,
          center = Offset(beretCenter.x - headRadius * 0.22f, beretCenter.y - headRadius * 0.08f),
        )
      }
    }
    "golden_collar" -> {
      drawLine(
        color = Color(0xFFFFC107),
        start = Offset(cx - headRadius * 0.64f, neckY),
        end = Offset(cx + headRadius * 0.64f, neckY),
        strokeWidth = headRadius * 0.18f,
        cap = StrokeCap.Round,
      )
      drawCircle(
        color = Color(0xFFFFF59D),
        radius = headRadius * 0.18f,
        center = Offset(cx, neckY + headRadius * 0.14f),
      )
      drawCircle(
        color = Color(0xFF00E5FF),
        radius = headRadius * 0.07f,
        center = Offset(cx, neckY + headRadius * 0.14f),
      )
      drawCircle(
        color = palette.outline,
        radius = headRadius * 0.18f,
        center = Offset(cx, neckY + headRadius * 0.14f),
        style = Stroke(width = strokeW * 0.8f),
      )
    }
    "diamond_crown" -> {
      val baseY = headCenterY - headRadius * 0.80f
      val tiaraPath =
        Path().apply {
          moveTo(cx - headRadius * 0.48f, baseY)
          lineTo(cx - headRadius * 0.54f, baseY - headRadius * 0.44f)
          lineTo(cx - headRadius * 0.24f, baseY - headRadius * 0.20f)
          lineTo(cx, baseY - headRadius * 0.62f)
          lineTo(cx + headRadius * 0.24f, baseY - headRadius * 0.20f)
          lineTo(cx + headRadius * 0.54f, baseY - headRadius * 0.44f)
          lineTo(cx + headRadius * 0.48f, baseY)
          close()
        }
      drawPath(tiaraPath, color = Color(0xFF80F5FF))
      drawPath(tiaraPath, color = palette.outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))
      val diamondPath =
        Path().apply {
          moveTo(cx, baseY - headRadius * 0.44f)
          lineTo(cx + headRadius * 0.14f, baseY - headRadius * 0.24f)
          lineTo(cx, baseY - headRadius * 0.05f)
          lineTo(cx - headRadius * 0.14f, baseY - headRadius * 0.24f)
          close()
        }
      drawPath(diamondPath, color = Color(0xFFE0FFFF))
      drawPath(diamondPath, color = Color(0xFF00838F), style = Stroke(width = strokeW * 0.6f))
      drawCircle(color = Color.White, radius = headRadius * 0.055f, center = Offset(cx - headRadius * 0.34f, baseY - headRadius * 0.16f))
      drawCircle(color = Color.White, radius = headRadius * 0.055f, center = Offset(cx + headRadius * 0.34f, baseY - headRadius * 0.16f))
    }
    "pilgrim_hat" -> {
      val brimY = headCenterY - headRadius * 0.82f
      drawRoundRect(
        color = Color(0xFF282422),
        topLeft = Offset(cx - headRadius * 0.82f, brimY - headRadius * 0.10f),
        size = Size(headRadius * 1.64f, headRadius * 0.22f),
        cornerRadius = CornerRadius(headRadius * 0.10f, headRadius * 0.10f),
      )
      drawRoundRect(
        color = Color(0xFF3E3834),
        topLeft = Offset(cx - headRadius * 0.48f, brimY - headRadius * 0.72f),
        size = Size(headRadius * 0.96f, headRadius * 0.68f),
        cornerRadius = CornerRadius(headRadius * 0.10f, headRadius * 0.10f),
      )
      drawRect(
        color = Color(0xFFFFC107),
        topLeft = Offset(cx - headRadius * 0.18f, brimY - headRadius * 0.26f),
        size = Size(headRadius * 0.36f, headRadius * 0.22f),
        style = Stroke(width = strokeW),
      )
    }
    "cranberry_scarf" -> {
      drawRoundRect(
        color = Color(0xFF9B1B30),
        topLeft = Offset(cx - headRadius * 0.76f, neckY - headRadius * 0.08f),
        size = Size(headRadius * 1.52f, headRadius * 0.28f),
        cornerRadius = CornerRadius(headRadius * 0.14f, headRadius * 0.14f),
      )
      drawRoundRect(
        color = Color(0xFFE65100),
        topLeft = Offset(cx + headRadius * 0.16f, neckY + headRadius * 0.06f),
        size = Size(headRadius * 0.32f, headRadius * 0.62f),
        cornerRadius = CornerRadius(headRadius * 0.10f, headRadius * 0.10f),
      )
      drawCircle(color = Color(0xFFFFD54F), radius = headRadius * 0.12f, center = Offset(cx - headRadius * 0.18f, neckY + headRadius * 0.06f))
    }
    "turkey_plush" -> {
      val tx = cx + headRadius * 0.88f
      val ty = neckY + headRadius * 0.04f
      drawCircle(color = Color(0xFFC62828), radius = headRadius * 0.24f, center = Offset(tx + headRadius * 0.08f, ty - headRadius * 0.08f))
      drawOval(
        color = Color(0xFF8D4E24),
        topLeft = Offset(tx - headRadius * 0.26f, ty - headRadius * 0.18f),
        size = Size(headRadius * 0.52f, headRadius * 0.36f),
      )
      drawCircle(color = Color(0xFFA95C29), radius = headRadius * 0.14f, center = Offset(tx - headRadius * 0.20f, ty - headRadius * 0.16f))
      drawCircle(color = Color(0xFFFFCA28), radius = headRadius * 0.06f, center = Offset(tx - headRadius * 0.34f, ty - headRadius * 0.16f))
    }
    "cornucopia_horn" -> {
      val hx = cx - headRadius * 1.05f
      val hy = neckY + headRadius * 0.02f
      val hornPath =
        Path().apply {
          moveTo(hx - headRadius * 0.22f, hy - headRadius * 0.22f)
          lineTo(hx + headRadius * 0.26f, hy + headRadius * 0.10f)
          lineTo(hx - headRadius * 0.14f, hy + headRadius * 0.28f)
          close()
        }
      drawPath(hornPath, color = Color(0xFFD68910))
      drawPath(hornPath, color = palette.outline, style = Stroke(width = strokeW * 0.8f, join = StrokeJoin.Round))
      drawCircle(color = Color(0xFFC62828), radius = headRadius * 0.10f, center = Offset(hx + headRadius * 0.22f, hy + headRadius * 0.04f))
      drawCircle(color = Color(0xFFFFCA28), radius = headRadius * 0.09f, center = Offset(hx + headRadius * 0.30f, hy + headRadius * 0.14f))
      drawCircle(color = Color(0xFF43A047), radius = headRadius * 0.08f, center = Offset(hx + headRadius * 0.16f, hy + headRadius * 0.18f))
    }
    "gravy_ladle" -> {
      val sx = cx + headRadius * 0.96f
      val sy = neckY - headRadius * 0.10f
      drawLine(
        color = Color(0xFFFFCA28),
        start = Offset(sx - headRadius * 0.14f, sy + headRadius * 0.42f),
        end = Offset(sx + headRadius * 0.16f, sy - headRadius * 0.34f),
        strokeWidth = strokeW * 1.6f,
        cap = StrokeCap.Round,
      )
      drawCircle(
        color = Color(0xFFFFB300),
        radius = headRadius * 0.22f,
        center = Offset(sx - headRadius * 0.16f, sy + headRadius * 0.42f),
      )
      drawCircle(
        color = Color(0xFFD84315),
        radius = headRadius * 0.12f,
        center = Offset(sx - headRadius * 0.16f, sy + headRadius * 0.42f),
      )
      drawCircle(
        color = Color.White,
        radius = headRadius * 0.07f,
        center = Offset(sx + headRadius * 0.16f, sy - headRadius * 0.36f),
      )
    }
    "melted_solstice_crown" -> {
      val baseY = headCenterY - headRadius * 0.80f
      val frostCrown =
        Path().apply {
          moveTo(cx - headRadius * 0.50f, baseY)
          lineTo(cx - headRadius * 0.56f, baseY - headRadius * 0.48f)
          lineTo(cx - headRadius * 0.25f, baseY - headRadius * 0.22f)
          lineTo(cx, baseY - headRadius * 0.66f)
          lineTo(cx + headRadius * 0.25f, baseY - headRadius * 0.22f)
          lineTo(cx + headRadius * 0.56f, baseY - headRadius * 0.48f)
          lineTo(cx + headRadius * 0.50f, baseY)
          close()
        }
      drawPath(frostCrown, color = Color(0xFFB3E5FC))
      drawPath(frostCrown, color = Color(0xFF0288D1), style = Stroke(width = strokeW, join = StrokeJoin.Round))
      // Festive crimson solstice berry gem in center + frost highlights + melting droplet
      drawCircle(color = Color(0xFFE53935), radius = headRadius * 0.09f, center = Offset(cx, baseY - headRadius * 0.20f))
      drawCircle(color = Color.White, radius = headRadius * 0.055f, center = Offset(cx - headRadius * 0.28f, baseY - headRadius * 0.14f))
      drawOval(
        color = Color(0xFF4FC3F7),
        topLeft = Offset(cx + headRadius * 0.46f, baseY - headRadius * 0.12f),
        size = Size(headRadius * 0.13f, headRadius * 0.22f),
      )
    }
  }
}
