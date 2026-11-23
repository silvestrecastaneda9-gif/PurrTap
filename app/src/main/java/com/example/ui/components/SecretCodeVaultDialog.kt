package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GameCatalog
import com.example.ui.viewmodel.PurrGameUiState

@Composable
fun SecretCodeVaultDialog(
  uiState: PurrGameUiState,
  onTalkToKeeper: () -> Boolean,
  onUnlockCatComputer: (String) -> Boolean,
  onOpenWindowGatoDesktop: () -> Unit,
  onDismissExitConfrontation: () -> Unit,
  onDismiss: () -> Unit,
) {
  val isKickedOut = uiState.vaultCooldownRemainingMillis > 0L
  val tapCount = uiState.vaultKeeperTapCount
  val maxTaps = GameCatalog.VAULT_KEEPER_KICKOUT_TAPS
  val hasHackerUnlock = uiState.challengeState.hackerVaultUnlocked
  val hasSearchedComputer = uiState.challengeState.dogComputerSearched
  val isComputerUnlocked = uiState.challengeState.catComputerUnlocked

  var passwordInput by rememberSaveable { mutableStateOf("") }
  var passwordError by rememberSaveable { mutableStateOf(false) }

  val dialogueLine =
    when {
      uiState.showWindowGatoExitConfrontation ->
        "Was it worth searching without permission? You know the truth now."
      hasSearchedComputer ->
        "...The Dog Vault Keeper watches in silence and leaves you alone."
      isKickedOut ->
        "LOCKOUT ENGAGED! I warned you not to click me so many times! Come back after the firewall cools down."
      tapCount == 0 ->
        "Halt! I am The Dog Vault Keeper. All I can say is... The World Of Cats is ending soon! The Hackers will destroy everything!"
      else -> {
        val warnings = GameCatalog.vaultKeeperWarnings
        warnings[(tapCount - 1).coerceIn(0, warnings.lastIndex)]
      }
    }

  val agitationRatio =
    if (isKickedOut) 1f else (tapCount.toFloat() / maxTaps.toFloat()).coerceIn(0f, 1f)

  val handleInteract = {
    val kickedOutNow = onTalkToKeeper()
    if (kickedOutNow) {
      onDismiss()
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      tonalElevation = 8.dp,
      color = MaterialTheme.colorScheme.surface,
      border =
        BorderStroke(
          1.5.dp,
          if (isKickedOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        ),
      modifier =
        Modifier.fillMaxWidth(0.94f).widthIn(max = 520.dp).testTag("secret_code_vault_dialog"),
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Header Bar
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
              color =
                if (isKickedOut) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer,
              shape = MaterialTheme.shapes.medium,
            ) {
              Icon(
                imageVector = if (isKickedOut) Icons.Rounded.GppBad else Icons.Rounded.Terminal,
                contentDescription = null,
                tint =
                  if (isKickedOut) MaterialTheme.colorScheme.error
                  else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp).size(24.dp),
              )
            }
            Column {
              Text(
                text = "SECRET CODE VAULT",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "VAULT://SECTOR-K9 • ENCRYPTED",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp).testTag("close_code_vault_button"),
          ) {
            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Exit Secret Code Vault")
          }
        }

        // Encrypted Log Banner
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            Text(
              text = "> SYS_ALERT: WORLD_OF_CATS.DAT CRITICAL",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "> THREAT_SOURCE: HACKERS // GUARD: DOG_VAULT_KEEPER",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        // Dog Vault Keeper Portrait Card (Clickable!)
        Card(
          colors =
            CardDefaults.cardColors(
              containerColor =
                if (isKickedOut) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            ),
          border =
            BorderStroke(
              1.dp,
              if (isKickedOut) MaterialTheme.colorScheme.error
              else MaterialTheme.colorScheme.outline,
            ),
          modifier =
            Modifier.fillMaxWidth()
              .clickable(enabled = !isKickedOut) { handleInteract() }
              .testTag("dog_vault_keeper_card"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Security,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "The Dog Vault Keeper",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                )
              }
              Surface(
                color =
                  when {
                    isKickedOut -> MaterialTheme.colorScheme.error
                    hasSearchedComputer -> MaterialTheme.colorScheme.surfaceVariant
                    tapCount >= 4 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                  },
                shape = MaterialTheme.shapes.small,
              ) {
                Text(
                  text =
                    when {
                      isKickedOut -> "KICKED OUT"
                      hasSearchedComputer -> "SILENT"
                      tapCount >= 4 -> "IRRITATED"
                      else -> "ON GUARD"
                    },
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color =
                    when {
                      isKickedOut -> MaterialTheme.colorScheme.onError
                      hasSearchedComputer -> MaterialTheme.colorScheme.onSurfaceVariant
                      tapCount >= 4 -> MaterialTheme.colorScheme.onTertiary
                      else -> MaterialTheme.colorScheme.onPrimary
                    },
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
            }

            DogVaultKeeperPortrait(
              agitationLevel = tapCount,
              isKickedOut = isKickedOut,
              modifier = Modifier.size(132.dp),
            )

            // Speech Bubble
            Surface(
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
              shape = MaterialTheme.shapes.large,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = "\"$dialogueLine\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color =
                  if (isKickedOut) MaterialTheme.colorScheme.error
                  else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp),
              )
            }

            if (!isKickedOut && !hasSearchedComputer) {
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = "Keeper Agitation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Text(
                    text = "$tapCount / $maxTaps",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color =
                      if (tapCount >= 4) MaterialTheme.colorScheme.error
                      else MaterialTheme.colorScheme.primary,
                  )
                }
                LinearProgressIndicator(
                  progress = { agitationRatio },
                  color =
                    if (tapCount >= 4) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.fillMaxWidth().height(6.dp),
                  strokeCap = StrokeCap.Round,
                )
              }
            }
          }
        }

        if (uiState.showWindowGatoExitConfrontation) {
          Card(
            colors =
              CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f)
              ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary),
            modifier = Modifier.fillMaxWidth().testTag("keeper_exit_confrontation_card"),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "Dog Vault Keeper:",
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
              Text(
                text = "\"Was it worth searching without permission? You know the truth now.\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
              OutlinedButton(
                onClick = onDismissExitConfrontation,
                modifier = Modifier.align(Alignment.End).height(48.dp).testTag("keeper_truth_ack_button"),
              ) {
                Text("Step Away")
              }
            }
          }
        }

        // Cat Computer / Window Gato Workstation Card
        Card(
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
          border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth().testTag("cat_computer_vault_card"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Computer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Cat Computer • Window Gato OS",
                  style = MaterialTheme.typography.titleSmall,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text =
                    if (isComputerUnlocked) "AUTHENTICATED • LANDSCAPE DESKTOP"
                    else "LOCKED TERMINAL • PASSWORD REQUIRED",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            if (isComputerUnlocked) {
              Button(
                onClick = onOpenWindowGatoDesktop,
                modifier =
                  Modifier.fillMaxWidth().height(50.dp).testTag("launch_window_gato_button"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.LockOpen,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Boot Window Gato Desktop", fontWeight = FontWeight.Bold)
              }
            } else {
              OutlinedTextField(
                value = passwordInput,
                onValueChange = {
                  passwordInput = it
                  passwordError = false
                },
                label = { Text("Enter Terminal Password") },
                singleLine = true,
                isError = passwordError,
                supportingText =
                  if (passwordError) {
                    { Text("Wrong password — read the riddle & hint below.") }
                  } else {
                    null
                  },
                modifier = Modifier.fillMaxWidth().testTag("cat_computer_password_input"),
              )

              Button(
                onClick = {
                  val ok = onUnlockCatComputer(passwordInput)
                  passwordError = !ok
                },
                modifier =
                  Modifier.fillMaxWidth().height(48.dp).testTag("unlock_cat_computer_button"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Terminal,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Submit Password & Boot", fontWeight = FontWeight.Bold)
              }

              // Riddle & Hint located directly below the Password Input
              Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().testTag("cat_computer_riddle_box"),
              ) {
                Column(
                  modifier = Modifier.padding(10.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = "PASSWORD RIDDLE:",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                  )
                  Text(
                    text = GameCatalog.CAT_COMPUTER_RIDDLE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = GameCatalog.CAT_COMPUTER_HINT,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary,
                  )
                }
              }
            }
          }
        }

        if (isKickedOut) {
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth().testTag("vault_lockout_banner"),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.LockClock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp),
              )
              Column {
                Text(
                  text = "Kicked Out for 3 Minutes",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                  text = "Cooldown remaining: ${uiState.vaultCooldownText}",
                  style = MaterialTheme.typography.bodyMedium,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.error,
                )
              }
            }
          }
        }

        if (hasHackerUnlock) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = "Unlocked: Hacker Protocol Theme & Code Vault Intruder Achievement!",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
        }

        Button(
          onClick = { handleInteract() },
          enabled = !isKickedOut && !hasSearchedComputer,
          colors =
            if (tapCount >= 4 && !isKickedOut && !hasSearchedComputer) {
              ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            } else {
              ButtonDefaults.buttonColors()
            },
          modifier =
            Modifier.fillMaxWidth().height(52.dp).testTag("talk_vault_keeper_button"),
        ) {
          Icon(
            imageVector =
              if (isKickedOut) Icons.Rounded.LockClock else Icons.Rounded.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text =
              when {
                hasSearchedComputer -> "Dog Keeper Left You Alone"
                isKickedOut -> "Vault Locked (${uiState.vaultCooldownText})"
                else -> "Talk to Dog Vault Keeper"
              },
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

@Composable
internal fun DogVaultKeeperPortrait(
  agitationLevel: Int,
  isKickedOut: Boolean,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f

    // Vault Radar Halo
    val haloColor =
      if (isKickedOut || agitationLevel >= 5) Color(0xFFFF5252) else Color(0xFF00E676)
    drawCircle(
      color = haloColor.copy(alpha = 0.14f),
      radius = w * 0.46f,
      center = Offset(cx, cy),
    )
    drawCircle(
      color = haloColor.copy(alpha = 0.55f),
      radius = w * 0.46f,
      center = Offset(cx, cy),
      style = Stroke(width = 3.dp.toPx()),
    )

    val furColor = Color(0xFFD99B52)
    val bellyCream = Color(0xFFFFE6C2)
    val earDark = Color(0xFF8C5623)

    // Left & Right Pointed Dog Ears
    val leftEar =
      Path().apply {
        moveTo(cx - w * 0.28f, cy - h * 0.08f)
        lineTo(cx - w * 0.34f, cy - h * 0.38f)
        lineTo(cx - w * 0.08f, cy - h * 0.24f)
        close()
      }
    drawPath(leftEar, color = furColor)
    drawPath(leftEar, color = earDark, style = Stroke(width = 2.dp.toPx()))

    val rightEar =
      Path().apply {
        moveTo(cx + w * 0.28f, cy - h * 0.08f)
        lineTo(cx + w * 0.34f, cy - h * 0.38f)
        lineTo(cx + w * 0.08f, cy - h * 0.24f)
        close()
      }
    drawPath(rightEar, color = furColor)
    drawPath(rightEar, color = earDark, style = Stroke(width = 2.dp.toPx()))

    // Shoulders & Security Jacket
    drawRoundRect(
      color = Color(0xFF1A2520),
      topLeft = Offset(cx - w * 0.26f, cy + h * 0.16f),
      size = Size(w * 0.52f, h * 0.24f),
      cornerRadius = CornerRadius(18f, 18f),
    )
    // Security Collar
    drawRoundRect(
      color = haloColor,
      topLeft = Offset(cx - w * 0.18f, cy + h * 0.16f),
      size = Size(w * 0.36f, h * 0.06f),
      cornerRadius = CornerRadius(10f, 10f),
    )

    // Dog Head
    drawCircle(
      color = furColor,
      radius = w * 0.27f,
      center = Offset(cx, cy - h * 0.02f),
    )
    // Shiba Cream Cheeks & Snout Patch
    drawOval(
      color = bellyCream,
      topLeft = Offset(cx - w * 0.20f, cy - h * 0.02f),
      size = Size(w * 0.40f, h * 0.24f),
    )

    // Cyber Hacker Visor Band
    drawRoundRect(
      color = Color(0xFF0A1410),
      topLeft = Offset(cx - w * 0.22f, cy - h * 0.14f),
      size = Size(w * 0.44f, h * 0.12f),
      cornerRadius = CornerRadius(14f, 14f),
    )
    drawRoundRect(
      color = haloColor.copy(alpha = 0.85f),
      topLeft = Offset(cx - w * 0.22f, cy - h * 0.14f),
      size = Size(w * 0.44f, h * 0.12f),
      cornerRadius = CornerRadius(14f, 14f),
      style = Stroke(width = 2.5f),
    )

    // Glowing Visor Eyes
    val eyeY = cy - h * 0.08f
    drawCircle(color = haloColor, radius = 4.5f, center = Offset(cx - w * 0.10f, eyeY))
    drawCircle(color = haloColor, radius = 4.5f, center = Offset(cx + w * 0.10f, eyeY))

    // Agitated Brows when annoyed
    if (agitationLevel >= 2 || isKickedOut) {
      drawLine(
        color = haloColor,
        start = Offset(cx - w * 0.16f, cy - h * 0.13f),
        end = Offset(cx - w * 0.04f, cy - h * 0.09f),
        strokeWidth = 3f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = haloColor,
        start = Offset(cx + w * 0.16f, cy - h * 0.13f),
        end = Offset(cx + w * 0.04f, cy - h * 0.09f),
        strokeWidth = 3f,
        cap = StrokeCap.Round,
      )
    }

    // Dog Nose & Snout Expression
    drawOval(
      color = Color(0xFF221A17),
      topLeft = Offset(cx - w * 0.045f, cy + h * 0.01f),
      size = Size(w * 0.09f, h * 0.06f),
    )
    if (isKickedOut || agitationLevel >= 4) {
      // Grumpy zig/frown bark
      drawLine(
        color = Color(0xFF221A17),
        start = Offset(cx - w * 0.08f, cy + h * 0.11f),
        end = Offset(cx, cy + h * 0.08f),
        strokeWidth = 3f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = Color(0xFF221A17),
        start = Offset(cx, cy + h * 0.08f),
        end = Offset(cx + w * 0.08f, cy + h * 0.11f),
        strokeWidth = 3f,
        cap = StrokeCap.Round,
      )
    } else {
      // Worried doggy muzzle
      drawLine(
        color = Color(0xFF221A17),
        start = Offset(cx, cy + h * 0.07f),
        end = Offset(cx, cy + h * 0.11f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = Color(0xFF221A17),
        start = Offset(cx - w * 0.06f, cy + h * 0.11f),
        end = Offset(cx + w * 0.06f, cy + h * 0.11f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round,
      )
    }
  }
}
