package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GameCatalog
import com.example.data.model.LeaderboardEntry
import com.example.ui.components.CatAvatarMini
import com.example.ui.viewmodel.PurrGameUiState
import com.example.ui.viewmodel.UiState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LeaderboardScreen(
  currentUserId: String?,
  uiState: PurrGameUiState,
  leaderboardState: UiState<List<LeaderboardEntry>>,
  onClaimWeeklyReward: () -> Unit,
  onSignInClick: () -> Unit,
  onOpenSignInOptions: () -> Unit = onSignInClick,
  isSigningIn: Boolean = false,
) {
  val numberFormat = NumberFormat.getNumberInstance(Locale.US)
  var showTierTable by remember { mutableStateOf(false) }
  val projectedTier = GameCatalog.weeklyTierForRank(uiState.currentPlayerGlobalRank)

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(
      modifier = Modifier.fillMaxSize().widthIn(max = 640.dp).padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Global Feline Leaderboard", style = MaterialTheme.typography.headlineSmall)
          Text(
            text = "Top dedicated caretakers ranked by lifetime Purrs earned.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Icon(
          imageVector = Icons.Rounded.EmojiEvents,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.size(30.dp),
        )
      }

      // Weekly Leaderboard Rewards Banner
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().testTag("weekly_rewards_card"),
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
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.Rounded.CardGiftcard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp),
              )
              Text(
                text = "Weekly Leaderboard Rewards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
            }
            Surface(
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
              shape = MaterialTheme.shapes.small,
            ) {
              Text(
                text = "Resets in ${uiState.weeklyResetCountdownText}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }

          Text(
            text =
              buildString {
                if (uiState.currentPlayerGlobalRank != null) {
                  append("Current Rank #${uiState.currentPlayerGlobalRank} • ${projectedTier.title}: ")
                } else {
                  append("${projectedTier.title}: ")
                }
                append("+${GameCatalog.formatCompactPurrs(projectedTier.purrsReward)} ${uiState.activeCurrencyShortLabel}")
                if (projectedTier.gemsReward > 0L) append(" + ${projectedTier.gemsReward} Gems")
                if (projectedTier.shardsReward > 0L) append(" + ${projectedTier.shardsReward} Shard(s)")
                if (projectedTier.bonusUpgradeLevels > 0L) append(" + ${projectedTier.bonusUpgradeLevels} Upgrade Lv")
              },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            TextButton(
              onClick = { showTierTable = !showTierTable },
              modifier = Modifier.testTag("toggle_weekly_tiers_button"),
            ) {
              Text(if (showTierTable) "Hide Rank Tiers" else "View Rank Tiers")
              Icon(
                imageVector =
                  if (showTierTable) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
            }
            Button(
              onClick = onClaimWeeklyReward,
              enabled = uiState.canClaimWeeklyReward,
              modifier = Modifier.height(44.dp).testTag("claim_weekly_reward_button"),
            ) {
              Text(
                text =
                  if (uiState.canClaimWeeklyReward) "Claim Weekly Reward"
                  else "Claimed (${uiState.playerStats.weeklyRewardsClaimedCount}x)",
                maxLines = 1,
              )
            }
          }

          AnimatedVisibility(visible = showTierTable) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              GameCatalog.weeklyLeaderboardTiers.forEach { tier ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = "${tier.rankLabel} ${tier.title}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                  )
                  Text(
                    text =
                      buildString {
                        append("+${GameCatalog.formatCompactPurrs(tier.purrsReward)}")
                        if (tier.gemsReward > 0L) append(" • ${tier.gemsReward} Gems")
                        if (tier.shardsReward > 0L) append(" • ${tier.shardsReward} Shard")
                        if (tier.bonusUpgradeLevels > 0L) append(" • +${tier.bonusUpgradeLevels} Lv")
                      },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          }
        }
      }

      if (currentUserId == null) {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          modifier = Modifier.fillMaxWidth().testTag("optional_sign_in_leaderboard_card"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.CloudUpload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = "Optional Cloud Sync & Ranks",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
            Text(
              text =
                "You're playing locally with no sign-in required — your Purrs, upgrades, and cats are saved on this device. Sign in with Google anytime to back up your sanctuary and join the Global Leaderboard!",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
            )
            Button(
              onClick = onSignInClick,
              enabled = !isSigningIn,
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("leaderboard_google_sign_in_button"),
            ) {
              if (isSigningIn) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.onPrimary,
                )
              } else {
                Text("Sign in with Google to Sync Ranks")
              }
            }
            OutlinedButton(
              onClick = onOpenSignInOptions,
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("leaderboard_other_sign_in_button"),
            ) {
              Text("Other Sign-In Options (Local Account & Save Backup)")
            }
          }
        }
      } else {
        when (leaderboardState) {
          is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
          }
          is UiState.Error -> {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = leaderboardState.message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(16.dp),
              )
            }
          }
          is UiState.Success -> {
            val entries = leaderboardState.data
            if (entries.isEmpty()) {
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(
                  text = "Pet your cat to appear on the global leaderboard!",
                  style = MaterialTheme.typography.bodyLarge,
                  modifier = Modifier.padding(20.dp),
                )
              }
            } else {
              LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("leaderboard_list"),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                itemsIndexed(entries, key = { _, entry -> entry.userId }) { index, entry ->
                  val isSelf = entry.userId == currentUserId
                  val rank = index + 1
                  val badgeName =
                    GameCatalog.badges.find { it.id == entry.equippedBadgeId }?.name

                  Card(
                    colors =
                      CardDefaults.cardColors(
                        containerColor =
                          if (isSelf) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                          else MaterialTheme.colorScheme.surfaceVariant
                      ),
                    border =
                      if (isSelf) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth().padding(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                      Surface(
                        shape = CircleShape,
                        color =
                          when (rank) {
                            1 -> Color(0xFFFFD54F)
                            2 -> Color(0xFFE0E0E0)
                            3 -> Color(0xFFFFB74D)
                            else -> MaterialTheme.colorScheme.surface
                          },
                        modifier = Modifier.size(36.dp),
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF241915),
                          )
                        }
                      }

                      CatAvatarMini(
                        patternId = entry.catPatternId,
                        accessoryId = entry.catAccessoryId,
                        modifier = Modifier.size(48.dp),
                      )

                      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                          Text(
                            text = if (isSelf) "${entry.displayName} (You)" else entry.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelf) FontWeight.Bold else FontWeight.SemiBold,
                          )
                        }
                        Text(
                          text = "Cat: ${entry.catName} • ${numberFormat.format(entry.totalPets)} pets",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (badgeName != null) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                          ) {
                            Icon(
                              imageVector = Icons.Rounded.MilitaryTech,
                              contentDescription = null,
                              tint = MaterialTheme.colorScheme.tertiary,
                              modifier = Modifier.size(14.dp),
                            )
                            Text(
                              text = badgeName,
                              style = MaterialTheme.typography.labelSmall,
                              color = MaterialTheme.colorScheme.tertiary,
                              fontWeight = FontWeight.SemiBold,
                            )
                          }
                        }
                      }

                      Column(horizontalAlignment = Alignment.End) {
                        Text(
                          text = numberFormat.format(entry.totalPurrsEarned),
                          style = MaterialTheme.typography.titleMedium,
                          color = MaterialTheme.colorScheme.primary,
                          fontWeight = FontWeight.Bold,
                        )
                        Text(
                          text = "Purrs",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
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
      Spacer(modifier = Modifier.height(4.dp))
    }
  }
}
