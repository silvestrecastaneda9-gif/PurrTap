package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.RestartAlt
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AscensionUpgradeDef
import com.example.data.model.ChallengeProgressState
import com.example.data.model.GameCatalog
import com.example.ui.viewmodel.PurrGameUiState

@Composable
fun ChallengeAndRebirthDialog(
  uiState: PurrGameUiState,
  onPerformRebirth: () -> Unit,
  onPerformGatoAscension: () -> Unit,
  onUnlockAscensionNode: (String) -> Unit,
  onToggleHardcoreMode: () -> Unit,
  onDismiss: () -> Unit,
) {
  val profile = uiState.profile
  val challenge = uiState.challengeState
  val rebirthCost = uiState.nextRebirthCost
  val canRebirth = uiState.activeCurrencyBalance >= rebirthCost
  val upgradeTotal = profile.upgradeLevels.values.sum()
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Rebirth", "Ascension", "Hardcore")

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
          .padding(horizontal = 16.dp, vertical = 20.dp)
          .widthIn(max = 560.dp)
          .testTag("challenge_rebirth_dialog"),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "Rebirth & Ascensión",
                style = MaterialTheme.typography.titleLarge,
              )
              if (challenge.gatoShards > 0L || challenge.ascensionCount > 0L) {
                Surface(
                  color = MaterialTheme.colorScheme.tertiaryContainer,
                  shape = CircleShape,
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Diamond,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.tertiary,
                      modifier = Modifier.size(13.dp),
                    )
                    Text(
                      text = "${challenge.gatoShards} Shards",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                  }
                }
              }
            }
            Text(
              text =
                "Balance: ${GameCatalog.formatCompactPurrs(uiState.activeCurrencyBalance)} ${uiState.activeCurrencyShortLabel}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_challenge_dialog_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close Rebirths & Ascensión")
          }
        }

        // Section Switcher
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          tabs.forEachIndexed { index, title ->
            SegmentedButton(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
              modifier = Modifier.testTag("challenge_tab_$index"),
            ) {
              Text(text = title, maxLines = 1)
            }
          }
        }

        Column(
          modifier =
            Modifier.fillMaxWidth()
              .weight(1f, fill = false)
              .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          when (selectedTab) {
            0 -> {
              // Rebirth Card
              Card(
                colors =
                  CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                      )
                      Text(
                        text = "Rebirth #${challenge.rebirthCount + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                      )
                    }
                    Surface(
                      color = MaterialTheme.colorScheme.primary,
                      shape = CircleShape,
                    ) {
                      Text(
                        text = "Active: ${uiState.rebirthMultiplier}x",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                      )
                    }
                  }

                  val nextMult =
                    GameCatalog.rebirthMultiplier(
                      challenge.rebirthBonusSteps + 1L,
                      challenge.unlockedAscensionIds,
                    )
                  val stepGain =
                    if ("asc_rebirth_echo" in challenge.unlockedAscensionIds) "+1.0x" else "+0.5x"
                  Text(
                    text =
                      "• Gain $stepGain permanent Purr Multiplier (Next: ${nextMult}x)\n" +
                        "• Rebirth price doubles every prestige (${GameCatalog.formatCompactPurrs(GameCatalog.rebirthCost(challenge.rebirthCount + 1, challenge.isHardcoreMode))} next)\n" +
                        "• Resets active run Purrs, Shop Upgrades & standard Skins/Accessories.\n" +
                        "• Bonus: +${GameCatalog.REBIRTH_GEM_REWARD} Rare Gems per Rebirth! Streak, Themes, Gems & Event items kept!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                  )

                  Button(
                    onClick = onPerformRebirth,
                    enabled = canRebirth,
                    modifier =
                      Modifier.fillMaxWidth().height(48.dp).testTag("perform_rebirth_button"),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.AutoAwesome,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                      text =
                        "Rebirth Now (${GameCatalog.formatCompactPurrs(rebirthCost)} ${uiState.activeCurrencyShortLabel})",
                      maxLines = 1,
                    )
                  }
                }
              }

              // Quick Gato Ascensión Card inside Rebirth tab
              GatoAscensionCard(
                challenge = challenge,
                canAscend = uiState.canAscend,
                onPerformGatoAscension = onPerformGatoAscension,
                onViewTree = { selectedTab = 1 },
              )
            }

            1 -> {
              // Gato Ascensión Header + Shard Wallet
              GatoAscensionCard(
                challenge = challenge,
                canAscend = uiState.canAscend,
                onPerformGatoAscension = onPerformGatoAscension,
                onViewTree = null,
              )

              // Ascension Tree Tiers
              Text(
                text = "Gato Ascension Tree",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )

              val tier1 = GameCatalog.ascensionUpgrades.filter { it.tier == 1 }
              val tier2 = GameCatalog.ascensionUpgrades.filter { it.tier == 2 }
              val tier3 = GameCatalog.ascensionUpgrades.filter { it.tier == 3 }
              val tier4 = GameCatalog.ascensionUpgrades.filter { it.tier == 4 }

              tier1.forEach { node ->
                AscensionNodeCard(
                  node = node,
                  challenge = challenge,
                  onUnlock = { onUnlockAscensionNode(node.id) },
                )
              }

              TreeBranchConnector()
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                tier2.forEach { node ->
                  AscensionNodeCard(
                    node = node,
                    challenge = challenge,
                    onUnlock = { onUnlockAscensionNode(node.id) },
                    modifier = Modifier.weight(1f),
                  )
                }
              }

              TreeBranchConnector()
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                tier3.forEach { node ->
                  AscensionNodeCard(
                    node = node,
                    challenge = challenge,
                    onUnlock = { onUnlockAscensionNode(node.id) },
                    modifier = Modifier.weight(1f),
                  )
                }
              }

              TreeBranchConnector()
              tier4.forEach { node ->
                AscensionNodeCard(
                  node = node,
                  challenge = challenge,
                  onUnlock = { onUnlockAscensionNode(node.id) },
                )
              }
            }

            else -> {
              // Hardcore Mode Card
              Card(
                colors =
                  CardDefaults.cardColors(
                    containerColor =
                      if (challenge.isHardcoreMode) MaterialTheme.colorScheme.errorContainer
                      else MaterialTheme.colorScheme.surfaceVariant
                  ),
                border =
                  if (challenge.isHardcoreMode) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                  else null,
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
                    ) {
                      Icon(
                        imageVector =
                          if (uiState.hardcoreEligible) Icons.Rounded.LocalFireDepartment
                          else Icons.Rounded.Lock,
                        contentDescription = null,
                        tint =
                          if (challenge.isHardcoreMode) MaterialTheme.colorScheme.error
                          else MaterialTheme.colorScheme.primary,
                      )
                      Text(
                        text = "Hardcore Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                      )
                    }
                    Surface(
                      color =
                        if (challenge.isHardcoreMode) MaterialTheme.colorScheme.error
                        else if (uiState.hardcoreEligible) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                      shape = CircleShape,
                    ) {
                      Text(
                        text =
                          when {
                            challenge.isHardcoreMode -> "ACTIVE"
                            uiState.hardcoreEligible -> "UNLOCKED"
                            else -> "LOCKED"
                          },
                        style = MaterialTheme.typography.labelSmall,
                        color =
                          if (challenge.isHardcoreMode) MaterialTheme.colorScheme.onError
                          else if (uiState.hardcoreEligible) MaterialTheme.colorScheme.onSecondary
                          else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                      )
                    }
                  }

                  if (challenge.isHardcoreMode) {
                    Surface(
                      color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                      shape = MaterialTheme.shapes.medium,
                      modifier = Modifier.fillMaxWidth().testTag("hardcore_purrs_wallet_card"),
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Text(
                          text = "Hardcore Purrs Wallet",
                          style = MaterialTheme.typography.labelLarge,
                          color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                          text = "${GameCatalog.formatCompactPurrs(challenge.hardcorePurrs)} HC Purrs",
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.error,
                        )
                      }
                    }
                  }

                  Text(
                    text =
                      "• Separate Currency: Uses Hardcore Purrs & isolated Hardcore Upgrades (only visible inside Hardcore Mode so Standard Purrs cannot be used to cheat!)\n" +
                        "• Shop & Upgrades cost 3.5x+ more (steeper exponential scaling)\n" +
                        "• Rebirths cost 2.0x more Hardcore Purrs\n" +
                        "• 0.75x Purr Earnings (-25%) & 2x Faster Combo Decay",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                      if (challenge.isHardcoreMode) MaterialTheme.colorScheme.onErrorContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                  )

                  Text(
                    text = "High Entry Requirements:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                  )
                  RequirementRow(
                    label = "1,000,000 Lifetime Purrs",
                    met =
                      challenge.hardcoreUnlocked ||
                        profile.totalPurrsEarned >= GameCatalog.HARDCORE_REQ_PURRS,
                    progressText =
                      "${GameCatalog.formatCompactPurrs(profile.totalPurrsEarned)}/1.00M",
                  )
                  RequirementRow(
                    label = "5,000 Lifetime Pets",
                    met =
                      challenge.hardcoreUnlocked ||
                        profile.totalPets >= GameCatalog.HARDCORE_REQ_PETS,
                    progressText = "${GameCatalog.formatCompactPurrs(profile.totalPets)}/5,000",
                  )
                  RequirementRow(
                    label = "50 Combined Upgrades (or 1 Rebirth)",
                    met =
                      challenge.hardcoreUnlocked ||
                        upgradeTotal >= GameCatalog.HARDCORE_REQ_UPGRADES ||
                        challenge.rebirthCount >= 1L,
                    progressText = "$upgradeTotal/50 Lv",
                  )
                  RequirementRow(
                    label = "5 Unlocked Badges",
                    met =
                      challenge.hardcoreUnlocked ||
                        profile.unlockedBadgeIds.size >= GameCatalog.HARDCORE_REQ_BADGES,
                    progressText = "${profile.unlockedBadgeIds.size}/5",
                  )

                  Button(
                    onClick = onToggleHardcoreMode,
                    enabled = uiState.hardcoreEligible,
                    colors =
                      if (challenge.isHardcoreMode) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                      } else {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                      },
                    modifier =
                      Modifier.fillMaxWidth().height(48.dp).testTag("toggle_hardcore_button"),
                  ) {
                    Text(
                      text =
                        when {
                          !uiState.hardcoreEligible -> "Meet High Requirements to Unlock"
                          challenge.isHardcoreMode -> "Exit Hardcore Mode"
                          else -> "Enter Hardcore Mode"
                        }
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

@Composable
private fun GatoAscensionCard(
  challenge: ChallengeProgressState,
  canAscend: Boolean,
  onPerformGatoAscension: () -> Unit,
  onViewTree: (() -> Unit)?,
) {
  val progress =
    (challenge.rebirthCount.toFloat() / GameCatalog.ASCENSION_REQUIRED_REBIRTHS.toFloat())
      .coerceIn(0f, 1f)
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    shape = MaterialTheme.shapes.large,
    modifier = Modifier.fillMaxWidth().testTag("gato_ascension_card"),
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
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Diamond,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
          )
          Text(
            text = "Gato Ascensión",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
          )
        }
        Surface(
          color = MaterialTheme.colorScheme.tertiary,
          shape = CircleShape,
        ) {
          Text(
            text = "${challenge.gatoShards} Shards • #${challenge.ascensionCount}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
          )
        }
      }

      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(7.dp),
        strokeCap = StrokeCap.Round,
      )

      Text(
        text =
          "• Costs 5 Rebirths (${challenge.rebirthCount}/5 ready)\n" +
            "• Acts like a Rebirth, resets Rebirths back to 1 and bonus multipliers back to 0\n" +
            "• Grants +1 Gato Shard (Ascension Tree) & +${GameCatalog.ASCENSION_GEM_REWARD} Rare Gems!",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          onClick = onPerformGatoAscension,
          enabled = canAscend,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
          modifier =
            Modifier.weight(1f).height(48.dp).testTag("perform_gato_ascension_button"),
        ) {
          Text(
            text =
              if (canAscend) "Ascend (+1 Shard, +${GameCatalog.ASCENSION_GEM_REWARD} Gems)"
              else "Need 5 Rebirths",
            maxLines = 1,
          )
        }
        if (onViewTree != null) {
          FilledTonalButton(
            onClick = onViewTree,
            modifier = Modifier.height(48.dp).testTag("open_ascension_tree_button"),
          ) {
            Text("Tree →", maxLines = 1)
          }
        }
      }
    }
  }
}

