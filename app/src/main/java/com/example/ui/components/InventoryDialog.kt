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
import com.example.ui.theme.AppColorTheme
import com.example.ui.viewmodel.PurrGameUiState

@Composable
fun InventoryDialog(
  uiState: PurrGameUiState,
  onSelectPattern: (String) -> Unit,
  onSelectAccessory: (String) -> Unit,
  onApplyColorTheme: (AppColorTheme) -> Unit,
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
          if (selectedFilter == 0 || selectedFilter == 1) {
            item {
              Text(
                text = "Limited & Pass Coat Skins",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
            }
            val sortedPatterns =
              limitedPatterns.sortedByDescending { it.id in uiState.profile.unlockedPatterns }
            items(sortedPatterns, key = { "inv_pat_${it.id}" }) { pattern ->
              InventoryPatternCard(
                pattern = pattern,
                selectedAccessoryId = uiState.profile.selectedAccessoryId,
                isOwned = pattern.id in uiState.profile.unlockedPatterns,
                isEquipped = pattern.id == uiState.profile.selectedPatternId,
                onEquip = { onSelectPattern(pattern.id) },
              )
            }
          }

          if (selectedFilter == 0 || selectedFilter == 2) {
            item {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Limited & Pass Accessories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
            }
            val sortedAccessories =
              limitedAccessories.sortedByDescending { it.id in uiState.profile.unlockedAccessories }
            items(sortedAccessories, key = { "inv_acc_${it.id}" }) { accessory ->
              InventoryAccessoryCard(
                accessory = accessory,
                selectedPatternId = uiState.profile.selectedPatternId,
                isOwned = accessory.id in uiState.profile.unlockedAccessories,
                isEquipped = accessory.id == uiState.profile.selectedAccessoryId,
                onEquip = { onSelectAccessory(accessory.id) },
              )
            }
          }

          if (selectedFilter == 0 || selectedFilter == 3) {
            item {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Limited & Prize Color Themes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
            }
            val sortedThemes =
              limitedThemes.sortedByDescending { it.id in uiState.settings.unlockedColorThemes }
            items(sortedThemes, key = { "inv_thm_${it.id}" }) { theme ->
              InventoryThemeCard(
                theme = theme,
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

@Composable
private fun InventoryPatternCard(
  pattern: CatPatternDef,
  selectedAccessoryId: String,
  isOwned: Boolean,
  isEquipped: Boolean,
  onEquip: () -> Unit,
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(text = pattern.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            pattern.unlockSourceLabel?.let { source ->
              Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = CircleShape,
              ) {
                Text(
                  text = source,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                )
              }
            }
          }
          Text(
            text =
              if (!isOwned && pattern.gemCost > 0L)
                "${pattern.description} (${pattern.gemCost} Gems in Shop -> Gems)"
              else pattern.description,
            style = MaterialTheme.typography.bodySmall,
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

@Composable
private fun InventoryAccessoryCard(
  accessory: CatAccessoryDef,
  selectedPatternId: String,
  isOwned: Boolean,
  isEquipped: Boolean,
  onEquip: () -> Unit,
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(text = accessory.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            accessory.unlockSourceLabel?.let { source ->
              Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = CircleShape,
              ) {
                Text(
                  text = source,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                )
              }
            }
          }
          Text(
            text =
              if (!isOwned && accessory.gemCost > 0L)
                "${accessory.description} (${accessory.gemCost} Gems in Shop -> Gems)"
              else accessory.description,
            style = MaterialTheme.typography.bodySmall,
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

@Composable
private fun InventoryThemeCard(
  theme: AppColorTheme,
  isOwned: Boolean,
  isActive: Boolean,
  onApply: () -> Unit,
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(text = theme.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Surface(
              color = MaterialTheme.colorScheme.tertiaryContainer,
              shape = CircleShape,
            ) {
              Text(
                text = theme.unlockSourceLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
              )
            }
          }
          Text(
            text =
              if (isOwned) theme.tagline
              else "Source: ${theme.unlockSourceLabel} • ${theme.requirementSummary}",
            style = MaterialTheme.typography.bodySmall,
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
