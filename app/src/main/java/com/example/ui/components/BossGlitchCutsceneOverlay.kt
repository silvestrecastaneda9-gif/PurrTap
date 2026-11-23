package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Climactic Boss Glitch Cutscene triggered near the end of the Cornucopia Citadel Boss Battle:
 * 0: Boss tries to annihilate player & cat -> 1: Boss violently glitches out ->
 * 2: Total pitch blackness -> 3: Frozen Christmas Cat slowly melting inside an ice cube ->
 * 4: "Fully Melts On December 1st." + Limited-Time Solstice Crown reward.
 */
@Composable
fun BossGlitchCutsceneOverlay(
  catName: String,
  playerName: String,
  patternId: String,
  firstTimeReward: Boolean,
  onComplete: () -> Unit,
) {
  BackHandler(enabled = true) { /* Cutscene runs cleanly until claim step */ }

  var phase by remember { mutableIntStateOf(0) }
  val safeCat = catName.ifBlank { "Mochi" }
  val safePlayer = playerName.ifBlank { "Caretaker" }

  LaunchedEffect(phase) {
    when (phase) {
      1 -> {
        delay(2600L)
        phase = 2
      }
      2 -> {
        delay(1700L)
        phase = 3
      }
      3 -> {
        delay(3200L)
        phase = 4
      }
    }
  }

  val infinite = rememberInfiniteTransition(label = "bossGlitchAnim")
  val glitchPhase by
    infinite.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(240, easing = LinearEasing), RepeatMode.Reverse),
      label = "glitchJitter",
    )
  val meltDropProgress by
    infinite.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
      label = "meltDrip",
    )
  val icePulse by
    infinite.animateFloat(
      initialValue = 0.65f,
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
      label = "iceShimmer",
    )

  val bgBrush =
    when (phase) {
      0 ->
        Brush.verticalGradient(
          listOf(Color(0xFF280509), Color(0xFF4F0E15), Color(0xFF150305))
        )
      1 ->
        Brush.verticalGradient(
          listOf(Color(0xFF0A0F14), Color(0xFF220512), Color(0xFF041418))
        )
      2 -> Brush.verticalGradient(listOf(Color.Black, Color.Black))
      else ->
        Brush.verticalGradient(
          listOf(Color(0xFF02070D), Color(0xFF071B2B), Color(0xFF030B14))
        )
    }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(bgBrush)
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(20.dp)
        .testTag("boss_glitch_cutscene_overlay"),
  ) {
    // Glitch scanlines during Phase 1
    if (phase == 1) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val bars = 14
        for (i in 0 until bars) {
          val y = (size.height / bars) * i + (glitchPhase * 22f)
          val color =
            when (i % 3) {
              0 -> Color(0x44FF1744)
              1 -> Color(0x3300E5FF)
              else -> Color(0x28FFFFFF)
            }
          drawRect(
            color = color,
            topLeft = Offset((i % 2) * glitchPhase * 18f, y),
            size = Size(size.width * 0.85f, (6 + (i % 4) * 4).toFloat()),
          )
        }
      }
    }

    AnimatedContent(
      targetState = phase,
      transitionSpec = { fadeIn(tween(380)) togetherWith fadeOut(tween(280)) },
      modifier = Modifier.align(Alignment.Center).fillMaxWidth().widthIn(max = 520.dp),
      label = "bossGlitchPhase",
    ) { currentPhase ->
      when (currentPhase) {
        0 -> {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
          ) {
            Surface(
              color = Color(0xFFFF1744).copy(alpha = 0.22f),
              shape = CircleShape,
              border = BorderStroke(1.5.dp, Color(0xFFFF5252)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Warning,
                  contentDescription = null,
                  tint = Color(0xFFFF8A80),
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "CRITICAL OVERLOAD • BOSS AT 15% HP",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFFCDD2),
                )
              }
            }

            MechaGobblerBossCanvas(
              glitchOffset = 0f,
              overdrive = true,
              modifier = Modifier.size(165.dp),
            )

            Card(
              colors = CardDefaults.cardColors(containerColor = Color(0xCC1E060A)),
              border = BorderStroke(1.5.dp, Color(0xFFFF1744)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = "MECHA-GOBBLER PRIME // OVERDRIVE",
                  style = MaterialTheme.typography.labelLarge,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFF5252),
                )
                Text(
                  text =
                    "\"ENOUGH!! LOCKING ALL CORNUCOPIA CANNONS ON $safePlayer AND $safeCat! IF I FALL, I AM TAKING BOTH OF YOU WITH ME!!\"",
                  style = MaterialTheme.typography.bodyLarge,
                  color = Color.White,
                  textAlign = TextAlign.Start,
                )
              }
            }

            Button(
              onClick = { phase = 1 },
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFFF1744),
                  contentColor = Color.White,
                ),
              modifier =
                Modifier.fillMaxWidth().height(52.dp).testTag("boss_cutscene_brace_button"),
            ) {
              Text("Brace For Impact!", fontWeight = FontWeight.Bold)
            }
          }
        }

        1 -> {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            MechaGobblerBossCanvas(
              glitchOffset = (glitchPhase - 0.5f) * 34f,
              overdrive = true,
              modifier = Modifier.size(160.dp),
            )

            Surface(
              color = Color(0xEE090D12),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
              modifier = Modifier.fillMaxWidth().testTag("boss_glitch_terminal"),
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Text(
                  text = "ERR_0xDEC1 // FIRING SEQUENCE INTERRUPTED",
                  color = Color(0xFFFF5252),
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "> TEMPERATURE DROPPING: -42°C...",
                  color = Color(0xFF80DEEA),
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                )
                Text(
                  text = "> MECHA_GOBBLER_PRIME.exe has glitched out!",
                  color = Color.White,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                )
              }
            }
          }
        }

        2 -> {
          Box(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "...",
              color = Color(0x44FFFFFF),
              style = MaterialTheme.typography.headlineMedium,
              fontFamily = FontFamily.Monospace,
            )
          }
        }

        3 -> {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            FrozenChristmasCatIceCubeCanvas(
              meltProgress = meltDropProgress,
              icePulse = icePulse,
              modifier = Modifier.size(220.dp),
            )
            Text(
              text = "Out of the darkness, a faint chill lingers...",
              style = MaterialTheme.typography.titleMedium,
              color = Color(0xFFB3E5FC),
              textAlign = TextAlign.Center,
            )
            Text(
              text =
                "A Christmas Cat sits frozen inside a solid block of crystal ice... and tiny droplets show it's slowly melting.",
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFFE1F5FE),
              textAlign = TextAlign.Center,
            )
          }
        }

        else -> {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
          ) {
            FrozenChristmasCatIceCubeCanvas(
              meltProgress = meltDropProgress,
              icePulse = icePulse,
              modifier = Modifier.size(185.dp),
            )

            Surface(
              color = Color(0xFF0D2B3E),
              shape = CircleShape,
              border = BorderStroke(1.5.dp, Color(0xFF81D4FA)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.AcUnit,
                  contentDescription = null,
                  tint = Color(0xFF81D4FA),
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "WINTER TEASER",
                  color = Color(0xFFE1F5FE),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                )
              }
            }

            Text(
              text = "Fully Melts On December 1st.",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF80DEEA),
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("december_1st_melt_text"),
            )

            Card(
              colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2336)),
              border = BorderStroke(1.5.dp, Color(0xFF4FC3F7)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
              ) {
                CatAvatarMini(
                  patternId = patternId,
                  accessoryId = "melted_solstice_crown",
                  modifier = Modifier.size(64.dp),
                )
                Column(
                  modifier = Modifier.weight(1f),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.AutoAwesome,
                      contentDescription = null,
                      tint = Color(0xFF80DEEA),
                      modifier = Modifier.size(16.dp),
                    )
                    Text(
                      text =
                        if (firstTimeReward) "LIMITED-TIME ITEM UNLOCKED"
                        else "CITADEL ANOMALY CLEARED",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF80DEEA),
                    )
                  }
                  Text(
                    text = "Melting Solstice Crown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                  )
                  Text(
                    text =
                      if (firstTimeReward) {
                        "Limited-Time Frost Accessory + 5 🦃 Turkeys + 1 💎 Gem + Purrs!"
                      } else {
                        "+3 🦃 Turkeys + Scaled Purrs & Pass XP!"
                      },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB3E5FC),
                  )
                }
              }
            }

            Button(
              onClick = onComplete,
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF4FC3F7),
                  contentColor = Color(0xFF031D2B),
                ),
              modifier =
                Modifier.fillMaxWidth().height(52.dp).testTag("boss_cutscene_claim_button"),
            ) {
              Text(
                text =
                  if (firstTimeReward) "Claim Limited-Time Item & Continue"
                  else "Collect Bonus & Return",
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Custom Canvas illustration of Mecha-Gobbler Prime (Citadel Boss).
 */
@Composable
fun MechaGobblerBossCanvas(
  glitchOffset: Float,
  overdrive: Boolean,
  modifier: Modifier = Modifier,
) {
  Canvas(
    modifier = modifier.semantics { contentDescription = "Cornucopia Citadel Boss Mecha-Gobbler Prime" }
  ) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f + glitchOffset
    val cy = h * 0.56f
    val r = minOf(w, h) * 0.30f

    // Metallic fan blades behind boss
    val bladeColors =
      if (overdrive) {
        listOf(Color(0xFFFF1744), Color(0xFFFF9100), Color(0xFFFFD54F), Color(0xFFFF9100), Color(0xFFFF1744))
      } else {
        listOf(Color(0xFF78909C), Color(0xFFD84315), Color(0xFFFFB300), Color(0xFFD84315), Color(0xFF78909C))
      }
    listOf(-55f, -27f, 0f, 27f, 55f).forEachIndexed { idx, deg ->
      val rad = Math.toRadians(deg.toDouble())
      val bx = cx + (Math.sin(rad) * r * 1.25).toFloat()
      val by = cy - (Math.cos(rad) * r * 1.18).toFloat()
      drawOval(
        color = bladeColors[idx],
        topLeft = Offset(bx - r * 0.28f, by - r * 0.52f),
        size = Size(r * 0.56f, r * 1.04f),
      )
      drawOval(
        color = Color(0xFF1E272C),
        topLeft = Offset(bx - r * 0.28f, by - r * 0.52f),
        size = Size(r * 0.56f, r * 1.04f),
        style = Stroke(width = 2.5f),
      )
    }

    // Armored chassis
    drawOval(
      color = Color(0xFF37474F),
      topLeft = Offset(cx - r * 1.08f, cy - r * 0.72f),
      size = Size(r * 2.16f, r * 1.62f),
    )
    drawOval(
      color = Color(0xFFFFB300),
      topLeft = Offset(cx - r * 0.65f, cy - r * 0.20f),
      size = Size(r * 1.30f, r * 0.85f),
      style = Stroke(width = 3f),
    )

    // Crimson power core
    drawCircle(
      color = if (overdrive) Color(0xFFFF1744) else Color(0xFFFF6D00),
      radius = r * 0.26f,
      center = Offset(cx, cy + r * 0.22f),
    )

    // Mech head + glowing visor
    val hx = cx
    val hy = cy - r * 0.68f
    drawRoundRect(
      color = Color(0xFF455A64),
      topLeft = Offset(hx - r * 0.56f, hy - r * 0.42f),
      size = Size(r * 1.12f, r * 0.78f),
      cornerRadius = CornerRadius(r * 0.24f, r * 0.24f),
    )
    drawRoundRect(
      color = Color(0xFFFF1744),
      topLeft = Offset(hx - r * 0.40f, hy - r * 0.16f),
      size = Size(r * 0.80f, r * 0.22f),
      cornerRadius = CornerRadius(r * 0.08f, r * 0.08f),
    )
    // Sharp beak
    val beak =
      Path().apply {
        moveTo(hx - r * 0.18f, hy + r * 0.12f)
        lineTo(hx + r * 0.18f, hy + r * 0.12f)
        lineTo(hx, hy + r * 0.44f)
        close()
      }
    drawPath(beak, Color(0xFFFFCA28))
  }
}

/**
 * Custom Canvas artwork showing the Christmas Cat frozen inside a slowly melting Ice Cube.
 */
@Composable
fun FrozenChristmasCatIceCubeCanvas(
  meltProgress: Float,
  icePulse: Float,
  modifier: Modifier = Modifier,
) {
  Canvas(
    modifier = modifier.semantics { contentDescription = "Christmas Cat Frozen Inside A Melting Ice Cube" }
  ) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.50f
    val boxSize = minOf(w, h) * 0.66f
    val left = cx - boxSize * 0.5f
    val top = cy - boxSize * 0.5f

    // Melt puddle beneath the ice cube
    drawOval(
      color = Color(0xFF4FC3F7).copy(alpha = 0.40f * icePulse),
      topLeft = Offset(cx - boxSize * 0.58f, top + boxSize - 10f),
      size = Size(boxSize * 1.16f, boxSize * 0.20f),
    )

    // Dim cold aura behind cube
    drawCircle(
      color = Color(0xFF29B6F6).copy(alpha = 0.22f * icePulse),
      radius = boxSize * 0.65f,
      center = Offset(cx, cy),
    )

    // Back face of ice cube
    drawRoundRect(
      color = Color(0x5581D4FA),
      topLeft = Offset(left + boxSize * 0.06f, top - boxSize * 0.05f),
      size = Size(boxSize, boxSize),
      cornerRadius = CornerRadius(22f, 22f),
    )

    // Christmas Cat silhouette inside the cube
    val catR = boxSize * 0.24f
    val catY = cy + boxSize * 0.06f
    // Ears
    listOf(-1f, 1f).forEach { side ->
      val ear =
        Path().apply {
          moveTo(cx + side * catR * 0.28f, catY - catR * 0.45f)
          lineTo(cx + side * catR * 0.85f, catY - catR * 1.10f)
          lineTo(cx + side * catR * 0.88f, catY - catR * 0.10f)
          close()
        }
      drawPath(ear, Color(0xFFE0F2F1))
    }
    // Cozy winter cat head & body
    drawOval(
      color = Color(0xFFF5F5F5),
      topLeft = Offset(cx - catR * 0.88f, catY + catR * 0.45f),
      size = Size(catR * 1.76f, catR * 1.10f),
    )
    drawCircle(
      color = Color(0xFFFAFAFA),
      radius = catR,
      center = Offset(cx, catY),
    )
    // Festive Red Scarf
    drawRoundRect(
      color = Color(0xFFD32F2F),
      topLeft = Offset(cx - catR * 0.72f, catY + catR * 0.74f),
      size = Size(catR * 1.44f, catR * 0.30f),
      cornerRadius = CornerRadius(10f, 10f),
    )
    // Sleeping closed eyes + tiny pink nose
    listOf(-1f, 1f).forEach { side ->
      val ex = cx + side * catR * 0.36f
      drawLine(
        color = Color(0xFF37474F),
        start = Offset(ex - catR * 0.14f, catY),
        end = Offset(ex + catR * 0.14f, catY),
        strokeWidth = 3f,
        cap = StrokeCap.Round,
      )
    }
    drawCircle(
      color = Color(0xFFFF8A80),
      radius = catR * 0.12f,
      center = Offset(cx, catY + catR * 0.22f),
    )
    // Red Santa Cap & White Pom-Pom
    val hatPath =
      Path().apply {
        moveTo(cx - catR * 0.76f, catY - catR * 0.62f)
        lineTo(cx + catR * 0.15f, catY - catR * 1.62f)
        lineTo(cx + catR * 0.76f, catY - catR * 0.62f)
        close()
      }
    drawPath(hatPath, Color(0xFFC62828))
    drawRoundRect(
      color = Color.White,
      topLeft = Offset(cx - catR * 0.84f, catY - catR * 0.78f),
      size = Size(catR * 1.68f, catR * 0.26f),
      cornerRadius = CornerRadius(12f, 12f),
    )
    drawCircle(
      color = Color.White,
      radius = catR * 0.22f,
      center = Offset(cx + catR * 0.18f, catY - catR * 1.66f),
    )

    // Translucent Front Ice Cube Frame + Facets
    drawRoundRect(
      brush =
        Brush.linearGradient(
          listOf(Color(0x88E1F5FE), Color(0x444FC3F7), Color(0x6681D4FA))
        ),
      topLeft = Offset(left, top),
      size = Size(boxSize, boxSize),
      cornerRadius = CornerRadius(24f, 24f),
    )
    drawRoundRect(
      color = Color(0xFFB3E5FC),
      topLeft = Offset(left, top),
      size = Size(boxSize, boxSize),
      cornerRadius = CornerRadius(24f, 24f),
      style = Stroke(width = 3.5f, join = StrokeJoin.Round),
    )

    // Frost specular shine + subtle thaw cracks
    drawLine(
      color = Color(0xAAFFFFFF),
      start = Offset(left + boxSize * 0.14f, top + boxSize * 0.16f),
      end = Offset(left + boxSize * 0.36f, top + boxSize * 0.16f),
      strokeWidth = 4f,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = Color(0x88E1F5FE),
      start = Offset(left + boxSize * 0.76f, top + boxSize * 0.20f),
      end = Offset(left + boxSize * 0.60f, top + boxSize * 0.36f),
      strokeWidth = 2.5f,
      cap = StrokeCap.Round,
    )

    // Animated slowly melting water droplets sliding down
    val dropY1 = top + boxSize * (0.28f + meltProgress * 0.74f)
    val dropY2 = top + boxSize * (0.12f + ((meltProgress + 0.5f) % 1f) * 0.88f)
    drawOval(
      color = Color(0xFF4FC3F7),
      topLeft = Offset(left + boxSize * 0.18f, dropY1),
      size = Size(10f, 16f),
    )
    drawOval(
      color = Color(0xFF81D4FA),
      topLeft = Offset(left + boxSize * 0.80f, dropY2),
      size = Size(9f, 15f),
    )
  }
}
