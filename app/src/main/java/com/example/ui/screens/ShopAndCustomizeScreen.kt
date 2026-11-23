package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.GameCatalog
import com.example.data.model.ShopUpgradeDef
import com.example.data.model.UpgradeCategory
import com.example.ui.components.CatAvatarMini
import com.example.ui.components.CatBackgroundPreviewSwatch
import com.example.ui.components.ItemHoverTooltipBox
import com.example.ui.components.SubtleGlitchLoreWhisper
import com.example.ui.theme.AppColorTheme
import com.example.ui.viewmodel.PurrGameUiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShopAndCustomizeScreen(
  uiState: PurrGameUiState,
  initialSubTab: Int = 0,
  onBuyUpgrade: (String) -> Unit,
  onSelectOrUnlockPattern: (String) -> Unit,
  onSelectOrUnlockAccessory: (String) -> Unit,
  onSelectOrUnlockBackground: (String) -> Unit = {},
  onRenameCat: (String) -> Unit,
  onOpenInventory: () -> Unit,
  onBuyGemCharm: () -> Unit = {},
  onActivateGemFrenzy: () -> Unit = {},
  onSummonCosmicWeather: () -> Unit = {},
  onUnlockPrismLens: () -> Unit = {},
  onUnlockGemPattern: (String) -> Unit = {},
  onUnlockGemAccessory: (String) -> Unit = {},
  onUnlockGemColorTheme: (AppColorTheme) -> Unit = {},
  onForgeGatoShard: () -> Unit = {},
) {
  var selectedTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }
  val numberFormat = NumberFormat.getNumberInstance(Locale.US)

  BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    val isNarrow = maxWidth < 360.dp
    val isUltraNarrow = maxWidth < 340.dp
    val horizontalPad = if (isNarrow) 10.dp else 16.dp
    val tabs =
      if (isUltraNarrow) listOf("Click", "PPS", "Style", "Gems")
      else listOf("Click", "Per Sec", "Customize", "Gems")

    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 640.dp)
          .padding(horizontal = horizontalPad, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      val headerTitleBlock: @Composable () -> Unit = {
        Column {
          Text(
            text = "Boutique & Upgrades",
            style =
              if (isNarrow) MaterialTheme.typography.titleLarge
              else MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "Boost tapping power or style ${uiState.profile.selectedCatName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      val purrsChip: @Composable () -> Unit = {
        Surface(
          color =
            if (uiState.challengeState.isHardcoreMode) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer,
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(
            text =
              "${GameCatalog.formatCompactPurrs(uiState.activeCurrencyBalance)} ${uiState.activeCurrencyShortLabel}",
            style = MaterialTheme.typography.labelLarge,
            color =
              if (uiState.challengeState.isHardcoreMode) MaterialTheme.colorScheme.onErrorContainer
              else MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          )
        }
      }

      val gemsChip: @Composable () -> Unit = {
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer,
          shape = MaterialTheme.shapes.small,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Diamond,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(13.dp),
            )
            Text(
              text = "${numberFormat.format(uiState.challengeState.gems)} Gems",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              maxLines = 1,
              modifier = Modifier.testTag("shop_gems_balance"),
            )
          }
        }
      }

      if (isNarrow) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          headerTitleBlock()
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            purrsChip()
            gemsChip()
          }
        }
      } else {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            headerTitleBlock()
          }
          Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            purrsChip()
            gemsChip()
          }
        }
      }

      if (uiState.challengeState.isHardcoreMode) {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth().testTag("shop_hardcore_banner"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.LocalFireDepartment,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "HARDCORE MODE • Using Hardcore Purrs & isolated 3.5x+ Shop",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onErrorContainer,
            )
          }
        }
      }

      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        tabs.forEachIndexed { index, title ->
          SegmentedButton(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
            modifier = Modifier.testTag("shop_tab_$index"),
          ) {
            Text(
              text = title,
              style =
                if (isNarrow) MaterialTheme.typography.labelSmall
                else MaterialTheme.typography.labelLarge,
              maxLines = 1,
              softWrap = false,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        when (selectedTab) {
          0 -> {
            val clickUpgrades = GameCatalog.upgrades.filter { it.category == UpgradeCategory.CLICK }
            UpgradeList(
              upgrades = clickUpgrades,
              uiState = uiState,
              onBuyUpgrade = onBuyUpgrade,
            )
          }
          1 -> {
            val ppsUpgrades = GameCatalog.upgrades.filter { it.category == UpgradeCategory.PER_SECOND }
            UpgradeList(
              upgrades = ppsUpgrades,
              uiState = uiState,
              onBuyUpgrade = onBuyUpgrade,
            )
          }
          2 -> {
            CustomizeCatPanel(
              uiState = uiState,
              isNarrow = isNarrow,
              onSelectOrUnlockPattern = onSelectOrUnlockPattern,
              onSelectOrUnlockAccessory = onSelectOrUnlockAccessory,
              onSelectOrUnlockBackground = onSelectOrUnlockBackground,
              onRenameCat = onRenameCat,
              onOpenInventory = onOpenInventory,
            )
          }
          else -> {
            GemVaultPanel(
              uiState = uiState,
              numberFormat = numberFormat,
              onBuyGemCharm = onBuyGemCharm,
              onActivateGemFrenzy = onActivateGemFrenzy,
              onSummonCosmicWeather = onSummonCosmicWeather,
              onUnlockPrismLens = onUnlockPrismLens,
              onUnlockGemPattern = onUnlockGemPattern,
              onUnlockGemAccessory = onUnlockGemAccessory,
              onUnlockGemColorTheme = onUnlockGemColorTheme,
              onForgeGatoShard = onForgeGatoShard,
            )
          }
        }
      }

      SubtleGlitchLoreWhisper(channel = "shop")
    }
  }
}

