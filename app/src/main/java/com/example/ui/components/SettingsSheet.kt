package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewCompact
import androidx.compose.material.icons.rounded.ViewSidebar
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.example.widget.PurrWidgetUpdater
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.MusicPlaybackStatus
import com.example.data.local.AppSettingsState
import com.example.data.model.GameCatalog
import com.example.data.model.NavigationLayoutFormat
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.PurrGameUiState
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
  uiState: PurrGameUiState,
  settings: AppSettingsState,
  playbackStatus: MusicPlaybackStatus = MusicPlaybackStatus(),
  onSelectThemeMode: (ThemeMode) -> Unit,
  onSelectNavigationFormat: (NavigationLayoutFormat) -> Unit = {},
  onSelectColorTheme: (AppColorTheme) -> Unit,
  onToggleSfx: (Boolean) -> Unit,
  onSfxVolumeChange: (Float) -> Unit,
  onTestSfx: () -> Unit,
  onToggleMusic: () -> Unit,
  onSelectTrackById: (String) -> Unit,
  onSkipTrack: (Int) -> Unit,
  onSeekTo: (Long) -> Unit = {},
  onCycleRepeatMode: () -> Unit = {},
  onToggleShuffle: () -> Unit = {},
  onSetPlaybackSpeed: (Float) -> Unit = {},
  onSetSleepTimer: (Int) -> Unit = {},
  onMusicVolumeChange: (Float) -> Unit,
  onUploadAudioUri: (Uri) -> Unit,
  onRemoveCustomTrack: (String) -> Unit,
  onOpenSignInOptions: () -> Unit = {},
  onOpenProfileEditor: () -> Unit = {},
  onOpenAdminPanel: () -> Unit = {},
  onDeleteAllData: () -> Unit = {},
  onOpenSecretVault: () -> Unit = {},
  onOpenWidgetsGallery: () -> Unit = {},
  onNotifyToast: (String) -> Unit = {},
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedTab by remember { mutableIntStateOf(0) }
  var showDeleteConfirmation by remember { mutableStateOf(false) }
  var deleteConfirmInput by remember { mutableStateOf("") }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(text = "Sanctuary Settings", style = MaterialTheme.typography.headlineSmall)

      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
          icon = {
            Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
          },
          modifier = Modifier.testTag("settings_tab_preferences"),
        ) {
          Text("Preferences", maxLines = 1)
        }
        SegmentedButton(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
          icon = {
            Icon(Icons.Rounded.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
          },
          modifier = Modifier.testTag("settings_tab_stats"),
        ) {
          Text("Stats", maxLines = 1)
        }
      }

      if (selectedTab == 1) {
        LifetimeStatsSection(uiState = uiState)
      } else {

      // 0. Sign-In & Caretaker Account Options
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Rounded.AccountCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Column {
              Text(
                text = "Caretaker Account & Backups",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
              Text(
                text =
                  when {
                    uiState.isSignedIn -> "Google Cloud (${uiState.profile.displayName})"
                    settings.accountMode == "local" ->
                      "Local Account (${settings.localAccountCode.ifBlank { "Offline ID" }})"
                    else -> "Guest Play • Export codes or create local ID"
                  },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
              )
            }
          }
          OutlinedButton(
            onClick = onOpenSignInOptions,
            modifier = Modifier.height(48.dp).testTag("settings_sign_in_options_button"),
          ) {
            Text("Manage")
          }
        }
      }

      // 1. Light / Dark / System Mode
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Theme Brightness",
          style = MaterialTheme.typography.titleMedium,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          ThemeMode.entries.forEachIndexed { index, mode ->
            val icon =
              when (mode) {
                ThemeMode.SYSTEM -> Icons.Rounded.SettingsBrightness
                ThemeMode.LIGHT -> Icons.Rounded.LightMode
                ThemeMode.DARK -> Icons.Rounded.DarkMode
              }
            SegmentedButton(
              selected = settings.themeMode == mode,
              onClick = { onSelectThemeMode(mode) },
              shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
              icon = {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
              },
              modifier = Modifier.testTag("theme_mode_${mode.id}"),
            ) {
              Text(text = mode.label, maxLines = 1)
            }
          }
        }
      }

      // 1B. Navigation Layout Format (Bottom Bar / Side Rail / Clean Floating Dock)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Navigation Layout Format",
          style = MaterialTheme.typography.titleMedium,
        )
        Text(
          text = settings.navigationFormat.subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          NavigationLayoutFormat.entries.forEachIndexed { index, format ->
            val icon =
              when (format) {
                NavigationLayoutFormat.BOTTOM_BAR -> Icons.Rounded.ViewAgenda
                NavigationLayoutFormat.SIDE_RAIL -> Icons.Rounded.ViewSidebar
                NavigationLayoutFormat.CLEAN_FLOATING -> Icons.Rounded.ViewCompact
              }
            SegmentedButton(
              selected = settings.navigationFormat == format,
              onClick = { onSelectNavigationFormat(format) },
              shape =
                SegmentedButtonDefaults.itemShape(
                  index = index,
                  count = NavigationLayoutFormat.entries.size,
                ),
              icon = {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
              },
              modifier = Modifier.testTag("nav_format_${format.id}"),
            ) {
              Text(
                text = format.label,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }

      // 2. Color Themes (Free Themes Only — Limited Themes live in Inventory)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "Color Themes",
            style = MaterialTheme.typography.titleMedium,
          )
        }
        Text(
          text = "Free Sanctuary palettes. Limited-time & event themes are organized in More → Inventory.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          AppColorTheme.freeThemes.forEach { palette ->
            val selected = settings.colorTheme == palette
            val isUnlocked = palette.isFree || palette.id in settings.unlockedColorThemes
            Card(
              colors =
                CardDefaults.cardColors(
                  containerColor =
                    when {
                      selected -> MaterialTheme.colorScheme.primaryContainer
                      isUnlocked -> MaterialTheme.colorScheme.surfaceVariant
                      else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    }
                ),
              border =
                if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
              modifier =
                Modifier.clip(MaterialTheme.shapes.medium)
                  .clickable { onSelectColorTheme(palette) }
                  .testTag("color_theme_${palette.id}"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Box(
                  modifier =
                    Modifier.size(18.dp)
                      .clip(CircleShape)
                      .background(palette.previewPrimary)
                )
                Box(
                  modifier =
                    Modifier.size(14.dp)
                      .clip(CircleShape)
                      .background(palette.previewSecondary)
                )
                Column {
                  Text(
                    text = palette.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                  )
                  Text(
                    text =
                      when {
                        palette.isFree -> "Free"
                        isUnlocked -> "Unlocked"
                        else -> "${palette.unlockSourceLabel} • ${palette.requirementSummary}"
                      },
                    style = MaterialTheme.typography.labelSmall,
                    color =
                      if (isUnlocked) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  if (!palette.isFree) {
                    Text(
                      text = "${uiState.existCountFor("THEME", palette.id)} Exist",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.tertiary,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                }
                if (selected) {
                  Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                  )
                } else if (!isUnlocked) {
                  Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp),
                  )
                }
              }
            }
          }
        }
      }

      // 3. Cat Purr & Meow Sound Effects
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
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
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.Rounded.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
              Column {
                Text(text = "Cat Purr & Meow SFX", style = MaterialTheme.typography.titleMedium)
                Text(
                  text = "Unique feline sounds when tapping your cat",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            Switch(
              checked = settings.sfxEnabled,
              onCheckedChange = onToggleSfx,
              modifier = Modifier.testTag("sfx_toggle_switch"),
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
              contentDescription = "SFX volume",
              modifier = Modifier.size(18.dp),
            )
            Slider(
              value = settings.sfxVolume,
              onValueChange = onSfxVolumeChange,
              enabled = settings.sfxEnabled,
              valueRange = 0f..1f,
              modifier = Modifier.weight(1f).testTag("sfx_volume_slider"),
            )
            FilledTonalButton(
              onClick = onTestSfx,
              enabled = settings.sfxEnabled,
              modifier = Modifier.height(40.dp).testTag("test_sfx_button"),
            ) {
              Text("Test")
            }
          }
        }
      }

      // 4B. Public Profile & Privacy
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Rounded.Public,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Column {
              Text(
                text = "Public Profile & Privacy",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text =
                  if (uiState.profile.statsPublic) "Stats Public • Custom PFP & Banner"
                  else "Stats Private • Locked on Leaderboard",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          OutlinedButton(
            onClick = onOpenProfileEditor,
            modifier = Modifier.height(48.dp).testTag("settings_public_profile_button"),
          ) {
            Text("Edit")
          }
        }
      }

      // 4C. Verified Admin Panel (strictly email-gated)
      if (uiState.isAdmin) {
        Card(
          onClick = onOpenAdminPanel,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
          border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary),
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth().testTag("settings_admin_panel_button"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.Rounded.AdminPanelSettings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
              )
              Column {
                Text(
                  text = "Sanctuary Admin Panel",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                  text = "Moderation, Global Messages, Weather & Currency Grants",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
                )
              }
            }
            Text(
              text = "OPEN",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.tertiary,
            )
          }
        }
      }

      // 4D. Danger Zone — Fresh Start Wipe (placed above Secret Vault at bottom)
      Card(
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
          ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.65f)),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Rounded.DeleteForever,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
            )
            Column {
              Text(
                text = "Danger Zone • Fresh Start",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
              )
              Text(
                text = "Delete all progress, currency, themes, custom tracks & cloud profile",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
              )
            }
          }
          OutlinedButton(
            onClick = {
              deleteConfirmInput = ""
              showDeleteConfirmation = true
            },
            colors =
              ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            modifier = Modifier.height(48.dp).testTag("danger_zone_delete_button"),
          ) {
            Text("Delete All")
          }
        }
      }

      // 5. Secret Vault at the very bottom of Settings
      val isVaultLockedOut = uiState.vaultCooldownRemainingMillis > 0L
      Card(
        onClick = onOpenSecretVault,
        colors =
          CardDefaults.cardColors(
            containerColor =
              if (isVaultLockedOut) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
          ),
        border =
          BorderStroke(
            1.dp,
            if (isVaultLockedOut) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
          ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().testTag("secret_vault_button"),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = if (isVaultLockedOut) Icons.Rounded.LockClock else Icons.Rounded.Terminal,
              contentDescription = null,
              tint =
                if (isVaultLockedOut) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
            )
            Column {
              Text(
                text = "??? Secret Vault",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text =
                  when {
                    isVaultLockedOut ->
                      "Kicked out by Dog Vault Keeper • ${uiState.vaultCooldownText} left"
                    uiState.challengeState.hackerVaultUnlocked ->
                      "Code Vault • Hacker Protocol & Intruder Unlocked"
                    else -> "Encrypted Chamber • Tap to enter Code Vault"
                  },
                style = MaterialTheme.typography.bodySmall,
                color =
                  if (isVaultLockedOut) MaterialTheme.colorScheme.error
                  else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Surface(
            color =
              if (isVaultLockedOut) MaterialTheme.colorScheme.error
              else MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small,
          ) {
            Text(
              text = if (isVaultLockedOut) "LOCKED" else "OPEN",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color =
                if (isVaultLockedOut) MaterialTheme.colorScheme.onError
                else MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
          }
        }
      }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }
  }

  if (showDeleteConfirmation) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmation = false },
      icon = {
        Icon(
          imageVector = Icons.Rounded.Warning,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error,
        )
      },
      title = { Text("Delete All Sanctuary Data?") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text =
              "This permanently wipes your Purrs, Rebirths, Ascensions, Gato Shards, Rare Gems, Battle Pass, unlocked Themes, custom tracks, and Leaderboard entry so you can start completely fresh.",
            style = MaterialTheme.typography.bodySmall,
          )
          OutlinedTextField(
            value = deleteConfirmInput,
            onValueChange = { deleteConfirmInput = it },
            label = { Text("Type DELETE to confirm") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("confirm_delete_all_input"),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirmation = false
            onDeleteAllData()
          },
          enabled = deleteConfirmInput.trim() == "DELETE",
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError,
            ),
          modifier = Modifier.testTag("confirm_delete_all_button"),
        ) {
          Text("Wipe & Start Fresh")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirmation = false }) {
          Text("Cancel")
        }
      },
    )
  }
}

