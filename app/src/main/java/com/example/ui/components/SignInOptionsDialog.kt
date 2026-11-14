package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun SignInOptionsDialog(
  isSignedIn: Boolean,
  currentDisplayName: String,
  accountMode: String,
  localAccountCode: String,
  hasLocalPin: Boolean,
  onDismiss: () -> Unit,
  onGoogleSignIn: () -> Unit,
  onGoogleSignOut: () -> Unit,
  onSaveLocalAccount: (displayName: String, pin: String) -> Unit,
  onSwitchToGuest: () -> Unit,
  onExportSaveCode: () -> String,
  onImportSaveCode: (rawCode: String) -> Boolean,
) {
  val clipboardManager = LocalClipboardManager.current
  var localNameInput by remember(currentDisplayName) { mutableStateOf(currentDisplayName) }
  var localPinInput by remember { mutableStateOf("") }
  var showBackupSection by remember { mutableStateOf(false) }
  var generatedSaveCode by remember { mutableStateOf<String?>(null) }
  var pasteCodeInput by remember { mutableStateOf("") }
  var backupStatusMessage by remember { mutableStateOf<String?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier =
        Modifier.fillMaxWidth(0.94f)
          .heightIn(max = 700.dp)
          .testTag("sign_in_options_dialog"),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Rounded.AccountCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Column {
              Text(
                text = "Sign-In & Account Options",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text =
                  when {
                    isSignedIn -> "Active: Google Cloud Account ($currentDisplayName)"
                    accountMode == "local" ->
                      "Active: Local Caretaker (${localAccountCode.ifBlank { "Offline ID" }})"
                    else -> "Active: Instant Guest Play (Saved on Device)"
                  },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp).testTag("close_sign_in_options_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close sign-in options")
          }
        }

        Column(
          modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // 1. Local Caretaker Account (No Google Needed)
          Card(
            colors =
              CardDefaults.cardColors(
                containerColor =
                  if (accountMode == "local" && !isSignedIn)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                  else MaterialTheme.colorScheme.surfaceVariant
              ),
            border =
              if (accountMode == "local" && !isSignedIn)
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
              else null,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
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
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                  )
                  Column {
                    Text(
                      text = "Local Caretaker Account",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = "No Google account needed! Generates your own Sanctuary ID & backup codes.",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
                if (accountMode == "local" && !isSignedIn) {
                  Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Active",
                    tint = MaterialTheme.colorScheme.primary,
                  )
                }
              }

              if (localAccountCode.isNotBlank()) {
                Surface(
                  color = MaterialTheme.colorScheme.surface,
                  shape = MaterialTheme.shapes.medium,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column {
                      Text(
                        text = "Your Sanctuary ID",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                      Text(
                        text = localAccountCode,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                      )
                    }
                    IconButton(
                      onClick = {
                        clipboardManager.setText(AnnotatedString(localAccountCode))
                        backupStatusMessage = "Copied Sanctuary ID $localAccountCode"
                      },
                      modifier = Modifier.size(48.dp),
                    ) {
                      Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy Sanctuary ID")
                    }
                  }
                }
              }

              OutlinedTextField(
                value = localNameInput,
                onValueChange = { if (it.length <= 20) localNameInput = it },
                label = { Text("Caretaker Display Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("local_account_name_input"),
              )

              OutlinedTextField(
                value = localPinInput,
                onValueChange = { input ->
                  val digits = input.filter { it.isDigit() }.take(4)
                  localPinInput = digits
                },
                label = {
                  Text(if (hasLocalPin) "Update 4-Digit Offline PIN (Optional)" else "4-Digit Offline PIN (Optional)")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth().testTag("local_account_pin_input"),
              )

              Button(
                onClick = {
                  onSaveLocalAccount(localNameInput.trim().ifBlank { "Caretaker" }, localPinInput)
                  localPinInput = ""
                  backupStatusMessage = "Local Caretaker profile saved!"
                },
                modifier =
                  Modifier.fillMaxWidth().height(48.dp).testTag("save_local_account_button"),
              ) {
                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (accountMode == "local") "Update Local Account" else "Create & Use Local Account")
              }

              HorizontalDivider()

              // Expandable Save Backup Code Export & Import
              OutlinedButton(
                onClick = { showBackupSection = !showBackupSection },
                modifier =
                  Modifier.fillMaxWidth().height(48.dp).testTag("toggle_backup_code_section"),
              ) {
                Icon(Icons.Rounded.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transfer / Backup Save Code", modifier = Modifier.weight(1f))
                Icon(
                  imageVector =
                    if (showBackupSection) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                  contentDescription = null,
                )
              }

              if (showBackupSection) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  FilledTonalButton(
                    onClick = {
                      val code = onExportSaveCode()
                      generatedSaveCode = code
                      clipboardManager.setText(AnnotatedString(code))
                      backupStatusMessage = "Save Backup Code generated & copied!"
                    },
                    modifier =
                      Modifier.fillMaxWidth().height(48.dp).testTag("generate_save_code_button"),
                  ) {
                    Icon(
                      Icons.Rounded.ContentCopy,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export & Copy Save Backup Code")
                  }

                  if (!generatedSaveCode.isNullOrBlank()) {
                    Surface(
                      color = MaterialTheme.colorScheme.surface,
                      shape = MaterialTheme.shapes.small,
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      SelectionContainer {
                        Text(
                          text = generatedSaveCode!!,
                          style = MaterialTheme.typography.labelSmall,
                          fontFamily = FontFamily.Monospace,
                          maxLines = 3,
                          modifier = Modifier.padding(10.dp),
                        )
                      }
                    }
                  }

                  OutlinedTextField(
                    value = pasteCodeInput,
                    onValueChange = { pasteCodeInput = it },
                    label = { Text("Paste PURRSAVE1:... Backup Code") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("paste_save_code_input"),
                  )

                  Button(
                    onClick = {
                      val ok = onImportSaveCode(pasteCodeInput)
                      backupStatusMessage =
                        if (ok) {
                          pasteCodeInput = ""
                          "Sanctuary restored from Backup Code!"
                        } else {
                          "Invalid Save Backup Code. Check the full PURRSAVE1:... string."
                        }
                    },
                    enabled = pasteCodeInput.isNotBlank(),
                    modifier =
                      Modifier.fillMaxWidth().height(48.dp).testTag("restore_save_code_button"),
                  ) {
                    Icon(
                      Icons.Rounded.Restore,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore From Backup Code")
                  }
                }
              }

              if (!backupStatusMessage.isNullOrBlank()) {
                Text(
                  text = backupStatusMessage!!,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
            }
          }

          // 2. Instant Guest Play
          Card(
            colors =
              CardDefaults.cardColors(
                containerColor =
                  if (accountMode == "guest" && !isSignedIn)
                    MaterialTheme.colorScheme.secondaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant
              ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Pets,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary,
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Instant Guest Play",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = "Play immediately with zero sign-up. Progress stays saved locally on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              FilledTonalButton(
                onClick = {
                  onSwitchToGuest()
                  onDismiss()
                },
                modifier =
                  Modifier.fillMaxWidth().height(48.dp).testTag("continue_guest_button"),
              ) {
                Text(
                  if (accountMode == "guest" && !isSignedIn) "Playing as Guest (Continue)"
                  else "Switch to Guest Caretaker"
                )
              }
            }
          }

          // 3. Google Account (Cloud Sync & Global Ranks)
          Card(
            colors =
              CardDefaults.cardColors(
                containerColor =
                  if (isSignedIn) MaterialTheme.colorScheme.tertiaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant
              ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.CloudDone,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.tertiary,
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Google Account (Optional Cloud Sync)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text =
                      if (isSignedIn)
                        "Connected as $currentDisplayName. Cloud saves & Global Ranks active."
                      else
                        "Optional: link Google to sync progress across Android devices and post to the global leaderboard.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              if (isSignedIn) {
                OutlinedButton(
                  onClick = {
                    onGoogleSignOut()
                    onDismiss()
                  },
                  modifier =
                    Modifier.fillMaxWidth()
                      .height(48.dp)
                      .testTag("dialog_google_sign_out_button"),
                ) {
                  Text("Sign Out of Google")
                }
              } else {
                Button(
                  onClick = {
                    onGoogleSignIn()
                    onDismiss()
                  },
                  modifier =
                    Modifier.fillMaxWidth()
                      .height(48.dp)
                      .testTag("dialog_google_sign_in_button"),
                ) {
                  Text("Sign in with Google")
                }
              }
            }
          }
        }
      }
    }
  }
}
