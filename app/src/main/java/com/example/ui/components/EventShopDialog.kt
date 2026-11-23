package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ActiveFestivalQuest
import com.example.data.model.FestivalTier
import com.example.data.model.GameCatalog
import com.example.ui.theme.AppColorTheme
import com.example.ui.viewmodel.PurrGameUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventShopDialog(
  uiState: PurrGameUiState,
  onTalkToTurkeyGato: () -> Unit,
  onOpenPart2Map: () -> Unit,
  onUnlockTurkeyPattern: (String) -> Unit,
  onUnlockTurkeyAccessory: (String) -> Unit,
  onUnlockTurkeyTheme: (AppColorTheme) -> Unit,
  onUnlockTurkeyBackground: (String) -> Unit = {},
  onClaimFestivalQuest: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Skins", "Gear", "Themes", "Quests")
  val challenge = uiState.challengeState
  val isMega = uiState.festivalTier == FestivalTier.MEGA
  val talkCount = challenge.turkeyGatoTalkCount
  val talkDialogue =
    GameCatalog.turkeyGatoDialogues[
      talkCount.coerceIn(0, GameCatalog.turkeyGatoDialogues.lastIndex)
    ]

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      shadowElevation = 12.dp,
      border =
        BorderStroke(
          1.5.dp,
          if (isMega) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        ),
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 18.dp)
          .widthIn(max = 560.dp)
          .heightIn(max = 740.dp)
          .testTag("event_shop_dialog"),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Header: Thanksgiving Event Part One + Tier + Turkeys Balance
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Surface(
                color = if (isMega) Color(0xFFD84315) else MaterialTheme.colorScheme.tertiary,
                shape = CircleShape,
                modifier = Modifier.testTag("festival_tier_badge"),
              ) {
                Text(
                  text = if (isMega) "33% GRAVY RUSH!" else "PART 2 REGULAR",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
              Surface(
                color = Color(0xFFFFE0B2),
                shape = CircleShape,
                modifier = Modifier.testTag("event_shop_turkeys_pill"),
              ) {
                Text(
                  text = "🦃 ${challenge.turkeys} Turkeys",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF4E2300),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
            Text(
              text = uiState.festivalTier.label,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text =
                "Part Two Live • Reroll in ${uiState.festivalRerollCountdownText} • +3 Golden Gobblers",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp).testTag("close_event_shop_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close Festival Shop")
          }
        }

        // Category Switcher (4 Tabs: Skins, Gear, Themes, Quests)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          tabs.forEachIndexed { index, label ->
            val badgeCount =
              if (index == 3) uiState.hourlyFestivalQuests.count { !it.isClaimed && it.isCompleted }
              else 0
            SegmentedButton(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
              modifier = Modifier.testTag("event_shop_tab_$index"),
            ) {
              Text(
                text = if (badgeCount > 0) "$label ($badgeCount)" else label,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Scrollable Body: Festival Info, Turkey Gato NPC, Part 2 Sneak Peek, and Tab Inventory
        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(min = 240.dp),
          contentPadding = PaddingValues(vertical = 4.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          // Hourly Festival Status Card
          item(key = "fest_status_banner") {
            Card(
              colors =
                CardDefaults.cardColors(
                  containerColor =
                    if (isMega) Color(0xFFFFECB3)
                    else MaterialTheme.colorScheme.secondaryContainer,
                ),
              border = if (isMega) BorderStroke(1.5.dp, Color(0xFFE65100)) else null,
              shape = MaterialTheme.shapes.large,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.AutoAwesome,
                      contentDescription = null,
                      tint = if (isMega) Color(0xFFBF360C) else MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp),
                    )
                    Text(
                      text = uiState.festivalTier.boostLabel,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = if (isMega) Color(0xFF4E2300) else MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                  }
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Schedule,
                      contentDescription = null,
                      tint = if (isMega) Color(0xFFBF360C) else MaterialTheme.colorScheme.secondary,
                      modifier = Modifier.size(14.dp),
                    )
                    Text(
                      text = "Reroll: ${uiState.festivalRerollCountdownText}",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = if (isMega) Color(0xFFBF360C) else MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                  }
                }
                Surface(
                  color =
                    if (uiState.isTurkeyRushActive) Color(0xFF2E7D32)
                    else Color(0xFF5D4037),
                  shape = CircleShape,
                  modifier = Modifier.testTag("turkey_rush_status_pill"),
                ) {
                  Text(
                    text = uiState.turkeyRushCountdownText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  )
                }
                Text(
                  text =
                    "Hourly Roll: 67% Regular (1.75x • 25% Turkey) • 33% Gravy Rush (3.0x • 45% Turkey) • +3 Golden Gobblers! Turkeys spawn during the first 5 min each hour.",
                  style = MaterialTheme.typography.labelSmall,
                  color =
                    if (isMega) Color(0xFF5D2E00)
                    else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                )
              }
            }
          }

          // Interactive Turkey Gato NPC Card
          item(key = "turkey_gato_npc_card") {
            Card(
              colors =
                CardDefaults.cardColors(
                  containerColor = Color(0xFFFFF3E0),
                ),
              border = BorderStroke(1.5.dp, Color(0xFFE65100).copy(alpha = 0.55f)),
              shape = MaterialTheme.shapes.large,
              modifier = Modifier.fillMaxWidth().testTag("turkey_gato_card"),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  TurkeyGatoAvatar(modifier = Modifier.size(62.dp))
                  Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Text(
                        text = "Turkey Gato",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF42210B),
                      )
                      Surface(
                        color = if (challenge.part2MapUnlocked) Color(0xFF2E7D32) else Color(0xFFE65100),
                        shape = CircleShape,
                      ) {
                        Text(
                          text =
                            if (challenge.part2MapUnlocked) "MAP GIFTED"
                            else "${talkCount.coerceAtMost(GameCatalog.TURKEY_GATO_MAP_TALKS)}/${GameCatalog.TURKEY_GATO_MAP_TALKS} Chats",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = Color.White,
                          modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                        )
                      }
                    }
                    Text(
                      text = "\"$talkDialogue\"",
                      style = MaterialTheme.typography.bodySmall,
                      color = Color(0xFF5D2906),
                    )
                  }
                }

                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  FilledTonalButton(
                    onClick = onTalkToTurkeyGato,
                    modifier =
                      Modifier.weight(1f).widthIn(min = 150.dp).height(48.dp).testTag("talk_turkey_gato_button"),
                  ) {
                    Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                      text =
                        if (challenge.part2MapUnlocked) "Chat with Turkey Gato"
                        else "Talk (${talkCount.coerceAtMost(GameCatalog.TURKEY_GATO_MAP_TALKS)}/${GameCatalog.TURKEY_GATO_MAP_TALKS})",
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }

                  if (challenge.part2MapUnlocked) {
                    Button(
                      onClick = onOpenPart2Map,
                      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF360C)),
                      modifier = Modifier.height(48.dp).testTag("view_part2_map_button"),
                    ) {
                      Icon(Icons.Rounded.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.size(5.dp))
                      Text("View Part 2 Map", maxLines = 1)
                    }
                  }
                }
              }
            }
          }

          // Live Announcement Card for Thanksgiving Event Part Two
          item(key = "part2_sneak_peek_card") {
            val peek = GameCatalog.thanksgivingSneakPeek
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.fillMaxWidth().testTag("thanksgiving_sneak_peek_card"),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = peek.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                  )
                  Surface(
                    color = Color(0xFF2E7D32),
                    shape = CircleShape,
                  ) {
                    Text(
                      text = "PART 2 LIVE!",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                    )
                  }
                }
                Text(
                  text = "${peek.dateWindow} • ${peek.previewHighlights.joinToString(" • ")}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.92f),
                )
              }
            }
          }

          // Active Tab Items (All Exclusively Purchased with Turkeys!)
          when (selectedTab) {
            0 -> {
              items(GameCatalog.turkeyEventPatterns, key = { "tk_pat_${it.id}" }) { pattern ->
                val isUnlocked = pattern.id in uiState.profile.unlockedPatterns
                val isEquipped = pattern.id == uiState.profile.selectedPatternId
                val canAfford = challenge.turkeys >= pattern.turkeyCost
                EventShopItemCard(
                  title = pattern.name,
                  description = pattern.description,
                  limitedTag = pattern.unlockSourceLabel ?: "Limited Thanksgiving",
                  existCount = uiState.existCountFor("PATTERN", pattern.id),
                  costLabel = "${pattern.turkeyCost} 🦃",
                  isUnlocked = isUnlocked,
                  isEquipped = isEquipped,
                  canAfford = canAfford,
                  testTag = "event_pattern_btn_${pattern.id}",
                  leading = {
                    CatAvatarMini(
                      patternId = pattern.id,
                      accessoryId = uiState.profile.selectedAccessoryId,
                      modifier = Modifier.size(48.dp),
                    )
                  },
                  onClick = { onUnlockTurkeyPattern(pattern.id) },
                )
              }
            }

            1 -> {
              items(GameCatalog.turkeyEventAccessories, key = { "tk_acc_${it.id}" }) { acc ->
                val isUnlocked = acc.id in uiState.profile.unlockedAccessories
                val isEquipped = acc.id == uiState.profile.selectedAccessoryId
                val canAfford = challenge.turkeys >= acc.turkeyCost
                EventShopItemCard(
                  title = acc.name,
                  description = acc.description,
                  limitedTag = acc.unlockSourceLabel ?: "Limited Thanksgiving",
                  existCount = uiState.existCountFor("ACCESSORY", acc.id),
                  costLabel = "${acc.turkeyCost} 🦃",
                  isUnlocked = isUnlocked,
                  isEquipped = isEquipped,
                  canAfford = canAfford,
                  testTag = "event_accessory_btn_${acc.id}",
                  leading = {
                    CatAvatarMini(
                      patternId = uiState.profile.selectedPatternId,
                      accessoryId = acc.id,
                      modifier = Modifier.size(48.dp),
                    )
                  },
                  onClick = { onUnlockTurkeyAccessory(acc.id) },
                )
              }
            }

            2 -> {
              items(AppColorTheme.turkeyEventThemes, key = { "tk_theme_${it.id}" }) { theme ->
                val isUnlocked = theme.id in uiState.settings.unlockedColorThemes
                val isEquipped = uiState.settings.colorTheme == theme
                val canAfford = challenge.turkeys >= theme.turkeyCost
                EventShopItemCard(
                  title = theme.displayName,
                  description = theme.tagline,
                  limitedTag = theme.unlockSourceLabel,
                  existCount = uiState.existCountFor("THEME", theme.id),
                  costLabel = "${theme.turkeyCost} 🦃",
                  isUnlocked = isUnlocked,
                  isEquipped = isEquipped,
                  canAfford = canAfford,
                  testTag = "event_theme_btn_${theme.id}",
                  leading = {
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                      Box(
                        modifier = Modifier.size(30.dp).clip(CircleShape).background(theme.previewPrimary)
                      )
                      Box(
                        modifier = Modifier.size(30.dp).clip(CircleShape).background(theme.previewSecondary)
                      )
                    }
                  },
                  onClick = { onUnlockTurkeyTheme(theme) },
                )
              }

              item {
                Text(
                  text = "Thanksgiving Room Backgrounds (Part 1 & 2)",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
              val turkeyBackgrounds = GameCatalog.backgrounds.filter { it.turkeyCost > 0L }
              items(turkeyBackgrounds, key = { "tk_bg_${it.id}" }) { bg ->
                val unlocked = bg.id in uiState.profile.unlockedBackgrounds
                val equipped = uiState.profile.selectedBackgroundId == bg.id
                EventShopItemCard(
                  title = bg.name,
                  description = bg.description,
                  limitedTag = bg.eventGroup ?: "Limited Thanksgiving",
                  existCount = uiState.existCountFor("BACKGROUND", bg.id),
                  costLabel = "${bg.turkeyCost} 🦃",
                  isUnlocked = unlocked,
                  isEquipped = equipped,
                  canAfford = challenge.turkeys >= bg.turkeyCost,
                  testTag = "turkey_bg_${bg.id}",
                  leading = {
                    CatBackgroundPreviewSwatch(
                      def = bg,
                      modifier = Modifier.size(48.dp),
                    )
                  },
                  onClick = { onUnlockTurkeyBackground(bg.id) },
                )
              }
            }

            else -> {
              items(uiState.hourlyFestivalQuests, key = { "tk_quest_${it.template.id}" }) { quest ->
                FestivalQuestCard(
                  quest = quest,
                  onClaim = { onClaimFestivalQuest(quest.template.id) },
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun FestivalQuestCard(
  quest: ActiveFestivalQuest,
  onClaim: () -> Unit,
) {
  val pct = (quest.progress.toFloat() / quest.template.target.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
  Card(
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (quest.isClaimed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
          else MaterialTheme.colorScheme.surfaceVariant,
      ),
    border =
      if (!quest.isClaimed && quest.isCompleted) {
        BorderStroke(1.5.dp, Color(0xFFE65100))
      } else null,
    modifier = Modifier.fillMaxWidth().testTag("fest_quest_card_${quest.template.id}"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
          Text(
            text = quest.template.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "${quest.template.description} (${quest.progress.coerceAtMost(quest.template.target)}/${quest.template.target})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Button(
          onClick = onClaim,
          enabled = !quest.isClaimed && quest.isCompleted,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
          modifier = Modifier.height(48.dp).testTag("claim_fest_quest_${quest.template.id}"),
        ) {
          Text(
            text =
              when {
                quest.isClaimed -> "Claimed"
                quest.isCompleted -> "+${quest.template.turkeyReward} 🦃"
                else -> "+${quest.template.turkeyReward} 🦃"
              },
            maxLines = 1,
          )
        }
      }
      LinearProgressIndicator(
        progress = { pct },
        modifier = Modifier.fillMaxWidth().height(6.dp),
        strokeCap = StrokeCap.Round,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventShopItemCard(
  title: String,
  description: String,
  limitedTag: String = "Limited Thanksgiving",
  existCount: Long,
  costLabel: String,
  isUnlocked: Boolean,
  isEquipped: Boolean,
  canAfford: Boolean,
  testTag: String,
  leading: @Composable () -> Unit,
  onClick: () -> Unit,
) {
  ItemHoverTooltipBox(
    title = title,
    description = description,
    limitedTag = limitedTag,
    existCount = existCount,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Card(
      colors =
        CardDefaults.cardColors(
          containerColor =
            if (isEquipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
      border = if (isEquipped) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.weight(1f).padding(end = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          leading()
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
              text = "Hover or hold for details",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

      Button(
        onClick = onClick,
        enabled = isEquipped || isUnlocked || canAfford,
        colors =
          if (!isEquipped && !isUnlocked) {
            ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315))
          } else {
            ButtonDefaults.buttonColors()
          },
        modifier = Modifier.height(48.dp).testTag(testTag),
      ) {
        if (isEquipped) {
          Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.size(4.dp))
          Text("Active")
        } else if (isUnlocked) {
          Text("Equip")
        } else {
          Text(costLabel, maxLines = 1)
        }
      }
    }
  }
}
}
