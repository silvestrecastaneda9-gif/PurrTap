package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GameCatalog
import com.example.data.model.LeaderboardEntry
import com.example.data.weather.WeatherConditionKind
import com.example.ui.viewmodel.PurrGameUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPanelDialog(
  uiState: PurrGameUiState,
  leaderboardEntries: List<LeaderboardEntry>,
  initialTargetUid: String = "",
  onModeratePlayer: (targetUserId: String, status: String, reason: String, timeoutMinutes: Long) -> Unit,
  onClearModeration: (targetUserId: String) -> Unit,
  onGrantSelfCurrency: (purrs: Long, hardcorePurrs: Long, gems: Long, shards: Long, passXp: Long) -> Unit,
  onSetWeather: (WeatherConditionKind) -> Unit,
  onSendGlobalMessage: (title: String, body: String, severity: String) -> Unit,
  onDeleteGlobalMessage: (messageId: String) -> Unit,
  onDismiss: () -> Unit,
) {
  if (!uiState.isAdmin) return

  var selectedTab by remember { mutableIntStateOf(0) }
  var targetUid by remember(initialTargetUid) { mutableStateOf(initialTargetUid) }
  var moderationReason by remember { mutableStateOf("Violation of Sanctuary rules") }
  var timeoutMinutes by remember { mutableLongStateOf(15L) }
  var announcementTitle by remember { mutableStateOf("Sanctuary Announcement") }
  var announcementBody by remember { mutableStateOf("") }
  var announcementSeverity by remember { mutableStateOf("EVENT") }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth(0.95f).heightIn(max = 680.dp).testTag("admin_panel_dialog"),
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        // Header
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
              Icons.Rounded.AdminPanelSettings,
              contentDescription = "Admin Panel",
              tint = MaterialTheme.colorScheme.primary,
            )
            Column {
              Text(
                text = "Sanctuary Admin Panel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
              )
              Text(
                text = "Verified: ${GameCatalog.ADMIN_EMAIL}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp).testTag("admin_panel_close_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close Admin Panel")
          }
        }

        Spacer(Modifier.height(8.dp))

        TabRow(selectedTabIndex = selectedTab) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Players", maxLines = 1) },
            icon = { Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(18.dp)) },
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Currency", maxLines = 1) },
            icon = { Icon(Icons.Rounded.Paid, contentDescription = null, modifier = Modifier.size(18.dp)) },
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = { Text("Weather", maxLines = 1) },
            icon = { Icon(Icons.Rounded.WbSunny, contentDescription = null, modifier = Modifier.size(18.dp)) },
          )
          Tab(
            selected = selectedTab == 3,
            onClick = { selectedTab = 3 },
            text = { Text("Global", maxLines = 1) },
            icon = { Icon(Icons.Rounded.Campaign, contentDescription = null, modifier = Modifier.size(18.dp)) },
          )
        }

        Spacer(Modifier.height(12.dp))

        Column(
          modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          when (selectedTab) {
            0 -> {
              Text(
                text = "Select Leaderboard Player or Enter UID",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
              if (leaderboardEntries.isNotEmpty()) {
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  leaderboardEntries.take(15).forEach { player ->
                    FilterChip(
                      selected = targetUid == player.userId,
                      onClick = { targetUid = player.userId },
                      label = { Text(player.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                  }
                }
              }

              OutlinedTextField(
                value = targetUid,
                onValueChange = { targetUid = it },
                label = { Text("Target Player UID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_target_uid_input"),
              )
              OutlinedTextField(
                value = moderationReason,
                onValueChange = { if (it.length <= 240) moderationReason = it },
                label = { Text("Warning / Timeout / Ban Reason") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth().testTag("admin_reason_input"),
              )

              Text(
                text = "Timeout Duration",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(5L to "5m", 15L to "15m", 60L to "1h", 1440L to "24h").forEach { (mins, label) ->
                  FilterChip(
                    selected = timeoutMinutes == mins,
                    onClick = { timeoutMinutes = mins },
                    label = { Text(label) },
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                OutlinedButton(
                  onClick = {
                    if (targetUid.isNotBlank()) {
                      onModeratePlayer(targetUid.trim(), "WARNED", moderationReason, 0L)
                    }
                  },
                  enabled = targetUid.isNotBlank(),
                  modifier = Modifier.weight(1f).height(48.dp).testTag("admin_warn_button"),
                ) {
                  Icon(Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Warn")
                }
                Button(
                  onClick = {
                    if (targetUid.isNotBlank()) {
                      onModeratePlayer(targetUid.trim(), "TIMED_OUT", moderationReason, timeoutMinutes)
                    }
                  },
                  enabled = targetUid.isNotBlank(),
                  modifier = Modifier.weight(1f).height(48.dp).testTag("admin_timeout_button"),
                ) {
                  Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Timeout")
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Button(
                  onClick = {
                    if (targetUid.isNotBlank()) {
                      onModeratePlayer(targetUid.trim(), "BANNED", moderationReason, 0L)
                    }
                  },
                  enabled = targetUid.isNotBlank(),
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.error,
                      contentColor = MaterialTheme.colorScheme.onError,
                    ),
                  modifier = Modifier.weight(1f).height(48.dp).testTag("admin_ban_button"),
                ) {
                  Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Ban")
                }
                OutlinedButton(
                  onClick = {
                    if (targetUid.isNotBlank()) {
                      onClearModeration(targetUid.trim())
                    }
                  },
                  enabled = targetUid.isNotBlank(),
                  modifier = Modifier.weight(1f).height(48.dp).testTag("admin_unban_button"),
                ) {
                  Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Unban / Clear")
                }
              }

              if (uiState.adminModerationList.isNotEmpty()) {
                Text(
                  text = "Active Moderation Records (${uiState.adminModerationList.size})",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                )
                uiState.adminModerationList.forEach { rec ->
                  Card(
                    colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(14.dp),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth().padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = "${rec.status} • ${rec.userId.take(14)}...",
                          style = MaterialTheme.typography.labelLarge,
                          fontWeight = FontWeight.Bold,
                          color =
                            if (rec.status == "BANNED") MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                          text = "Warnings: ${rec.warningCount} • ${rec.reason}",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          maxLines = 2,
                        )
                      }
                      TextButton(onClick = { onClearModeration(rec.userId) }) {
                        Text("Clear")
                      }
                    }
                  }
                }
              }
            }

            1 -> {
              Text(
                text = "Grant Admin Sanctuary Currencies",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text =
                  "Balance: ${GameCatalog.formatCompactPurrs(uiState.profile.purrs)} Purrs • " +
                    "${GameCatalog.formatCompactPurrs(uiState.challengeState.hardcorePurrs)} HC • " +
                    "${uiState.challengeState.gems} Gems • ${uiState.challengeState.gatoShards} Shards",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Button(onClick = { onGrantSelfCurrency(100_000L, 0L, 0L, 0L, 0L) }) {
                  Text("+100K Purrs")
                }
                Button(onClick = { onGrantSelfCurrency(10_000_000L, 0L, 0L, 0L, 0L) }) {
                  Text("+10M Purrs")
                }
                Button(onClick = { onGrantSelfCurrency(1_000_000_000L, 0L, 0L, 0L, 0L) }) {
                  Text("+1B Purrs")
                }
                Button(onClick = { onGrantSelfCurrency(0L, 1_000_000L, 0L, 0L, 0L) }) {
                  Text("+1M HC Purrs")
                }
                Button(onClick = { onGrantSelfCurrency(0L, 0L, 25L, 0L, 0L) }) {
                  Text("+25 Rare Gems")
                }
                Button(onClick = { onGrantSelfCurrency(0L, 0L, 250L, 0L, 0L) }) {
                  Text("+250 Gems")
                }
                Button(onClick = { onGrantSelfCurrency(0L, 0L, 0L, 5L, 0L) }) {
                  Text("+5 Gato Shards")
                }
                Button(onClick = { onGrantSelfCurrency(0L, 0L, 0L, 0L, 2_500L) }) {
                  Text("+2,500 Pass XP")
                }
              }
            }

            2 -> {
              Text(
                text = "Override Sanctuary Weather Immediately",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "Active: ${uiState.weather.condition.label} (${uiState.weather.condition.buffTitle})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
              )
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                WeatherConditionKind.entries.forEach { cond ->
                  FilterChip(
                    selected = uiState.weather.condition == cond,
                    onClick = { onSetWeather(cond) },
                    label = { Text("${cond.label} (${cond.clickMultiplier}x)") },
                    modifier = Modifier.testTag("admin_weather_${cond.name.lowercase()}"),
                  )
                }
              }
            }

            3 -> {
              Text(
                text = "Broadcast Global Sanctuary Message",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
              OutlinedTextField(
                value = announcementTitle,
                onValueChange = { if (it.length <= 80) announcementTitle = it },
                label = { Text("Announcement Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_global_title_input"),
              )
              OutlinedTextField(
                value = announcementBody,
                onValueChange = { if (it.length <= 320) announcementBody = it },
                label = { Text("Global Message Body") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("admin_global_body_input"),
              )
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("INFO", "EVENT", "WARNING").forEach { sev ->
                  FilterChip(
                    selected = announcementSeverity == sev,
                    onClick = { announcementSeverity = sev },
                    label = { Text(sev) },
                  )
                }
              }
              Button(
                onClick = {
                  if (announcementBody.isNotBlank()) {
                    onSendGlobalMessage(announcementTitle, announcementBody, announcementSeverity)
                    announcementBody = ""
                  }
                },
                enabled = announcementBody.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_send_global_button"),
              ) {
                Icon(Icons.Rounded.Campaign, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Send Global Message")
              }

              if (uiState.globalAnnouncements.isNotEmpty()) {
                Text(
                  text = "Recent Global Broadcasts",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                )
                uiState.globalAnnouncements.forEach { msg ->
                  Card(
                    colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(14.dp),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth().padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = "[${msg.severity}] ${msg.title}",
                          style = MaterialTheme.typography.labelLarge,
                          fontWeight = FontWeight.Bold,
                        )
                        Text(
                          text = msg.body,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                      IconButton(onClick = { onDeleteGlobalMessage(msg.messageId) }) {
                        Icon(
                          Icons.Rounded.Delete,
                          contentDescription = "Delete Global Message",
                          tint = MaterialTheme.colorScheme.error,
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
