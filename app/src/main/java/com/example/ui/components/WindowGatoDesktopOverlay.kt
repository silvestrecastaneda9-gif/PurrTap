package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private enum class GatoDesktopWindow(val title: String, val iconLabel: String) {
  FILE_MANAGER("Gato Explorer — C:\\Sanctuary\\Characters", "Files"),
  MAIL_CLIENT("Gato Mail — Inbox (5 Unread Clues)", "Gato Mail"),
  CORRUPT_AUDIO("Corrupt Audio Player v0.9", "Audio"),
  README_NOTE("README_VAULT.txt — Notepad", "README"),
}

private enum class GatoFileDeleteOutcome {
  UNKNOWN_ERROR,
  SCOLD_SELF,
  SCOLD_CAT,
}

private data class GatoSystemFile(
  val id: String,
  val name: String,
  val category: String,
  val sizeLabel: String,
  val statusTag: String,
  val isDeletedEntry: Boolean,
  val inspectSummary: String,
  val deleteOutcome: GatoFileDeleteOutcome,
)

private data class AnimalEmail(
  val id: String,
  val senderName: String,
  val senderSpecies: String,
  val address: String,
  val subject: String,
  val timestamp: String,
  val body: String,
)

private data class CorruptedAudioClip(
  val id: String,
  val fileName: String,
  val durationLabel: String,
  val corruptionPercent: Int,
  val sourceTag: String,
  val decodedTranscript: String,
)

private tailrec fun Context.findHostActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
  }

// Retro Windows desktop palette
private val WinDesktopTeal = Color(0xFF0A6E6E)
private val WinDesktopDeepTeal = Color(0xFF054545)
private val WinBevelGray = Color(0xFFD4D0C8)
private val WinDarkShadow = Color(0xFF404040)
private val WinTitleBlueStart = Color(0xFF0A246A)
private val WinTitleBlueEnd = Color(0xFFA6CAF0)
private val WinPaperBg = Color(0xFFFAFAFA)
private val WinInk = Color(0xFF14181F)
private val WinMutedInk = Color(0xFF525A66)

