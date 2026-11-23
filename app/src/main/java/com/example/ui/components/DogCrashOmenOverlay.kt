package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val omenDialogueLines =
  listOf(
    "...Did you really think nothing was wrong?",
    "I tried warning you. I told you the Hackers are coming to destroy the World Of Cats... and you didn't listen.",
    "The Day is coming very soon. When everything falls apart, remember I tried to warn you.",
  )

private val fakeCrashLogLines =
  listOf(
    "FATAL EXCEPTION: main",
    "Process: com.aistudio.purrtap.kxmpzq, PID: 09404",
    "WorldOfCatsRuntimeException: SANCTUARY_CORE_BREACHED",
    "  at sanctuary.kernel.PurrEngine.onPet(PurrEngine.kt:404)",
    "  at vault.keeper.Firewall.override(Unknown Source)",
    "ERR 0x09F4: EXTERNAL_INTRUSION_DETECTED [HACKERS]",
    "SYS_HALT: Attempting emergency recovery...",
  )

/**
 * One-time cinematic omen triggered on the player's first cat pet:
 * 1) Rapid glitch errors & fake crash dialog
 * 2) The Dog Vault Keeper intercepts the screen to deliver a final warning
 * 3) Vanishes with static and never appears again.
 */
@Composable
fun DogCrashOmenOverlay(
  onDismiss: () -> Unit,
) {
  // 0 = Glitch error flood, 1 = Fake crash popup, 2 = Dog Keeper speaking, 3 = Signal lost fade
  var stage by remember { mutableIntStateOf(0) }
  var dialogueIndex by remember { mutableIntStateOf(0) }
  var jitterTick by remember { mutableIntStateOf(0) }
  var visibleLogCount by remember { mutableIntStateOf(1) }
  var closing by remember { mutableStateOf(false) }

  // Prevent accidental back dismissal during the fake crash intro; allow after Dog finishes
  BackHandler(enabled = true) {
    if (stage == 2 && dialogueIndex >= omenDialogueLines.lastIndex) {
      closing = true
    }
  }

  LaunchedEffect(Unit) {
    for (i in 1..fakeCrashLogLines.size) {
      visibleLogCount = i
      jitterTick = i
      delay(160L)
    }
    stage = 1
    repeat(6) { tick ->
      jitterTick = tick + 10
      delay(240L)
    }
    stage = 2
  }

  LaunchedEffect(closing) {
    if (closing) {
      stage = 3
      delay(650L)
      onDismiss()
    }
  }

  val jitterX = if (stage <= 1 || stage == 3) ((jitterTick % 3) - 1) * 4 else 0
  val jitterY = if (stage <= 1 || stage == 3) (((jitterTick + 1) % 3) - 1) * 2 else 0

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Color(0xFF050807))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = {
            if (stage == 2 && !closing) {
              if (dialogueIndex < omenDialogueLines.lastIndex) {
                dialogueIndex++
              } else {
                closing = true
              }
            }
          },
        )
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(20.dp)
        .testTag("dog_crash_omen_overlay"),
    contentAlignment = Alignment.Center,
  ) {
    // Stage 0: Scrolling fatal error flood
    AnimatedVisibility(visible = stage == 0, enter = fadeIn(), exit = fadeOut()) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .widthIn(max = 520.dp)
            .offset(x = jitterX.dp, y = jitterY.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = "/// CRITICAL SYSTEM ERROR ///",
          color = Color(0xFFFF5252),
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        fakeCrashLogLines.take(visibleLogCount).forEach { line ->
          Text(
            text = line,
            color =
              if (line.startsWith("FATAL") || line.startsWith("ERR")) Color(0xFFFF6E6E)
              else Color(0xFF00E676),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
          )
        }
      }
    }

    // Stage 1: Convincing fake Android app crash dialog
    AnimatedVisibility(visible = stage == 1, enter = fadeIn(), exit = fadeOut()) {
      Surface(
        color = Color(0xFF1F2124),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, Color(0xFFFF5252).copy(alpha = 0.7f)),
        shadowElevation = 16.dp,
        modifier =
          Modifier.fillMaxWidth()
            .widthIn(max = 360.dp)
            .offset(x = jitterX.dp, y = jitterY.dp)
            .testTag("fake_crash_dialog"),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(22.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.ErrorOutline,
              contentDescription = null,
              tint = Color(0xFFFF5252),
              modifier = Modifier.size(26.dp),
            )
            Text(
              text = "PurrTap keeps stopping",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
          }
          Text(
            text = "Sanctuary process interrupted by unknown signal [0x09F4]. Attempting to close...",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFBDC1C6),
          )
          Text(
            text = "OVERRIDE DETECTED...",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF00E676),
          )
        }
      }
    }

    // Stage 2: The Dog Vault Keeper intercepts and delivers his one-time warning
    AnimatedVisibility(visible = stage == 2, enter = fadeIn(), exit = fadeOut()) {
      Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Text(
          text = "[ INCOMING TRANSMISSION — VAULT KEEPER ]",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = Color(0xFFFF5252),
          textAlign = TextAlign.Center,
        )

        DogVaultKeeperPortrait(
          agitationLevel = 6,
          isKickedOut = false,
          modifier = Modifier.size(164.dp),
        )

        Surface(
          color = Color(0xFF0E1914),
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.5.dp, Color(0xFF00E676).copy(alpha = 0.65f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              text = "THE DOG VAULT KEEPER",
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = Color(0xFF00E676),
            )
            Text(
              text = "\"${omenDialogueLines[dialogueIndex]}\"",
              style = MaterialTheme.typography.bodyLarge,
              fontFamily = FontFamily.Monospace,
              color = Color(0xFFE8FFF2),
              lineHeight = 24.sp,
              modifier = Modifier.testTag("dog_omen_dialogue_text"),
            )
            Text(
              text = "${dialogueIndex + 1} / ${omenDialogueLines.size}",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              color = Color(0xFF7FA892),
            )
          }
        }

        Button(
          onClick = {
            if (dialogueIndex < omenDialogueLines.lastIndex) {
              dialogueIndex++
            } else {
              closing = true
            }
          },
          colors =
            ButtonDefaults.buttonColors(
              containerColor =
                if (dialogueIndex < omenDialogueLines.lastIndex) Color(0xFF00E676)
                else Color(0xFFFF5252),
              contentColor = Color(0xFF04140B),
            ),
          modifier =
            Modifier.fillMaxWidth()
              .height(48.dp)
              .testTag("dog_omen_continue_button"),
        ) {
          Text(
            text =
              if (dialogueIndex < omenDialogueLines.lastIndex) "Continue..."
              else "Watch him vanish...",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
          )
        }
      }
    }

    // Stage 3: Signal lost static before vanishing forever
    AnimatedVisibility(visible = stage == 3, enter = fadeIn(), exit = fadeOut()) {
      Text(
        text = "[ SIGNAL LOST — HE IS GONE ]",
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Color(0xFF00E676).copy(alpha = 0.75f),
        textAlign = TextAlign.Center,
      )
    }
  }
}
