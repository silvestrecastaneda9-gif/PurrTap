package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Dehaze
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Looks
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MoonlightEventState
import com.example.data.model.MoonlightPhase
import com.example.data.weather.LiveWeatherState
import com.example.data.weather.WeatherConditionKind
import com.example.data.weather.WeatherRepository

private data class WeatherBuffGroup(
  val title: String,
  val conditions: List<WeatherConditionKind>,
)

private val weatherBuffGroups =
  listOf(
    WeatherBuffGroup(
      title = "Warm & Fair Skies",
      conditions =
        listOf(
          WeatherConditionKind.SUNNY,
          WeatherConditionKind.CLOUDY,
          WeatherConditionKind.SUNSET_GAZE,
        ),
    ),
    WeatherBuffGroup(
      title = "Rain, Mist & Storms",
      conditions =
        listOf(
          WeatherConditionKind.RAINY,
          WeatherConditionKind.FOGGY_MIST,
          WeatherConditionKind.STORMY,
          WeatherConditionKind.RAINBOW_SHOWER,
        ),
    ),
    WeatherBuffGroup(
      title = "Seasonal & Celestial Skies",
      conditions =
        listOf(
          WeatherConditionKind.CHERRY_BREEZE,
          WeatherConditionKind.AUTUMN_LEAVES,
          WeatherConditionKind.SNOWY,
          WeatherConditionKind.AURORA_BOREALIS,
          WeatherConditionKind.METEOR_SHOWER,
        ),
    ),
  )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSyncSheet(
  weather: LiveWeatherState,
  moonlightState: MoonlightEventState = MoonlightEventState(),
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val elapsedFraction =
    ((WeatherRepository.WEATHER_INTERVAL_SECONDS - weather.nextShiftSeconds).toFloat() /
        WeatherRepository.WEATHER_INTERVAL_SECONDS.toFloat())
      .coerceIn(0f, 1f)

  val moonlightPhase = moonlightState.phase
  val moonlightContainer =
    when (moonlightPhase) {
      MoonlightPhase.BLOODMOON -> MaterialTheme.colorScheme.errorContainer
      MoonlightPhase.MOONLIGHT -> MaterialTheme.colorScheme.tertiaryContainer
      MoonlightPhase.INACTIVE -> MaterialTheme.colorScheme.surfaceVariant
    }
  val moonlightContent =
    when (moonlightPhase) {
      MoonlightPhase.BLOODMOON -> MaterialTheme.colorScheme.onErrorContainer
      MoonlightPhase.MOONLIGHT -> MaterialTheme.colorScheme.onTertiaryContainer
      MoonlightPhase.INACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "Sanctuary Sky & Moonlight",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text =
          "Atmosphere shifts every 60 seconds across 12 skies. Night cycles also trigger Moonlight or Bloodmoon multipliers below.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      // 1. Current Active Sky Card (Hover or hold for full buff details)
      ItemHoverTooltipBox(
        title = "${weather.condition.label} • ${weather.condition.buffTitle}",
        description = weather.condition.buffDescription,
        limitedTag = "Active Sky",
        modifier = Modifier.fillMaxWidth(),
      ) {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth().testTag("weather_active_sky_card"),
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
              Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = sheetWeatherIcon(weather.condition),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp),
                  )
                }
              }
              Column {
                Text(
                  text = "${weather.condition.label} • ${weather.temperatureC.toInt()}°C",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                  text = "${weather.condition.buffTitle} • Hover or hold for details",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                )
              }
            }
          }
        }
      }

      // 2. Moonlight / Bloodmoon Status Card (Hover or hold for details)
      ItemHoverTooltipBox(
        title = moonlightPhase.title,
        description =
          if (moonlightPhase == MoonlightPhase.INACTIVE) {
            "Nightly celestial cycle with a chance for Moonlight (1.75x) or rare Bloodmoon (2.5x)."
          } else {
            "${moonlightPhase.boostLabel} active under the night sky!"
          },
        limitedTag = if (moonlightPhase == MoonlightPhase.BLOODMOON) "33% Rare" else "Nightly",
        modifier = Modifier.fillMaxWidth(),
      ) {
        Card(
          colors = CardDefaults.cardColors(containerColor = moonlightContainer),
          border =
            if (moonlightPhase == MoonlightPhase.BLOODMOON) {
              BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
            } else null,
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth().testTag("weather_moonlight_card"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.Rounded.NightsStay,
                contentDescription = null,
                tint =
                  if (moonlightPhase == MoonlightPhase.BLOODMOON) MaterialTheme.colorScheme.error
                  else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
              Column {
                Text(
                  text = moonlightPhase.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = moonlightContent,
                )
                Text(
                  text = "Hover or hold for details",
                  style = MaterialTheme.typography.labelSmall,
                  color = moonlightContent.copy(alpha = 0.78f),
                )
              }
            }
            Surface(
              color =
                when (moonlightPhase) {
                  MoonlightPhase.BLOODMOON -> MaterialTheme.colorScheme.error
                  MoonlightPhase.MOONLIGHT -> MaterialTheme.colorScheme.primary
                  MoonlightPhase.INACTIVE -> MaterialTheme.colorScheme.surface
                },
              shape = CircleShape,
            ) {
              Text(
                text =
                  when (moonlightPhase) {
                    MoonlightPhase.BLOODMOON -> "2.5x • RARE"
                    MoonlightPhase.MOONLIGHT -> "1.75x ACTIVE"
                    MoonlightPhase.INACTIVE -> "NIGHTLY"
                  },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color =
                  when (moonlightPhase) {
                    MoonlightPhase.BLOODMOON -> MaterialTheme.colorScheme.onError
                    MoonlightPhase.MOONLIGHT -> MaterialTheme.colorScheme.onPrimary
                    MoonlightPhase.INACTIVE -> MaterialTheme.colorScheme.onSurface
                  },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
          }
        }
      }

      // 3. Organized Weather Forecast Groups
      Text(
        text = "12 Sky Conditions & Buffs",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )

      weatherBuffGroups.forEach { group ->
        Card(
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            ),
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = group.title,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
            group.conditions.forEach { kind ->
              val isCurrent = kind == weather.condition
              ItemHoverTooltipBox(
                title = "${kind.label} • ${kind.buffTitle}",
                description = kind.buffDescription,
                limitedTag = if (isCurrent) "Active" else null,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Surface(
                  color =
                    if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                  border =
                    if (isCurrent) BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
                    else null,
                  shape = MaterialTheme.shapes.medium,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      modifier = Modifier.weight(1f).padding(end = 8.dp),
                    ) {
                      Icon(
                        imageVector = sheetWeatherIcon(kind),
                        contentDescription = null,
                        tint =
                          if (isCurrent) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                      )
                      Text(
                        text = if (isCurrent) "${kind.label} (Active)" else kind.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                      )
                    }
                    Text(
                      text = "Hover/hold",
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

      Spacer(modifier = Modifier.height(14.dp))
    }
  }
}

private fun sheetWeatherIcon(condition: WeatherConditionKind) =
  when (condition) {
    WeatherConditionKind.SUNNY -> Icons.Rounded.WbSunny
    WeatherConditionKind.CLOUDY -> Icons.Rounded.Cloud
    WeatherConditionKind.RAINY -> Icons.Rounded.WaterDrop
    WeatherConditionKind.SNOWY -> Icons.Rounded.AcUnit
    WeatherConditionKind.STORMY -> Icons.Rounded.Thunderstorm
    WeatherConditionKind.SUNSET_GAZE -> Icons.Rounded.WbTwilight
    WeatherConditionKind.CHERRY_BREEZE -> Icons.Rounded.LocalFlorist
    WeatherConditionKind.FOGGY_MIST -> Icons.Rounded.Dehaze
    WeatherConditionKind.RAINBOW_SHOWER -> Icons.Rounded.Looks
    WeatherConditionKind.METEOR_SHOWER -> Icons.Rounded.AutoAwesome
    WeatherConditionKind.AURORA_BOREALIS -> Icons.Rounded.AutoFixHigh
    WeatherConditionKind.AUTUMN_LEAVES -> Icons.Rounded.Eco
  }
