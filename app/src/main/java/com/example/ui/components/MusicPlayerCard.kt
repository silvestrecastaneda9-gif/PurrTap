package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.MusicPlaybackStatus
import com.example.audio.MusicTrack
import com.example.audio.RepeatMode
import com.example.data.local.AppSettingsState
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioMusicPlayerCard(
  settings: AppSettingsState,
  playbackStatus: MusicPlaybackStatus,
  onToggleMusic: () -> Unit,
  onSelectTrackById: (String) -> Unit,
  onSkipTrack: (Int) -> Unit,
  onSeekTo: (Long) -> Unit,
  onCycleRepeatMode: () -> Unit,
  onToggleShuffle: () -> Unit,
  onSetPlaybackSpeed: (Float) -> Unit,
  onSetSleepTimer: (Int) -> Unit,
  onMusicVolumeChange: (Float) -> Unit,
  onUploadAudioUri: (Uri) -> Unit,
  onRemoveCustomTrack: (String) -> Unit,
) {
  val audioPickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
      if (uri != null) {
        onUploadAudioUri(uri)
      }
    }

  val (trackTitle, trackSubtitle) = settings.currentTrackTitleAndSubtitle()
  var isDraggingSlider by remember { mutableStateOf(false) }
  var sliderDragFraction by remember { mutableFloatStateOf(0f) }
  var showSleepMenu by remember { mutableStateOf(false) }
  var showSpeedMenu by remember { mutableStateOf(false) }

  val safeDuration = playbackStatus.durationMs.coerceAtLeast(1L)
  val liveFraction = (playbackStatus.currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
  val displayedFraction = if (isDraggingSlider) sliderDragFraction else liveFraction
  val displayedPosMs = (displayedFraction * safeDuration).toLong()

  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    shape = RoundedCornerShape(24.dp),
    modifier = Modifier.fillMaxWidth().testTag("studio_music_player_card"),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Top Deck Header: Album Art + Track Info + Live Status Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
          modifier =
            Modifier.size(56.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.linearGradient(
                  colors =
                    listOf(
                      MaterialTheme.colorScheme.primary,
                      MaterialTheme.colorScheme.tertiary,
                    )
                )
              ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector =
              if (settings.isMusicPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.LibraryMusic,
            contentDescription = "Sanctuary Studio Deck",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(30.dp),
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = trackTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false),
            )
            Surface(
              color =
                if (settings.isMusicPlaying) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHighest,
              shape = RoundedCornerShape(8.dp),
            ) {
              Text(
                text = if (settings.isMusicPlaying) "LIVE" else "PAUSED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color =
                  if (settings.isMusicPlaying) MaterialTheme.colorScheme.onPrimaryContainer
                  else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
          Text(
            text = trackSubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      // Animated Equalizer Visualizer Strip
      Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().height(38.dp).padding(horizontal = 14.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          playbackStatus.waveformBars.forEach { barLevel ->
            val animHeight by animateFloatAsState(
              targetValue = barLevel.coerceIn(0.15f, 1f),
              animationSpec = tween(durationMillis = 220),
              label = "eq_bar",
            )
            Box(
              modifier =
                Modifier.weight(1f)
                  .height((24.dp * animHeight).coerceAtLeast(4.dp))
                  .clip(RoundedCornerShape(4.dp))
                  .background(
                    if (settings.isMusicPlaying) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                  )
            )
          }
        }
      }

      // Scrubbable Progress Bar & Timestamps
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Slider(
          value = displayedFraction,
          onValueChange = { fraction ->
            isDraggingSlider = true
            sliderDragFraction = fraction
          },
          onValueChangeFinished = {
            onSeekTo((sliderDragFraction * safeDuration).toLong())
            isDraggingSlider = false
          },
          modifier = Modifier.fillMaxWidth().testTag("music_seek_slider"),
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = formatTrackClock(displayedPosMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = formatTrackClock(safeDuration),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Transport Controls: Shuffle | Prev | Play/Pause | Next | Repeat
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = onToggleShuffle,
          modifier = Modifier.size(48.dp).testTag("music_shuffle_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Shuffle,
            contentDescription = "Toggle Shuffle",
            tint =
              if (settings.shuffleEnabled) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        FilledTonalIconButton(
          onClick = { onSkipTrack(-1) },
          modifier = Modifier.size(48.dp).testTag("music_prev_button"),
        ) {
          Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous Track")
        }

        FilledIconButton(
          onClick = onToggleMusic,
          colors =
            IconButtonDefaults.filledIconButtonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
          modifier = Modifier.size(58.dp).testTag("music_play_pause_button"),
        ) {
          Icon(
            imageVector =
              if (settings.isMusicPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (settings.isMusicPlaying) "Pause Music" else "Play Music",
            modifier = Modifier.size(30.dp),
          )
        }

        FilledTonalIconButton(
          onClick = { onSkipTrack(1) },
          modifier = Modifier.size(48.dp).testTag("music_next_button"),
        ) {
          Icon(Icons.Rounded.SkipNext, contentDescription = "Next Track")
        }

        IconButton(
          onClick = onCycleRepeatMode,
          modifier = Modifier.size(48.dp).testTag("music_repeat_button"),
        ) {
          Icon(
            imageVector =
              if (settings.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne
              else Icons.Rounded.Repeat,
            contentDescription = "Repeat Mode: ${settings.repeatMode.label}",
            tint =
              if (settings.repeatMode == RepeatMode.OFF) MaterialTheme.colorScheme.onSurfaceVariant
              else MaterialTheme.colorScheme.primary,
          )
        }
      }

      // Utility Pills: Speed + Sleep Timer + Upload Audio
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box {
          AssistChip(
            onClick = { showSpeedMenu = true },
            leadingIcon = {
              Icon(
                Icons.Rounded.Speed,
                contentDescription = "Playback Speed",
                modifier = Modifier.size(16.dp),
              )
            },
            label = { Text("${formatSpeedLabel(settings.playbackSpeed)}x") },
            modifier = Modifier.testTag("music_speed_button"),
          )
          DropdownMenu(
            expanded = showSpeedMenu,
            onDismissRequest = { showSpeedMenu = false },
          ) {
            listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { spd ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = "${formatSpeedLabel(spd)}x" + if (abs(settings.playbackSpeed - spd) < 0.02f) " ✓" else "",
                    fontWeight = if (abs(settings.playbackSpeed - spd) < 0.02f) FontWeight.Bold else FontWeight.Normal,
                  )
                },
                onClick = {
                  onSetPlaybackSpeed(spd)
                  showSpeedMenu = false
                },
              )
            }
          }
        }

        Box {
          val sleepActive = playbackStatus.sleepTimerRemainingMs > 0L
          AssistChip(
            onClick = { showSleepMenu = true },
            leadingIcon = {
              Icon(
                Icons.Rounded.Bedtime,
                contentDescription = "Sleep Timer",
                modifier = Modifier.size(16.dp),
              )
            },
            label = {
              Text(
                if (sleepActive) formatTrackClock(playbackStatus.sleepTimerRemainingMs)
                else "Sleep Timer"
              )
            },
            modifier = Modifier.testTag("music_sleep_timer_button"),
          )
          DropdownMenu(
            expanded = showSleepMenu,
            onDismissRequest = { showSleepMenu = false },
          ) {
            listOf(0 to "Off", 5 to "5 Minutes", 15 to "15 Minutes", 30 to "30 Minutes").forEach { (mins, label) ->
              DropdownMenuItem(
                text = { Text(label) },
                onClick = {
                  onSetSleepTimer(mins)
                  showSleepMenu = false
                },
              )
            }
          }
        }

        Spacer(Modifier.weight(1f))
        Text(
          text = settings.repeatMode.label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      // Volume Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          Icons.AutoMirrored.Rounded.VolumeUp,
          contentDescription = "Music Volume",
          tint = MaterialTheme.colorScheme.primary,
        )
        Slider(
          value = settings.musicVolume,
          onValueChange = onMusicVolumeChange,
          modifier = Modifier.weight(1f).testTag("music_volume_slider"),
        )
        Text(
          text = "${(settings.musicVolume * 100).toInt()}%",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.width(38.dp),
        )
      }

      // Track Queue Chips
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Sanctuary Queue & Custom Uploads",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          MusicTrack.entries.forEach { track ->
            val selected = settings.selectedTrackId == track.id
            FilterChip(
              selected = selected,
              onClick = { onSelectTrackById(track.id) },
              leadingIcon = {
                Icon(
                  imageVector = if (selected) Icons.Rounded.MusicNote else Icons.Rounded.LibraryMusic,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
              },
              label = { Text(track.title) },
              modifier = Modifier.testTag("music_track_${track.id}"),
            )
          }

          settings.customTracks.forEach { custom ->
            val selected = settings.selectedTrackId == custom.id
            FilterChip(
              selected = selected,
              onClick = { onSelectTrackById(custom.id) },
              label = { Text(custom.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
              trailingIcon = {
                IconButton(
                  onClick = { onRemoveCustomTrack(custom.id) },
                  modifier = Modifier.size(20.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Remove ${custom.title}",
                    modifier = Modifier.size(15.dp),
                  )
                }
              },
              modifier = Modifier.testTag("music_track_${custom.id}"),
            )
          }
        }
      }

      OutlinedButton(
        onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("upload_music_button"),
      ) {
        Icon(Icons.Rounded.UploadFile, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Import Audio Track (MP3, WAV, M4A, OGG)")
      }
    }
  }
}

private fun formatTrackClock(millis: Long): String {
  val totalSec = (millis / 1000L).coerceAtLeast(0L)
  val mins = totalSec / 60L
  val secs = totalSec % 60L
  return String.format(Locale.US, "%d:%02d", mins, secs)
}

private fun formatSpeedLabel(speed: Float): String {
  return if (abs(speed - 1.0f) < 0.02f) "1.0"
  else String.format(Locale.US, "%.2f", speed).trimEnd('0').trimEnd('.')
}
