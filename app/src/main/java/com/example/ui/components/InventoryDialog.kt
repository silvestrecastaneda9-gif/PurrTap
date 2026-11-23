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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CatAccessoryDef
import com.example.data.model.CatPatternDef
import com.example.data.model.GameCatalog
import com.example.data.model.InventoryEventGroup
import com.example.ui.theme.AppColorTheme
import com.example.ui.viewmodel.PurrGameUiState

@Composable
fun InventoryDialog(
  uiState: PurrGameUiState,
  onSelectPattern: (String) -> Unit,
  onSelectAccessory: (String) -> Unit,
  onApplyColorTheme: (AppColorTheme) -> Unit,
  onOpenPart2Map: () -> Unit = {},
  onDismiss: () -> Unit,
) {
  var selectedFilter by remember { mutableIntStateOf(0) }
  val limitedPatterns = remember { GameCatalog.patterns.filter { it.isEventExclusive } }
  val limitedAccessories = remember { GameCatalog.accessories.filter { it.isEventExclusive } }
  val limitedThemes = remember { AppColorTheme.entries.filter { !it.isFree } }

  val ownedPatternCount = limitedPatterns.count { it.id in uiState.profile.unlockedPatterns }
  val ownedAccessoryCount = limitedAccessories.count { it.id in uiState.profile.unlockedAccessories }
  val ownedThemeCount = limitedThemes.count { it.id in uiState.settings.unlockedColorThemes }
  val totalLimitedItems = limitedPatterns.size + limitedAccessories.size + limitedThemes.size
  val totalOwnedLimited = ownedPatternCount + ownedAccessoryCount + ownedThemeCount

  val filterTabs = listOf("All", "Skins", "Gear", "Themes")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth(0.95f).heightIn(max = 680.dp),
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
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
                  imageVector = Icons.Rounded.Inventory2,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                )
              }
            }
            Column {
              Text(
                text = "Seasonal & Event Inventory",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "$totalOwnedLimited/$totalLimitedItems Limited Skins, Accessories & Themes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_inventory_button"),
          ) {
            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close Inventory")
          }
        }

        // Owned + Shards summary strip
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Skins $ownedPatternCount/${limitedPatterns.size} • Gear $ownedAccessoryCount/${limitedAccessories.size} • Themes $ownedThemeCount/${limitedThemes.size}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.weight(1f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Surface(
                color = Color(0xFFFFE0B2),
                shape = CircleShape,
                modifier = Modifier.testTag("inventory_turkeys_chip"),
              ) {
                Text(
                  text = "${uiState.challengeState.turkeys} 🦃",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF4E2300),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
              ) {
                Text(
                  text = "${uiState.challengeState.gems} Gems",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
              Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = CircleShape,
              ) {
                Text(
                  text = "${uiState.challengeState.gatoShards} Shards",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
            }
          }
        }

        if (uiState.challengeState.part2MapUnlocked) {
          Spacer(modifier = Modifier.height(8.dp))
          Card(
            onClick = onOpenPart2Map,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = BorderStroke(1.dp, Color(0xFFE65100).copy(alpha = 0.5f)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("inventory_part2_map_card"),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Map,
                  contentDescription = null,
                  tint = Color(0xFFBF360C),
                )
                Column {
                  Text(
                    text = "Part 2 Feast Map (LIVE)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF42210B),
                  )
                  Text(
                    text = "Gifted by Turkey Gato • View anytime!",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6D3510),
                  )
                }
              }
              Text(
                text = "View →",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBF360C),
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        SingleChoiceSegmentedButtonRow(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
          filterTabs.forEachIndexed { idx, label ->
            SegmentedButton(
              selected = selectedFilter == idx,
              onClick = { selectedFilter = idx },
              shape = SegmentedButtonDefaults.itemShape(index = idx, count = filterTabs.size),
              modifier = Modifier.testTag("inv_filter_$idx"),
            ) {
              Text(text = label, maxLines = 1)
            }
          }
        }

        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          val orderedGroups = InventoryEventGroup.entries.sortedBy { it.sortOrder }
          orderedGroups.forEach { group ->
            val groupPatternsAll =
              limitedPatterns
                .filter { InventoryEventGroup.fromSourceLabel(it.unlockSourceLabel) == group }
                .sortedByDescending { it.id in uiState.profile.unlockedPatterns }
            val groupAccessoriesAll =
              limitedAccessories
                .filter { InventoryEventGroup.fromSourceLabel(it.unlockSourceLabel) == group }
                .sortedByDescending { it.id in uiState.profile.unlockedAccessories }
            val groupThemesAll =
              limitedThemes
                .filter { InventoryEventGroup.fromSourceLabel(it.unlockSourceLabel) == group }
                .sortedByDescending { it.id in uiState.settings.unlockedColorThemes }

            val visiblePatterns =
              if (selectedFilter == 0 || selectedFilter == 1) groupPatternsAll else emptyList()
            val visibleAccessories =
              if (selectedFilter == 0 || selectedFilter == 2) groupAccessoriesAll else emptyList()
            val visibleThemes =
              if (selectedFilter == 0 || selectedFilter == 3) groupThemesAll else emptyList()

            val totalVisible =
              visiblePatterns.size + visibleAccessories.size + visibleThemes.size

            if (totalVisible > 0) {
              val groupTotal = groupPatternsAll.size + groupAccessoriesAll.size + groupThemesAll.size
              val groupOwned =
                groupPatternsAll.count { it.id in uiState.profile.unlockedPatterns } +
                  groupAccessoriesAll.count { it.id in uiState.profile.unlockedAccessories } +
                  groupThemesAll.count { it.id in uiState.settings.unlockedColorThemes }

              item(key = "inv_group_header_${group.name}") {
                Surface(
                  color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                  border =
                    BorderStroke(
                      1.dp,
                      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    ),
                  shape = MaterialTheme.shapes.medium,
                  modifier = Modifier.fillMaxWidth().testTag("inv_event_header_${group.name}"),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                      ) {
                        Text(
                          text = group.title,
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Bold,
                        )
                        Surface(
                          color = MaterialTheme.colorScheme.tertiaryContainer,
                          shape = CircleShape,
                        ) {
                          Text(
                            text = group.badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                          )
                        }
                      }
                      Text(
                        text = group.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    Text(
                      text = "$groupOwned/$groupTotal",
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }

              items(visiblePatterns, key = { "inv_${group.name}_pat_${it.id}" }) { pattern ->
                InventoryPatternCard(
                  pattern = pattern,
                  selectedAccessoryId = uiState.profile.selectedAccessoryId,
                  existCount = uiState.existCountFor("PATTERN", pattern.id),
                  isOwned = pattern.id in uiState.profile.unlockedPatterns,
                  isEquipped = pattern.id == uiState.profile.selectedPatternId,
                  onEquip = { onSelectPattern(pattern.id) },
                )
              }

              items(visibleAccessories, key = { "inv_${group.name}_acc_${it.id}" }) { accessory ->
                InventoryAccessoryCard(
                  accessory = accessory,
                  selectedPatternId = uiState.profile.selectedPatternId,
                  existCount = uiState.existCountFor("ACCESSORY", accessory.id),
                  isOwned = accessory.id in uiState.profile.unlockedAccessories,
                  isEquipped = accessory.id == uiState.profile.selectedAccessoryId,
                  onEquip = { onSelectAccessory(accessory.id) },
                )
              }

              items(visibleThemes, key = { "inv_${group.name}_thm_${it.id}" }) { theme ->
                InventoryThemeCard(
                  theme = theme,
                  existCount = uiState.existCountFor("THEME", theme.id),
                  isOwned = theme.id in uiState.settings.unlockedColorThemes,
                  isActive = theme == uiState.settings.colorTheme,
                  onApply = { onApplyColorTheme(theme) },
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
private fun InventoryPatternCard(
  pattern: CatPatternDef,
  selectedAccessoryId: String,
  existCount: Long,
  isOwned: Boolean,
  isEquipped: Boolean,
  onEquip: () -> Unit,
) {
  val descText =
    if (!isOwned && pattern.gemCost > 0L)
      "${pattern.description} (${pattern.gemCost} Gems in Shop -> Gems)"
    else pattern.description

  ItemHoverTooltipBox(
    title = pattern.name,
    description = descText,
    limitedTag = pattern.unlockSourceLabel,
    existCount = existCount,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Card(
      colors =
        CardDefaults.cardColors(
          containerColor =
            when {
              isEquipped -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
              isOwned -> MaterialTheme.colorScheme.surfaceVariant
              else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
      border = if (isEquipped) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f).padding(end = 8.dp),
        ) {
          CatAvatarMini(
            patternId = pattern.id,
            accessoryId = selectedAccessoryId,
            modifier = Modifier.size(50.dp),
          )
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = pattern.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
              text = "Hover or hold for details",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Button(
          onClick = onEquip,
          enabled = isOwned || isEquipped,
          modifier = Modifier.height(48.dp).testTag("inv_pattern_${pattern.id}"),
        ) {
          if (isEquipped) {
            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("Active", maxLines = 1)
          } else if (isOwned) {
            Text("Equip", maxLines = 1)
          } else {
            Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("Locked", maxLines = 1)
          }
        }
      }
    }
  }
}

@Composable
private fun InventoryAccessoryCard(
  accessory: CatAccessoryDef,
  selectedPatternId: String,
  existCount: Long,
  isOwned: Boolean,
  isEquipped: Boolean,
  onEquip: () -> Unit,
) {
  val descText =
    if (!isOwned && accessory.gemCost > 0L)
      "${accessory.description} (${accessory.gemCost} Gems in Shop -> Gems)"
    else accessory.description

  ItemHoverTooltipBox(
    title = accessory.name,
    description = descText,
    limitedTag = accessory.unlockSourceLabel,
    existCount = existCount,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Card(
      colors =
        CardDefaults.cardColors(
          containerColor =
            when {
              isEquipped -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
              isOwned -> MaterialTheme.colorScheme.surfaceVariant
              else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
      border = if (isEquipped) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f).padding(end = 8.dp),
        ) {
          CatAvatarMini(
            patternId = selectedPatternId,
            accessoryId = accessory.id,
            modifier = Modifier.size(50.dp),
          )
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = accessory.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
              text = "Hover or hold for details",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Button(
          onClick = onEquip,
          enabled = isOwned || isEquipped,
          modifier = Modifier.height(48.dp).testTag("inv_accessory_${accessory.id}"),
        ) {
          if (isEquipped) {
            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("Active", maxLines = 1)
          } else if (isOwned) {
            Text("Equip", maxLines = 1)
          } else {
            Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("Locked", maxLines = 1)
          }
        }
      }
    }
  }
}

@Composable
private fun InventoryThemeCard(
  theme: AppColorTheme,
  existCount: Long,
  isOwned: Boolean,
  isActive: Boolean,
  onApply: () -> Unit,
) {
  val descText =
    if (isOwned) theme.tagline
    else "Source: ${theme.unlockSourceLabel} • ${theme.requirementSummary}"

  ItemHoverTooltipBox(
    title = theme.displayName,
    description = descText,
    limitedTag = theme.unlockSourceLabel,
    existCount = existCount,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Card(
      colors =
        CardDefaults.cardColors(
          containerColor =
            when {
              isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
              isOwned -> MaterialTheme.colorScheme.surfaceVariant
              else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
      border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f).padding(end = 8.dp),
        ) {
          Box(
            modifier =
              Modifier.size(44.dp)
                .clip(CircleShape)
                .background(theme.previewPrimary),
            contentAlignment = Alignment.Center,
          ) {
            Box(
              modifier =
                Modifier.size(20.dp)
                  .clip(CircleShape)
                  .background(theme.previewSecondary),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp),
              )
            }
          }
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = theme.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
              text = "Hover or hold for details",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Button(
          onClick = onApply,
          enabled = isOwned || isActive,
          modifier = Modifier.height(48.dp).testTag("inv_theme_${theme.id}"),
        ) {
          if (isActive) {
            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("Active", maxLines = 1)
          } else if (isOwned) {
            Text("Apply", maxLines = 1)
          } else {
            Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("Locked", maxLines = 1)
          }
        }
      }
    }
  }
}
