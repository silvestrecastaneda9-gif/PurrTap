package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GameCatalog
import com.example.data.model.MoonlightPhase
import com.example.ui.theme.AppColorTheme
import com.example.ui.viewmodel.PurrGameUiState

@Composable
fun EventShopDialog(
  uiState: PurrGameUiState,
  onSelectOrUnlockPattern: (String) -> Unit,
  onSelectOrUnlockAccessory: (String) -> Unit,
  onSelectOrUnlockColorTheme: (AppColorTheme) -> Unit,
  onDismiss: () -> Unit,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Cat Skins", "Accessories", "Themes")
  val eventProgress =
    (uiState.profile.eventPurrsEarned.toFloat() / uiState.activeEvent.milestoneGoal.coerceAtLeast(1L))
      .coerceIn(0f, 1f)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      shadowElevation = 12.dp,
      border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 24.dp)
          .widthIn(max = 540.dp)
          .testTag("event_shop_dialog"),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Bubble Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape,
              ) {
                Text(
                  text = "EVENT BOUTIQUE",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onTertiary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
              Text(
                text =
                  if (uiState.activeEvent.id == "starlight_festival") {
                    "Ends Nov 18 • ${uiState.eventCountdownText}"
                  } else {
                    uiState.eventCountdownText
                  },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }
            Text(
              text = uiState.activeEvent.title,
              style = MaterialTheme.typography.titleLarge,
            )
            Text(
              text = uiState.activeEvent.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_event_shop_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close Event Boutique")
          }
        }

        // Balance + Moonlight + Event Milestone Bubble Card
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth(),
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
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.AutoAwesome,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text =
                    "${uiState.activeCurrencyShortLabel}: ${GameCatalog.formatCompactPurrs(uiState.activeCurrencyBalance)}",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                )
              }
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.NightsStay,
                  contentDescription = null,
                  tint =
                    if (uiState.moonlightState.phase == MoonlightPhase.BLOODMOON)
                      MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text =
                    when (uiState.moonlightState.phase) {
                      MoonlightPhase.BLOODMOON -> "Bloodmoon 2.5x!"
                      MoonlightPhase.MOONLIGHT -> "Moonlight 1.75x!"
                      MoonlightPhase.INACTIVE -> uiState.activeEvent.boostLabel
                    },
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
            }
            LinearProgressIndicator(
              progress = { eventProgress },
              modifier = Modifier.fillMaxWidth().height(7.dp),
              strokeCap = StrokeCap.Round,
            )
            Text(
              text =
                "Event Milestone: ${GameCatalog.formatCompactPurrs(uiState.profile.eventPurrsEarned)} / ${GameCatalog.formatCompactPurrs(uiState.activeEvent.milestoneGoal)} Purrs (Festival Spirit Badge)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
            )
          }
        }

        // Brief Sneak Peek of the upcoming Thanksgiving Event
        if (uiState.activeEvent.id == "starlight_festival") {
          val peek = GameCatalog.thanksgivingSneakPeek
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().testTag("thanksgiving_sneak_peek_card"),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "NEXT UP: ${peek.title}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Surface(
                  color = MaterialTheme.colorScheme.tertiary,
                  shape = CircleShape,
                ) {
                  Text(
                    text = "SNEAK PEEK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                  )
                }
              }
              Text(
                text = "${peek.dateWindow} • ${peek.previewHighlights.joinToString(" • ")}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
              )
            }
          }
        }

        // Category Switcher
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          tabs.forEachIndexed { index, label ->
            SegmentedButton(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
              modifier = Modifier.testTag("event_shop_tab_$index"),
            ) {
              Text(text = label, maxLines = 1)
            }
          }
        }

        // Boutique Inventory List
        LazyColumn(
          modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 270.dp),
          contentPadding = PaddingValues(vertical = 2.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          when (selectedTab) {
            0 -> {
              items(GameCatalog.eventExclusivePatterns, key = { "ev_pat_${it.id}" }) { pattern ->
                val isUnlocked = pattern.id in uiState.profile.unlockedPatterns
                val isEquipped = pattern.id == uiState.profile.selectedPatternId
                val effectiveCost =
                  GameCatalog.effectiveItemCost(
                    pattern.cost,
                    uiState.challengeState.isHardcoreMode,
                    uiState.challengeState.unlockedAscensionIds,
                  )
                val canAfford = uiState.activeCurrencyBalance >= effectiveCost
                EventShopItemCard(
                  title = pattern.name,
                  description = pattern.description,
                  requirementLine = null,
                  requirementMet = true,
                  costLabel = GameCatalog.formatCompactPurrs(effectiveCost),
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
                  onClick = { onSelectOrUnlockPattern(pattern.id) },
                )
              }
            }
            1 -> {
              items(GameCatalog.eventExclusiveAccessories, key = { "ev_acc_${it.id}" }) { acc ->
                val isUnlocked = acc.id in uiState.profile.unlockedAccessories
                val isEquipped = acc.id == uiState.profile.selectedAccessoryId
                val effectiveCost =
                  GameCatalog.effectiveItemCost(
                    acc.cost,
                    uiState.challengeState.isHardcoreMode,
                    uiState.challengeState.unlockedAscensionIds,
                  )
                val canAfford = uiState.activeCurrencyBalance >= effectiveCost
                EventShopItemCard(
                  title = acc.name,
                  description = acc.description,
                  requirementLine = null,
                  requirementMet = true,
                  costLabel = GameCatalog.formatCompactPurrs(effectiveCost),
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
                  onClick = { onSelectOrUnlockAccessory(acc.id) },
                )
              }
            }
            else -> {
              items(AppColorTheme.eventThemes, key = { "ev_theme_${it.id}" }) { theme ->
                val isUnlocked = theme.id in uiState.settings.unlockedColorThemes
                val isEquipped = uiState.settings.colorTheme == theme
                val (reqMet, failReason) = GameCatalog.evaluateThemeRequirement(theme, uiState.profile)
                val effectiveCost =
                  GameCatalog.effectiveItemCost(
                    theme.eventCost,
                    uiState.challengeState.isHardcoreMode,
                    uiState.challengeState.unlockedAscensionIds,
                  )
                val canBuy = reqMet && uiState.activeCurrencyBalance >= effectiveCost
                val reqDisplay =
                  if (isUnlocked) "Requirement Met"
                  else if (reqMet) "Req Met: ${theme.requirementSummary}"
                  else "Locked: ${failReason ?: theme.requirementSummary}"
                EventShopItemCard(
                  title = theme.displayName,
                  description = theme.tagline,
                  requirementLine = reqDisplay,
                  requirementMet = reqMet || isUnlocked,
                  costLabel = GameCatalog.formatCompactPurrs(effectiveCost),
                  isUnlocked = isUnlocked,
                  isEquipped = isEquipped,
                  canAfford = canBuy,
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
                  onClick = { onSelectOrUnlockColorTheme(theme) },
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
private fun EventShopItemCard(
  title: String,
  description: String,
  requirementLine: String?,
  requirementMet: Boolean,
  costLabel: String,
  isUnlocked: Boolean,
  isEquipped: Boolean,
  canAfford: Boolean,
  testTag: String,
  leading: @Composable () -> Unit,
  onClick: () -> Unit,
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
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          if (requirementLine != null) {
            Text(
              text = requirementLine,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color =
                if (requirementMet) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.error,
            )
          }
        }
      }

      Button(
        onClick = onClick,
        enabled = isEquipped || isUnlocked || (requirementMet && canAfford),
        modifier = Modifier.height(48.dp).testTag(testTag),
      ) {
        if (isEquipped) {
          Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.size(4.dp))
          Text("Active")
        } else if (isUnlocked) {
          Text("Equip")
        } else if (!requirementMet) {
          Text("Locked", maxLines = 1)
        } else {
          Text(costLabel, maxLines = 1)
        }
      }
    }
  }
}
