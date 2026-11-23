package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GameCatalog
import com.example.data.model.Part2MapLandmark
import kotlin.math.roundToInt

/**
 * Custom illustrated Turkey Gato mascot greeting players inside the Thanksgiving Festival.
 */
@Composable
fun TurkeyGatoAvatar(
  modifier: Modifier = Modifier,
) {
  Canvas(
    modifier =
      modifier.semantics { contentDescription = "Turkey Gato Festival Mascot" }
  ) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.54f
    val r = minOf(w, h) * 0.34f
    val outline = Color(0xFF42210B)
    val strokeW = (w * 0.03f).coerceIn(2f, 4.5f)

    // Turkey tail feather fan behind Turkey Gato
    val featherColors =
      listOf(
        Color(0xFFB71C1C),
        Color(0xFFE65100),
        Color(0xFFFFB300),
        Color(0xFF8E24AA),
        Color(0xFFD84315),
      )
    val featherAngles = listOf(-48f, -24f, 0f, 24f, 48f)
    featherAngles.forEachIndexed { idx, deg ->
      val rad = Math.toRadians(deg.toDouble())
      val fx = cx + (Math.sin(rad) * r * 1.15).toFloat()
      val fy = cy - (Math.cos(rad) * r * 1.10).toFloat()
      drawOval(
        color = featherColors[idx],
        topLeft = Offset(fx - r * 0.34f, fy - r * 0.52f),
        size = Size(r * 0.68f, r * 1.04f),
      )
      drawOval(
        color = outline,
        topLeft = Offset(fx - r * 0.34f, fy - r * 0.52f),
        size = Size(r * 0.68f, r * 1.04f),
        style = Stroke(width = strokeW * 0.7f),
      )
    }

    // Cat ears poking through feathers
    listOf(-1f, 1f).forEach { side ->
      val ear =
        Path().apply {
          moveTo(cx + side * r * 0.28f, cy - r * 0.45f)
          lineTo(cx + side * r * 0.85f, cy - r * 1.15f)
          lineTo(cx + side * r * 0.88f, cy - r * 0.15f)
          close()
        }
      drawPath(ear, Color(0xFFD97228))
      drawPath(ear, outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))
    }

    // Round roasted-caramel Gato head
    drawOval(
      color = Color(0xFFE88A38),
      topLeft = Offset(cx - r * 1.04f, cy - r * 0.82f),
      size = Size(r * 2.08f, r * 1.72f),
    )
    drawOval(
      color = outline,
      topLeft = Offset(cx - r * 1.04f, cy - r * 0.82f),
      size = Size(r * 2.08f, r * 1.72f),
      style = Stroke(width = strokeW),
    )

    // Crimson turkey wattle beside nose
    drawOval(
      color = Color(0xFFC62828),
      topLeft = Offset(cx + r * 0.04f, cy + r * 0.16f),
      size = Size(r * 0.26f, r * 0.42f),
    )

    // Golden-orange beak/nose + friendly cat smile
    val beak =
      Path().apply {
        moveTo(cx - r * 0.14f, cy + r * 0.06f)
        lineTo(cx + r * 0.14f, cy + r * 0.06f)
        lineTo(cx, cy + r * 0.28f)
        close()
      }
    drawPath(beak, Color(0xFFFFCA28))
    drawPath(beak, outline, style = Stroke(width = strokeW * 0.7f, join = StrokeJoin.Round))

    // Sparkling eyes
    listOf(-1f, 1f).forEach { side ->
      val ex = cx + side * r * 0.42f
      drawCircle(color = Color(0xFF2B180B), radius = r * 0.15f, center = Offset(ex, cy - r * 0.08f))
      drawCircle(color = Color.White, radius = r * 0.06f, center = Offset(ex - r * 0.04f, cy - r * 0.12f))
    }

    // Miniature buckled pilgrim hat perched on top
    val hatY = cy - r * 0.72f
    drawRoundRect(
      color = Color(0xFF2E2925),
      topLeft = Offset(cx - r * 0.76f, hatY - r * 0.08f),
      size = Size(r * 1.52f, r * 0.18f),
      cornerRadius = CornerRadius(r * 0.08f, r * 0.08f),
    )
    drawRoundRect(
      color = Color(0xFF3E3732),
      topLeft = Offset(cx - r * 0.46f, hatY - r * 0.64f),
      size = Size(r * 0.92f, r * 0.60f),
      cornerRadius = CornerRadius(r * 0.10f, r * 0.10f),
    )
    drawRect(
      color = Color(0xFFFFCA28),
      topLeft = Offset(cx - r * 0.16f, hatY - r * 0.24f),
      size = Size(r * 0.32f, r * 0.20f),
      style = Stroke(width = strokeW * 0.8f),
    )
  }
}

/**
 * Clickable wild Turkey / Golden Gobbler badge spawned onto the screen during Thanksgiving Event: Part Two.
 */
