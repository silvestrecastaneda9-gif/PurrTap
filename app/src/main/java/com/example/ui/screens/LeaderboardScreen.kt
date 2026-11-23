package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.GameCatalog
import com.example.data.model.LeaderboardEntry
import com.example.ui.components.CatAvatarMini
import com.example.ui.components.LocalPurrFeedback
import com.example.ui.components.rememberBase64ImageBitmap
import com.example.ui.viewmodel.PurrGameUiState
import com.example.ui.viewmodel.UiState
import kotlin.math.roundToLong

@Composable
fun LeaderboardScreen(
  currentUserId: String?,
  uiState: PurrGameUiState,
  leaderboardState: UiState<List<LeaderboardEntry>>,
  onClaimWeeklyReward: () -> Unit,
  onSignInClick: () -> Unit,
  onOpenSignInOptions: () -> Unit = onSignInClick,
  onSelectPlayerProfile: (LeaderboardEntry, Int?) -> Unit = { _, _ -> },
  onOpenOwnProfileEditor: () -> Unit = {},
  isSigningIn: Boolean = false,
) {
  val feedback = LocalPurrFeedback.current
  var showTierTable by remember { mutableStateOf(false) }
  val projectedTier = GameCatalog.weeklyTierForRank(uiState.currentPlayerGlobalRank)

  BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    val isNarrow = maxWidth < 360.dp
    val horizontalPad = if (isNarrow) 12.dp else 16.dp

    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 660.dp)
          .padding(horizontal = horizontalPad, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Sleek, uncluttered Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f).padding(end = 8.dp),
        ) {
          Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(40.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = "Leaderboard Trophy",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(22.dp),
              )
            }
          }
          Column {
            Text(
              text = "Leaderboard",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "Tap any caretaker to view their profile",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        FilledTonalButton(
          onClick = {
            feedback.tap()
            onOpenOwnProfileEditor()
          },
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
          modifier = Modifier.height(48.dp).testTag("leaderboard_my_profile_button"),
        ) {
          Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("My Profile", maxLines = 1)
        }
      }

      // Unified Smooth Scrolling Body so Rankings have full room to breathe
      val scrollEntries =
        if (currentUserId != null && leaderboardState is UiState.Success) leaderboardState.data
        else emptyList()
      val usePodium = currentUserId != null && scrollEntries.size >= 3 && !isNarrow
      val listStartIndex = if (usePodium) 3 else 0
      val selfIndex =
        if (currentUserId != null) scrollEntries.indexOfFirst { it.userId == currentUserId } else -1

      LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("leaderboard_list"),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // 1. Compact Weekly Rewards Card
        item(key = "weekly_rewards_banner") {
          Card(
            colors =
              CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
              ),
            border =
              BorderStroke(
                1.dp,
                if (uiState.canClaimWeeklyReward) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
              ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().testTag("weekly_rewards_card"),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
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
                  modifier = Modifier.weight(1f).padding(end = 6.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.CardGiftcard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = "Weekly Rewards",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                Surface(
                  color = MaterialTheme.colorScheme.surface,
                  shape = CircleShape,
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Schedule,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(12.dp),
                    )
                    Text(
                      text = uiState.weeklyResetCountdownText,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                    )
                  }
                }
              }

              // Projected reward summary strip
              Text(
                text =
                  buildString {
                    if (uiState.currentPlayerGlobalRank != null) {
                      append("Rank #${uiState.currentPlayerGlobalRank} • ")
                    }
                    append("${projectedTier.title}: +${GameCatalog.formatCompactPurrs(projectedTier.purrsReward)} ${uiState.activeCurrencyShortLabel}")
                    if (projectedTier.gemsReward > 0L) append(" • +${projectedTier.gemsReward} Gems")
                    if (projectedTier.shardsReward > 0L) append(" • +${projectedTier.shardsReward} Shards")
                    if (projectedTier.bonusUpgradeLevels > 0L) append(" • +${projectedTier.bonusUpgradeLevels} Lv")
                  },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                TextButton(
                  onClick = {
                    feedback.tap()
                    showTierTable = !showTierTable
                  },
                  modifier = Modifier.heightIn(min = 48.dp).testTag("toggle_weekly_tiers_button"),
                ) {
                  Text(if (showTierTable) "Hide Tiers" else "Rank Tiers", maxLines = 1)
                  Icon(
                    imageVector =
                      if (showTierTable) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                }
                Button(
                  onClick = {
                    feedback.tap()
                    onClaimWeeklyReward()
                  },
                  enabled = uiState.canClaimWeeklyReward,
                  contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                  modifier = Modifier.height(48.dp).testTag("claim_weekly_reward_button"),
                ) {
                  Text(
                    text =
                      if (uiState.canClaimWeeklyReward) "Claim Weekly"
                      else "Claimed (${uiState.playerStats.weeklyRewardsClaimedCount}x)",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }

              AnimatedVisibility(visible = showTierTable) {
                Column(
                  modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                  GameCatalog.weeklyLeaderboardTiers.forEach { tier ->
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = "${tier.rankLabel} ${tier.title}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                      )
                      Text(
                        text =
                          buildString {
                            append("+${GameCatalog.formatCompactPurrs(tier.purrsReward)}")
                            if (tier.gemsReward > 0L) append(" • ${tier.gemsReward}G")
                            if (tier.shardsReward > 0L) append(" • ${tier.shardsReward}S")
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
        }

        // 2. Signed-Out Local Self Card & Optional Sync Card
        if (currentUserId == null) {
          item(key = "local_self_preview") {
            val selfProfile = uiState.profile
            val selfPfp = rememberBase64ImageBitmap(selfProfile.avatarBase64)
            val selfBanner = rememberBase64ImageBitmap(selfProfile.bannerBase64)
            val selfEntry =
              LeaderboardEntry(
                userId = selfProfile.userId,
                displayName = selfProfile.displayName,
                catName = selfProfile.selectedCatName,
                catPatternId = selfProfile.selectedPatternId,
                catAccessoryId = selfProfile.selectedAccessoryId,
                equippedBadgeId = selfProfile.equippedBadgeId,
                totalPurrsEarned = selfProfile.totalPurrsEarned,
                totalPets = selfProfile.totalPets,
                badgesCount = selfProfile.unlockedBadgeIds.size.toLong(),
                statsPublic = selfProfile.statsPublic,
                bio = selfProfile.bio,
                avatarBase64 = selfProfile.avatarBase64,
                bannerBase64 = selfProfile.bannerBase64,
                publicRebirths = uiState.challengeState.rebirthCount,
                publicAscensions = uiState.challengeState.ascensionCount,
                publicDailyStreak = selfProfile.dailyStreak,
                publicHighestComboX100 =
                  (uiState.playerStats.highestComboMultiplier * 100.0).roundToLong().coerceAtLeast(100L),
              )
            Card(
              onClick = {
                feedback.tap()
                onSelectPlayerProfile(selfEntry, null)
              },
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
              shape = MaterialTheme.shapes.large,
              modifier = Modifier.fillMaxWidth().testTag("local_self_profile_card"),
            ) {
              Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().height(50.dp)) {
                  if (selfBanner != null) {
                    Image(
                      bitmap = selfBanner,
                      contentDescription = "Your Sanctuary Banner",
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize(),
                    )
                  } else {
                    Box(
                      modifier =
                        Modifier.fillMaxSize()
                          .background(
                            Brush.horizontalGradient(
                              listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer,
                              )
                            )
                          )
                    )
                  }
                  Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                  ) {
                    Text(
                      text = "Local Card",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                  }
                }
                Row(
                  modifier = Modifier.fillMaxWidth().padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  if (selfPfp != null) {
                    Image(
                      bitmap = selfPfp,
                      contentDescription = "${selfProfile.displayName} profile picture",
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.size(48.dp).clip(CircleShape),
                    )
                  } else {
                    CatAvatarMini(
                      patternId = selfProfile.selectedPatternId,
                      accessoryId = selfProfile.selectedAccessoryId,
                      modifier = Modifier.size(48.dp),
                    )
                  }
                  Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Text(
                        text = "${selfProfile.displayName} (You)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                      if (!selfProfile.statsPublic) {
                        Icon(
                          imageVector = Icons.Rounded.Lock,
                          contentDescription = "Private stats",
                          tint = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.size(14.dp),
                        )
                      }
                    }
                    Text(
                      text =
                        selfProfile.bio.ifBlank {
                          "${selfProfile.selectedCatName} • ${GameCatalog.formatCompactPurrs(selfProfile.totalPets)} pets"
                        },
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                  PurrScorePill(amount = selfProfile.totalPurrsEarned, highlighted = true)
                }
              }
            }
          }

          item(key = "optional_sign_in") {
            Card(
              colors =
                CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                ),
              shape = MaterialTheme.shapes.large,
              modifier = Modifier.fillMaxWidth().testTag("optional_sign_in_leaderboard_card"),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                    text = "Optional Cloud Sync & Global Ranks",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                  )
                }
                Text(
                  text =
                    "Playing locally on this device — sign in with Google anytime to back up your sanctuary and compete on the Global Leaderboard.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f),
                )
                Button(
                  onClick = onSignInClick,
                  enabled = !isSigningIn,
                  modifier =
                    Modifier.fillMaxWidth().height(48.dp).testTag("leaderboard_google_sign_in_button"),
                ) {
                  if (isSigningIn) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(20.dp),
                      strokeWidth = 2.dp,
                      color = MaterialTheme.colorScheme.onPrimary,
                    )
                  } else {
                    Text("Sign in with Google to Sync Ranks", maxLines = 1)
                  }
                }
                OutlinedButton(
                  onClick = onOpenSignInOptions,
                  modifier =
                    Modifier.fillMaxWidth().height(48.dp).testTag("leaderboard_other_sign_in_button"),
                ) {
                  Text("Other Sign-In & Save Backup Options", maxLines = 1)
                }
              }
            }
          }
        } else {
          // 3. Signed-In Global Leaderboard States
          when (leaderboardState) {
            is UiState.Loading -> {
              item(key = "lb_loading") {
                Box(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  CircularProgressIndicator()
                }
              }
            }

            is UiState.Error -> {
              item(key = "lb_error") {
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
            }

            is UiState.Success -> {
              val entries = leaderboardState.data
              if (entries.isEmpty()) {
                item(key = "lb_empty") {
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
                }
              } else {
                // Pinned Your Standing Chip Bar
                if (selfIndex >= 0) {
                  val selfEntry = entries[selfIndex]
                  val selfRank = selfIndex + 1
                  item(key = "your_rank_summary") {
                    Surface(
                      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f),
                      border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                      shape = MaterialTheme.shapes.medium,
                      onClick = {
                        feedback.tap()
                        onSelectPlayerProfile(selfEntry, selfRank)
                      },
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp),
                          modifier = Modifier.weight(1f),
                        ) {
                          Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                            Text(
                              text = "#$selfRank",
                              style = MaterialTheme.typography.labelMedium,
                              fontWeight = FontWeight.Bold,
                              color = MaterialTheme.colorScheme.onPrimary,
                              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                          }
                          Text(
                            text = "Your Global Standing • ${selfEntry.displayName}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                          )
                        }
                        Text(
                          text = "${GameCatalog.formatCompactPurrs(selfEntry.totalPurrsEarned)} Purrs",
                          style = MaterialTheme.typography.labelLarge,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.primary,
                        )
                      }
                    }
                  }
                }

                // Top 3 Podium Showcase on standard+ widths
                if (usePodium) {
                  item(key = "top_3_podium") {
                    Row(
                      modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalAlignment = Alignment.Bottom,
                    ) {
                      // Visual podium order: #2 left, #1 center (tallest), #3 right
                      PodiumPlayerCard(
                        entry = entries[1],
                        rank = 2,
                        isSelf = entries[1].userId == currentUserId,
                        accentColor = Color(0xFFB0BEC5),
                        topPadding = 16.dp,
                        onClick = {
                          feedback.tap()
                          onSelectPlayerProfile(entries[1], 2)
                        },
                        modifier = Modifier.weight(1f),
                      )
                      PodiumPlayerCard(
                        entry = entries[0],
                        rank = 1,
                        isSelf = entries[0].userId == currentUserId,
                        accentColor = Color(0xFFFFCA28),
                        topPadding = 0.dp,
                        onClick = {
                          feedback.tap()
                          onSelectPlayerProfile(entries[0], 1)
                        },
                        modifier = Modifier.weight(1.08f),
                      )
                      PodiumPlayerCard(
                        entry = entries[2],
                        rank = 3,
                        isSelf = entries[2].userId == currentUserId,
                        accentColor = Color(0xFFFFA726),
                        topPadding = 22.dp,
                        onClick = {
                          feedback.tap()
                          onSelectPlayerProfile(entries[2], 3)
                        },
                        modifier = Modifier.weight(1f),
                      )
                    }
                  }
                }

                val remainingEntries = entries.drop(listStartIndex)
                itemsIndexed(remainingEntries, key = { _, entry -> entry.userId }) { offset, entry ->
                  val rank = listStartIndex + offset + 1
                  val isSelf = entry.userId == currentUserId
                  CleanLeaderboardRowCard(
                    entry = entry,
                    rank = rank,
                    isSelf = isSelf,
                    onClick = {
                      feedback.tap()
                      onSelectPlayerProfile(entry, rank)
                    },
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

@Composable
private fun PodiumPlayerCard(
  entry: LeaderboardEntry,
  rank: Int,
  isSelf: Boolean,
  accentColor: Color,
  topPadding: androidx.compose.ui.unit.Dp,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val pfpBitmap = rememberBase64ImageBitmap(entry.avatarBase64)
  Card(
    onClick = onClick,
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (isSelf) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
      ),
    border =
      BorderStroke(
        width = if (rank == 1 || isSelf) 2.dp else 1.dp,
        color = if (isSelf) MaterialTheme.colorScheme.primary else accentColor,
      ),
    shape = MaterialTheme.shapes.large,
    modifier =
      modifier.padding(top = topPadding).testTag("leaderboard_entry_${entry.userId}"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Surface(color = accentColor, shape = CircleShape) {
        Text(
          text = "#$rank",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF231808),
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
        )
      }

      if (pfpBitmap != null) {
        Image(
          bitmap = pfpBitmap,
          contentDescription = "${entry.displayName} profile picture",
          contentScale = ContentScale.Crop,
          modifier = Modifier.size(if (rank == 1) 52.dp else 46.dp).clip(CircleShape),
        )
      } else {
        CatAvatarMini(
          patternId = entry.catPatternId,
          accessoryId = entry.catAccessoryId,
          modifier = Modifier.size(if (rank == 1) 52.dp else 46.dp),
        )
      }

      Text(
        text = if (isSelf) "${entry.displayName} (You)" else entry.displayName,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = entry.catName,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      PurrScorePill(amount = entry.totalPurrsEarned, highlighted = rank == 1 || isSelf)
    }
  }
}

@Composable
private fun CleanLeaderboardRowCard(
  entry: LeaderboardEntry,
  rank: Int,
  isSelf: Boolean,
  onClick: () -> Unit,
) {
  val badgeName = GameCatalog.badges.find { it.id == entry.equippedBadgeId }?.name
  val pfpBitmap = rememberBase64ImageBitmap(entry.avatarBase64)

  Card(
    onClick = onClick,
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (isSelf) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
      ),
    border =
      if (isSelf) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
      else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    shape = MaterialTheme.shapes.large,
    modifier = Modifier.fillMaxWidth().testTag("leaderboard_entry_${entry.userId}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Surface(
        shape = CircleShape,
        color =
          when (rank) {
            1 -> Color(0xFFFFCA28)
            2 -> Color(0xFFCFD8DC)
            3 -> Color(0xFFFFB74D)
            else -> MaterialTheme.colorScheme.surface
          },
        modifier = Modifier.size(34.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color =
              if (rank <= 3) Color(0xFF241915) else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
          )
        }
      }

      if (pfpBitmap != null) {
        Image(
          bitmap = pfpBitmap,
          contentDescription = "${entry.displayName} profile picture",
          contentScale = ContentScale.Crop,
          modifier = Modifier.size(46.dp).clip(CircleShape),
        )
      } else {
        CatAvatarMini(
          patternId = entry.catPatternId,
          accessoryId = entry.catAccessoryId,
          modifier = Modifier.size(46.dp),
        )
      }

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
          Text(
            text = if (isSelf) "${entry.displayName} (You)" else entry.displayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (isSelf) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          if (!entry.statsPublic) {
            Icon(
              imageVector = Icons.Rounded.Lock,
              contentDescription = "Private stats",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(13.dp),
            )
          }
        }
        Text(
          text = "${entry.catName} • ${GameCatalog.formatCompactPurrs(entry.totalPets)} pets",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
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
              modifier = Modifier.size(13.dp),
            )
            Text(
              text = badgeName,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.tertiary,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      PurrScorePill(amount = entry.totalPurrsEarned, highlighted = isSelf)
    }
  }
}

@Composable
private fun PurrScorePill(
  amount: Long,
  highlighted: Boolean,
) {
  Surface(
    color =
      if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
      else MaterialTheme.colorScheme.surface,
    shape = CircleShape,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      horizontalAlignment = Alignment.End,
    ) {
      Text(
        text = GameCatalog.formatCompactPurrs(amount),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
      )
      Text(
        text = "Purrs",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
      )
    }
  }
}