@Composable
fun WindowGatoDesktopOverlay(
  playerName: String,
  catName: String,
  totalPurrsEarned: Long,
  onPlayCorruptedAudio: (String) -> Unit,
  onPlayErrorSound: () -> Unit,
  onPlayCatScoldSound: () -> Unit,
  onExitDesktop: () -> Unit,
) {
  val context = LocalContext.current

  // Enforce landscape mode while Window Gato OS is running
  DisposableEffect(context) {
    val activity = context.findHostActivity()
    val previousOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    onDispose {
      activity?.requestedOrientation = previousOrientation
    }
  }

  BackHandler { onExitDesktop() }

  val safePlayerName = playerName.ifBlank { "Caretaker" }
  val safeCatName = catName.ifBlank { "Mochi" }
  val safePlayerFile = safePlayerName.replace(" ", "_").take(18) + ".usr"
  val safeCatFile = safeCatName.replace(" ", "_").take(18) + ".cat"

  val files =
    remember(safePlayerName, safeCatName, totalPurrsEarned) {
      listOf(
        GatoSystemFile(
          id = "user_self",
          name = safePlayerFile,
          category = "You (Caretaker)",
          sizeLabel = "64 KB",
          statusTag = "ACTIVE USER",
          isDeletedEntry = false,
          inspectSummary =
            "USER PROFILE: $safePlayerName\nRole: Primary Sanctuary Caretaker\nLifetime Purrs Recorded: $totalPurrsEarned\nNote: Cleared for petting duty, NOT cleared for Sector K9 root access.",
          deleteOutcome = GatoFileDeleteOutcome.SCOLD_SELF,
        ),
        GatoSystemFile(
          id = "your_cat",
          name = safeCatFile,
          category = "Your Cat",
          sizeLabel = "128 KB",
          statusTag = "PROTECTED",
          isDeletedEntry = false,
          inspectSummary =
            "COMPANION RECORD: $safeCatName\nStatus: Cozy & loafing in the Sanctuary.\nKernel flag: DO_NOT_UNINSTALL (Protected by Royal Cat Decree).",
          deleteOutcome = GatoFileDeleteOutcome.SCOLD_CAT,
        ),
        GatoSystemFile(
          id = "miga_deleted",
          name = "Miga the great",
          category = "Royal Archive [DELETED]",
          sizeLabel = "0 KB (PURGED)",
          statusTag = "DELETED FILE",
          isDeletedEntry = true,
          inspectSummary =
            "TOMBSTONE RECORD: \"Miga the great\"\nAction: > sudo delete \"Miga the great\"\nSource IP: Pug_The_Hacker (Terminal_09)\nTrace: A pair of glowing red eyes vanished right after executing the wipe. Fragment moved to Recycle Void.",
          deleteOutcome = GatoFileDeleteOutcome.UNKNOWN_ERROR,
        ),
        GatoSystemFile(
          id = "dog_keeper",
          name = "Dog_Vault_Keeper.sys",
          category = "Guard Daemon",
          sizeLabel = "256 KB",
          statusTag = "LOCKED",
          isDeletedEntry = false,
          inspectSummary =
            "DAEMON: Dog Vault Keeper\nDuties: Guard Sector K9 & quarantine corrupted World Of Cats backups.\nLog: \"If anyone reads this, they already solved my password...\"",
          deleteOutcome = GatoFileDeleteOutcome.UNKNOWN_ERROR,
        ),
        GatoSystemFile(
          id = "turkey_gato",
          name = "Turkey_Gato.dat",
          category = "Character Entity",
          sizeLabel = "48 KB",
          statusTag = "READ-ONLY",
          isDeletedEntry = false,
          inspectSummary =
            "CHARACTER FILE: Turkey Gato\nStatus: Hosting Autumn Harvest outside the Firewall.\nSuspicion: Reported static feathers near the Pug's relay.",
          deleteOutcome = GatoFileDeleteOutcome.UNKNOWN_ERROR,
        ),
        GatoSystemFile(
          id = "pug_hacker",
          name = "Pug_The_Hacker.exe",
          category = "Hostile Binary",
          sizeLabel = "999 KB",
          statusTag = "MALWARE",
          isDeletedEntry = false,
          inspectSummary =
            "WARNING: UNSIGNED EXECUTABLE\nSignature: \"Pug the Hacker\" — Squad of the Hackers.\nTarget Queue: 1) Miga the great [DONE], 2) World_Of_Cats.db [PENDING].",
          deleteOutcome = GatoFileDeleteOutcome.UNKNOWN_ERROR,
        ),
        GatoSystemFile(
          id = "world_of_cats",
          name = "World_Of_Cats.db",
          category = "World Kernel",
          sizeLabel = "4.09 GB",
          statusTag = "CRITICAL",
          isDeletedEntry = false,
          inspectSummary =
            "SANCTUARY WORLD DATABASE\nIntegrity: 61% — firewall cracks widening near Sector K9.\nAlert: Miga the Great's royal signature missing.",
          deleteOutcome = GatoFileDeleteOutcome.UNKNOWN_ERROR,
        ),
      )
    }

  val emails =
    remember(safePlayerName, safeCatName) {
      listOf(
        AnimalEmail(
          id = "email_owl",
          senderName = "Barnaby the Owl",
          senderSpecies = "Night Watch",
          address = "barnaby@nightbranch.cat",
          subject = "Saw red eyes right after Miga disappeared",
          timestamp = "Yesterday, 11:48 PM",
          body =
            "Hoo...\n\nI was perched above the Royal Hall when Miga the Great finished congratulating $safePlayerName and $safeCatName.\n\nThe second the curtain closed, the air snapped into green static. A stubby shadow with glowing red eyes — it looked like THAT PUG — typed 'sudo delete' and Miga the Great simply faded out of existence!\n\nThe Hackers are real. Don't let the Dog Keeper pretend everything is normal.\n\n— Barnaby",
        ),
        AnimalEmail(
          id = "email_bunny",
          senderName = "Clover the Bunny",
          senderSpecies = "Meadow Courier",
          address = "clover@meadowhop.burrow",
          subject = "Where did Miga the Great go?!",
          timestamp = "Today, 02:15 AM",
          body =
            "Please tell me someone else got Miga's final sticky note!!\n\nWhen I hopped by the palace, the server rack was chewed through and covered in peanut-butter dog kibble crumbs. Pip swears 'the Pug' leads a whole ring called the Hackers.\n\nEveryone thought the Dog Vault Keeper was just paranoid, but Miga the Great is actually gone from the directory.\n\n— Clover",
        ),
        AnimalEmail(
          id = "email_captain",
          senderName = "Captain Whiskers",
          senderSpecies = "Harbor Patrol",
          address = "whiskers@dockwatch.sea",
          subject = "Intercepted packet signed by 'Pug'",
          timestamp = "Today, 04:09 AM",
          body =
            "Ahoy,\n\nMy harbor radar caught an unencrypted burst on Port 999 tagged 'PUG_THE_HACKER'.\n\nThe message boasted: 'One royal feline erased. Keep chasing turkeys while the Hackers rewrite the World Of Cats.'\n\nCheck the .corrupt audio files in C:\\Audio if you have the guts to listen.\n\n— Capt. Whiskers",
        ),
        AnimalEmail(
          id = "email_pip",
          senderName = "Pip the Squirrel",
          senderSpecies = "Cache Scout",
          address = "pip@oakcache.nut",
          subject = "The Hackers left clues in Sector K9",
          timestamp = "Today, 06:32 AM",
          body =
            "Hey $safePlayerName,\n\nBefore Miga the Great disappeared, she split her warning across the Dog's Vault computer.\n\nWatch out for Pug the Hacker — he wears crimson night-vision goggles and uses the Dog Vault's own backdoor to test commands on deleted character files.\n\nKeep $safeCatName safe!\n\n— Pip",
        ),
        AnimalEmail(
          id = "email_dog",
          senderName = "Dog Vault Keeper",
          senderSpecies = "Vault Guardian",
          address = "keeper@sector-k9.vault",
          subject = "[PRIVATE] Stay out of Window Gato",
          timestamp = "Today, 07:00 AM",
          body =
            "NOTE TO SELF:\n\nPug used to be one of the Sanctuary's trusted kennel engineers before he joined the Hackers. When Miga the Great vanished right under my nose, I pulled the plug and locked this terminal with a 3-letter riddle.\n\nIf anyone from the Sanctuary breaks in without my permission, they'll finally know the truth.\n\n— Guard K9",
        ),
      )
    }

  val audioClips =
    remember(safePlayerName, safeCatName) {
      listOf(
        CorruptedAudioClip(
          id = "miga_last_purr.corrupt",
          fileName = "miga_last_purr.corrupt",
          durationLabel = "0:02.1",
          corruptionPercent = 44,
          sourceTag = "HALL_MIC_01 // AFTER GALA",
          decodedTranscript =
            "\"[...purr static...] Thank you $safePlayerName and $safeCatName... wait—who turned off the lights?! Those red eyes— [CRUNCH / DELETION CHIME]\"",
        ),
        CorruptedAudioClip(
          id = "red_eyes_port999.corrupt",
          fileName = "red_eyes_port999.corrupt",
          durationLabel = "0:02.1",
          corruptionPercent = 71,
          sourceTag = "INTERCEPT // PUG_UPLINK",
          decodedTranscript =
            "\"[DISTORTED SNORT] ...'Miga the great' deleted. The Hackers own the mainframe now. Good luck explaining this to the Sanctuary, little pug-hater... [BEEP]\"",
        ),
        CorruptedAudioClip(
          id = "pug_override_log.corrupt",
          fileName = "pug_override_log.corrupt",
          durationLabel = "0:02.1",
          corruptionPercent = 63,
          sourceTag = "SECTOR_K9 // BLACKBOX",
          decodedTranscript =
            "\"[MODEM SCREECH] ...Firewall status: fractured. Dog Keeper changed his password to 'cat'—hah! Pug the Hacker Phase 2 queued... [SIGNAL LOST]\"",
        ),
      )
    }

  var activeWindow by remember { mutableStateOf(GatoDesktopWindow.FILE_MANAGER) }
  var selectedFile by remember { mutableStateOf(files.first()) }
  var inspectedFile by remember { mutableStateOf<GatoSystemFile?>(null) }
  var deleteModalTitle by remember { mutableStateOf<String?>(null) }
  var deleteModalBody by remember { mutableStateOf<String?>(null) }
  var deleteModalIsJoke by remember { mutableStateOf(false) }

  var selectedEmail by remember { mutableStateOf(emails.first()) }
  var activeClipId by remember { mutableStateOf<String?>(null) }
  var showStartMenu by remember { mutableStateOf(false) }

  // Clear "playing" waveform animation ~2.2s after a clip starts
  LaunchedEffect(activeClipId) {
    if (activeClipId != null) {
      delay(2200L)
      activeClipId = null
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "corrupt_wave")
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Restart),
    label = "wave_phase",
  )

  val clockString =
    remember(activeWindow) {
      SimpleDateFormat("h:mm a", Locale.US).format(Date())
    }

  val handleAttemptDelete = { file: GatoSystemFile ->
    when (file.deleteOutcome) {
      GatoFileDeleteOutcome.SCOLD_SELF -> {
        onPlayCatScoldSound()
        deleteModalTitle = "Excuse Me?! (${file.name})"
        deleteModalBody =
          "Whoa, hold on $safePlayerName! Did you seriously just try to delete YOURSELF?!\n\nIf you vanish into the Recycle Bin, who's gonna pet $safeCatName and clean up the cosmic catnip?! Put that Delete key away right now!"
        deleteModalIsJoke = true
      }
      GatoFileDeleteOutcome.SCOLD_CAT -> {
        onPlayCatScoldSound()
        deleteModalTitle = "PAWS OFF! (${file.name})"
        deleteModalBody =
          "HISS!! Hey!! You just tried to delete YOUR OWN CAT ($safeCatName)!!\n\n$safeCatName knocked your coffee mug over in protest and locked the Trash Bin. Go apologize with extra head-scratches immediately!"
        deleteModalIsJoke = true
      }
      GatoFileDeleteOutcome.UNKNOWN_ERROR -> {
        onPlayErrorSound()
        deleteModalTitle = "Window Gato — Delete Failed"
        deleteModalBody =
          "Cannot delete \"${file.name}\": The system encountered an unknown error (Code 0x8007F41L).\n\nAccess handle is locked by an encrypted process in Sector K9."
        deleteModalIsJoke = false
      }
    }
  }

  Surface(
    color = WinDesktopTeal,
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(listOf(WinDesktopTeal, WinDesktopDeepTeal))
        )
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .testTag("window_gato_desktop"),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Desktop Workspace: Left Desktop Icons + Active Window Frame
        Row(
          modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Left Desktop Icon Column
          Column(
            modifier = Modifier.width(86.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            DesktopIconTile(
              label = "Files",
              icon = Icons.Rounded.Folder,
              tint = Color(0xFFFFD54F),
              selected = activeWindow == GatoDesktopWindow.FILE_MANAGER,
              tag = "desktop_icon_files",
              onClick = {
                showStartMenu = false
                activeWindow = GatoDesktopWindow.FILE_MANAGER
              },
            )
            DesktopIconTile(
              label = "Gato Mail",
              icon = Icons.Rounded.Email,
              tint = Color(0xFF90CAF9),
              selected = activeWindow == GatoDesktopWindow.MAIL_CLIENT,
              tag = "desktop_icon_mail",
              onClick = {
                showStartMenu = false
                activeWindow = GatoDesktopWindow.MAIL_CLIENT
              },
            )
            DesktopIconTile(
              label = "Audio",
              icon = Icons.Rounded.GraphicEq,
              tint = Color(0xFFEF9A9A),
              selected = activeWindow == GatoDesktopWindow.CORRUPT_AUDIO,
              tag = "desktop_icon_audio",
              onClick = {
                showStartMenu = false
                activeWindow = GatoDesktopWindow.CORRUPT_AUDIO
              },
            )
            DesktopIconTile(
              label = "README",
              icon = Icons.Rounded.Description,
              tint = Color(0xFFE0E0E0),
              selected = activeWindow == GatoDesktopWindow.README_NOTE,
              tag = "desktop_icon_readme",
              onClick = {
                showStartMenu = false
                activeWindow = GatoDesktopWindow.README_NOTE
              },
            )
          }

          // Active Classic Windows Frame
          Surface(
            color = WinBevelGray,
            shape = RoundedCornerShape(2.dp),
            border = BorderStroke(2.dp, Color.White),
            modifier =
              Modifier.weight(1f)
                .fillMaxHeight()
                .border(1.dp, WinDarkShadow),
          ) {
            Column(modifier = Modifier.fillMaxSize().padding(3.dp)) {
              // Classic Windows Blue Title Bar
              Row(
                modifier =
                  Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(WinTitleBlueStart, WinTitleBlueEnd)))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Computer,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                  )
                  Text(
                    text = activeWindow.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                Surface(
                  color = WinBevelGray,
                  border = BorderStroke(1.dp, WinDarkShadow),
                  modifier =
                    Modifier.size(width = 36.dp, height = 26.dp)
                      .clickable { onExitDesktop() }
                      .testTag("window_gato_title_close_button"),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Rounded.Close,
                      contentDescription = "Leave Window Gato",
                      tint = WinInk,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              // Window Client Area
              Surface(
                color = WinPaperBg,
                border = BorderStroke(1.5.dp, WinDarkShadow),
                modifier = Modifier.weight(1f).fillMaxWidth(),
              ) {
                when (activeWindow) {
                  GatoDesktopWindow.FILE_MANAGER ->
                    FileManagerPane(
                      files = files,
                      selectedFile = selectedFile,
                      onSelectFile = { selectedFile = it },
                      onInspectFile = { inspectedFile = it },
                      onDeleteFile = handleAttemptDelete,
                    )
                  GatoDesktopWindow.MAIL_CLIENT ->
                    MailClientPane(
                      emails = emails,
                      selectedEmail = selectedEmail,
                      onSelectEmail = { selectedEmail = it },
                    )
                  GatoDesktopWindow.CORRUPT_AUDIO ->
                    CorruptAudioPane(
                      clips = audioClips,
                      activeClipId = activeClipId,
                      wavePhase = wavePhase,
                      onPlayClip = { clip ->
                        activeClipId = clip.id
                        onPlayCorruptedAudio(clip.id)
                      },
                    )
                  GatoDesktopWindow.README_NOTE ->
                    ReadmeNotepadPane(
                      playerName = safePlayerName,
                      catName = safeCatName,
                    )
                }
              }
            }
          }
        }

        // Bottom Windows Taskbar
        Surface(
          color = WinBevelGray,
          border = BorderStroke(1.5.dp, Color.White),
          modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            // Start Button
            Surface(
              color = if (showStartMenu) Color(0xFFB8B4AC) else WinBevelGray,
              border = BorderStroke(1.5.dp, WinDarkShadow),
              modifier =
                Modifier.height(40.dp)
                  .clickable { showStartMenu = !showStartMenu }
                  .testTag("window_gato_start_button"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Computer,
                  contentDescription = null,
                  tint = Color(0xFF006600),
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "Start",
                  color = WinInk,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace,
                )
              }
            }

            // Taskbar window switch tabs
            Row(
              modifier = Modifier.weight(1f),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              GatoDesktopWindow.entries.forEach { win ->
                val isSelected = activeWindow == win
                Surface(
                  color = if (isSelected) Color.White else Color(0xFFC2BEB6),
                  border = BorderStroke(1.dp, if (isSelected) WinTitleBlueStart else WinDarkShadow),
                  modifier =
                    Modifier.height(38.dp)
                      .weight(1f)
                      .clickable {
                        showStartMenu = false
                        activeWindow = win
                      }
                      .testTag("taskbar_tab_${win.name.lowercase(Locale.US)}"),
                ) {
                  Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(
                      text = win.iconLabel,
                      color = WinInk,
                      fontSize = 11.sp,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }
              }
            }

            // Log Off / Exit Computer Button
            Button(
              onClick = onExitDesktop,
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFB71C1C),
                  contentColor = Color.White,
                ),
              shape = RoundedCornerShape(3.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.height(40.dp).testTag("window_gato_exit_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text(
                text = "Leave PC",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
              )
            }

            // System Tray Clock
            Surface(
              color = Color(0xFFC8C4BC),
              border = BorderStroke(1.dp, WinDarkShadow),
              modifier = Modifier.height(38.dp),
            ) {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                  text = clockString,
                  color = WinInk,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }

      // Optional Start Menu Popup
      if (showStartMenu) {
        Surface(
          color = WinBevelGray,
          border = BorderStroke(2.dp, WinDarkShadow),
          shadowElevation = 8.dp,
          modifier =
            Modifier.align(Alignment.BottomStart)
              .padding(start = 8.dp, bottom = 54.dp)
              .width(230.dp)
              .testTag("window_gato_start_menu"),
        ) {
          Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "WINDOW GATO v0.95",
              color = Color.White,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              modifier =
                Modifier.fillMaxWidth()
                  .background(WinTitleBlueStart)
                  .padding(horizontal = 8.dp, vertical = 5.dp),
            )
            GatoDesktopWindow.entries.forEach { win ->
              Row(
                modifier =
                  Modifier.fillMaxWidth()
                    .clickable {
                      activeWindow = win
                      showStartMenu = false
                    }
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Folder,
                  contentDescription = null,
                  tint = WinTitleBlueStart,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = win.iconLabel,
                  color = WinInk,
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
            HorizontalDivider(color = WinDarkShadow)
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clickable {
                    showStartMenu = false
                    onExitDesktop()
                  }
                  .padding(horizontal = 8.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = null,
                tint = Color(0xFFB71C1C),
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "Log Off & Leave PC",
                color = Color(0xFFB71C1C),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }

  // File Inspect Popup
  inspectedFile?.let { target ->
    AlertDialog(
      onDismissRequest = { inspectedFile = null },
      title = {
        Text(
          text = target.name,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "${target.category} • ${target.sizeLabel} • [${target.statusTag}]",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = if (target.isDeletedEntry) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
          )
          Text(
            text = target.inspectSummary,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { inspectedFile = null }) {
          Text("OK")
        }
      },
    )
  }

  // Unknown Error OR Jokey Scold Popup on Delete
  if (deleteModalTitle != null && deleteModalBody != null) {
    AlertDialog(
      onDismissRequest = {
        deleteModalTitle = null
        deleteModalBody = null
      },
      icon = {
        Icon(
          imageVector =
            if (deleteModalIsJoke) Icons.Rounded.SentimentVerySatisfied
            else Icons.Rounded.ErrorOutline,
          contentDescription = null,
          tint =
            if (deleteModalIsJoke) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error,
          modifier = Modifier.size(34.dp),
        )
      },
      title = {
        Text(
          text = deleteModalTitle ?: "",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Text(
          text = deleteModalBody ?: "",
          style = MaterialTheme.typography.bodyMedium,
        )
      },
      confirmButton = {
        Button(
          onClick = {
            deleteModalTitle = null
            deleteModalBody = null
          },
          modifier = Modifier.testTag("window_gato_delete_modal_ok"),
        ) {
          Text(if (deleteModalIsJoke) "Sorry, won't do it again!" else "OK")
        }
      },
    )
  }
}

@Composable
private fun DesktopIconTile(
  label: String,
  icon: ImageVector,
  tint: Color,
  selected: Boolean,
  tag: String,
  onClick: () -> Unit,
) {
  Surface(
    color = if (selected) Color(0x550A246A) else Color.Transparent,
    border = if (selected) BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)) else null,
    shape = RoundedCornerShape(4.dp),
    modifier =
      Modifier.fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 4.dp, horizontal = 2.dp)
        .testTag(tag),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp),
      modifier = Modifier.padding(4.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = tint,
        modifier = Modifier.size(28.dp),
      )
      Text(
        text = label,
        color = Color.White,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun FileManagerPane(
  files: List<GatoSystemFile>,
  selectedFile: GatoSystemFile,
  onSelectFile: (GatoSystemFile) -> Unit,
  onInspectFile: (GatoSystemFile) -> Unit,
  onDeleteFile: (GatoSystemFile) -> Unit,
) {
  Row(modifier = Modifier.fillMaxSize()) {
    // Left file list
    LazyColumn(
      modifier = Modifier.weight(1.35f).fillMaxHeight().padding(6.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      item {
        Text(
          text = "Name | Category | Status",
          color = WinMutedInk,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
      }
      items(files, key = { it.id }) { file ->
        val isSelected = selectedFile.id == file.id
        Surface(
          color =
            when {
              isSelected -> Color(0xFFD6E4FF)
              file.isDeletedEntry -> Color(0xFFFFEBEE)
              else -> Color.White
            },
          border =
            BorderStroke(
              1.dp,
              when {
                isSelected -> WinTitleBlueStart
                file.isDeletedEntry -> Color(0xFFE57373)
                else -> Color(0xFFE0E0E0)
              },
            ),
          modifier =
            Modifier.fillMaxWidth()
              .clickable { onSelectFile(file) }
              .testTag("gato_file_row_${file.id}"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = if (file.isDeletedEntry) Icons.Rounded.Warning else Icons.Rounded.Description,
              contentDescription = null,
              tint =
                if (file.isDeletedEntry) Color(0xFFC62828)
                else WinTitleBlueStart,
              modifier = Modifier.size(18.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = file.name,
                color = if (file.isDeletedEntry) Color(0xFFB71C1C) else WinInk,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "${file.category} • ${file.sizeLabel}",
                color = WinMutedInk,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Surface(
              color =
                if (file.isDeletedEntry) Color(0xFFB71C1C)
                else Color(0xFFECEFF1),
              shape = RoundedCornerShape(3.dp),
            ) {
              Text(
                text = file.statusTag,
                color = if (file.isDeletedEntry) Color.White else WinInk,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
              )
            }
          }
        }
      }
    }

    // Right Details & Actions Pane
    Column(
      modifier =
        Modifier.weight(1f)
          .fillMaxHeight()
          .background(Color(0xFFF2F3F5))
          .border(1.dp, Color(0xFFD0D5DD))
          .padding(10.dp)
          .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = selectedFile.name,
        color = if (selectedFile.isDeletedEntry) Color(0xFFB71C1C) else WinTitleBlueStart,
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.ExtraBold,
      )
      Text(
        text = "${selectedFile.category} (${selectedFile.sizeLabel})",
        color = WinMutedInk,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
      )
      HorizontalDivider(color = Color(0xFFD0D5DD))
      Text(
        text = selectedFile.inspectSummary,
        color = WinInk,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 15.sp,
      )
      Spacer(Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        OutlinedButton(
          onClick = { onInspectFile(selectedFile) },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier =
            Modifier.weight(1f).height(44.dp).testTag("file_inspect_${selectedFile.id}"),
        ) {
          Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(Modifier.width(4.dp))
          Text("Inspect", fontSize = 11.sp, color = WinInk)
        }
        Button(
          onClick = { onDeleteFile(selectedFile) },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier =
            Modifier.weight(1f).height(44.dp).testTag("file_delete_${selectedFile.id}"),
        ) {
          Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(Modifier.width(4.dp))
          Text("Delete", fontSize = 11.sp, color = Color.White)
        }
      }
    }
  }
}

@Composable
private fun MailClientPane(
  emails: List<AnimalEmail>,
  selectedEmail: AnimalEmail,
  onSelectEmail: (AnimalEmail) -> Unit,
) {
  Row(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier =
        Modifier.weight(0.95f)
          .fillMaxHeight()
          .background(Color(0xFFF3F4F6))
          .padding(6.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      items(emails, key = { it.id }) { email ->
        val isSelected = selectedEmail.id == email.id
        Surface(
          color = if (isSelected) Color(0xFF0A246A) else Color.White,
          border = BorderStroke(1.dp, if (isSelected) WinTitleBlueStart else Color(0xFFD5D7DC)),
          modifier =
            Modifier.fillMaxWidth()
              .clickable { onSelectEmail(email) }
              .testTag("gato_mail_item_${email.id}"),
        ) {
          Column(modifier = Modifier.padding(7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = "${email.senderName} (${email.senderSpecies})",
              color = if (isSelected) Color.White else WinInk,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = email.subject,
              color = if (isSelected) Color(0xFFE3F2FD) else WinMutedInk,
              fontSize = 11.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }

    // Email Reading Pane
    Column(
      modifier =
        Modifier.weight(1.45f)
          .fillMaxHeight()
          .background(WinPaperBg)
          .padding(12.dp)
          .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = selectedEmail.subject,
        color = WinInk,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "From: ${selectedEmail.senderName} <${selectedEmail.address}>",
        color = WinTitleBlueStart,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = "Received: ${selectedEmail.timestamp}",
        color = WinMutedInk,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
      )
      HorizontalDivider(color = Color(0xFFD0D5DD))
      Text(
        text = selectedEmail.body,
        color = WinInk,
        fontSize = 12.sp,
        lineHeight = 17.sp,
      )
    }
  }
}

@Composable
private fun CorruptAudioPane(
  clips: List<CorruptedAudioClip>,
  activeClipId: String?,
  wavePhase: Float,
  onPlayClip: (CorruptedAudioClip) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize().padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Surface(
      color = Color(0xFF10141A),
      shape = RoundedCornerShape(4.dp),
      border = BorderStroke(1.dp, Color(0xFF334155)),
      modifier = Modifier.fillMaxWidth().height(54.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text =
            if (activeClipId != null) "PLAYING: $activeClipId"
            else "SECTOR K9 AUDIO RECOVERER // SELECT A .CORRUPT TAPE",
          color = if (activeClipId != null) Color(0xFFFF5252) else Color(0xFF00E676),
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f),
        )
        Canvas(modifier = Modifier.width(120.dp).height(34.dp)) {
          val bars = 16
          val step = size.width / bars
          for (i in 0 until bars) {
            val hRatio =
              if (activeClipId != null) {
                val wobble = kotlin.math.abs(kotlin.math.sin((wavePhase * 6.28f) + i * 0.7f))
                (0.25f + wobble * 0.75f).coerceIn(0.15f, 1f)
              } else {
                0.15f
              }
            val barH = size.height * hRatio
            drawRect(
              color = if (activeClipId != null) Color(0xFFFF5252) else Color(0xFF00E676),
              topLeft = Offset(i * step + 1f, (size.height - barH) / 2f),
              size = Size((step - 3f).coerceAtLeast(2f), barH),
            )
          }
        }
      }
    }

    LazyColumn(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      items(clips, key = { it.id }) { clip ->
        val isPlaying = activeClipId == clip.id
        Surface(
          color = if (isPlaying) Color(0xFFFFEBEE) else Color.White,
          border = BorderStroke(1.dp, if (isPlaying) Color(0xFFD32F2F) else Color(0xFFD5D7DC)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Button(
              onClick = { onPlayClip(clip) },
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = if (isPlaying) Color(0xFFC62828) else WinTitleBlueStart
                ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.height(44.dp).testTag("play_corrupt_audio_${clip.id}"),
            ) {
              Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play ${clip.fileName}",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(3.dp))
              Text(
                text = if (isPlaying) "Glitching" else "Play",
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
              )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "${clip.fileName} (${clip.durationLabel}) • ${clip.corruptionPercent}% CORRUPTED",
                color = WinInk,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "${clip.sourceTag}: ${clip.decodedTranscript}",
                color = WinMutedInk,
                fontSize = 11.sp,
                lineHeight = 14.sp,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ReadmeNotepadPane(
  playerName: String,
  catName: String,
) {
  Column(
    modifier =
      Modifier.fillMaxSize()
        .background(WinPaperBg)
        .padding(14.dp)
        .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = "WINDOW GATO // EMERGENCY ARCHIVE NODE (SECTOR K9)",
      color = WinTitleBlueStart,
      fontSize = 13.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text =
        "Logged In As: $playerName (via Dog Vault Keeper's Override)\nProtected Sanctuary Companion: $catName\n\nSUMMARY:\n1. After Miga the Great summoned you at 1 Trillion Purrs, a remote root command erased 'Miga the great'.\n2. Surveillance frames show glowing red visor eyes matching the rogue engineer known as 'Pug the Hacker'.\n3. Do NOT attempt to wipe character directories — leave Sector K9 when finished reviewing the emails and corrupted audio tapes.",
      color = WinInk,
      fontSize = 12.sp,
      fontFamily = FontFamily.Monospace,
      lineHeight = 18.sp,
    )
  }
}
