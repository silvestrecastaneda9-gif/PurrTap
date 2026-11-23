package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GameCatalog
import com.example.ui.viewmodel.PurrGameUiState
import com.example.widget.PurrWidgetUpdater

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WidgetsGalleryDialog(
  uiState: PurrGameUiState,
  onNotifyToast: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val canPin = remember(context) { PurrWidgetUpdater.canRequestPinWidget(context) }
  var lastPinStatus by remember { mutableStateOf<String?>(null) }

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
          .widthIn(max = 540.dp)
          .heightIn(max = 720.dp)
          .testTag("widgets_gallery_dialog"),
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Header
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
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = CircleShape,
              modifier = Modifier.size(42.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Rounded.Widgets,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                )
              }
            }
            Column {
              Text(
                text = "Home Screen Widgets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "Pet your cat & track Purrs straight from your Home Screen!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp).testTag("close_widgets_gallery_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close Widgets Gallery")
          }
        }

        if (lastPinStatus != null) {
          Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().testTag("widget_pin_status_banner"),
          ) {
            Text(
              text = lastPinStatus.orEmpty(),
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(12.dp),
            )
          }
        }

        // Widget 1: PurrTap Sanctuary (4x2)
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth().testTag("sanctuary_widget_preview_card"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "PurrTap Sanctuary (4×2)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
              ) {
                Text(
                  text = "FULL DASHBOARD",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }

            // Live Preview Mockup of the 4x2 Widget
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = MaterialTheme.shapes.large,
              border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
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
                      patternId = uiState.profile.selectedPatternId,
                      accessoryId = uiState.profile.selectedAccessoryId,
                      modifier = Modifier.size(38.dp),
                    )
                    Column {
                      Text(
                        text = "${uiState.profile.selectedCatName}'s Sanctuary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                      Text(
                        text = "${uiState.weather.condition.label} • ${uiState.weather.temperatureC.toInt()}°C",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                      )
                    }
                  }
                  Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(13.dp),
                      )
                      Text(
                        text = "Pet Cat",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                      )
                    }
                  }
                }

                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  WidgetMiniChip(
                    icon = Icons.Rounded.Pets,
                    label = "${GameCatalog.formatCompactPurrs(uiState.activeCurrencyBalance)} ${uiState.activeCurrencyShortLabel}",
                  )
                  WidgetMiniChip(
                    icon = Icons.Rounded.TouchApp,
                    label = "+${GameCatalog.formatCompactPurrs(uiState.effectiveClickPower)}/tap",
                  )
                  WidgetMiniChip(
                    icon = Icons.Rounded.AutoAwesome,
                    label = "+${GameCatalog.formatCompactPurrs(uiState.effectivePps)}/sec",
                  )
                  WidgetMiniChip(
                    icon = Icons.Rounded.Diamond,
                    label = "${uiState.challengeState.gems} Gems",
                  )
                }
              }
            }

            Button(
              onClick = {
                val ok = PurrWidgetUpdater.requestPinWidget(context, mini = false)
                val msg =
                  if (ok) {
                    "Sanctuary Widget added / confirmation sent! Check your Home Screen."
                  } else {
                    "Long-press your Home Screen -> Widgets -> drag 'PurrTap Sanctuary' onto a page!"
                  }
                lastPinStatus = msg
                onNotifyToast(msg)
              },
              modifier =
                Modifier.fillMaxWidth().height(48.dp).testTag("gallery_pin_sanctuary_widget"),
            ) {
              Icon(Icons.Rounded.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.size(6.dp))
              Text(
                text = if (canPin) "Add Sanctuary Widget to Home Screen" else "Pin Sanctuary Widget",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Widget 2: PurrTap Mini Cat (2x1)
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
          shape = MaterialTheme.shapes.large,
          modifier = Modifier.fillMaxWidth().testTag("mini_widget_preview_card"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "PurrTap Mini Cat (2×1)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
              ) {
                Text(
                  text = "COMPACT PET",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }

            Surface(
              color = MaterialTheme.colorScheme.secondaryContainer,
              shape = MaterialTheme.shapes.medium,
              border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  CatAvatarMini(
                    patternId = uiState.profile.selectedPatternId,
                    accessoryId = uiState.profile.selectedAccessoryId,
                    modifier = Modifier.size(34.dp),
                  )
                  Column {
                    Text(
                      text = uiState.profile.selectedCatName,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                      text = "${GameCatalog.formatCompactPurrs(uiState.activeCurrencyBalance)} ${uiState.activeCurrencyShortLabel}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                    )
                  }
                }
                Surface(
                  color = MaterialTheme.colorScheme.secondary,
                  shape = CircleShape,
                ) {
                  Text(
                    text = "Tap Pet +${GameCatalog.formatCompactPurrs(uiState.effectiveClickPower)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  )
                }
              }
            }

            FilledTonalButton(
              onClick = {
                val ok = PurrWidgetUpdater.requestPinWidget(context, mini = true)
                val msg =
                  if (ok) {
                    "Mini Cat Widget added / confirmation sent! Check your Home Screen."
                  } else {
                    "Long-press your Home Screen -> Widgets -> drag 'PurrTap Mini Cat' onto a page!"
                  }
                lastPinStatus = msg
                onNotifyToast(msg)
              },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("gallery_pin_mini_widget"),
            ) {
              Icon(Icons.Rounded.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.size(6.dp))
              Text(
                text = if (canPin) "Add Mini Cat Widget to Home Screen" else "Pin Mini Cat Widget",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Manual fallback instructions
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text =
              "Tip: If your Android launcher doesn't show the Add pop-up automatically, long-press empty space on your Home Screen -> tap Widgets -> pick PurrTap!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun WidgetMiniChip(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
    shape = CircleShape,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(12.dp),
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}
