package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.EndlessDailyPayout
import com.example.data.model.GameCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRewardsSheet(
  dailyStreak: Long,
  nextDailyDayIndex: Int,
  nextDailyPayout: EndlessDailyPayout,
  canClaimToday: Boolean,
  currencyShortLabel: String,
  onClaim: () -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val cycleBaseOffset = (nextDailyPayout.cycleIndex - 1L) * 14L

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Endless Daily Prizes", style = MaterialTheme.typography.headlineSmall)
          Text(
            text =
              "Streaks go forever! Cycle #${nextDailyPayout.cycleIndex} (${nextDailyPayout.streakMultiplier}x) • Every 30d grants +1 Gato Shard",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.tertiaryContainer,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.LocalFireDepartment,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "${dailyStreak}d Streak",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.testTag("endless_streak_badge"),
            )
          }
        }
      }

      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Streak Day #${nextDailyPayout.streakDay} Payout",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
          Text(
            text =
              buildString {
                append("+${GameCatalog.formatCompactPurrs(nextDailyPayout.totalPurrs)} $currencyShortLabel")
                if (nextDailyPayout.bonusGems > 0L) append(" • +${nextDailyPayout.bonusGems} Gems")
                if (nextDailyPayout.bonusShards > 0L) append(" • +${nextDailyPayout.bonusShards} Shard")
                if (nextDailyPayout.bonusUpgradeLevel) append(" • +1 Upgrade")
              },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }

      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth().height(330.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(GameCatalog.dailyRewards) { reward ->
          val slotStreakDay = cycleBaseOffset + reward.day
          val slotPayout = GameCatalog.computeEndlessDailyPayout(slotStreakDay)
          val isCurrent = reward.day == nextDailyDayIndex
          val isPast =
            if (canClaimToday) reward.day < nextDailyDayIndex else reward.day <= nextDailyDayIndex
          Card(
            colors =
              CardDefaults.cardColors(
                containerColor =
                  when {
                    isCurrent && canClaimToday -> MaterialTheme.colorScheme.primaryContainer
                    isPast -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                  }
              ),
            border =
              if (isCurrent) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Day $slotStreakDay",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                )
                if (isPast && !(isCurrent && canClaimToday)) {
                  Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Claimed",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp),
                  )
                } else {
                  Icon(
                    imageVector = Icons.Rounded.CardGiftcard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
              Text(
                text = "+${GameCatalog.formatCompactPurrs(slotPayout.totalPurrs)} $currencyShortLabel",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
              )
              val gemNote = if (slotPayout.bonusGems > 0L) " + ${slotPayout.bonusGems} Gems" else ""
              val badgeText =
                when {
                  slotPayout.bonusShards > 0L -> "+${slotPayout.bonusShards} Gato Shard$gemNote!"
                  nextDailyPayout.cycleIndex == 1L && reward.bonusPrizeLabel.isNotBlank() ->
                    "${reward.bonusPrizeLabel}$gemNote"
                  slotPayout.bonusGems > 0L && slotPayout.bonusUpgradeLevel ->
                    "+${slotPayout.bonusGems} Gems • +1 Upgrade Lv"
                  slotPayout.bonusGems > 0L -> "+${slotPayout.bonusGems} Rare Gems"
                  slotPayout.bonusUpgradeLevel -> "+1 Free Upgrade Lv"
                  else -> reward.bonusNote
                }
              if (
                slotPayout.bonusShards > 0L ||
                  slotPayout.bonusGems > 0L ||
                  slotPayout.bonusUpgradeLevel ||
                  (nextDailyPayout.cycleIndex == 1L && reward.bonusPrizeLabel.isNotBlank())
              ) {
                Surface(
                  color = MaterialTheme.colorScheme.tertiaryContainer,
                  shape = MaterialTheme.shapes.small,
                ) {
                  Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  )
                }
              } else {
                Text(
                  text = badgeText,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }

      Button(
        onClick = onClaim,
        enabled = canClaimToday,
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("claim_daily_reward_button"),
      ) {
        Text(
          text =
            if (canClaimToday) {
              "Claim Day ${nextDailyPayout.streakDay} Prize"
            } else {
              "Claimed Day ${nextDailyPayout.streakDay} — Come Back Tomorrow!"
            }
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
