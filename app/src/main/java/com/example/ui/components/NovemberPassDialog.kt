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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ActiveBattlePassQuest
import com.example.data.model.BattlePassTierDef
import com.example.data.model.GameCatalog
import com.example.data.model.PassRewardKind
import com.example.ui.viewmodel.PurrGameUiState

@Composable
fun NovemberPassDialog(
  uiState: PurrGameUiState,
  onUnlockVip: () -> Unit,
  onClaimTierReward: (tier: Int, isVip: Boolean) -> Unit,
  onClaimAll: () -> Unit,
  onClaimPassQuest: (String) -> Unit,
  onRestartPass: () -> Unit = {},
  onDismiss: () -> Unit,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val passState = uiState.battlePassState
  val currentFreeTier = uiState.currentPassTier
  val currentVipTier = uiState.currentVipPassTier
  val (freeXpIntoStep, freeStepTarget) = uiState.freePassStepProgress
  val freeProgressFrac = (freeXpIntoStep.toFloat() / freeStepTarget.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
  val (vipXpIntoStep, vipStepTarget) = uiState.vipPassStepProgress
  val vipProgressFrac = (vipXpIntoStep.toFloat() / vipStepTarget.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)

  val (meetsVip, vipFailReason) =
    GameCatalog.meetsNovemberVipRequirements(
      uiState.profile,
      uiState.challengeState,
      uiState.playerStats,
    )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth(0.95f).heightIn(max = 700.dp),
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Festive Autumn / Gold Header
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .background(
                Brush.horizontalGradient(
                  colors = listOf(Color(0xFF5D260E), Color(0xFFB34712), Color(0xFFD98A1A))
                )
              )
              .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                  imageVector = Icons.Rounded.WorkspacePremium,
                  contentDescription = null,
                  tint = Color(0xFFFFE082),
                  modifier = Modifier.size(26.dp),
                )
                Column {
                  val cycleSuffix =
                    if (passState.passCompletions > 0L) " • Cycle #${passState.passCompletions + 1}"
                    else ""
                  Text(
                    text = "November Pass$cycleSuffix",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text =
                      if (passState.vipUnlocked)
                        "Free Tier $currentFreeTier/30 • VIP Tier $currentVipTier/30"
                      else "Free Tier $currentFreeTier/30 • Scaling Seasonal Pass",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFECB3),
                  )
                }
              }
              if (uiState.claimablePassCount > 0) {
                FilledTonalButton(
                  onClick = onClaimAll,
                  modifier = Modifier.height(40.dp).testTag("claim_all_pass_button"),
                ) {
                  Text("Claim (${uiState.claimablePassCount})", maxLines = 1)
                }
              }
              IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_november_pass_button"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Close,
                  contentDescription = "Close November Pass",
                  tint = Color.White,
                )
              }
            }

            // Free Track Progress Bar (+5% per tier)
            LinearProgressIndicator(
              progress = { freeProgressFrac },
              modifier = Modifier.fillMaxWidth().height(7.dp),
              color = Color(0xFFFFE082),
              trackColor = Color(0x44FFFFFF),
              strokeCap = StrokeCap.Round,
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text =
                  if (currentFreeTier >= GameCatalog.NOVEMBER_PASS_MAX_TIER)
                    "Free Track MAX TIER!"
                  else
                    "Free Tier ${currentFreeTier + 1}: ${GameCatalog.formatCompactPurrs(freeXpIntoStep)} / ${GameCatalog.formatCompactPurrs(freeStepTarget)} XP (+5%/tier)",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFFF8E1),
              )
              Text(
                text = "Total XP: ${GameCatalog.formatCompactPurrs(passState.passXp)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFFE082),
              )
            }

            // VIP Track Progress Bar (Starts 500 XP, +15% per tier)
            LinearProgressIndicator(
              progress = { vipProgressFrac },
              modifier = Modifier.fillMaxWidth().height(7.dp),
              color = Color(0xFFFFCA28),
              trackColor = Color(0x33000000),
              strokeCap = StrokeCap.Round,
            )
            Text(
              text =
                if (currentVipTier >= GameCatalog.NOVEMBER_PASS_MAX_TIER)
                  "VIP Track MAX TIER!"
                else
                  "VIP Tier ${currentVipTier + 1}: ${GameCatalog.formatCompactPurrs(vipXpIntoStep)} / ${GameCatalog.formatCompactPurrs(vipStepTarget)} XP (+15%/tier)",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFFFD54F),
              fontWeight = FontWeight.SemiBold,
            )
          }
        }

        // Two Tabs: Tiers (30) & Hourly Quests
        val passTabs =
          listOf(
            "Tiers (30)" + if (uiState.claimablePassCount > 0) " • ${uiState.claimablePassCount}" else "",
            "Hourly Quests" +
              if (uiState.claimablePassQuestsCount > 0) " • ${uiState.claimablePassQuestsCount}" else "",
          )
        SingleChoiceSegmentedButtonRow(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          passTabs.forEachIndexed { idx, label ->
            SegmentedButton(
              selected = selectedTab == idx,
              onClick = { selectedTab = idx },
              shape = SegmentedButtonDefaults.itemShape(index = idx, count = passTabs.size),
              modifier = Modifier.testTag("november_pass_tab_$idx"),
            ) {
              Text(text = label, maxLines = 1)
            }
          }
        }

        if (selectedTab == 1) {
          HourlyPassQuestsContent(
            quests = uiState.hourlyPassQuests,
            refreshCountdownText = uiState.hourlyQuestRefreshText,
            onClaimPassQuest = onClaimPassQuest,
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            // VIP Tier Unlock & Golden Touch Showcase Card
            item {
              Card(
                colors =
                  CardDefaults.cardColors(
                    containerColor =
                      if (passState.vipUnlocked) Color(0xFF3E2A06)
                      else MaterialTheme.colorScheme.secondaryContainer
                  ),
                border =
                  BorderStroke(
                    1.5.dp,
                    if (passState.vipUnlocked) Color(0xFFFFCA28) else MaterialTheme.colorScheme.primary,
                  ),
                modifier = Modifier.fillMaxWidth().testTag("vip_pass_card"),
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
                      CatAvatarMini(
                        patternId = GameCatalog.GOLDEN_TOUCH_SKIN_ID,
                        accessoryId = "golden_collar",
                        modifier = Modifier.size(48.dp),
                      )
                      Column {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                          Text(
                            text = "VIP Track • Golden Touch",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color =
                              if (passState.vipUnlocked) Color(0xFFFFE082)
                              else MaterialTheme.colorScheme.onSecondaryContainer,
                          )
                          if (passState.vipUnlocked) {
                            Surface(color = Color(0xFFFFCA28), shape = CircleShape) {
                              Text(
                                text = "VIP ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF332000),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                              )
                            }
                          }
                        }
                        Text(
                          text =
                            "VIP starts at 500 XP (+15% XP each tier). Tier 30 unlocks Golden Touch: every 30m equipped summons Golden Rain (4.0x for 3m)!",
                          style = MaterialTheme.typography.bodySmall,
                          color =
                            if (passState.vipUnlocked) Color(0xFFFFF8E1)
                            else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.88f),
                        )
                      }
                    }
                  }

                  if (!passState.vipUnlocked) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text =
                            "• Cost: ${uiState.challengeState.gatoShards}/${GameCatalog.NOVEMBER_PASS_VIP_SHARD_COST} Gato Shard",
                          style = MaterialTheme.typography.labelSmall,
                          color =
                            if (uiState.challengeState.gatoShards >= GameCatalog.NOVEMBER_PASS_VIP_SHARD_COST)
                              MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                        )
                        Text(
                          text =
                            "• Upgrades: ${uiState.vipRequirementUpgradeCount}/${GameCatalog.NOVEMBER_PASS_VIP_REQUIRED_UPGRADES} Owned",
                          style = MaterialTheme.typography.labelSmall,
                          color =
                            if (uiState.vipRequirementUpgradeCount >= GameCatalog.NOVEMBER_PASS_VIP_REQUIRED_UPGRADES)
                              MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                        )
                        if (vipFailReason != null) {
                          Text(
                            text = vipFailReason,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f),
                          )
                        }
                      }
                      Button(
                        onClick = onUnlockVip,
                        enabled = meetsVip,
                        modifier = Modifier.height(48.dp).testTag("unlock_vip_pass_button"),
                      ) {
                        Text("Unlock VIP (1 Shard)", maxLines = 1)
                      }
                    }
                  } else {
                    val skinEquipped = uiState.profile.selectedPatternId == GameCatalog.GOLDEN_TOUCH_SKIN_ID
                    val goldenStatus =
                      when {
                        uiState.goldenRainActive ->
                          "GOLDEN RAIN ACTIVE (4.0x) • ${uiState.goldenRainCountdownText} left!"
                        skinEquipped ->
                          "Golden Touch equipped • Charging: ${uiState.goldenTouchProgressText}"
                        GameCatalog.GOLDEN_TOUCH_SKIN_ID in uiState.profile.unlockedPatterns ->
                          "Equip Golden Touch from Inventory to charge Golden Rain (30m -> 3m)!"
                        else -> "Reach VIP Tier 30 to claim the Golden Touch Cat Skin!"
                      }
                    Text(
                      text = goldenStatus,
                      style = MaterialTheme.typography.labelMedium,
                      color = Color(0xFFFFD54F),
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                }
              }
            }

            if (GameCatalog.isNovemberPassCompleted(passState)) {
              item {
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                  border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                  modifier = Modifier.fillMaxWidth().testTag("november_pass_completed_card"),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                      Text(
                        text = "November Pass Completed!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                      )
                      Text(
                        text =
                          if (uiState.claimablePassCount > 0)
                            "Claim your remaining ${uiState.claimablePassCount} tier reward(s), then restart to farm rewards again!"
                          else
                            "Ready for another run? Restarting resets Tiers 1–30 & XP while keeping VIP & unlocked skins.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f),
                      )
                    }
                    Button(
                      onClick = {
                        if (uiState.claimablePassCount > 0) onClaimAll() else onRestartPass()
                      },
                      modifier = Modifier.height(48.dp).testTag("restart_november_pass_button"),
                    ) {
                      Text(
                        text =
                          if (uiState.claimablePassCount > 0) "Claim All (${uiState.claimablePassCount})"
                          else "Restart Pass",
                        maxLines = 1,
                      )
                    }
                  }
                }
              }
            }

            // Tier 30 Grand Finale Preview Strip
            item {
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceEvenly,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                  ) {
                    CatAvatarMini(
                      patternId = GameCatalog.NOVEMBER_FREE_SKIN_ID,
                      accessoryId = "none",
                      modifier = Modifier.size(44.dp),
                    )
                    Column {
                      Text("Tier 30 Free", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                      Text("November Maple", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                      Text(
                        text = "${GameCatalog.formatCompactPurrs(uiState.existCountFor("PATTERN", GameCatalog.NOVEMBER_FREE_SKIN_ID))} Exist",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                  ) {
                    CatAvatarMini(
                      patternId = GameCatalog.GOLDEN_TOUCH_SKIN_ID,
                      accessoryId = "golden_collar",
                      modifier = Modifier.size(44.dp),
                    )
                    Column {
                      Text("Tier 30 VIP", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD48800))
                      Text("Golden Touch", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                      Text(
                        text = "${GameCatalog.formatCompactPurrs(uiState.existCountFor("PATTERN", GameCatalog.GOLDEN_TOUCH_SKIN_ID))} Exist",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                }
              }
            }

            // Column Headers
            item {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Spacer(modifier = Modifier.width(42.dp))
                Text(
                  text = "FREE TRACK (+5%/tier)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.weight(1f),
                )
                Text(
                  text = "VIP TRACK (+15%/tier)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFD48800),
                  modifier = Modifier.weight(1f),
                )
              }
            }

            items(GameCatalog.novemberPassTiers, key = { it.tier }) { tierDef ->
              val freeExist =
                when (tierDef.freeReward.kind) {
                  PassRewardKind.SKIN -> uiState.existCountFor("PATTERN", tierDef.freeReward.itemId)
                  PassRewardKind.ACCESSORY -> uiState.existCountFor("ACCESSORY", tierDef.freeReward.itemId)
                  else -> null
                }
              val vipExist =
                when (tierDef.vipReward.kind) {
                  PassRewardKind.SKIN -> uiState.existCountFor("PATTERN", tierDef.vipReward.itemId)
                  PassRewardKind.ACCESSORY -> uiState.existCountFor("ACCESSORY", tierDef.vipReward.itemId)
                  else -> null
                }
              BattlePassTierRow(
                tierDef = tierDef,
                currentFreeTier = currentFreeTier,
                currentVipTier = currentVipTier,
                vipUnlocked = passState.vipUnlocked,
                freeClaimed = tierDef.tier in passState.claimedFreeTiers,
                vipClaimed = tierDef.tier in passState.claimedVipTiers,
                freeExistCount = freeExist,
                vipExistCount = vipExist,
                onClaimFree = { onClaimTierReward(tierDef.tier, false) },
                onClaimVip = { onClaimTierReward(tierDef.tier, true) },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HourlyPassQuestsContent(
  quests: List<ActiveBattlePassQuest>,
  refreshCountdownText: String,
  onClaimPassQuest: (String) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxWidth(),
    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Hourly Battle Pass Quests",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
              text = "Complete quests for Pass XP that scales with your Tier! Refreshes every hour.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
            )
          }
          Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = refreshCountdownText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }

    items(quests, key = { it.template.id }) { quest ->
      val ratio =
        (quest.progress.coerceAtMost(quest.template.target).toFloat() /
            quest.template.target.coerceAtLeast(1L).toFloat())
          .coerceIn(0f, 1f)
      Card(
        colors =
          CardDefaults.cardColors(
            containerColor =
              if (quest.isCompleted && !quest.isClaimed)
                MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceVariant
          ),
        border =
          if (quest.isCompleted && !quest.isClaimed)
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
          else null,
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
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
              Text(
                text = quest.template.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = quest.template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Surface(
              color = Color(0xFFD98A1A),
              shape = CircleShape,
            ) {
              Text(
                text = "+${GameCatalog.formatCompactPurrs(quest.xpReward)} XP",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }

          LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            strokeCap = StrokeCap.Round,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text =
                "${GameCatalog.formatCompactPurrs(quest.progress.coerceAtMost(quest.template.target))} / ${GameCatalog.formatCompactPurrs(quest.template.target)}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
              onClick = { onClaimPassQuest(quest.template.id) },
              enabled = quest.isCompleted && !quest.isClaimed,
              modifier = Modifier.height(48.dp).testTag("claim_pass_quest_${quest.template.id}"),
            ) {
              Text(
                text =
                  when {
                    quest.isClaimed -> "Claimed"
                    quest.isCompleted -> "Claim XP"
                    else -> "In Progress"
                  },
                maxLines = 1,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BattlePassTierRow(
  tierDef: BattlePassTierDef,
  currentFreeTier: Int,
  currentVipTier: Int,
  vipUnlocked: Boolean,
  freeClaimed: Boolean,
  vipClaimed: Boolean,
  freeExistCount: Long?,
  vipExistCount: Long?,
  onClaimFree: () -> Unit,
  onClaimVip: () -> Unit,
) {
  val isFreeReached = currentFreeTier >= tierDef.tier
  val isVipReached = currentVipTier >= tierDef.tier
  val isMilestone = tierDef.tier == 30 || tierDef.tier % 5 == 0
  val freeStepReq = GameCatalog.freeStepXpForTier(tierDef.tier)
  val vipStepReq = GameCatalog.vipStepXpForTier(tierDef.tier)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(
      color =
        when {
          tierDef.tier == 30 -> Color(0xFFD98A1A)
          isFreeReached -> MaterialTheme.colorScheme.primary
          else -> MaterialTheme.colorScheme.surfaceVariant
        },
      shape = CircleShape,
      modifier = Modifier.size(42.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = "${tierDef.tier}",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color =
            if (isFreeReached || tierDef.tier == 30) Color.White
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    PassTrackTile(
      label = tierDef.freeReward.label,
      stepXpLabel = "${GameCatalog.formatCompactPurrs(freeStepReq)} XP",
      existCount = freeExistCount,
      isSkinOrSpecial =
        tierDef.freeReward.kind == PassRewardKind.SKIN ||
          tierDef.freeReward.kind == PassRewardKind.UPGRADE_LEVEL,
      isVipTile = false,
      isReached = isFreeReached,
      isTrackUnlocked = true,
      isClaimed = freeClaimed,
      isMilestone = isMilestone,
      onClick = onClaimFree,
      modifier = Modifier.weight(1f).testTag("claim_pass_free_${tierDef.tier}"),
    )

    PassTrackTile(
      label = tierDef.vipReward.label,
      stepXpLabel = "${GameCatalog.formatCompactPurrs(vipStepReq)} XP",
      existCount = vipExistCount,
      isSkinOrSpecial =
        tierDef.vipReward.kind == PassRewardKind.SKIN ||
          tierDef.vipReward.kind == PassRewardKind.ACCESSORY ||
          tierDef.vipReward.kind == PassRewardKind.GATO_SHARD,
      isVipTile = true,
      isReached = isVipReached,
      isTrackUnlocked = vipUnlocked,
      isClaimed = vipClaimed,
      isMilestone = isMilestone,
      onClick = onClaimVip,
      modifier = Modifier.weight(1f).testTag("claim_pass_vip_${tierDef.tier}"),
    )
  }
}

@Composable
private fun PassTrackTile(
  label: String,
  stepXpLabel: String,
  existCount: Long?,
  isSkinOrSpecial: Boolean,
  isVipTile: Boolean,
  isReached: Boolean,
  isTrackUnlocked: Boolean,
  isClaimed: Boolean,
  isMilestone: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val canClaim = isReached && isTrackUnlocked && !isClaimed
  val containerColor =
    when {
      isClaimed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
      canClaim && isVipTile -> Color(0xFFFFF3C4)
      canClaim -> MaterialTheme.colorScheme.primaryContainer
      isVipTile -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
      else -> MaterialTheme.colorScheme.surfaceVariant
    }
  val border =
    when {
      canClaim && isVipTile -> BorderStroke(1.5.dp, Color(0xFFD98A1A))
      canClaim -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
      isMilestone && !isClaimed -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
      else -> null
    }

  Card(
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = border,
    modifier = modifier.heightIn(min = 72.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector =
            when {
              isClaimed -> Icons.Rounded.CheckCircle
              !isTrackUnlocked -> Icons.Rounded.Lock
              isVipTile -> Icons.Rounded.Star
              isSkinOrSpecial -> Icons.Rounded.AutoAwesome
              else -> Icons.Rounded.Star
            },
          contentDescription = null,
          tint =
            when {
              isClaimed -> MaterialTheme.colorScheme.secondary
              !isTrackUnlocked -> MaterialTheme.colorScheme.outline
              isVipTile -> Color(0xFFD48800)
              else -> MaterialTheme.colorScheme.primary
            },
          modifier = Modifier.size(16.dp),
        )
        Text(
          text =
            when {
              isClaimed -> "Claimed"
              !isTrackUnlocked -> "VIP Locked"
              canClaim -> "CLAIM"
              else -> stepXpLabel
            },
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color =
            when {
              canClaim -> if (isVipTile) Color(0xFF8C5300) else MaterialTheme.colorScheme.primary
              isClaimed -> MaterialTheme.colorScheme.secondary
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (isSkinOrSpecial || canClaim) FontWeight.Bold else FontWeight.Medium,
        color =
          if (canClaim && isVipTile) Color(0xFF3E2600)
          else MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      if (existCount != null) {
        Text(
          text = "${GameCatalog.formatCompactPurrs(existCount)} Exist",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = if (isVipTile) Color(0xFF8C5300) else MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}