@Composable
private fun TreeBranchConnector() {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = Icons.Rounded.ArrowDownward,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
      modifier = Modifier.size(18.dp),
    )
  }
}

@Composable
private fun AscensionNodeCard(
  node: AscensionUpgradeDef,
  challenge: ChallengeProgressState,
  onUnlock: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isOwned = node.id in challenge.unlockedAscensionIds
  val (canUnlock, reason) = GameCatalog.canUnlockAscensionNode(node, challenge)
  Card(
    colors =
      CardDefaults.cardColors(
        containerColor =
          when {
            isOwned -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            canUnlock -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
      ),
    border =
      if (isOwned) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
      else if (canUnlock) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
      else null,
    modifier = modifier.fillMaxWidth().testTag("ascension_node_${node.id}"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(11.dp),
      verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = node.name,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f),
        )
        Surface(
          color =
            if (isOwned) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.tertiaryContainer,
          shape = CircleShape,
        ) {
          Text(
            text = if (isOwned) "OWNED" else "T${node.tier}",
            style = MaterialTheme.typography.labelSmall,
            color =
              if (isOwned) MaterialTheme.colorScheme.onPrimary
              else MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
          )
        }
      }
      Text(
        text = node.effectSummary,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = node.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Button(
        onClick = onUnlock,
        enabled = canUnlock,
        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("unlock_ascension_${node.id}"),
      ) {
        Text(
          text =
            when {
              isOwned -> "Active"
              canUnlock -> "Unlock (${node.shardCost} Shard)"
              else -> reason ?: "${node.shardCost} Shards"
            },
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun RequirementRow(
  label: String,
  met: Boolean,
  progressText: String,
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
        imageVector = if (met) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
        contentDescription = null,
        tint = if (met) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(15.dp),
      )
      Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
    Text(
      text = progressText,
      style = MaterialTheme.typography.labelSmall,
      color = if (met) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = FontWeight.SemiBold,
    )
  }
}
