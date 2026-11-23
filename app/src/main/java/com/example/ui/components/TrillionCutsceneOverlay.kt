package com.example.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.MilitaryTech
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TrillionCutsceneOverlay(
  catName: String,
  playerName: String,
  onComplete: () -> Unit,
) {
  var phase by remember { mutableIntStateOf(0) }
  val safeCat = catName.ifBlank { "Mochi" }
  val safePlayer = playerName.ifBlank { "Caretaker" }

  // Auto-advance the glitch deletion & red eyes beats for cinematic pacing
  LaunchedEffect(phase) {
    if (phase == 2) {
      delay(3600L)
      phase = 3
    } else if (phase == 3) {
      delay(3400L)
      phase = 4
    }
  }

  val infinite = rememberInfiniteTransition(label = "trillionCutscenePulse")
  val eyeGlow by
    infinite.animateFloat(
      initialValue = 0.7f,
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
      label = "redEyesGlow",
    )

  val bgBrush =
    when (phase) {
      0, 1 ->
        Brush.verticalGradient(
          listOf(Color(0xFF1F1235), Color(0xFF3A1F5D), Color(0xFF1A0F2E))
        )
      2 ->
        Brush.verticalGradient(
          listOf(Color(0xFF14070B), Color(0xFF2B0A10), Color(0xFF0A0406))
        )
      3 ->
        Brush.verticalGradient(
          listOf(Color(0xFF050203), Color(0xFF0A0204), Color(0xFF000000))
        )
      else ->
        Brush.verticalGradient(
          listOf(Color(0xFF1B1328), Color(0xFF2B1B3D), Color(0xFF120B1C))
        )
    }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(bgBrush)
        .statusBarsPadding()
        .padding(20.dp)
        .testTag("trillion_cutscene_overlay"),
  ) {
    // Top-left terminal deletion command during Phase 2
    if (phase == 2) {
      Surface(
        color = Color(0xDD0B0B0D),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFFF1744)),
        modifier =
          Modifier.align(Alignment.TopStart)
            .padding(top = 8.dp)
            .testTag("miga_delete_command_box"),
      ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
          Text(
            text = "root@sanctuary:~$ sudo delete --entity MIGA_THE_GREAT",
            color = Color(0xFFFF5252),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "> Purging Miga the Great... [DELETED]",
            color = Color(0xFFFF8A80),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
          )
        }
      }
    }

    AnimatedContent(
      targetState = phase,
      transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(350)) },
      modifier = Modifier.align(Alignment.Center).fillMaxWidth().widthIn(max = 520.dp),
      label = "cutscenePhase",
    ) { currentPhase ->
      when (currentPhase) {
        0 -> {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            MigaRoyalEmblem(glitching = false)
            Surface(
              color = Color(0xFFFFD54F).copy(alpha = 0.2f),
              shape = CircleShape,
              border = BorderStroke(1.dp, Color(0xFFFFD54F)),
            ) {
              Text(
                text = "1,000,000,000,000 PURRS REACHED",
                color = Color(0xFFFFE082),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              )
            }
            Text(
              text = "A Royal Invitation",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              textAlign = TextAlign.Center,
            )
            Text(
              text =
                "The Sanctuary doors open to a golden hall. Miga the Great, Sovereign of the Ancient Purrs, has personally invited $safeCat and $safePlayer to an audience.",
              style = MaterialTheme.typography.bodyLarge,
              color = Color(0xFFE6DFF5),
              textAlign = TextAlign.Center,
            )
            Button(
              onClick = { phase = 1 },
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFFFCA28),
                  contentColor = Color(0xFF2B1600),
                ),
              modifier = Modifier.height(48.dp).testTag("trillion_cutscene_next_0"),
            ) {
              Text("Step Into the Hall", fontWeight = FontWeight.Bold)
            }
          }
        }
        1 -> {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            MigaRoyalEmblem(glitching = false)
            Text(
              text = "Miga the Great",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFFD54F),
            )
            Card(
              colors = CardDefaults.cardColors(containerColor = Color(0x44000000)),
              border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.6f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text =
                  "\"$safePlayer... and dear $safeCat. Few in all the realms have ever shown such devotion. One Trillion Purrs — earned paw by paw, heartbeat by heartbeat. You have my deepest admiration. Carry this honor back to your Sanctuary...\"",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(18.dp),
              )
            }
            Button(
              onClick = { phase = 2 },
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFFFCA28),
                  contentColor = Color(0xFF2B1600),
                ),
              modifier = Modifier.height(48.dp).testTag("trillion_cutscene_next_1"),
            ) {
              Text("Bow & Leave the Hall", fontWeight = FontWeight.Bold)
            }
          }
        }
        2 -> {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            MigaRoyalEmblem(glitching = true)
            Text(
              text = "As $safeCat steps out of the hall...",
              style = MaterialTheme.typography.titleMedium,
              color = Color(0xFFFF8A80),
              fontFamily = FontFamily.Monospace,
              textAlign = TextAlign.Center,
            )
            Text(
              text = "Miga the Great flickers into crimson static and vanishes.",
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFFFFCDD2),
              textAlign = TextAlign.Center,
            )
          }
        }
        3 -> {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
          ) {
            // Glowing Red Eyes in the dark
            Canvas(modifier = Modifier.size(width = 220.dp, height = 90.dp)) {
              val leftEye = Offset(size.width * 0.30f, size.height * 0.5f)
              val rightEye = Offset(size.width * 0.70f, size.height * 0.5f)
              listOf(leftEye, rightEye).forEach { center ->
                drawCircle(
                  color = Color(0xFFFF1744).copy(alpha = 0.25f * eyeGlow),
                  radius = 36f,
                  center = center,
                )
                drawCircle(
                  color = Color(0xFFFF1744).copy(alpha = 0.85f * eyeGlow),
                  radius = 16f,
                  center = center,
                )
                drawCircle(
                  color = Color(0xFFFFEBEE),
                  radius = 5f,
                  center = center,
                )
              }
            }
            Text(
              text = "Could it be... Pug the Hacker?",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFF5252),
              fontFamily = FontFamily.Monospace,
              textAlign = TextAlign.Center,
            )
          }
        }
        else -> {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Surface(
              color = Color(0xFFFFD54F).copy(alpha = 0.18f),
              shape = CircleShape,
              border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.MilitaryTech,
                  contentDescription = null,
                  tint = Color(0xFFFFD54F),
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "ROYAL ACHIEVEMENT UNLOCKED",
                  color = Color(0xFFFFE082),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                )
              }
            }

            Text(
              text = "A Note Left Behind by Miga",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              textAlign = TextAlign.Center,
            )

            Card(
              colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1D3D)),
              border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Text(
                  text = "Dearest $safePlayer & $safeCat,",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFFE082),
                )
                Text(
                  text =
                    "Congratulations on reaching One Trillion Purrs. Truly, no caretaker has ever shone brighter. Take my Royal Decree — a rank rarer than Mythic.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.White,
                )
                Text(
                  text =
                    "But please... be careful. Someone with red eyes is rewriting the Sanctuary from the shadows. Guard your purrs, and trust your instincts.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFFFFAB91),
                  fontWeight = FontWeight.Medium,
                )
                Text(
                  text = "— Miga the Great",
                  style = MaterialTheme.typography.labelLarge,
                  color = Color(0xFFFFD54F),
                  modifier = Modifier.align(Alignment.End),
                )
              }
            }

            Surface(
              color = Color(0xFF4A148C),
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.AutoAwesome,
                  contentDescription = null,
                  tint = Color(0xFFFFD54F),
                  modifier = Modifier.size(24.dp),
                )
                Column {
                  Text(
                    text = "Miga's Royal Decree",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                  )
                  Text(
                    text = "ROYAL TIER • Rarer than Mythic",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFE082),
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
              onClick = onComplete,
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFFFCA28),
                  contentColor = Color(0xFF2B1600),
                ),
              modifier = Modifier.fillMaxWidth().height(50.dp).testTag("trillion_cutscene_claim_button"),
            ) {
              Text("Accept Royal Achievement", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MigaRoyalEmblem(glitching: Boolean) {
  Canvas(modifier = Modifier.size(110.dp)) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.55f
    // Aura
    drawCircle(
      color = (if (glitching) Color(0xFFFF1744) else Color(0xFFFFD54F)).copy(alpha = 0.22f),
      radius = size.minDimension * 0.48f,
      center = Offset(cx, cy),
    )
    // Crown
    val crown = Path().apply {
      moveTo(cx - 28f, cy - 18f)
      lineTo(cx - 22f, cy - 42f)
      lineTo(cx - 8f, cy - 26f)
      lineTo(cx, cy - 46f)
      lineTo(cx + 8f, cy - 26f)
      lineTo(cx + 22f, cy - 42f)
      lineTo(cx + 28f, cy - 18f)
      close()
    }
    drawPath(crown, color = if (glitching) Color(0xFFFF5252) else Color(0xFFFFD54F))
    // Royal feline head silhouette
    drawCircle(
      color = if (glitching) Color(0xFF3E0B14) else Color(0xFFEDE7F6),
      radius = 28f,
      center = Offset(cx, cy + 10f),
    )
    // Eyes
    drawCircle(
      color = if (glitching) Color(0xFFFF1744) else Color(0xFF5E35B1),
      radius = 4.5f,
      center = Offset(cx - 10f, cy + 8f),
    )
    drawCircle(
      color = if (glitching) Color(0xFFFF1744) else Color(0xFF5E35B1),
      radius = 4.5f,
      center = Offset(cx + 10f, cy + 8f),
    )
  }
}