@Composable
fun SpawnedTurkeyBadge(
  onTap: () -> Unit,
  modifier: Modifier = Modifier,
  isGoldenGobbler: Boolean = false,
) {
  val bob = rememberInfiniteTransition(label = "turkeyBob")
  val dy by
    bob.animateFloat(
      initialValue = -5f,
      targetValue = 5f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(if (isGoldenGobbler) 420 else 550, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "turkeyFloatY",
    )

  val surfaceBg = if (isGoldenGobbler) Color(0xFFFFF8DC) else Color(0xFFFFF3E0)
  val borderColor = if (isGoldenGobbler) Color(0xFFFFB300) else Color(0xFFD84315)
  val pillColor = if (isGoldenGobbler) Color(0xFFE65100) else Color(0xFFD84315)
  val semanticsDesc =
    if (isGoldenGobbler) "Golden Gobbler +3 Turkeys" else "Wild Turkey +1 Turkey"

  Surface(
    onClick = onTap,
    shape = CircleShape,
    color = surfaceBg,
    border = BorderStroke(if (isGoldenGobbler) 3.dp else 2.5.dp, borderColor),
    shadowElevation = if (isGoldenGobbler) 14.dp else 10.dp,
    modifier =
      modifier.size(if (isGoldenGobbler) 68.dp else 64.dp)
        .offset(y = dy.dp)
        .semantics { contentDescription = semanticsDesc },
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(6.dp)) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w * 0.52f
        val cy = h * 0.58f
        val r = minOf(w, h) * 0.28f

        if (isGoldenGobbler) {
          drawCircle(
            color = Color(0x55FFD54F),
            radius = minOf(w, h) * 0.48f,
            center = Offset(w * 0.5f, h * 0.5f),
          )
        }

        // Fan feathers
        val fanColors =
          if (isGoldenGobbler) {
            listOf(Color(0xFFFFCA28), Color(0xFFFF9800), Color(0xFFFFE082), Color(0xFFE65100))
          } else {
            listOf(Color(0xFFC62828), Color(0xFFEF6C00), Color(0xFFFFB300), Color(0xFF8D6E63))
          }
        listOf(-42f, -14f, 14f, 42f).forEachIndexed { idx, deg ->
          val rad = Math.toRadians(deg.toDouble())
          val fx = cx + (Math.sin(rad) * r * 1.25).toFloat()
          val fy = cy - (Math.cos(rad) * r * 1.15).toFloat()
          drawCircle(color = fanColors[idx], radius = r * 0.42f, center = Offset(fx, fy))
        }
        // Plump turkey body
        drawOval(
          color = if (isGoldenGobbler) Color(0xFFD49400) else Color(0xFF8D4E24),
          topLeft = Offset(cx - r * 0.95f, cy - r * 0.62f),
          size = Size(r * 1.85f, r * 1.45f),
        )
        // Head + beak + red wattle
        val hx = cx - r * 0.62f
        val hy = cy - r * 0.55f
        drawCircle(
          color = if (isGoldenGobbler) Color(0xFFFFB300) else Color(0xFFA95C29),
          radius = r * 0.46f,
          center = Offset(hx, hy),
        )
        drawOval(
          color = Color(0xFFD32F2F),
          topLeft = Offset(hx - r * 0.16f, hy + r * 0.10f),
          size = Size(r * 0.22f, r * 0.36f),
        )
        val beak =
          Path().apply {
            moveTo(hx - r * 0.35f, hy - r * 0.08f)
            lineTo(hx - r * 0.72f, hy + r * 0.04f)
            lineTo(hx - r * 0.35f, hy + r * 0.14f)
            close()
          }
        drawPath(beak, Color(0xFFFFCA28))
        drawCircle(color = Color(0xFF1A1008), radius = r * 0.10f, center = Offset(hx - r * 0.14f, hy - r * 0.10f))
      }
      Surface(
        color = pillColor,
        shape = CircleShape,
        modifier = Modifier.align(Alignment.BottomEnd),
      ) {
        Text(
          text = if (isGoldenGobbler) "+3" else "+1",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          fontWeight = FontWeight.Bold,
          color = Color.White,
          modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
        )
      }
    }
  }
}

/**
 * Permanent collectible Map gifted by Turkey Gato showing what Thanksgiving Part Two (Nov 22) looks like.
 */
