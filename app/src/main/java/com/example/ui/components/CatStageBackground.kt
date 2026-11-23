package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.CatBackgroundDef
import com.example.data.model.GameCatalog

@Composable
fun CatStageBackground(
  backgroundId: String,
  modifier: Modifier = Modifier,
) {
  val def = GameCatalog.backgrounds.find { it.id == backgroundId } ?: GameCatalog.backgrounds.first()
  val primary = Color(def.primaryHex)
  val secondary = Color(def.secondaryHex)
  val accent = Color(def.accentHex)

  Box(
    modifier =
      modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(primary, secondary))
      )
  ) {
    when (def.id) {
      "cozy_parlor" -> {
        Image(
          painter = painterResource(id = R.drawable.img_sanctuary_banner),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          alpha = 0.28f,
          modifier = Modifier.fillMaxSize(),
        )
      }
      "sakura_garden" -> {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Soft sun halo
          drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = size.minDimension * 0.28f,
            center = Offset(size.width * 0.78f, size.height * 0.24f),
          )
          // Veranda floor strip
          drawRect(
            color = Color(0xFF8D6E63).copy(alpha = 0.26f),
            topLeft = Offset(0f, size.height * 0.78f),
            size = Size(size.width, size.height * 0.22f),
          )
          // Drifting sakura petals
          val petals = listOf(
            Offset(0.14f, 0.20f) to 10f,
            Offset(0.28f, 0.36f) to 7f,
            Offset(0.42f, 0.16f) to 9f,
            Offset(0.64f, 0.28f) to 11f,
            Offset(0.84f, 0.44f) to 8f,
            Offset(0.20f, 0.62f) to 8f,
            Offset(0.76f, 0.68f) to 10f,
          )
          petals.forEach { (pos, r) ->
            drawOval(
              color = accent.copy(alpha = 0.48f),
              topLeft = Offset(size.width * pos.x, size.height * pos.y),
              size = Size(r * 1.8f, r * 1.1f),
            )
          }
        }
      }
      "autumn_porch" -> {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Warm harvest glow
          drawCircle(
            color = Color(0xFFFFB74D).copy(alpha = 0.42f),
            radius = size.minDimension * 0.34f,
            center = Offset(size.width * 0.24f, size.height * 0.22f),
          )
          // Porch railing
          drawRect(
            color = Color(0xFF6D4C41).copy(alpha = 0.30f),
            topLeft = Offset(0f, size.height * 0.74f),
            size = Size(size.width, size.height * 0.05f),
          )
          drawRect(
            color = Color(0xFF5D4037).copy(alpha = 0.25f),
            topLeft = Offset(0f, size.height * 0.79f),
            size = Size(size.width, size.height * 0.21f),
          )
          // Autumn leaves
          val leaves = listOf(
            Offset(0.15f, 0.30f),
            Offset(0.35f, 0.18f),
            Offset(0.72f, 0.25f),
            Offset(0.85f, 0.48f),
            Offset(0.22f, 0.58f),
          )
          leaves.forEach { pos ->
            drawOval(
              color = accent.copy(alpha = 0.50f),
              topLeft = Offset(size.width * pos.x, size.height * pos.y),
              size = Size(20f, 12f),
            )
          }
        }
      }
      "starlight_observatory" -> {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Nebula glow
          drawCircle(
            color = accent.copy(alpha = 0.28f),
            radius = size.minDimension * 0.45f,
            center = Offset(size.width * 0.75f, size.height * 0.30f),
          )
          drawCircle(
            color = Color(0xFF40C4FF).copy(alpha = 0.18f),
            radius = size.minDimension * 0.38f,
            center = Offset(size.width * 0.25f, size.height * 0.65f),
          )
          // Stars
          val stars = listOf(
            Offset(0.12f, 0.16f) to 3.5f,
            Offset(0.24f, 0.28f) to 2.5f,
            Offset(0.38f, 0.14f) to 4f,
            Offset(0.55f, 0.22f) to 2.8f,
            Offset(0.70f, 0.18f) to 4.2f,
            Offset(0.86f, 0.26f) to 3.2f,
            Offset(0.18f, 0.50f) to 2.4f,
            Offset(0.82f, 0.56f) to 3.5f,
            Offset(0.62f, 0.72f) to 2.6f,
          )
          stars.forEach { (pos, r) ->
            drawCircle(
              color = Color.White.copy(alpha = 0.85f),
              radius = r,
              center = Offset(size.width * pos.x, size.height * pos.y),
            )
          }
        }
      }
      "tropical_cabana" -> {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Setting sun
          drawCircle(
            color = accent.copy(alpha = 0.72f),
            radius = size.minDimension * 0.22f,
            center = Offset(size.width * 0.5f, size.height * 0.44f),
          )
          // Ocean horizon
          drawRect(
            color = Color(0xFF00ACC1).copy(alpha = 0.45f),
            topLeft = Offset(0f, size.height * 0.58f),
            size = Size(size.width, size.height * 0.24f),
          )
          // Sandy beach
          drawRect(
            color = Color(0xFFFFD54F).copy(alpha = 0.52f),
            topLeft = Offset(0f, size.height * 0.80f),
            size = Size(size.width, size.height * 0.20f),
          )
        }
      }
      "crystal_shrine" -> {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Glowing crystal spires
          fun drawCrystal(xFrac: Float, heightFrac: Float, widthFrac: Float, alpha: Float) {
            val cx = size.width * xFrac
            val topY = size.height * (1f - heightFrac)
            val halfW = size.width * widthFrac * 0.5f
            val path = Path().apply {
              moveTo(cx, topY)
              lineTo(cx + halfW, size.height)
              lineTo(cx - halfW, size.height)
              close()
            }
            drawPath(path, color = accent.copy(alpha = alpha))
          }
          drawCrystal(0.14f, 0.55f, 0.18f, 0.30f)
          drawCrystal(0.86f, 0.62f, 0.20f, 0.28f)
          drawCrystal(0.28f, 0.34f, 0.12f, 0.22f)
          drawCrystal(0.74f, 0.38f, 0.14f, 0.22f)
          // Floating sparkles
          drawCircle(Color.White.copy(alpha = 0.6f), 4f, Offset(size.width * 0.20f, size.height * 0.26f))
          drawCircle(Color.White.copy(alpha = 0.6f), 3.5f, Offset(size.width * 0.78f, size.height * 0.22f))
        }
      }
      "neon_cyber_lounge" -> {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Synthwave sun
          drawCircle(
            color = Color(0xFFFF4081).copy(alpha = 0.42f),
            radius = size.minDimension * 0.25f,
            center = Offset(size.width * 0.5f, size.height * 0.42f),
          )
          // Neon horizon & grid lines
          val horizonY = size.height * 0.68f
          drawLine(
            color = accent.copy(alpha = 0.65f),
            start = Offset(0f, horizonY),
            end = Offset(size.width, horizonY),
            strokeWidth = 3f,
          )
          for (i in 1..4) {
            val y = horizonY + (size.height - horizonY) * (i / 4f)
            drawLine(
              color = accent.copy(alpha = 0.32f),
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = 1.8f,
            )
          }
          for (i in 1..5) {
            val x = size.width * (i / 6f)
            drawLine(
              color = accent.copy(alpha = 0.26f),
              start = Offset(x, horizonY),
              end = Offset(x + (x - size.width * 0.5f) * 0.8f, size.height),
              strokeWidth = 1.6f,
            )
          }
        }
      }
      "thanksgiving_feast_hall" -> {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Warm harvest chandelier glow
          drawCircle(
            color = accent.copy(alpha = 0.35f),
            radius = size.minDimension * 0.42f,
            center = Offset(size.width * 0.5f, size.height * 0.18f),
          )
          // Banquet wooden table silhouette
          drawRect(
            color = Color(0xFF3E1C00).copy(alpha = 0.48f),
            topLeft = Offset(0f, size.height * 0.76f),
            size = Size(size.width, size.height * 0.24f),
          )
          // Golden candle flames
          listOf(0.16f, 0.30f, 0.70f, 0.84f).forEach { xFrac ->
            drawCircle(
              color = Color(0xFFFFE082).copy(alpha = 0.75f),
              radius = 9f,
              center = Offset(size.width * xFrac, size.height * 0.71f),
            )
          }
          // Falling golden garland leaves
          listOf(
            Offset(0.12f, 0.22f),
            Offset(0.28f, 0.34f),
            Offset(0.76f, 0.24f),
            Offset(0.88f, 0.42f),
          ).forEach { pos ->
            drawOval(
              color = Color(0xFFFF8F00).copy(alpha = 0.55f),
              topLeft = Offset(size.width * pos.x, size.height * pos.y),
              size = Size(18f, 11f),
            )
          }
        }
      }
      "thanksgiving_pumpkin_meadow" -> {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Harvest moon
          drawCircle(
            color = Color(0xFFFFE0B2).copy(alpha = 0.78f),
            radius = size.minDimension * 0.19f,
            center = Offset(size.width * 0.78f, size.height * 0.22f),
          )
          // Meadow hills
          drawOval(
            color = Color(0xFF1F0C1F).copy(alpha = 0.55f),
            topLeft = Offset(-size.width * 0.15f, size.height * 0.68f),
            size = Size(size.width * 1.3f, size.height * 0.45f),
          )
          // Glowing pumpkins
          listOf(0.16f to 22f, 0.82f to 26f, 0.28f to 16f).forEach { (xFrac, r) ->
            val cx = size.width * xFrac
            val cy = size.height * 0.82f
            drawCircle(
              color = accent.copy(alpha = 0.82f),
              radius = r,
              center = Offset(cx, cy),
            )
            drawCircle(
              color = Color(0xFFFFEB3B).copy(alpha = 0.75f),
              radius = r * 0.28f,
              center = Offset(cx, cy),
            )
          }
          // Drifting crimson maple leaves
          listOf(Offset(0.18f, 0.26f), Offset(0.45f, 0.18f), Offset(0.64f, 0.36f)).forEach { pos ->
            drawOval(
              color = Color(0xFFE53935).copy(alpha = 0.55f),
              topLeft = Offset(size.width * pos.x, size.height * pos.y),
              size = Size(16f, 10f),
            )
          }
        }
      }
      "cornucopia_citadel" -> {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Citadel golden archways
          drawCircle(
            color = accent.copy(alpha = 0.32f),
            radius = size.minDimension * 0.38f,
            center = Offset(size.width * 0.5f, size.height * 0.26f),
          )
          listOf(0.18f, 0.50f, 0.82f).forEach { xFrac ->
            drawRoundRect(
              color = Color(0xFF4E2300).copy(alpha = 0.42f),
              topLeft = Offset(size.width * xFrac - size.width * 0.10f, size.height * 0.35f),
              size = Size(size.width * 0.20f, size.height * 0.65f),
              cornerRadius = CornerRadius(48f, 48f),
            )
          }
          // Glowing royal banners
          listOf(0.18f, 0.82f).forEach { xFrac ->
            drawCircle(
              color = accent.copy(alpha = 0.78f),
              radius = 9f,
              center = Offset(size.width * xFrac, size.height * 0.24f),
            )
          }
        }
      }
      "golden_gravy_falls" -> {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Harvest stars
          listOf(Offset(0.16f, 0.18f), Offset(0.32f, 0.12f), Offset(0.68f, 0.15f), Offset(0.84f, 0.24f)).forEach { pos ->
            drawCircle(
              color = Color(0xFFFFF8E1).copy(alpha = 0.78f),
              radius = 3.2f,
              center = Offset(size.width * pos.x, size.height * pos.y),
            )
          }
          // Cascading molten gravy waterfall ribbons
          listOf(0.22f, 0.78f).forEach { xFrac ->
            drawRoundRect(
              brush = Brush.verticalGradient(listOf(accent.copy(alpha = 0.72f), Color(0xFFFFD54F).copy(alpha = 0.45f))),
              topLeft = Offset(size.width * xFrac - size.width * 0.06f, size.height * 0.22f),
              size = Size(size.width * 0.12f, size.height * 0.78f),
              cornerRadius = CornerRadius(24f, 24f),
            )
          }
          // Mist pool
          drawOval(
            color = Color(0xFFFFE082).copy(alpha = 0.35f),
            topLeft = Offset(size.width * 0.08f, size.height * 0.78f),
            size = Size(size.width * 0.84f, size.height * 0.22f),
          )
        }
      }
    }
  }
}

@Composable
fun CatBackgroundPreviewSwatch(
  def: CatBackgroundDef,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    drawRoundRect(
      brush = Brush.verticalGradient(listOf(Color(def.primaryHex), Color(def.secondaryHex))),
      cornerRadius = CornerRadius(18f, 18f),
    )
    drawCircle(
      color = Color(def.accentHex).copy(alpha = 0.85f),
      radius = size.minDimension * 0.22f,
      center = Offset(size.width * 0.72f, size.height * 0.32f),
    )
  }
}
