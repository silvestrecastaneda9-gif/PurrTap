package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GameCatalog
import com.example.data.model.OfflineEarningsReport
import com.example.data.model.WeeklyRewardReceipt

@Composable
fun OfflineEarningsDialog(
  report: OfflineEarningsReport,
  catName: String,
  onDismiss: () -> Unit,
) {
  val currencyLabel = if (report.isHardcore) "HC Purrs" else "Purrs"
  val elapsedLabel = GameCatalog.formatElapsedDuration(report.elapsedMillis)

  Dialog(onDismissRequest = onDismiss) {
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = MaterialTheme.shapes.extraLarge,
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier.fillMaxWidth().testTag("offline_earnings_dialog"),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = CircleShape,
          modifier = Modifier.size(56.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Bedtime,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(14.dp),
          )
        }

        Text(
          text = "Welcome Back!",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
        )
        Text(
          text = "$catName was happily purring while you were away.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )

        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = MaterialTheme.shapes.medium,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Schedule,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "Gone for $elapsedLabel",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.testTag("offline_time_gone_text"),
            )
          }
        }

        Card(
          colors =
            CardDefaults.cardColors(
              containerColor =
                if (report.isHardcore) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer
            ),
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "OFFLINE EARNINGS",
              style = MaterialTheme.typography.labelSmall,
              color =
                if (report.isHardcore) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "+${GameCatalog.formatCompactPurrs(report.purrsEarned)} $currencyLabel",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color =
                if (report.isHardcore) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.testTag("offline_earnings_amount_text"),
            )
            Text(
              text =
                if (report.ppsRateUsed > 0L) {
                  "+${GameCatalog.formatCompactPurrs(report.ppsRateUsed)}/s • ${report.efficiencyPercent}% Offline Rate (24h cap)"
                } else {
                  "Buy Per-Second Auto-Purr upgrades to earn while away!"
                },
              style = MaterialTheme.typography.bodySmall,
              color =
                if (report.isHardcore) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
              textAlign = TextAlign.Center,
            )
          }
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("offline_earnings_claim_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.size(8.dp))
          Text(if (report.purrsEarned > 0L) "Collect & Continue" else "Back to Sanctuary")
        }
      }
    }
  }
}

@Composable
fun WeeklyRewardCelebrationDialog(
  receipt: WeeklyRewardReceipt,
  isHardcore: Boolean,
  onDismiss: () -> Unit,
) {
  val currencyLabel = if (isHardcore) "HC Purrs" else "Purrs"
  Dialog(onDismissRequest = onDismiss) {
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = MaterialTheme.shapes.extraLarge,
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier.fillMaxWidth().testTag("weekly_reward_dialog"),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer,
          shape = CircleShape,
          modifier = Modifier.size(56.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.EmojiEvents,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(14.dp),
          )
        }

        Text(
          text = "Weekly Leaderboard Reward!",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
        )
        Text(
          text =
            if (receipt.rankAchieved != null) {
              "You finished Rank #${receipt.rankAchieved} (${receipt.tierTitle})!"
            } else {
              "You earned the ${receipt.tierTitle} payout for this week!"
            },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )

        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "+${GameCatalog.formatCompactPurrs(receipt.purrsEarned)} $currencyLabel",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            if (receipt.gemsEarned > 0L) {
              Text(
                text = "+${receipt.gemsEarned} Rare Gems",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
              )
            }
            if (receipt.shardsEarned > 0L) {
              Text(
                text = "+${receipt.shardsEarned} Gato Shard(s)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }
            if (receipt.bonusUpgradeName != null) {
              Text(
                text = receipt.bonusUpgradeName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
            }
          }
        }

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("weekly_reward_celebration_dismiss"),
        ) {
          Text("Awesome!")
        }
      }
    }
  }
}