@Composable
private fun LifetimeStatsSection(uiState: PurrGameUiState) {
  val profile = uiState.profile
  val challenge = uiState.challengeState
  val stats = uiState.playerStats
  val numberFormat = NumberFormat.getNumberInstance(Locale.US)
  val joinedDate =
    if (stats.joinedTimestampMillis > 0L) {
      DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(Date(stats.joinedTimestampMillis))
    } else {
      "Today"
    }
  val timePlayedText =
    GameCatalog.formatElapsedDuration(stats.totalTimePlayedSeconds.coerceAtLeast(1L) * 1000L)
  val timeAwayText =
    if (stats.totalOfflineSeconds > 0L) GameCatalog.formatElapsedDuration(stats.totalOfflineSeconds * 1000L)
    else "0s"

  Column(
    modifier = Modifier.fillMaxWidth().testTag("lifetime_stats_container"),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
      shape = MaterialTheme.shapes.large,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "${profile.displayName}'s Sanctuary Record",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
        Text(
          text = "Joined $joinedDate • Active Time: $timePlayedText • ${numberFormat.format(stats.totalPlaySessions)} Sessions",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
        )
      }
    }

    StatsGroupCard(
      title = "Petting & Purr Economy",
      rows =
        listOf(
          "Lifetime Purrs Earned" to GameCatalog.formatCompactPurrs(profile.totalPurrsEarned),
          "Active ${uiState.activeCurrencyLabel}" to
            GameCatalog.formatCompactPurrs(uiState.activeCurrencyBalance),
          "Lifetime Cat Pets" to numberFormat.format(profile.totalPets),
          "Current Tap Power" to "+${GameCatalog.formatCompactPurrs(uiState.effectiveClickPower)}/tap",
          "Current Auto-Purr Rate" to "+${GameCatalog.formatCompactPurrs(uiState.effectivePps)}/sec",
          "Biggest Single Tap" to
            "${GameCatalog.formatCompactPurrs(stats.biggestSingleTapPurrs)} Purrs",
          "Highest Combo Reached" to
            String.format(Locale.US, "%.2fx", stats.highestComboMultiplier),
        ),
    )

    StatsGroupCard(
      title = "Offline, Streaks & Ranks",
      rows =
        listOf(
          "Offline Purrs Earned" to GameCatalog.formatCompactPurrs(stats.totalOfflinePurrsEarned),
          "Total Time Spent Away" to timeAwayText,
          "Current Daily Streak" to "${profile.dailyStreak} Days",
          "Longest Daily Streak" to
            "${maxOf(stats.longestDailyStreak, profile.dailyStreak)} Days",
          "Total Daily Rewards Claimed" to numberFormat.format(stats.totalDailyRewardsClaimed),
          "Weekly Rank Payouts Claimed" to
            "${stats.weeklyRewardsClaimedCount} (${GameCatalog.formatCompactPurrs(stats.totalWeeklyPurrsEarned)} Purrs)",
          "Global Rank" to
            (uiState.currentPlayerGlobalRank?.let { "#$it" } ?: "Local / Unranked"),
        ),
    )

    StatsGroupCard(
      title = "Prestige, Style & Milestones",
      rows =
        listOf(
          "Rebirths Completed" to numberFormat.format(challenge.rebirthCount),
          "Active Rebirth Multiplier" to
            String.format(Locale.US, "%.1fx", uiState.rebirthMultiplier),
          "Gato Ascensións" to numberFormat.format(challenge.ascensionCount),
          "Gato Shards Owned" to numberFormat.format(challenge.gatoShards),
          "Rare Gems Owned" to numberFormat.format(challenge.gems),
          "Lifetime Gems Earned" to numberFormat.format(challenge.totalGemsEarned),
          "Gemstone Purr Charm" to
            "Lv.${challenge.gemCharmLevel} (${String.format(Locale.US, "%.2fx", uiState.gemCharmMultiplier)})",
          "Ascension Tree Nodes" to
            "${challenge.unlockedAscensionIds.size}/${GameCatalog.ascensionUpgrades.size}",
          "Hardcore Mode Status" to
            when {
              challenge.isHardcoreMode -> "Active (3.5x+)"
              uiState.hardcoreEligible -> "Unlocked"
              else -> "Locked"
            },
          "Lifetime Upgrades Bought" to numberFormat.format(stats.totalUpgradesPurchased),
          "Active Upgrades Owned" to numberFormat.format(uiState.activeUpgradeLevels.values.sum()),
          "Cat Coats Unlocked" to
            "${profile.unlockedPatterns.size}/${GameCatalog.patterns.size}",
          "Accessories Unlocked" to
            "${profile.unlockedAccessories.size}/${GameCatalog.accessories.size}",
          "Color Themes Unlocked" to
            "${uiState.settings.unlockedColorThemes.size}/${AppColorTheme.entries.size}",
          "Quests & Badges" to
            "${profile.unlockedBadgeIds.size}/${GameCatalog.badges.size}",
          "Weather Shifts Witnessed" to numberFormat.format(profile.weatherCheckedCount),
          "Event Purrs Earned" to GameCatalog.formatCompactPurrs(profile.eventPurrsEarned),
        ),
    )
  }
}

@Composable
private fun StatsGroupCard(
  title: String,
  rows: List<Pair<String, String>>,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = MaterialTheme.shapes.large,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
      )
      rows.forEach { (label, value) ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}
