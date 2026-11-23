package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.BlogPostDef
import com.example.data.model.GameCatalog
import com.example.ui.components.SubtleGlitchLoreWhisper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuideScreen() {
  var selectedSection by remember { mutableIntStateOf(0) }
  var activeBlogPostIndex by remember { mutableStateOf<Int?>(null) }

  BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    val isNarrow = maxWidth < 360.dp
    val isUltraNarrow = maxWidth < 335.dp
    val horizontalPad = if (isNarrow) 10.dp else 16.dp
    val sections =
      if (isUltraNarrow) listOf("Blog", "Play", "FAQ")
      else listOf("Update Blog", "How to Play", "FAQ")

    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 640.dp)
          .padding(horizontal = horizontalPad, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_guide_banner),
        contentDescription = "PurrTap Field Guide & Update Blog illustration",
        modifier = Modifier.fillMaxWidth().height(126.dp).clip(MaterialTheme.shapes.large),
        contentScale = ContentScale.Crop,
      )

      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        sections.forEachIndexed { index, title ->
          SegmentedButton(
            selected = selectedSection == index,
            onClick = { selectedSection = index },
            shape = SegmentedButtonDefaults.itemShape(index = index, count = sections.size),
            modifier = Modifier.testTag("guide_tab_$index"),
          ) {
            Text(
              text = title,
              style =
                if (isNarrow) MaterialTheme.typography.labelSmall
                else MaterialTheme.typography.labelLarge,
              maxLines = 1,
              softWrap = false,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        when (selectedSection) {
          0 -> {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(bottom = 16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              itemsIndexed(GameCatalog.blogPosts, key = { _, post -> post.version }) { index, post ->
                Card(
                  onClick = { activeBlogPostIndex = index },
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                  modifier = Modifier.fillMaxWidth().testTag("blog_card_${post.version}"),
                ) {
                  Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    FlowRow(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                      ) {
                        Surface(
                          color = MaterialTheme.colorScheme.primaryContainer,
                          shape = MaterialTheme.shapes.small,
                        ) {
                          Text(
                            text = post.version,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                          )
                        }
                        Text(
                          text = "${post.date} • ${post.readTimeLabel}",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                      Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                      ) {
                        Text(
                          text = "Read Full Post",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.SemiBold,
                          color = MaterialTheme.colorScheme.onSecondaryContainer,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                      }
                    }
                    Text(text = post.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                      text = post.summary,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    post.highlights.take(2).forEach { bullet ->
                      Text(
                        text = "• $bullet",
                        style = MaterialTheme.typography.bodySmall,
                      )
                    }
                  }
                }
              }
            }
          }
          1 -> {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(bottom = 16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              items(GameCatalog.howToPlaySteps, key = { it.first }) { (title, body) ->
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Text(
                      text = title,
                      style = MaterialTheme.typography.titleMedium,
                      color = MaterialTheme.colorScheme.primary,
                    )
                    Text(text = body, style = MaterialTheme.typography.bodyMedium)
                  }
                }
              }
            }
          }
          else -> {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(bottom = 16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              itemsIndexed(GameCatalog.faqItems, key = { idx, _ -> idx }) { index, item ->
                var expanded by remember { mutableStateOf(index == 0) }
                Card(
                  onClick = { expanded = !expanded },
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                  modifier = Modifier.fillMaxWidth().testTag("faq_card_$index"),
                ) {
                  Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = item.question,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                      )
                      Icon(
                        imageVector =
                          if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                      )
                    }
                    AnimatedVisibility(visible = expanded) {
                      Text(
                        text = item.answer,
                        style = MaterialTheme.typography.bodyMedium,
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

      SubtleGlitchLoreWhisper(channel = "guide")
    }
  }

  val openIdx = activeBlogPostIndex
  if (openIdx != null && openIdx in GameCatalog.blogPosts.indices) {
    UpdateBlogReaderDialog(
      post = GameCatalog.blogPosts[openIdx],
      issuePosition = openIdx + 1,
      totalIssues = GameCatalog.blogPosts.size,
      hasPrevious = openIdx > 0,
      hasNext = openIdx < GameCatalog.blogPosts.lastIndex,
      onPrevious = { if (openIdx > 0) activeBlogPostIndex = openIdx - 1 },
      onNext = { if (openIdx < GameCatalog.blogPosts.lastIndex) activeBlogPostIndex = openIdx + 1 },
      onDismiss = { activeBlogPostIndex = null },
    )
  }
}

@Composable
private fun UpdateBlogReaderDialog(
  post: BlogPostDef,
  issuePosition: Int,
  totalIssues: Int,
  hasPrevious: Boolean,
  hasNext: Boolean,
  onPrevious: () -> Unit,
  onNext: () -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier =
        Modifier.fillMaxWidth(0.94f)
          .heightIn(max = 680.dp)
          .testTag("blog_reader_bubble"),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Bubble Header
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
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = MaterialTheme.shapes.small,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.AutoStories,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text = post.version,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            }
            Text(
              text = "${post.date} • ${post.readTimeLabel}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp).testTag("close_blog_bubble_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close blog reader")
          }
        }

        Column(
          modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = post.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "By ${post.authorTag}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          HorizontalDivider()

          if (post.fullArticleParagraphs.isNotEmpty()) {
            post.fullArticleParagraphs.forEach { paragraph ->
              Text(
                text = paragraph,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          } else {
            Text(
              text = post.summary,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }

          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = "Release Highlights",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
              post.highlights.forEach { bullet ->
                Text(
                  text = "• $bullet",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
            }
          }
        }

        HorizontalDivider()

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedButton(
            onClick = onPrevious,
            enabled = hasPrevious,
            modifier = Modifier.height(48.dp).testTag("blog_prev_issue_button"),
          ) {
            Icon(
              Icons.AutoMirrored.Rounded.ArrowBack,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Newer")
          }
          Text(
            text = "Issue $issuePosition / $totalIssues",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Button(
            onClick = onNext,
            enabled = hasNext,
            modifier = Modifier.height(48.dp).testTag("blog_next_issue_button"),
          ) {
            Text("Older")
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              Icons.AutoMirrored.Rounded.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }
    }
  }
}
