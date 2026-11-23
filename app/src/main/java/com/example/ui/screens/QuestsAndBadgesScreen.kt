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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BadgeDef
import com.example.data.model.GameCatalog
import com.example.data.model.QuestDef
import com.example.ui.components.SubtleGlitchLoreWhisper
import com.example.ui.viewmodel.PurrGameUiState

private data class EvaluatedQuest(
  val quest: QuestDef,
  val progress: Long,
  val isClaimed: Boolean,
  val canClaim: Boolean,
  val badgeName: String?,
) {
  val ratio: Float
    get() = (progress.toFloat() / quest.target.toFloat()).coerceIn(0f, 1f)
}

@Composable
fun QuestsAndBadgesScreen(
  uiState: PurrGameUiState,
  onClaimQuest: (String) -> Unit,
  onEquipBadge: (String) -> Unit,
) {
  val profile = uiState.profile
  var showAchievementsDialog by rememberSaveable { mutableStateOf(false) }

  val evaluatedQuests =
    remember(profile, uiState.challengeState, uiState.playerStats) {
      GameCatalog.quests.map { quest ->
        val progress =
          quest.progressExtractor(profile, uiState.challengeState, uiState.playerStats)
            .coerceAtMost(quest.target)
        val isClaimed = quest.id in profile.claimedQuestIds
        val canClaim = !isClaimed && progress >= quest.target
        val badgeName = quest.badgeId?.let { id -> GameCatalog.badges.find { it.id == id }?.name }
        EvaluatedQuest(
          quest = quest,
          progress = progress,
          isClaimed = isClaimed,
          canClaim = canClaim,
          badgeName = badgeName,
        )
      }
    }

  val claimedCount = evaluatedQuests.count { it.isClaimed }
  val claimableCount = evaluatedQuests.count { it.canClaim }
  val totalCount = evaluatedQuests.size
  val equippedBadge = GameCatalog.badges.find { it.id == profile.equippedBadgeId }

  val spotlightQuests =
    remember(evaluatedQuests) {
      val ready = evaluatedQuests.filter { it.canClaim }
      if (ready.isNotEmpty()) {
        ready.take(2)
      } else {
        evaluatedQuests
          .filter { !it.isClaimed }
          .sortedByDescending { it.ratio }
          .take(2)
      }
    }

  BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    val isNarrow = maxWidth < 360.dp
    val horizontalPad = if (isNarrow) 10.dp else 16.dp

    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 640.dp),
      contentPadding = PaddingValues(horizontal = horizontalPad, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Quests & Achievements",
            style =
              if (isNarrow) MaterialTheme.typography.titleLarge
              else MaterialTheme.typography.headlineSmall,
          )
          Text(
            text =
              "Track Sanctuary milestones, claim Purrs & Rare Gems, and equip Leaderboard Badges.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      item {
        Card(
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            ),
          border =
            if (claimableCount > 0) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            else null,
          modifier = Modifier.fillMaxWidth().testTag("achievements_hub_card"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = MaterialTheme.shapes.medium,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp).size(26.dp),
                  )
                }
                Column {
                  Text(
                    text = "Sanctuary Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text =
                      "$claimedCount/$totalCount Claimed • ${profile.unlockedBadgeIds.size}/${GameCatalog.badges.size} Badges",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              if (claimableCount > 0) {
                Surface(
                  color = MaterialTheme.colorScheme.primary,
                  shape = MaterialTheme.shapes.small,
                ) {
                  Text(
                    text = "$claimableCount Ready",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  )
                }
              }
            }

            LinearProgressIndicator(
              progress = {
                if (totalCount > 0) (claimedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
                else 0f
              },
              modifier = Modifier.fillMaxWidth().height(8.dp),
              strokeCap = StrokeCap.Round,
            )

            Surface(
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.MilitaryTech,
                    contentDescription = null,
                    tint =
                      if (equippedBadge != null) MaterialTheme.colorScheme.tertiary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                  )
                  Column {
                    Text(
                      text = "Equipped Leaderboard Badge",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                      text = equippedBadge?.name ?: "None equipped yet",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }
                if (equippedBadge != null) {
                  Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small,
                  ) {
                    Text(
                      text = equippedBadge.rarity,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onTertiaryContainer,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                  }
                }
              }
            }

            Button(
              onClick = { showAchievementsDialog = true },
              modifier =
                Modifier.fillMaxWidth().height(52.dp).testTag("open_achievements_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text =
                  if (claimableCount > 0) {
                    "Achievements & Badges ($claimableCount Ready)"
                  } else {
                    "Achievements & Badges ($claimedCount/$totalCount)"
                  },
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = if (claimableCount > 0) "Ready to Claim" else "Up Next",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          TextButton(
            onClick = { showAchievementsDialog = true },
            modifier = Modifier.heightIn(min = 48.dp).testTag("see_all_achievements_button"),
          ) {
            Text(text = "See All $totalCount")
          }
        }
      }

      if (spotlightQuests.isEmpty()) {
        item {
          Card(
            colors =
              CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
              ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
              )
              Column {
                Text(
                  text = "All $totalCount Achievements Complete!",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "Open Achievements & Badges above to browse and equip any badge.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      } else {
        items(spotlightQuests, key = { "spotlight_${it.quest.id}" }) { evaluated ->
          AchievementQuestCard(
            evaluated = evaluated,
            onClaimQuest = onClaimQuest,
            tagPrefix = "spotlight_quest",
          )
        }
      }

      item {
        SubtleGlitchLoreWhisper(channel = "quests")
      }
    }
  }

  if (showAchievementsDialog) {
    AchievementsDialog(
      evaluatedQuests = evaluatedQuests,
      unlockedBadgeIds = profile.unlockedBadgeIds,
      equippedBadgeId = profile.equippedBadgeId,
      onClaimQuest = onClaimQuest,
      onEquipBadge = onEquipBadge,
      onDismiss = { showAchievementsDialog = false },
    )
  }
}

@Composable
private fun AchievementQuestCard(
  evaluated: EvaluatedQuest,
  onClaimQuest: (String) -> Unit,
  tagPrefix: String = "quest_card",
) {
  val quest = evaluated.quest
  Card(
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (evaluated.canClaim) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
          else MaterialTheme.colorScheme.surfaceVariant
      ),
    modifier = Modifier.fillMaxWidth().testTag("${tagPrefix}_${quest.id}"),
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
            imageVector =
              if (evaluated.isClaimed) Icons.Rounded.CheckCircle else Icons.Rounded.TaskAlt,
            contentDescription = null,
            tint =
              if (evaluated.isClaimed) MaterialTheme.colorScheme.secondary
              else MaterialTheme.colorScheme.primary,
          )
          Column {
            Text(
              text = quest.title,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = quest.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
          onClick = { onClaimQuest(quest.id) },
          enabled = evaluated.canClaim,
          modifier = Modifier.height(48.dp).testTag("claim_quest_${quest.id}"),
        ) {
          Text(
            text =
              when {
                evaluated.isClaimed -> "Claimed"
                evaluated.canClaim -> "Claim +${GameCatalog.formatCompactPurrs(quest.rewardPurrs)}"
                else ->
                  "${GameCatalog.formatCompactPurrs(evaluated.progress)}/${GameCatalog.formatCompactPurrs(quest.target)}"
              },
            maxLines = 1,
          )
        }
      }

      LinearProgressIndicator(
        progress = { evaluated.ratio },
        modifier = Modifier.fillMaxWidth().height(8.dp),
        strokeCap = StrokeCap.Round,
      )

      @OptIn(ExperimentalLayoutApi::class)
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "+${GameCatalog.formatCompactPurrs(quest.rewardPurrs)} Purrs",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
          )
          if (quest.rewardGems > 0L) {
            Icon(
              imageVector = Icons.Rounded.Diamond,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = "+${quest.rewardGems} Gems",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.tertiary,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }

        if (evaluated.badgeName != null) {
          Text(
            text = "Badge: ${evaluated.badgeName}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
private fun AchievementsDialog(
  evaluatedQuests: List<EvaluatedQuest>,
  unlockedBadgeIds: List<String>,
  equippedBadgeId: String,
  onClaimQuest: (String) -> Unit,
  onEquipBadge: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var selectedTab by rememberSaveable { mutableIntStateOf(0) }
  val claimableQuests = remember(evaluatedQuests) { evaluatedQuests.filter { it.canClaim } }
  val sortedAllQuests =
    remember(evaluatedQuests) {
      evaluatedQuests.sortedWith(
        compareBy<EvaluatedQuest> {
          when {
            it.canClaim -> 0
            !it.isClaimed -> 1
            else -> 2
          }
        }
          .thenByDescending { it.ratio }
      )
    }
  val sortedBadges =
    remember(unlockedBadgeIds, equippedBadgeId) {
      GameCatalog.badges.sortedWith(
        compareBy<BadgeDef> {
          when {
            it.id == equippedBadgeId -> 0
            it.id in unlockedBadgeIds -> 1
            else -> 2
          }
        }
      )
    }
  val claimedCount = evaluatedQuests.count { it.isClaimed }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      tonalElevation = 6.dp,
      modifier =
        Modifier.fillMaxWidth(0.95f)
          .widthIn(max = 600.dp)
          .heightIn(max = 720.dp)
          .testTag("achievements_dialog"),
    ) {
      Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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
            Icon(
              imageVector = Icons.Rounded.EmojiEvents,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(28.dp),
            )
            Column {
              Text(
                text = "Achievements & Badges",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text =
                  "$claimedCount/${evaluatedQuests.size} Completed • ${unlockedBadgeIds.size}/${GameCatalog.badges.size} Badges",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp).testTag("close_achievements_dialog_button"),
          ) {
            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close achievements")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val tabLabels =
          listOf(
            "All (${evaluatedQuests.size})",
            "Ready (${claimableQuests.size})",
            "Badges (${unlockedBadgeIds.size})",
          )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          tabLabels.forEachIndexed { index, label ->
            SegmentedButton(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              shape = SegmentedButtonDefaults.itemShape(index = index, count = tabLabels.size),
              modifier = Modifier.testTag("achievements_tab_$index"),
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 8.dp),
        ) {
          when (selectedTab) {
            0 -> {
              items(sortedAllQuests, key = { it.quest.id }) { evaluated ->
                AchievementQuestCard(evaluated = evaluated, onClaimQuest = onClaimQuest)
              }
            }
            1 -> {
              if (claimableQuests.isEmpty()) {
                item {
                  Card(
                    colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Column(
                      modifier = Modifier.fillMaxWidth().padding(20.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                      )
                      Text(
                        text = "No achievements ready to claim right now.",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                      )
                      Text(
                        text = "Keep petting your cat, ascending, and collecting Gems!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                }
              } else {
                items(claimableQuests, key = { it.quest.id }) { evaluated ->
                  AchievementQuestCard(evaluated = evaluated, onClaimQuest = onClaimQuest)
                }
              }
            }
            else -> {
              items(sortedBadges, key = { it.id }) { badge ->
                val unlocked = badge.id in unlockedBadgeIds
                val equipped = badge.id == equippedBadgeId
                Card(
                  onClick = { if (unlocked) onEquipBadge(badge.id) },
                  enabled = unlocked,
                  colors =
                    CardDefaults.cardColors(
                      containerColor =
                        when {
                          equipped -> MaterialTheme.colorScheme.tertiaryContainer
                          unlocked -> MaterialTheme.colorScheme.secondaryContainer
                          else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    ),
                  border =
                    if (equipped) BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary) else null,
                  modifier = Modifier.fillMaxWidth().testTag("badge_card_${badge.id}"),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                  ) {
                    Icon(
                      imageVector =
                        if (unlocked) Icons.Rounded.MilitaryTech else Icons.Rounded.Lock,
                      contentDescription = null,
                      tint =
                        if (unlocked) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(28.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                      ) {
                        Text(
                          text = badge.name,
                          style = MaterialTheme.typography.titleSmall,
                          fontWeight = FontWeight.Bold,
                        )
                        Surface(
                          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                          shape = MaterialTheme.shapes.small,
                        ) {
                          Text(
                            text = badge.rarity,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                          )
                        }
                      }
                      Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    Surface(
                      color =
                        if (equipped) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.surface,
                      shape = MaterialTheme.shapes.small,
                    ) {
                      Text(
                        text =
                          when {
                            equipped -> "Equipped"
                            unlocked -> "Tap to Equip"
                            else -> "Locked"
                          },
                        style = MaterialTheme.typography.labelSmall,
                        color =
                          if (equipped) MaterialTheme.colorScheme.onTertiary
                          else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
