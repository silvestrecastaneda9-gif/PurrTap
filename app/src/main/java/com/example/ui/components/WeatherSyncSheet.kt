package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.data.weather.LiveWeatherState
import com.example.data.weather.WeatherConditionKind
import com.example.data.weather.WeatherRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSyncSheet(
  weather: LiveWeatherState,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val elapsedFraction =
    ((WeatherRepository.WEATHER_INTERVAL_SECONDS - weather.nextShiftSeconds).toFloat() /
        WeatherRepository.WEATHER_INTERVAL_SECONDS.toFloat())
      .coerceIn(0f, 1f)

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(text = "Sanctuary Game Weather", style = MaterialTheme.typography.headlineSmall)
      Text(
        text =
          "In-game atmosphere randomly shifts every 60 seconds across 12 skies with no real-world location needed. Each condition transforms your sanctuary and activates a unique Purr buff!",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "${weather.condition.label} • ${weather.temperatureC.toInt()}°C",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
          Text(
            text = "Active Buff: ${weather.condition.buffTitle} — ${weather.condition.buffDescription}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
          LinearProgressIndicator(
            progress = { elapsedFraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            strokeCap = StrokeCap.Round,
          )
          Text(
            text = "Next random weather shift in ${weather.nextShiftSeconds}s",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
          )
        }
      }

      Text(
        text = "12 Game Weather Buffs",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WeatherConditionKind.entries.forEach { kind ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "${kind.label} (${kind.buffTitle})",
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            Text(
              text = kind.buffDescription.substringBefore(" while").substringBefore(" from").substringBefore(" during").substringBefore(" to"),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }
      Text(
        text = "Tip: Midnight until Morning activates Moonlight (1.75x) with a 33% Bloodmoon chance (2.5x)!",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.tertiary,
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
