package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemHoverTooltipBox(
  title: String,
  description: String,
  limitedTag: String? = null,
  existCount: Long? = null,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val tooltipState = rememberTooltipState(isPersistent = false)
  TooltipBox(
    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
    tooltip = {
      RichTooltip(
        title = {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
        },
        modifier = Modifier.padding(4.dp),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
          )
          if (limitedTag != null || existCount != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              limitedTag?.let { tag ->
                Surface(
                  color = MaterialTheme.colorScheme.tertiaryContainer,
                  shape = MaterialTheme.shapes.small,
                ) {
                  Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  )
                }
              }
              existCount?.let { count ->
                Surface(
                  color = MaterialTheme.colorScheme.secondaryContainer,
                  shape = MaterialTheme.shapes.small,
                ) {
                  Text(
                    text = "${count} exist",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  )
                }
              }
            }
          }
        }
      }
    },
    state = tooltipState,
    modifier = modifier,
    content = content,
  )
}
