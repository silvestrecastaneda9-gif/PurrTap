package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GameCatalog
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerProfilePopupDialog(
  entry: LeaderboardEntry,
  rank: Int?,
  isSelf: Boolean,
  isAdmin: Boolean,
  onEditOwnProfile: () -> Unit,
  onModeratePlayer: (LeaderboardEntry) -> Unit,
  onDismiss: () -> Unit,
) {
  val bannerBitmap = rememberBase64ImageBitmap(entry.bannerBase64)
  val avatarBitmap = rememberBase64ImageBitmap(entry.avatarBase64)
  val badge = GameCatalog.badges.find { it.id == entry.equippedBadgeId }
  val patternName =
    GameCatalog.patterns.find { it.id == entry.catPatternId }?.name ?: entry.catPatternId
  val accessoryName =
    GameCatalog.accessories.find { it.id == entry.catAccessoryId }?.name ?: entry.catAccessoryId

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth(0.93f).testTag("player_profile_popup"),
    ) {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
      ) {
        // Hero Banner + Overlapping PFP + Close Button
        Box(modifier = Modifier.fillMaxWidth().height(152.dp)) {
          if (bannerBitmap != null) {
            Image(
              bitmap = bannerBitmap,
              contentDescription = "${entry.displayName} Banner",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth().height(120.dp),
            )
          } else {
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .height(120.dp)
                  .background(
                    Brush.linearGradient(
                      colors =
                        listOf(
                          MaterialTheme.colorScheme.primaryContainer,
                          MaterialTheme.colorScheme.tertiaryContainer,
                          MaterialTheme.colorScheme.secondaryContainer,
                        )
                    )
                  ),
              contentAlignment = Alignment.CenterEnd,
            ) {
              Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.22f),
                modifier = Modifier.size(84.dp).padding(end = 18.dp),
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            colors =
              IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
              ),
            modifier =
              Modifier.align(Alignment.TopEnd)
                .padding(8.dp)
                .size(48.dp)
                .testTag("profile_popup_close_icon"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close Profile")
          }

          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.surface),
            shadowElevation = 4.dp,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 18.dp).size(78.dp),
          ) {
            if (avatarBitmap != null) {
              Image(
                bitmap = avatarBitmap,
                contentDescription = "${entry.displayName} Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
              )
            } else {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CatAvatarMini(
                  patternId = entry.catPatternId,
                  accessoryId = entry.catAccessoryId,
                  modifier = Modifier.size(64.dp),
                )
              }
            }
          }

          if (rank != null) {
            Surface(
              color = MaterialTheme.colorScheme.primary,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 6.dp),
            ) {
              Text(
                text = "Rank #$rank",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }

        Column(
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = entry.displayName,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false),
            )
            if (isSelf) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
              ) {
                Text(
                  text = "YOU",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
              }
            }
            if (isSelf && isAdmin) {
              Icon(
                Icons.Rounded.Verified,
                contentDescription = "Verified Admin",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }

          if (entry.bio.isNotBlank() || isSelf) {
            Text(
              text =
                entry.bio.ifBlank {
                  "No sanctuary bio yet — tap Edit Profile to add a custom bio, banner, or PFP!"
                },
              style = MaterialTheme.typography.bodyMedium,
              color =
                if (entry.bio.isNotBlank()) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          // Cat Companion Showcase
          Card(
            colors =
              CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(18.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              CatAvatarMini(
                patternId = entry.catPatternId,
                accessoryId = entry.catAccessoryId,
                modifier = Modifier.size(54.dp),
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Companion: ${entry.catName}",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "$patternName • $accessoryName",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (badge != null) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp),
                  ) {
                    Icon(
                      Icons.Rounded.MilitaryTech,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp),
                    )
                    Text(
                      text = "${badge.name} (${badge.rarity})",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }
            }
          }

          // Public Stats vs Private Stats
          val canViewStats = entry.statsPublic || isSelf
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = if (entry.statsPublic) Icons.Rounded.Public else Icons.Rounded.Lock,
              contentDescription = null,
              tint =
                if (entry.statsPublic) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text =
                when {
                  entry.statsPublic -> "Public Sanctuary Stats"
                  isSelf -> "Private Stats (Only visible to you)"
                  else -> "Private Sanctuary Profile"
                },
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
          }

          if (canViewStats) {
            val comboText =
              String.format(
                Locale.US,
                "%.2fx",
                (entry.publicHighestComboX100.coerceAtLeast(100L) / 100.0),
              )
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              ProfileStatPill("Lifetime Purrs", GameCatalog.formatCompactPurrs(entry.totalPurrsEarned), Modifier.weight(1f))
              ProfileStatPill("Total Pets", GameCatalog.formatCompactPurrs(entry.totalPets), Modifier.weight(1f))
              ProfileStatPill("Badges", "${entry.badgesCount}", Modifier.weight(1f))
            }
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              ProfileStatPill("Rebirths", "${entry.publicRebirths}", Modifier.weight(1f))
              ProfileStatPill("Ascensions", "${entry.publicAscensions}", Modifier.weight(1f))
              ProfileStatPill("Streak", "${entry.publicDailyStreak}d", Modifier.weight(1f))
              ProfileStatPill("Peak Combo", comboText, Modifier.weight(1f))
            }
          } else {
            Card(
              colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Icon(
                  Icons.Rounded.Lock,
                  contentDescription = "Private Stats",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = "This caretaker set their detailed profile stats to Private.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }

          // Action Row
          Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (isSelf) {
              Button(
                onClick = onEditOwnProfile,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(48.dp).testTag("profile_popup_edit_button"),
              ) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Edit Profile & PFP")
              }
            } else if (isAdmin) {
              FilledTonalButton(
                onClick = { onModeratePlayer(entry) },
                colors =
                  ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                  ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(48.dp).testTag("profile_popup_moderate_button"),
              ) {
                Icon(Icons.Rounded.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Moderate Player")
              }
            }
            TextButton(
              onClick = onDismiss,
              modifier = Modifier.height(48.dp).testTag("profile_popup_done_button"),
            ) {
              Text("Close")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProfileStatPill(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape = RoundedCornerShape(14.dp),
    modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
      )
    }
  }
}

@Composable
fun EditPublicProfileDialog(
  currentProfile: UserProfile,
  onSave: (statsPublic: Boolean, bio: String, avatarBase64: String, bannerBase64: String) -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var statsPublic by remember(currentProfile.userId) { mutableStateOf(currentProfile.statsPublic) }
  var bio by remember(currentProfile.userId) { mutableStateOf(currentProfile.bio) }
  var avatarBase64 by remember(currentProfile.userId) { mutableStateOf(currentProfile.avatarBase64) }
  var bannerBase64 by remember(currentProfile.userId) { mutableStateOf(currentProfile.bannerBase64) }
  var isProcessingPfp by remember { mutableStateOf(false) }
  var isProcessingBanner by remember { mutableStateOf(false) }
  var uploadError by remember { mutableStateOf<String?>(null) }

  val avatarPicker =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
      if (uri != null) {
        isProcessingPfp = true
        uploadError = null
        scope.launch {
          val encoded =
            withContext(Dispatchers.IO) {
              runCatching {
                ImageEncodingUtil.encodeUriToCompactBase64(
                  context = context,
                  uri = uri,
                  maxWidthPx = 320,
                  maxHeightPx = 320,
                  maxBase64Chars = 54_000,
                )
              }.getOrNull()
            }
          isProcessingPfp = false
          if (encoded != null) {
            avatarBase64 = encoded
            uploadError = null
          } else {
            uploadError = "Could not load selected profile picture. Try another photo."
          }
        }
      }
    }

  val bannerPicker =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
      if (uri != null) {
        isProcessingBanner = true
        uploadError = null
        scope.launch {
          val encoded =
            withContext(Dispatchers.IO) {
              runCatching {
                ImageEncodingUtil.encodeUriToCompactBase64(
                  context = context,
                  uri = uri,
                  maxWidthPx = 680,
                  maxHeightPx = 280,
                  maxBase64Chars = 110_000,
                )
              }.getOrNull()
            }
          isProcessingBanner = false
          if (encoded != null) {
            bannerBase64 = encoded
            uploadError = null
          } else {
            uploadError = "Could not load selected banner image. Try another photo."
          }
        }
      }
    }

  val bannerPreview = rememberBase64ImageBitmap(bannerBase64)
  val avatarPreview = rememberBase64ImageBitmap(avatarBase64)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth(0.93f).testTag("edit_public_profile_dialog"),
    ) {
      Column(
        modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Customize Public Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Rounded.Close, contentDescription = "Close")
          }
        }

        // Live Banner & PFP Preview
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(128.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
          if (bannerPreview != null) {
            Image(
              bitmap = bannerPreview,
              contentDescription = "Banner Preview",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth().height(98.dp),
            )
          } else {
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .height(98.dp)
                  .background(
                    Brush.linearGradient(
                      colors =
                        listOf(
                          MaterialTheme.colorScheme.primaryContainer,
                          MaterialTheme.colorScheme.tertiaryContainer,
                        )
                    )
                  ),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "Default Sanctuary Banner",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }

          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            modifier =
              Modifier.align(Alignment.BottomStart)
                .offset(x = 14.dp)
                .size(64.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
          ) {
            if (avatarPreview != null) {
              Image(
                bitmap = avatarPreview,
                contentDescription = "Avatar Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
              )
            } else {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CatAvatarMini(
                  patternId = currentProfile.selectedPatternId,
                  accessoryId = currentProfile.selectedAccessoryId,
                  modifier = Modifier.size(50.dp),
                )
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = {
              avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            enabled = !isProcessingPfp && !isProcessingBanner,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).height(48.dp).testTag("upload_pfp_button"),
          ) {
            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (isProcessingPfp) "Processing PFP..." else "Upload PFP", maxLines = 1)
          }
          if (avatarBase64.isNotBlank()) {
            IconButton(
              onClick = { avatarBase64 = "" },
              enabled = !isProcessingPfp,
              modifier = Modifier.size(48.dp).testTag("clear_pfp_button"),
            ) {
              Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove PFP")
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = {
              bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            enabled = !isProcessingPfp && !isProcessingBanner,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).height(48.dp).testTag("upload_banner_button"),
          ) {
            Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (isProcessingBanner) "Processing Banner..." else "Upload Banner", maxLines = 1)
          }
          if (bannerBase64.isNotBlank()) {
            IconButton(
              onClick = { bannerBase64 = "" },
              enabled = !isProcessingBanner,
              modifier = Modifier.size(48.dp).testTag("clear_banner_button"),
            ) {
              Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove Banner")
            }
          }
        }

        uploadError?.let { msg ->
          Text(
            text = msg,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
        }

        OutlinedTextField(
          value = bio,
          onValueChange = { if (it.length <= 140) bio = it },
          label = { Text("Sanctuary Bio (max 140 chars)") },
          placeholder = { Text("Tell other caretakers about your cat...") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth().testTag("profile_bio_input"),
        )

        Card(
          colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          shape = RoundedCornerShape(16.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
              Text(
                text = if (statsPublic) "Public Stats Enabled" else "Private Stats Mode",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text =
                  if (statsPublic) "Anyone on Ranks can view your Rebirths, Ascensions, Streak & Stats."
                  else "Other players only see your Name, Cat, Banner & PFP.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = statsPublic,
              onCheckedChange = { statsPublic = it },
              modifier = Modifier.testTag("profile_privacy_switch"),
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).height(48.dp),
          ) {
            Text("Cancel")
          }
          Button(
            onClick = { onSave(statsPublic, bio, avatarBase64, bannerBase64) },
            enabled = !isProcessingPfp && !isProcessingBanner,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).height(48.dp).testTag("save_public_profile_button"),
          ) {
            Text("Save Profile")
          }
        }
      }
    }
  }
}
