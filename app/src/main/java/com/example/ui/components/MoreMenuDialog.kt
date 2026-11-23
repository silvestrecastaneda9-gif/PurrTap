package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GameCatalog
import com.example.ui.theme.AppColorTheme
import com.example.ui.viewmodel.PurrGameUiState

@Composable
fun MoreMenuDialog(
  uiState: PurrGameUiState,
  onOpenInventory: () -> Unit,
  onOpenStyle: () -> Unit,
  onOpenDailyRewards: () -> Unit,
  onOpenWidgets: () -> Unit,
  onOpenPart2Map: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenAccount: () -> Unit,
  onOpenAdminPanel: () -> Unit,
  onOpenMusicPlayer: () -> Unit = {},
  onDismiss: () -> Unit,
) {
  val totalLimitedOwned =
    GameCatalog.patterns.count { it.isEventExclusive && it.id in uiState.profile.unlockedPatterns } +
      GameCatalog.accessories.count { it.isEventExclusive && it.id in uiState.profile.unlockedAccessories } +
      AppColorTheme.entries.count { !it.isFree && it.id in uiState.settings.unlockedColorThemes }

  val accountSubtitle =
    when {
      uiState.isSignedIn -> "Signed in • ${uiState.profile.displayName} (Google Cloud)"
      uiState.settings.accountMode == "local" ->
        "Local ID • ${uiState.settings.localAccountCode.ifBlank { uiState.profile.displayName }}"
      else -> "Guest Caretaker • Sign in or manage save code"
    }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      shadowElevation = 12.dp,
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 20.dp)
          .widthIn(max = 520.dp)
          .heightIn(max = 720.dp)
          .testTag("more_menu_dialog"),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = CircleShape,
              modifier = Modifier.size(42.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Rounded.Apps,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                )
              }
            }
            Column {
              Text(
                text = "Sanctuary Hub",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "Cosmetics, Settings, Rewards & Tools",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp).testTag("close_more_menu_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close Sanctuary Hub")
          }
        }

        Column(
          modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          MoreMenuActionRow(
            icon = Icons.Rounded.MusicNote,
            title = "Music Studio",
            subtitle =
              if (uiState.settings.isMusicPlaying)
                "Playing • Cozy beats & custom audio"
              else "Cozy beats, custom audio & sleep timer",
            badgeText = if (uiState.settings.isMusicPlaying) "ON" else null,
            badgeColor = MaterialTheme.colorScheme.primary,
            badgeTextColor = MaterialTheme.colorScheme.onPrimary,
            highlighted = uiState.settings.isMusicPlaying,
            testTag = "more_music_button",
            onClick = {
              onDismiss()
              onOpenMusicPlayer()
            },
          )

          MoreMenuActionRow(
            icon = Icons.Rounded.Inventory2,
            title = "Inventory",
            subtitle = "Event-sorted limited skins, gear & themes ($totalLimitedOwned owned)",
            badgeText = "$totalLimitedOwned",
            badgeColor = MaterialTheme.colorScheme.secondaryContainer,
            badgeTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
            testTag = "more_inventory_button",
            onClick = {
              onDismiss()
              onOpenInventory()
            },
          )

          MoreMenuActionRow(
            icon = Icons.Rounded.Edit,
            title = "Cat Style",
            subtitle = "Rename ${uiState.profile.selectedCatName}, equip standard coats & gear",
            testTag = "more_style_button",
            onClick = {
              onDismiss()
              onOpenStyle()
            },
          )

          MoreMenuActionRow(
            icon = Icons.Rounded.CardGiftcard,
            title = "Daily Rewards",
            subtitle = "Day ${uiState.profile.dailyStreak + if (uiState.canClaimDailyReward) 1 else 0} streak • ${uiState.nextDailyPayout.streakMultiplier}x bonus",
            badgeText = if (uiState.canClaimDailyReward) "READY" else null,
            badgeColor = MaterialTheme.colorScheme.error,
            badgeTextColor = MaterialTheme.colorScheme.onError,
            highlighted = uiState.canClaimDailyReward,
            testTag = "more_daily_rewards_button",
            onClick = {
              onDismiss()
              onOpenDailyRewards()
            },
          )

          MoreMenuActionRow(
            icon = Icons.Rounded.Widgets,
            title = "Home Widgets",
            subtitle = "Pin live Purrs & mini petting widgets to home screen",
            testTag = "more_widgets_button",
            onClick = {
              onDismiss()
              onOpenWidgets()
            },
          )

          if (uiState.challengeState.part2MapUnlocked) {
            MoreMenuActionRow(
              icon = Icons.Rounded.Map,
              title = "Part 2 Feast Map",
              subtitle = "Gifted by Turkey Gato • Explore live Banquet & Gravy Falls zones",
              badgeText = "LIVE",
              badgeColor = Color(0xFFD84315),
              badgeTextColor = Color.White,
              testTag = "more_part2_map_button",
              onClick = {
                onDismiss()
                onOpenPart2Map()
              },
            )
          }

          MoreMenuActionRow(
            icon = Icons.Rounded.Settings,
            title = "Settings & Music",
            subtitle = "Themes, Nav layout, Music Player, Danger Zone & Code Vault",
            testTag = "more_settings_button",
            onClick = {
              onDismiss()
              onOpenSettings()
            },
          )

          MoreMenuActionRow(
            icon = Icons.Rounded.AccountCircle,
            title = "Account & Sign-In",
            subtitle = accountSubtitle,
            testTag = "more_account_button",
            onClick = {
              onDismiss()
              onOpenAccount()
            },
          )

          if (uiState.isAdmin) {
            MoreMenuActionRow(
              icon = Icons.Rounded.AdminPanelSettings,
              title = "Admin Panel",
              subtitle = "Moderation, weather overrides, currency grants & broadcasts",
              badgeText = "ADMIN",
              badgeColor = MaterialTheme.colorScheme.tertiary,
              badgeTextColor = MaterialTheme.colorScheme.onTertiary,
              highlighted = true,
              testTag = "more_admin_button",
              onClick = {
                onDismiss()
                onOpenAdminPanel()
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MoreMenuActionRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  badgeText: String? = null,
  badgeColor: Color = MaterialTheme.colorScheme.primaryContainer,
  badgeTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
  highlighted: Boolean = false,
  testTag: String,
  onClick: () -> Unit,
) {
  val feedback = LocalPurrFeedback.current
  Card(
    onClick = {
      feedback.tap()
      onClick()
    },
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (highlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
          else MaterialTheme.colorScheme.surfaceVariant,
      ),
    border =
      if (highlighted) BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
      else null,
    shape = MaterialTheme.shapes.large,
    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(testTag),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f).padding(end = 8.dp),
      ) {
        Surface(
          color = MaterialTheme.colorScheme.surface,
          shape = CircleShape,
          modifier = Modifier.size(40.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
          }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (badgeText != null) {
              Surface(color = badgeColor, shape = CircleShape) {
                Text(
                  text = badgeText,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = badgeTextColor,
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                )
              }
            }
          }
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
