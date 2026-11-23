package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Terminal
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val glitchWhispersByChannel =
  mapOf(
    "sanctuary" to
      listOf(
        "SYS_CHK // world_of_cats integrity 99.2% — minor packet drift",
        "K9_KEEPER:// ...the hackers are probing the perimeter...",
        "0x0F4A // firewall crack detected beneath Sanctuary Settings",
      ),
    "shop" to
      listOf(
        "LEDGER_SYNC // 0xDEAD_PAW: item table checksum flicker",
        "K9_ECHO // stock the vault... World Of Cats won't hold forever",
        "TRACE_LOG // unverified signal near Vault Terminal [Settings]",
      ),
    "quests" to
      listOf(
        "BADGE_BUS // signal interference: ...don't trust the quiet...",
        "SYS_WARN // vault_dog_01: hackers testing the outer gates",
        "CHK_SUM // timeline desync 0.04s — keep purring",
      ),
    "guide" to
      listOf(
        "ARCHIVE_09 // redacted footer: the dog inside Settings wasn't lying",
        "PKT_404 // corrupted footnote detected in Sanctuary records",
        "K9_SIGNAL // ...when the sky fractures, remember the Code Vault...",
      ),
  )

@Composable
fun SubtleGlitchLoreWhisper(
  channel: String,
  modifier: Modifier = Modifier,
) {
  val lines =
    remember(channel) {
      glitchWhispersByChannel[channel]
        ?: listOf("SYS_PING // static_leak: ...the hackers are near...")
    }
  var messageIndex by remember(channel) { mutableIntStateOf(0) }
  var isGlitching by remember(channel) { mutableStateOf(false) }
  var jitterX by remember(channel) { mutableIntStateOf(0) }

  LaunchedEffect(channel) {
    delay(4_000L)
    while (true) {
      isGlitching = true
      val offsets = listOf(-3, 4, -2, 3, -1, 2, 0)
      for (dx in offsets) {
        jitterX = dx
        delay(85L)
      }
      isGlitching = false
      jitterX = 0
      delay(16_000L)
      messageIndex = (messageIndex + 1) % lines.size
    }
  }

  val baseText = lines[messageIndex]
  val displayedText =
    if (isGlitching) {
      baseText.replace('e', '3').replace('o', '0')
    } else {
      baseText
    }

  Surface(
    color =
      if (isGlitching) Color(0x2200E5A3)
      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
    border =
      BorderStroke(
        width = 0.75.dp,
        color =
          if (isGlitching) Color(0x88FF4D6D)
          else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
      ),
    shape = MaterialTheme.shapes.small,
    modifier =
      modifier
        .fillMaxWidth()
        .alpha(if (isGlitching) 0.92f else 0.58f)
        .testTag("glitch_lore_whisper_$channel"),
  ) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
      if (isGlitching) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.offset { IntOffset(x = -jitterX, y = 0) }.alpha(0.55f),
        ) {
          Icon(
            imageVector = Icons.Rounded.Terminal,
            contentDescription = null,
            tint = Color(0xFFFF4D6D),
            modifier = Modifier.size(12.dp),
          )
          Text(
            text = displayedText,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color(0xFFFF4D6D),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.offset { IntOffset(x = jitterX, y = 0) },
      ) {
        Icon(
          imageVector = Icons.Rounded.Terminal,
          contentDescription = null,
          tint =
            if (isGlitching) Color(0xFF00E5A3)
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
          modifier = Modifier.size(12.dp),
        )
        Text(
          text = displayedText,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          fontWeight = if (isGlitching) FontWeight.Bold else FontWeight.Normal,
          color =
            if (isGlitching) Color(0xFF00E5A3)
            else MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
