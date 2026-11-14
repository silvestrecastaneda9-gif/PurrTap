package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Dehaze
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Looks
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.GameCatalog
import com.example.data.model.MoonlightPhase
import com.example.data.weather.WeatherConditionKind
import com.example.ui.components.InteractivePurrCat
import com.example.ui.components.WeatherParticlesOverlay
import com.example.ui.viewmodel.PurrGameUiState
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SanctuaryScreen(
  uiState: PurrGameUiState,
  onPetCat: (Float, Float) -> Unit,
  onOpenDailyRewards: () -> Unit,
  onOpenWeatherSync: () -> Unit,
  onOpenEventShop: () -> Unit,
  onOpenNovemberPass: () -> Unit,
  onOpenInventory: () -> Unit,
  onOpenChallengeDialog: () -> Unit,
  onOpenSettings: () -> Unit,
  onNavigateToCustomize: () -> Unit,
  onSignInClick: () -> Unit,
  onSignOut: () -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val numberFormat = NumberFormat.getNumberInstance(Locale.US)
  val animatedCombo by animateFloatAsState(targetValue = uiState.comboCharge, label = "comboBar")

  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 640.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Top Bar: Player + Weather + Daily Rewards + Settings + Optional Cloud Sync / Sign Out
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "PurrTap Sanctuary",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text =
              when {
                uiState.isSignedIn -> "Caretaker ${uiState.profile.displayName} • Google Cloud"
                uiState.settings.accountMode == "local" ->
                  "Caretaker ${uiState.profile.displayName} • ${uiState.settings.localAccountCode.ifBlank { "Local ID" }}"
                else -> "Guest Caretaker • Local"
              },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Surface(
            modifier =
              Modifier.clip(CircleShape)
                .clickable(onClick = onOpenWeatherSync)
                .testTag("weather_pill_button"),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = CircleShape,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = weatherIcon(uiState.weather.condition),
                contentDescription = "Game weather",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "${uiState.weather.temperatureC.toInt()}°C",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
            }
          }

          IconButton(
            onClick = onOpenDailyRewards,
            modifier = Modifier.testTag("daily_rewards_button"),
          ) {
            BadgedBox(
              badge = {
                if (uiState.canClaimDailyReward) {
                  Badge { Text("!") }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Rounded.CardGiftcard,
                contentDescription = "Daily Rewards",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }

          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("settings_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Settings,
              contentDescription = "Settings",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          if (uiState.isSignedIn) {
            IconButton(
              onClick = onSignInClick,
              modifier = Modifier.testTag("sign_out_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = "Account & Sign-In Options",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          } else {
            IconButton(
              onClick = onSignInClick,
              modifier = Modifier.testTag("cloud_sync_sign_in_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = "Sign-In & Account Options",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }

      // Purrs / Hardcore Purrs Balance & Rate Dashboard Card
      val isHardcore = uiState.challengeState.isHardcoreMode
      Card(
        colors =
          CardDefaults.cardColors(
            containerColor =
              if (isHardcore) MaterialTheme.colorScheme.errorContainer
              else MaterialTheme.colorScheme.primaryContainer
          ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = if (isHardcore) "HARDCORE PURRS BALANCE" else "PURRS BALANCE",
              style = MaterialTheme.typography.labelSmall,
              color =
                if (isHardcore) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
            )
            Text(
              text = GameCatalog.formatCompactPurrs(uiState.activeCurrencyBalance),
              style = MaterialTheme.typography.displaySmall,
              color =
                if (isHardcore) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.testTag("purrs_balance_text"),
            )
            Text(
              text =
                "Lifetime: ${GameCatalog.formatCompactPurrs(uiState.profile.totalPurrsEarned)} • ${numberFormat.format(uiState.profile.totalPets)} pets",
              style = MaterialTheme.typography.bodySmall,
              color =
                if (isHardcore) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
          }

          Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            StatChip(
              icon = Icons.Rounded.TouchApp,
              label = "+${GameCatalog.formatCompactPurrs(uiState.effectiveClickPower)}/tap",
            )
            StatChip(
              icon = Icons.Rounded.AutoAwesome,
              label = "+${GameCatalog.formatCompactPurrs(uiState.effectivePps)}/sec",
            )
            StatChip(
              icon = Icons.Rounded.Diamond,
              label = "${numberFormat.format(uiState.challengeState.gems)} Gems",
              modifier = Modifier.testTag("gems_balance_text"),
            )
          }
        }
      }

      if (uiState.gemFrenzyActive) {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
          border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary),
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth().testTag("gem_frenzy_active_banner"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Diamond,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = "STARLIGHT GEM FRENZY • 3.0x All Purrs",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
            }
            Text(
              text = uiState.gemFrenzyCountdownText,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.tertiary,
            )
          }
        }
      }

      // Rebirth Prestige, Gato Ascensión & Hardcore Mode Status Card
      Card(
        onClick = onOpenChallengeDialog,
        colors =
          CardDefaults.cardColors(
            containerColor =
              if (isHardcore) MaterialTheme.colorScheme.errorContainer
              else MaterialTheme.colorScheme.surfaceVariant
          ),
        border =
          if (isHardcore) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error) else null,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("challenge_rebirth_card"),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector =
                if (isHardcore) Icons.Rounded.LocalFireDepartment else Icons.Rounded.RestartAlt,
              contentDescription = null,
              tint = if (isHardcore) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = "Rebirth #${uiState.challengeState.rebirthCount} • ${uiState.rebirthMultiplier}x",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color =
                    if (isHardcore) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onSurface,
                )
                if (uiState.challengeState.gatoShards > 0L || uiState.challengeState.ascensionCount > 0L) {
                  Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = CircleShape,
                  ) {
                    Text(
                      text = "${uiState.challengeState.gatoShards} Shards",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onTertiaryContainer,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                  }
                }
                Surface(
                  color =
                    when {
                      isHardcore -> MaterialTheme.colorScheme.error
                      uiState.canAscend -> MaterialTheme.colorScheme.tertiary
                      uiState.hardcoreEligible -> MaterialTheme.colorScheme.secondary
                      else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                    },
                  shape = CircleShape,
                ) {
                  Text(
                    text =
                      when {
                        isHardcore -> "HARDCORE"
                        uiState.canAscend -> "ASCEND READY"
                        uiState.hardcoreEligible -> "HC READY"
                        else -> "HC LOCKED"
                      },
                    style = MaterialTheme.typography.labelSmall,
                    color =
                      when {
                        isHardcore -> MaterialTheme.colorScheme.onError
                        uiState.canAscend -> MaterialTheme.colorScheme.onTertiary
                        uiState.hardcoreEligible -> MaterialTheme.colorScheme.onSecondary
                        else -> MaterialTheme.colorScheme.onSurface
                      },
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                  )
                }
              }
              Text(
                text =
                  if (uiState.canAscend) {
                    "Gato Ascensión Ready (5 Rebirths) • Tap for Ascension Tree!"
                  } else {
                    "Next Rebirth: ${GameCatalog.formatCompactPurrs(uiState.nextRebirthCost)} ${uiState.activeCurrencyShortLabel} • ${uiState.challengeState.rebirthCount}/5 to Ascend"
                  },
                style = MaterialTheme.typography.bodySmall,
                color =
                  if (isHardcore) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                  else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Text(
            text = "Open →",
            style = MaterialTheme.typography.labelLarge,
            color = if (isHardcore) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
          )
        }
      }

      // November Pass (30 Tiers) & Golden Touch / Golden Rain Card
      val (freeIntoStep, freeStepMax) = uiState.freePassStepProgress
      val passBarProgress = (freeIntoStep.toFloat() / freeStepMax.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
      val totalPassReady = uiState.claimablePassCount + uiState.claimablePassQuestsCount
      Card(
        onClick = onOpenNovemberPass,
        colors =
          CardDefaults.cardColors(
            containerColor =
              if (uiState.goldenRainActive) Color(0xFFFFF1B8)
              else if (uiState.battlePassState.vipUnlocked) Color(0xFF3A2708)
              else MaterialTheme.colorScheme.secondaryContainer
          ),
        border =
          BorderStroke(
            1.5.dp,
            if (uiState.goldenRainActive || uiState.battlePassState.vipUnlocked) Color(0xFFFFCA28)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
          ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("november_pass_card"),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
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
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint =
                  if (uiState.goldenRainActive) Color(0xFFB36B00)
                  else if (uiState.battlePassState.vipUnlocked) Color(0xFFFFD54F)
                  else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Text(
                    text =
                      if (uiState.battlePassState.vipUnlocked)
                        "November Pass • Free ${uiState.currentPassTier}/30 • VIP ${uiState.currentVipPassTier}/30"
                      else "November Pass • Tier ${uiState.currentPassTier}/30",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color =
                      if (uiState.goldenRainActive) Color(0xFF3E2600)
                      else if (uiState.battlePassState.vipUnlocked) Color(0xFFFFF8E1)
                      else MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  if (uiState.battlePassState.vipUnlocked) {
                    Surface(color = Color(0xFFFFCA28), shape = CircleShape) {
                      Text(
                        text = "VIP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF332000),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                      )
                    }
                  }
                  if (totalPassReady > 0) {
                    Surface(color = MaterialTheme.colorScheme.error, shape = CircleShape) {
                      Text(
                        text = "$totalPassReady READY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                      )
                    }
                  }
                }
                Text(
                  text =
                    when {
                      uiState.goldenRainActive ->
                        "GOLDEN RAIN (4.0x Purrs!) • ${uiState.goldenRainCountdownText} remaining"
                      uiState.profile.selectedPatternId == GameCatalog.GOLDEN_TOUCH_SKIN_ID ->
                        "Golden Touch charging Golden Rain: ${uiState.goldenTouchProgressText}"
                      else ->
                        "Tier 30 Free: November Maple • VIP: Golden Touch (4.0x Golden Rain)"
                    },
                  style = MaterialTheme.typography.bodySmall,
                  color =
                    if (uiState.goldenRainActive) Color(0xFF6B4200)
                    else if (uiState.battlePassState.vipUnlocked) Color(0xFFFFE082)
                    else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                )
              }
            }
            Text(
              text = "Pass →",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color =
                if (uiState.goldenRainActive) Color(0xFF8C5300)
                else if (uiState.battlePassState.vipUnlocked) Color(0xFFFFD54F)
                else MaterialTheme.colorScheme.primary,
            )
          }
          LinearProgressIndicator(
            progress = { passBarProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color =
              if (uiState.goldenRainActive || uiState.battlePassState.vipUnlocked) Color(0xFFFFCA28)
              else MaterialTheme.colorScheme.primary,
            trackColor = Color.Black.copy(alpha = 0.14f),
            strokeCap = StrokeCap.Round,
          )
        }
      }

      // Periodic Limited-Time Event Card (tap to open Pop-Up Event Boutique!)
      Card(
        onClick = onOpenEventShop,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().testTag("live_event_card"),
      ) {
        Box(modifier = Modifier.fillMaxWidth().height(104.dp)) {
          Image(
            painter = painterResource(id = R.drawable.img_event_festival),
            contentDescription = "Limited-time event banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(Color(0xD91A1633), Color(0x991A1633), Color(0x661A1633))
                  )
                )
          )
          Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
              ) {
                Surface(
                  color = MaterialTheme.colorScheme.tertiary,
                  shape = CircleShape,
                ) {
                  Text(
                    text = "LIVE EVENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  )
                }
                Text(
                  text = uiState.activeEvent.title,
                  style = MaterialTheme.typography.titleMedium,
                  color = Color.White,
                )
              }
              Text(
                text =
                  if (uiState.activeEvent.id == "starlight_festival") {
                    "Ends Nov 18 (${uiState.eventCountdownText})"
                  } else {
                    uiState.eventCountdownText
                  },
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFFD86B),
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text =
                  if (uiState.activeEvent.id == "starlight_festival") {
                    "${uiState.activeEvent.boostLabel} • Thanksgiving Preview inside"
                  } else {
                    "${uiState.activeEvent.boostLabel} • ${uiState.weather.condition.buffTitle}"
                  },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF3EBF8),
                modifier = Modifier.weight(1f).padding(end = 8.dp),
              )
              Surface(
                color = Color(0xFFFFD86B),
                shape = CircleShape,
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Storefront,
                    contentDescription = null,
                    tint = Color(0xFF281900),
                    modifier = Modifier.size(13.dp),
                  )
                  Text(
                    text = "Event Shop",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF281900),
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          }
        }
      }

      // Midnight-to-Morning Moonlight / Bloodmoon Event Card
      val moonlightPhase = uiState.moonlightState.phase
      val moonlightContainer =
        when (moonlightPhase) {
          MoonlightPhase.BLOODMOON -> MaterialTheme.colorScheme.errorContainer
          MoonlightPhase.MOONLIGHT -> MaterialTheme.colorScheme.secondaryContainer
          MoonlightPhase.INACTIVE -> MaterialTheme.colorScheme.surfaceVariant
        }
      val moonlightContent =
        when (moonlightPhase) {
          MoonlightPhase.BLOODMOON -> MaterialTheme.colorScheme.onErrorContainer
          MoonlightPhase.MOONLIGHT -> MaterialTheme.colorScheme.onSecondaryContainer
          MoonlightPhase.INACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
        }
      Card(
        onClick = onOpenEventShop,
        colors = CardDefaults.cardColors(containerColor = moonlightContainer),
        border =
          if (moonlightPhase == MoonlightPhase.BLOODMOON) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
          } else null,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("moonlight_event_card"),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = moonlightPhase.title,
                  style = MaterialTheme.typography.titleSmall,
                  color = moonlightContent,
                )
                if (moonlightPhase != MoonlightPhase.INACTIVE) {
                  Surface(
                    color =
                      if (moonlightPhase == MoonlightPhase.BLOODMOON) MaterialTheme.colorScheme.error
                      else MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                  ) {
                    Text(
                      text =
                        if (moonlightPhase == MoonlightPhase.BLOODMOON) "33% RARE" else "ACTIVE",
                      style = MaterialTheme.typography.labelSmall,
                      color =
                        if (moonlightPhase == MoonlightPhase.BLOODMOON)
                          MaterialTheme.colorScheme.onError
                        else MaterialTheme.colorScheme.onPrimary,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                  }
                }
              }
              Text(
                text =
                  if (moonlightPhase == MoonlightPhase.INACTIVE) {
                    uiState.moonlightState.statusText
                  } else {
                    "${moonlightPhase.boostLabel} • ${uiState.moonlightState.statusText}"
                  },
                style = MaterialTheme.typography.bodySmall,
                color = moonlightContent.copy(alpha = 0.9f),
              )
            }
          }
          Text(
            text = "Shop →",
            style = MaterialTheme.typography.labelLarge,
            color =
              if (moonlightPhase == MoonlightPhase.BLOODMOON) MaterialTheme.colorScheme.error
              else MaterialTheme.colorScheme.primary,
          )
        }
      }

      // Interactive Cat Petting Stage
      Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f).padding(end = 6.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
              Text(
                text = uiState.profile.selectedCatName,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              FilledTonalButton(
                onClick = onOpenInventory,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(48.dp).testTag("inventory_button"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Inventory2,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("Inventory", maxLines = 1)
              }
              FilledTonalButton(
                onClick = onNavigateToCustomize,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(48.dp).testTag("customize_cat_shortcut"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Edit,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("Style", maxLines = 1)
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Stage Box with Cozy Background + Interactive Canvas Cat + Floating Tap Particles
          BoxWithConstraints(
            modifier =
              Modifier.fillMaxWidth()
                .height(280.dp)
                .clip(MaterialTheme.shapes.large)
                .background(
                  Brush.verticalGradient(
                    colors =
                      listOf(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                      )
                  )
                )
                .testTag("cat_petting_stage")
          ) {
            val boxW = constraints.maxWidth
            val boxH = constraints.maxHeight

            Image(
              painter = painterResource(id = R.drawable.img_sanctuary_banner),
              contentDescription = null,
              modifier = Modifier.fillMaxSize(),
              alpha = 0.24f,
              contentScale = ContentScale.Crop,
            )

            InteractivePurrCat(
              catName = uiState.profile.selectedCatName,
              patternId = uiState.profile.selectedPatternId,
              accessoryId = uiState.profile.selectedAccessoryId,
              weatherCondition = uiState.weather.condition,
              moonlightPhase = uiState.moonlightState.phase,
              comboCharge = uiState.comboCharge,
              isGoldenRainActive = uiState.goldenRainActive,
              onPetAt = { xFrac, yFrac ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPetCat(xFrac, yFrac)
              },
              modifier = Modifier.fillMaxSize(),
            )

            // Floating Tap Particles
            uiState.particles.forEach { particle ->
              Surface(
                color =
                  when {
                    particle.isGemDrop -> MaterialTheme.colorScheme.secondary
                    particle.isComboCrit -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                  },
                shape = CircleShape,
                shadowElevation = 4.dp,
                modifier =
                  Modifier.offset {
                    IntOffset(
                      x = (particle.xFraction * boxW).roundToInt() - 36,
                      y = (particle.yFraction * boxH).roundToInt() - 48,
                    )
                  },
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                  Icon(
                    imageVector =
                      if (particle.isGemDrop) Icons.Rounded.Diamond else Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp),
                  )
                  Text(
                    text = particle.text,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }

            Surface(
              modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
              shape = CircleShape,
            ) {
              Text(
                text = "Tap ${uiState.profile.selectedCatName} to pet!",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Purr Frenzy Combo Meter
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.FlashOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = "Purr Frenzy Combo",
                style = MaterialTheme.typography.titleSmall,
              )
            }
            Text(
              text = String.format(Locale.US, "%.2fx", uiState.comboMultiplier),
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          LinearProgressIndicator(
            progress = { animatedCombo },
            modifier = Modifier.fillMaxWidth().height(10.dp),
            color =
              if (uiState.comboMultiplier >= 2.0) MaterialTheme.colorScheme.tertiary
              else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface,
            strokeCap = StrokeCap.Round,
          )
        }
      }

      // Game Weather Active Buff Banner
      Card(
        onClick = onOpenWeatherSync,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("weather_banner_card"),
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
            Icon(
              imageVector = weatherIcon(uiState.weather.condition),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(24.dp),
            )
            Column {
              Text(
                text = "${uiState.weather.condition.label} • ${uiState.weather.condition.buffTitle}",
                style = MaterialTheme.typography.titleSmall,
              )
              Text(
                text = "${uiState.weather.condition.buffDescription} (Next in ${uiState.weather.nextShiftSeconds}s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Text(
            text = "Forecast",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }

    // Full-screen ambient visual weather & Golden Rain particle overlay (non-interactive)
    WeatherParticlesOverlay(
      weatherCondition = uiState.weather.condition,
      isGoldenRainActive = uiState.goldenRainActive,
      modifier = Modifier.fillMaxSize(),
    )
  }
}

@Composable
private fun StatChip(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    shape = CircleShape,
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(14.dp),
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

private fun weatherIcon(condition: WeatherConditionKind) =
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