@Composable
private fun UpgradeList(
  upgrades: List<ShopUpgradeDef>,
  uiState: PurrGameUiState,
  onBuyUpgrade: (String) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(upgrades, key = { it.id }) { upgrade ->
      val level = uiState.activeUpgradeLevels[upgrade.id] ?: 0L
      val cost =
        GameCatalog.effectiveUpgradeCost(
          upgrade,
          level,
          uiState.challengeState.isHardcoreMode,
          uiState.challengeState.unlockedAscensionIds,
        )
      val canAfford = uiState.activeCurrencyBalance >= cost
      val icon =
        if (upgrade.category == UpgradeCategory.CLICK) Icons.Rounded.TouchApp
        else Icons.Rounded.AutoAwesome
      val bonusText =
        if (upgrade.category == UpgradeCategory.CLICK)
          "+${GameCatalog.formatCompactPurrs(upgrade.powerPerLevel)}/tap"
        else "+${GameCatalog.formatCompactPurrs(upgrade.powerPerLevel)}/sec"

      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().testTag("upgrade_card_${upgrade.id}"),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.size(44.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                )
              }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              @OptIn(ExperimentalLayoutApi::class)
              FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(text = upgrade.name, style = MaterialTheme.typography.titleMedium)
                Surface(
                  color = MaterialTheme.colorScheme.secondaryContainer,
                  shape = MaterialTheme.shapes.small,
                ) {
                  Text(
                    text = "Lv.$level",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  )
                }
              }
              Text(
                text = upgrade.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = bonusText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
              )
            }
          }

          Button(
            onClick = { onBuyUpgrade(upgrade.id) },
            enabled = canAfford,
            modifier = Modifier.height(48.dp).testTag("buy_upgrade_${upgrade.id}"),
          ) {
            Text(text = GameCatalog.formatCompactPurrs(cost), maxLines = 1)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomizeCatPanel(
  uiState: PurrGameUiState,
  isNarrow: Boolean,
  onSelectOrUnlockPattern: (String) -> Unit,
  onSelectOrUnlockAccessory: (String) -> Unit,
  onSelectOrUnlockBackground: (String) -> Unit,
  onRenameCat: (String) -> Unit,
  onOpenInventory: () -> Unit,
) {
  var nameInput by remember(uiState.profile.selectedCatName) {
    mutableStateOf(uiState.profile.selectedCatName)
  }
  val standardPatterns = remember { GameCatalog.patterns.filter { !it.isEventExclusive } }
  val standardAccessories = remember { GameCatalog.accessories.filter { !it.isEventExclusive } }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Card(
        onClick = onOpenInventory,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth().testTag("customize_open_inventory_button"),
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
              imageVector = Icons.Rounded.Inventory2,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Column {
              Text(
                text = "Limited & Pass Inventory",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
              Text(
                text = "Equip Event, Daily Prize & November Pass Skins, Accessories & Themes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
              )
            }
          }
          Text(
            text = "Open →",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
      ) {
        if (isNarrow) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              CatAvatarMini(
                patternId = uiState.profile.selectedPatternId,
                accessoryId = uiState.profile.selectedAccessoryId,
                modifier = Modifier.size(52.dp),
              )
              OutlinedTextField(
                value = nameInput,
                onValueChange = { if (it.length <= 24) nameInput = it },
                label = { Text("Cat Name") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("cat_name_input"),
              )
            }
            FilledTonalButton(
              onClick = { onRenameCat(nameInput) },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_cat_name_button"),
            ) {
              Text("Save Cat Name")
            }
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            CatAvatarMini(
              patternId = uiState.profile.selectedPatternId,
              accessoryId = uiState.profile.selectedAccessoryId,
              modifier = Modifier.size(64.dp),
            )
            OutlinedTextField(
              value = nameInput,
              onValueChange = { if (it.length <= 24) nameInput = it },
              label = { Text("Cat Name") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("cat_name_input"),
            )
            FilledTonalButton(
              onClick = { onRenameCat(nameInput) },
              modifier = Modifier.height(48.dp).testTag("save_cat_name_button"),
            ) {
              Text("Save")
            }
          }
        }
      }
    }

    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.Wallpaper,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
        Text(text = "Sanctuary Room Backgrounds", style = MaterialTheme.typography.titleLarge)
      }
    }

    val visibleBackgrounds =
      GameCatalog.backgrounds.filter { !it.isEventExclusive || it.id in uiState.profile.unlockedBackgrounds }
    items(visibleBackgrounds, key = { "bg_${it.id}" }) { bg ->
      val isUnlocked = bg.id in uiState.profile.unlockedBackgrounds
      val isEquipped = bg.id == uiState.profile.selectedBackgroundId
      val effectiveCost =
        GameCatalog.effectiveItemCost(
          bg.cost,
          uiState.challengeState.isHardcoreMode,
          uiState.challengeState.unlockedAscensionIds,
        )
      val canAfford = uiState.activeCurrencyBalance >= effectiveCost

      ItemHoverTooltipBox(
        title = bg.name,
        description = bg.description,
        limitedTag = if (bg.isEventExclusive) "Limited ${bg.eventGroup ?: "Event"}" else null,
        existCount = if (bg.isEventExclusive) uiState.existCountFor("BACKGROUND", bg.id) else null,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Card(
          colors =
            CardDefaults.cardColors(
              containerColor =
                if (isEquipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.surfaceVariant
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
              modifier = Modifier.weight(1f).padding(end = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              CatBackgroundPreviewSwatch(
                def = bg,
                modifier = Modifier.size(50.dp),
              )
              Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = bg.name, style = MaterialTheme.typography.titleMedium)
                Text(
                  text = "Hover or hold for details",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Button(
              onClick = { onSelectOrUnlockBackground(bg.id) },
              enabled = isEquipped || isUnlocked || canAfford,
              modifier = Modifier.height(48.dp).testTag("background_action_${bg.id}"),
            ) {
              if (isEquipped) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Active")
              } else if (isUnlocked) {
                Text("Equip")
              } else {
                Text(GameCatalog.formatCompactPurrs(effectiveCost))
              }
            }
          }
        }
      }
    }

    item {
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
        Text(text = "Coat Patterns", style = MaterialTheme.typography.titleLarge)
      }
    }

    items(standardPatterns, key = { "pat_${it.id}" }) { pattern ->
      val isUnlocked = pattern.id in uiState.profile.unlockedPatterns
      val isEquipped = pattern.id == uiState.profile.selectedPatternId
      val effectiveCost =
        GameCatalog.effectiveItemCost(
          pattern.cost,
          uiState.challengeState.isHardcoreMode,
          uiState.challengeState.unlockedAscensionIds,
        )
      val canAfford = uiState.activeCurrencyBalance >= effectiveCost

      Card(
        colors =
          CardDefaults.cardColors(
            containerColor =
              if (isEquipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
              else MaterialTheme.colorScheme.surfaceVariant
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
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            CatAvatarMini(
              patternId = pattern.id,
              accessoryId = uiState.profile.selectedAccessoryId,
              modifier = Modifier.size(50.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(text = pattern.name, style = MaterialTheme.typography.titleMedium)
                pattern.unlockSourceLabel?.let { source ->
                  Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small,
                  ) {
                    Text(
                      text = source,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onTertiaryContainer,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                  }
                }
              }
              Text(
                text = pattern.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          val isDailyLocked = !isUnlocked && pattern.cost == 0L && pattern.unlockSourceLabel != null
          Button(
            onClick = { onSelectOrUnlockPattern(pattern.id) },
            enabled = isEquipped || isUnlocked || isDailyLocked || canAfford,
            modifier = Modifier.height(48.dp).testTag("pattern_action_${pattern.id}"),
          ) {
            if (isEquipped) {
              Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.size(4.dp))
              Text("Active")
            } else if (isUnlocked) {
              Text("Equip")
            } else if (isDailyLocked) {
              Text(pattern.unlockSourceLabel ?: "Daily", maxLines = 1)
            } else {
              Text(GameCatalog.formatCompactPurrs(effectiveCost), maxLines = 1)
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = "Accessories", style = MaterialTheme.typography.titleLarge)
    }

    items(standardAccessories, key = { "acc_${it.id}" }) { accessory ->
      val isUnlocked = accessory.id in uiState.profile.unlockedAccessories
      val isEquipped = accessory.id == uiState.profile.selectedAccessoryId
      val effectiveCost =
        GameCatalog.effectiveItemCost(
          accessory.cost,
          uiState.challengeState.isHardcoreMode,
          uiState.challengeState.unlockedAscensionIds,
        )
      val canAfford = uiState.activeCurrencyBalance >= effectiveCost

      Card(
        colors =
          CardDefaults.cardColors(
            containerColor =
              if (isEquipped) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
              else MaterialTheme.colorScheme.surfaceVariant
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
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            CatAvatarMini(
              patternId = uiState.profile.selectedPatternId,
              accessoryId = accessory.id,
              modifier = Modifier.size(50.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(text = accessory.name, style = MaterialTheme.typography.titleMedium)
                accessory.unlockSourceLabel?.let { source ->
                  Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small,
                  ) {
                    Text(
                      text = source,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onTertiaryContainer,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                  }
                }
              }
              Text(
                text = accessory.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          val isDailyLocked =
            !isUnlocked && accessory.cost == 0L && accessory.unlockSourceLabel != null
          Button(
            onClick = { onSelectOrUnlockAccessory(accessory.id) },
            enabled = isEquipped || isUnlocked || isDailyLocked || canAfford,
            modifier = Modifier.height(48.dp).testTag("accessory_action_${accessory.id}"),
          ) {
            if (isEquipped) {
              Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.size(4.dp))
              Text("Active")
            } else if (isUnlocked) {
              Text("Equip")
            } else if (isDailyLocked) {
              Text(accessory.unlockSourceLabel ?: "Daily", maxLines = 1)
            } else {
              Text(GameCatalog.formatCompactPurrs(effectiveCost), maxLines = 1)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun GemVaultPanel(
  uiState: PurrGameUiState,
  numberFormat: NumberFormat,
  onBuyGemCharm: () -> Unit,
  onActivateGemFrenzy: () -> Unit,
  onSummonCosmicWeather: () -> Unit,
  onUnlockPrismLens: () -> Unit,
  onUnlockGemPattern: (String) -> Unit,
  onUnlockGemAccessory: (String) -> Unit,
  onUnlockGemColorTheme: (AppColorTheme) -> Unit,
  onForgeGatoShard: () -> Unit,
) {
  val gems = uiState.challengeState.gems
  val charmLevel = uiState.challengeState.gemCharmLevel
  val nextCharmCost = uiState.nextGemCharmCost
  val gemPatterns = GameCatalog.patterns.filter { it.gemCost > 0L }
  val gemAccessories = GameCatalog.accessories.filter { it.gemCost > 0L }
  val gemThemes = AppColorTheme.gemThemes

  LazyColumn(
    modifier = Modifier.fillMaxSize().testTag("gem_vault_list"),
    contentPadding = PaddingValues(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Diamond,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
            )
            Text(
              text = "Rare Gem Vault • ${numberFormat.format(gems)} Gems",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
          }
          Text(
            text = "Gems drop rarely from high-combo pets (boosted during Bloodmoon, Aurora, Starfall & Golden Rain) and via Quests, Endless Streaks, Rebirths & Pass tiers. Gems survive Rebirths!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
          )
        }
      }
    }

    // 1. Repeatable Gemstone Purr Charm
    item {
      GemVaultOfferCard(
        icon = Icons.Rounded.AutoAwesome,
        title = "Gemstone Purr Charm (Lv.$charmLevel)",
        description =
          "Permanent +25% Purrs multiplier per level (${String.format(Locale.US, "%.2fx", uiState.gemCharmMultiplier)} active -> ${String.format(Locale.US, "%.2fx", GameCatalog.gemCharmMultiplier(charmLevel + 1L))}).",
        buttonLabel = "$nextCharmCost Gems",
        enabled = gems >= nextCharmCost,
        onClick = onBuyGemCharm,
        testTag = "gem_charm_buy_button",
      )
    }

    // 2. Starlight Gem Frenzy Boost
    item {
      GemVaultOfferCard(
        icon = Icons.Rounded.FlashOn,
        title =
          if (uiState.gemFrenzyActive) "Starlight Gem Frenzy Active (${uiState.gemFrenzyCountdownText})"
          else "Starlight Gem Frenzy (2m Surge)",
        description = "Channel 3.0x All Purrs (Tap & Auto-Purr) for 2 minutes. Stacks with Weather & Golden Rain!",
        buttonLabel = if (uiState.gemFrenzyActive) "Active" else "${GameCatalog.GEM_FRENZY_COST} Gems",
        enabled = !uiState.gemFrenzyActive && gems >= GameCatalog.GEM_FRENZY_COST,
        onClick = onActivateGemFrenzy,
        testTag = "gem_frenzy_button",
      )
    }

    // 3. Cosmic Sky Summoner
    item {
      GemVaultOfferCard(
        icon = Icons.Rounded.WbSunny,
        title = "Summon Cosmic Sanctuary Sky",
        description = "Immediately shift weather to Aurora Borealis, Starfall Sky, or Rainbow Sunshower (+15 Pass XP).",
        buttonLabel = "${GameCatalog.GEM_WEATHER_SUMMON_COST} Gems",
        enabled = gems >= GameCatalog.GEM_WEATHER_SUMMON_COST,
        onClick = onSummonCosmicWeather,
        testTag = "gem_weather_summon_button",
      )
    }

    // 4. Prismatic Weather Lens
    item {
      val unlocked = uiState.challengeState.prismLensUnlocked
      GemVaultOfferCard(
        icon = Icons.Rounded.Palette,
        title = "Prismatic Weather Lens",
        description = "Permanent relic: amplifies every Weather click & PPS bonus by an extra +20%!",
        buttonLabel = if (unlocked) "Unlocked" else "${GameCatalog.GEM_PRISM_LENS_COST} Gems",
        enabled = !unlocked && gems >= GameCatalog.GEM_PRISM_LENS_COST,
        onClick = onUnlockPrismLens,
        testTag = "gem_prism_lens_button",
      )
    }

    // 5. Exclusive Gem Skins
    items(gemPatterns, key = { "gem_pat_${it.id}" }) { pattern ->
      val isUnlocked = pattern.id in uiState.profile.unlockedPatterns
      val isEquipped = pattern.id == uiState.profile.selectedPatternId
      val existCount = uiState.existCountFor("PATTERN", pattern.id)
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = if (isEquipped) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            CatAvatarMini(
              patternId = pattern.id,
              accessoryId = uiState.profile.selectedAccessoryId,
              modifier = Modifier.size(50.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              @OptIn(ExperimentalLayoutApi::class)
              FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(text = pattern.name, style = MaterialTheme.typography.titleMedium)
                Surface(
                  color = MaterialTheme.colorScheme.secondaryContainer,
                  shape = MaterialTheme.shapes.small,
                ) {
                  Text(
                    text = "${GameCatalog.formatCompactPurrs(existCount)} Exist",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  )
                }
              }
              Text(
                text = pattern.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Button(
            onClick = { onUnlockGemPattern(pattern.id) },
            enabled = isEquipped || isUnlocked || gems >= pattern.gemCost,
            modifier = Modifier.height(48.dp).testTag("gem_pattern_${pattern.id}_button"),
          ) {
            Text(
              when {
                isEquipped -> "Active"
                isUnlocked -> "Equip"
                else -> "${pattern.gemCost} Gems"
              }
            )
          }
        }
      }
    }

    // 6. Exclusive Gem Accessories
    items(gemAccessories, key = { "gem_acc_${it.id}" }) { accessory ->
      val isUnlocked = accessory.id in uiState.profile.unlockedAccessories
      val isEquipped = accessory.id == uiState.profile.selectedAccessoryId
      val existCount = uiState.existCountFor("ACCESSORY", accessory.id)
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = if (isEquipped) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            CatAvatarMini(
              patternId = uiState.profile.selectedPatternId,
              accessoryId = accessory.id,
              modifier = Modifier.size(50.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              @OptIn(ExperimentalLayoutApi::class)
              FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(text = accessory.name, style = MaterialTheme.typography.titleMedium)
                Surface(
                  color = MaterialTheme.colorScheme.secondaryContainer,
                  shape = MaterialTheme.shapes.small,
                ) {
                  Text(
                    text = "${GameCatalog.formatCompactPurrs(existCount)} Exist",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  )
                }
              }
              Text(
                text = accessory.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Button(
            onClick = { onUnlockGemAccessory(accessory.id) },
            enabled = isEquipped || isUnlocked || gems >= accessory.gemCost,
            modifier = Modifier.height(48.dp).testTag("gem_accessory_${accessory.id}_button"),
          ) {
            Text(
              when {
                isEquipped -> "Active"
                isUnlocked -> "Equip"
                else -> "${accessory.gemCost} Gems"
              }
            )
          }
        }
      }
    }

    // 7. Exclusive Gem Themes
    items(gemThemes, key = { "gem_theme_${it.id}" }) { theme ->
      val isUnlocked = theme.id in uiState.settings.unlockedColorThemes
      val isSelected = uiState.settings.colorTheme == theme
      val existCount = uiState.existCountFor("THEME", theme.id)
      GemVaultOfferCard(
        icon = Icons.Rounded.Palette,
        title = "${theme.displayName} Theme (${GameCatalog.formatCompactPurrs(existCount)} Exist)",
        description = "${theme.requirementSummary} — jewel-toned luxury Sanctuary palette.",
        buttonLabel =
          when {
            isSelected -> "Applied"
            isUnlocked -> "Apply"
            else -> "${theme.gemCost} Gems"
          },
        enabled = isSelected || isUnlocked || gems >= theme.gemCost,
        onClick = { onUnlockGemColorTheme(theme) },
        testTag = "gem_theme_${theme.id}_button",
      )
    }

    // 8. Gato Shard Forge
    item {
      GemVaultOfferCard(
        icon = Icons.Rounded.RestartAlt,
        title = "Forge +1 Gato Shard",
        description = "Transmute ${GameCatalog.GEM_SHARD_FORGE_COST} Rare Gems into +1 permanent Gato Shard for Ascension Tree nodes!",
        buttonLabel = "${GameCatalog.GEM_SHARD_FORGE_COST} Gems",
        enabled = gems >= GameCatalog.GEM_SHARD_FORGE_COST,
        onClick = onForgeGatoShard,
        testTag = "gem_forge_shard_button",
      )
    }
  }
}

@Composable
private fun GemVaultOfferCard(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  description: String,
  buttonLabel: String,
  enabled: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        modifier = Modifier.weight(1f).padding(end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.size(44.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
            )
          }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(text = title, style = MaterialTheme.typography.titleMedium)
          Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(48.dp).testTag(testTag),
      ) {
        Text(buttonLabel, maxLines = 1)
      }
    }
  }
}