@Composable
fun ThanksgivingPart2MapDialog(
  onTeleportToZone: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val landmarks = GameCatalog.part2MapLandmarks
  var selectedLandmark by remember { mutableStateOf<Part2MapLandmark>(landmarks.first()) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      shadowElevation = 12.dp,
      border = BorderStroke(2.dp, Color(0xFFE65100).copy(alpha = 0.6f)),
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 20.dp)
          .widthIn(max = 560.dp)
          .heightIn(max = 720.dp)
          .testTag("part2_map_dialog"),
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            Surface(
              color = Color(0xFFFFE0B2),
              shape = CircleShape,
              modifier = Modifier.size(42.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Rounded.Map,
                  contentDescription = null,
                  tint = Color(0xFFBF360C),
                )
              }
            }
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = "Part 2 Feast Map",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                )
                Surface(color = Color(0xFF2E7D32), shape = CircleShape) {
                  Text(
                    text = "PART 2 LIVE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                  )
                }
              }
              Text(
                text = "Turkey Gato's Feast Atlas • All 5 Part Two Zones Are Open!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp).testTag("close_part2_map_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close Part 2 Map")
          }
        }

        // Illustrated Parchment Island Canvas with Interactive Landmark Pins
        BoxWithConstraints(
          modifier =
            Modifier.fillMaxWidth()
              .height(290.dp)
              .clip(MaterialTheme.shapes.large)
              .background(
                Brush.radialGradient(
                  listOf(Color(0xFFFFF3DC), Color(0xFFF6DFB8), Color(0xFFE5C491))
                )
              )
              .testTag("part2_map_canvas")
        ) {
          val density = LocalDensity.current
          val wPx = with(density) { maxWidth.toPx() }
          val hPx = with(density) { maxHeight.toPx() }

          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Map sea & parchment island silhouette
            val island =
              Path().apply {
                moveTo(w * 0.14f, h * 0.40f)
                cubicTo(w * 0.10f, h * 0.16f, w * 0.36f, h * 0.08f, w * 0.56f, h * 0.12f)
                cubicTo(w * 0.84f, h * 0.14f, w * 0.92f, h * 0.38f, w * 0.86f, h * 0.64f)
                cubicTo(w * 0.82f, h * 0.88f, w * 0.45f, h * 0.92f, w * 0.24f, h * 0.86f)
                cubicTo(w * 0.08f, h * 0.80f, w * 0.16f, h * 0.58f, w * 0.14f, h * 0.40f)
                close()
              }
            drawPath(
              path = island,
              brush = Brush.verticalGradient(listOf(Color(0xFFFFE7BE), Color(0xFFF5C97B))),
            )
            drawPath(
              path = island,
              color = Color(0xFF9C5E22),
              style = Stroke(width = 3.dp.toPx()),
            )

            // Dashed golden trail connecting landmarks
            val trail =
              Path().apply {
                landmarks.forEachIndexed { i, lm ->
                  val px = lm.xFraction * w
                  val py = lm.yFraction * h
                  if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
              }
            drawPath(
              path = trail,
              color = Color(0xFFA04000).copy(alpha = 0.65f),
              style =
                Stroke(
                  width = 2.5.dp.toPx(),
                  cap = StrokeCap.Round,
                  pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                ),
            )

            // Gravy River
            val gravyRiver =
              Path().apply {
                moveTo(w * 0.20f, h * 0.44f)
                cubicTo(w * 0.34f, h * 0.52f, w * 0.48f, h * 0.54f, w * 0.62f, h * 0.48f)
              }
            drawPath(
              path = gravyRiver,
              color = Color(0xFFD9822B).copy(alpha = 0.72f),
              style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
            )
          }

          landmarks.forEach { lm ->
            val isSelected = selectedLandmark.id == lm.id
            val px = (lm.xFraction * wPx).roundToInt() - with(density) { 46.dp.toPx() }.roundToInt()
            val py = (lm.yFraction * hPx).roundToInt() - with(density) { 24.dp.toPx() }.roundToInt()
            Surface(
              onClick = {
                if (isSelected) {
                  onTeleportToZone(lm.id)
                } else {
                  selectedLandmark = lm
                }
              },
              color = if (isSelected) Color(0xFFBF360C) else Color(0xFF5D2906),
              shape = CircleShape,
              border = BorderStroke(if (isSelected) 2.5.dp else 1.5.dp, Color(0xFFFFD54F)),
              shadowElevation = if (isSelected) 8.dp else 3.dp,
              modifier =
                Modifier.offset { IntOffset(px.coerceAtLeast(4), py.coerceAtLeast(4)) }
                  .widthIn(min = 92.dp, max = 148.dp)
                  .heightIn(min = 48.dp)
                  .testTag("map_pin_${lm.id}"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Place,
                  contentDescription = lm.title,
                  tint = Color(0xFFFFD54F),
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = lm.title,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }

            Surface(
              color = Color(0xCC3E1E04),
              shape = CircleShape,
              modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
            ) {
              Text(
                text = "Tap any area to preview, or tap again to Teleport!",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFFE8C2),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }

        // Selected Landmark Inspector Card
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
          border = BorderStroke(1.dp, Color(0xFFBF360C).copy(alpha = 0.45f)),
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth().testTag("part2_map_landmark_card"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = selectedLandmark.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
              Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape,
              ) {
                Text(
                  text = selectedLandmark.subtitle,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onTertiary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
            Text(
              text = selectedLandmark.previewDetail,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFBF360C),
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "Rewards: ${selectedLandmark.rewardSummary}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBF360C),
                textAlign = TextAlign.Start,
              )
            }
            Button(
              onClick = { onTeleportToZone(selectedLandmark.id) },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("teleport_zone_button"),
            ) {
              Text(selectedLandmark.actionLabel, fontWeight = FontWeight.Bold)
            }
          }
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
          Text("Back to Festival")
        }
      }
    }
  }
}
