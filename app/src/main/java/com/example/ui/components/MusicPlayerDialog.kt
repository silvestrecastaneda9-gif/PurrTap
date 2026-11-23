package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.MusicPlaybackStatus
import com.example.data.local.AppSettingsState

@Composable
fun MusicPlayerDialog(
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
  onDismiss: () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 24.dp)
          .testTag("music_player_dialog"),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.MusicNote,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp),
            )
            Column {
              Text(
                text = "Sanctuary Music Studio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text =
                  if (playbackStatus.isPlaying) "Now Playing • Cozy Studio"
                  else "Cozy beats & custom tracks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("music_player_close_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close Music Player")
          }
        }

        Column(
          modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        ) {
          StudioMusicPlayerCard(
            settings = settings,
            playbackStatus = playbackStatus,
            onToggleMusic = onToggleMusic,
            onSelectTrackById = onSelectTrackById,
            onSkipTrack = onSkipTrack,
            onSeekTo = onSeekTo,
            onCycleRepeatMode = onCycleRepeatMode,
            onToggleShuffle = onToggleShuffle,
            onSetPlaybackSpeed = onSetPlaybackSpeed,
            onSetSleepTimer = onSetSleepTimer,
            onMusicVolumeChange = onMusicVolumeChange,
            onUploadAudioUri = onUploadAudioUri,
            onRemoveCustomTrack = onRemoveCustomTrack,
          )
        }
      }
    }
  }
}
