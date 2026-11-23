package com.example.ui.components

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Phishing
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeastZoneActivityType
import com.example.data.model.GameCatalog
import com.example.data.model.Part2MapLandmark
import com.example.ui.viewmodel.PurrGameUiState
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun FeastZoneAreaScreen(
  landmark: Part2MapLandmark,
  uiState: PurrGameUiState,
  onGrantZoneReward: (zoneTitle: String, basePurrs: Long, turkeys: Long, passXp: Long, bonusGems: Long) -> Long,
  onTriggerBossCutscene: () -> Unit,
  onExitZone: () -> Unit,
) {
  BackHandler(onBack = onExitZone)

  val zoneGradient =
    when (landmark.activityType) {
      FeastZoneActivityType.BOSS_BATTLE ->
        Brush.verticalGradient(listOf(Color(0xFF280B12), Color(0xFF4B150E), Color(0xFF1A070B)))
      FeastZoneActivityType.FISHING ->
        Brush.verticalGradient(listOf(Color(0xFF3E1C00), Color(0xFF7B3E07), Color(0xFF261202)))
      FeastZoneActivityType.MINIGAME_BAKERY ->
        Brush.verticalGradient(listOf(Color(0xFF3F1D10), Color(0xFF6B3219), Color(0xFF281209)))
      FeastZoneActivityType.MINIGAME_CRANBERRY ->
        Brush.verticalGradient(listOf(Color(0xFF360B1E), Color(0xFF691834), Color(0xFF1F0812)))
      FeastZoneActivityType.COOKING ->
        Brush.verticalGradient(listOf(Color(0xFF331B04), Color(0xFF6B3C0A), Color(0xFF211002)))
    }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(zoneGradient)
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .testTag("feast_zone_screen_${landmark.id}"),
  ) {
    Column(
      modifier =
        Modifier.align(Alignment.TopCenter)
          .fillMaxSize()
          .widthIn(max = 620.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Top Area Navigation Header
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
            color = Color(0x33FFFFFF),
            shape = CircleShape,
          ) {
            IconButton(
              onClick = onExitZone,
              modifier = Modifier.size(48.dp).testTag("exit_feast_zone_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back to Feast Map",
                tint = Color.White,
              )
            }
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = landmark.title,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = landmark.subtitle,
              style = MaterialTheme.typography.labelMedium,
              color = Color(0xFFFFD54F),
              fontWeight = FontWeight.Bold,
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Surface(color = Color(0x44FFFFFF), shape = CircleShape) {
            Text(
              text = GameCatalog.formatCompactPurrs(uiState.activeCurrencyBalance),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
          }
          Surface(color = Color(0xFFFFE0B2), shape = CircleShape) {
            Text(
              text = "${uiState.challengeState.turkeys} 🦃",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF4E2300),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
          }
        }
      }

      when (landmark.activityType) {
        FeastZoneActivityType.MINIGAME_CRANBERRY ->
          CranberryMeadowZone(
            onFinish = { score ->
              val turkeys = if (score >= 85) 2L else if (score >= 30) 1L else 0L
              val basePurrs = maxOf(1_500L, score * 95L)
              onGrantZoneReward("Cranberry Meadow", basePurrs, turkeys, 30L, 0L)
            },
          )

        FeastZoneActivityType.MINIGAME_BAKERY ->
          SpicedPieBakeryZone(
            onFinish = { ordersCompleted ->
              val turkeys = if (ordersCompleted >= 3) 2L else 1L
              val basePurrs = ordersCompleted * 3_500L
              onGrantZoneReward("Spiced Pie Bakery", basePurrs, turkeys, 35L, 0L)
            },
          )

        FeastZoneActivityType.FISHING ->
          GoldenGravyFishingZone(
            onCatchReward = { catchTitle, basePurrs, turkeys, gems ->
              onGrantZoneReward(catchTitle, basePurrs, turkeys, 28L, gems)
            },
          )

        FeastZoneActivityType.COOKING ->
          TurkeyFeastCookingZone(
            onServeFeast = { stars, basePurrs, turkeys ->
              onGrantZoneReward("Banquet Kitchen ($stars★)", basePurrs, turkeys, 40L, 0L)
            },
          )

        FeastZoneActivityType.BOSS_BATTLE ->
          CornucopiaCitadelBossZone(
            uiState = uiState,
            onTriggerBossCutscene = onTriggerBossCutscene,
            onExitZone = onExitZone,
          )
      }
    }
  }
}

private data class BerryTarget(
  val id: Int,
  val kind: Int, // 0 = Cranberry (+10), 1 = Golden Berry (+25), 2 = Spoiled Acorn (-12)
  val xFraction: Float,
  val yFraction: Float,
)

@Composable
private fun CranberryMeadowZone(
  onFinish: (score: Int) -> Long,
) {
  var playing by remember { mutableStateOf(false) }
  var finished by remember { mutableStateOf(false) }
  var secondsLeft by remember { mutableIntStateOf(16) }
  var score by remember { mutableIntStateOf(0) }
  var lastPurrsEarned by remember { mutableLongStateOf(0L) }
  var nextId by remember { mutableIntStateOf(1) }
  val targets = remember { mutableStateListOf<BerryTarget>() }

  LaunchedEffect(playing) {
    if (!playing) return@LaunchedEffect
    secondsLeft = 16
    score = 0
    finished = false
    targets.clear()
    var elapsedTicks = 0
    while (secondsLeft > 0 && playing) {
      if (targets.size < 4) {
        val roll = Random.nextInt(100)
        val kind =
          when {
            roll < 18 -> 2
            roll < 38 -> 1
            else -> 0
          }
        targets.add(
          BerryTarget(
            id = nextId++,
            kind = kind,
            xFraction = 0.14f + Random.nextFloat() * 0.72f,
            yFraction = 0.14f + Random.nextFloat() * 0.72f,
          )
        )
      }
      delay(450L)
      elapsedTicks++
      if (elapsedTicks % 2 == 0) {
        secondsLeft = (secondsLeft - 1).coerceAtLeast(0)
        if (targets.size > 3) {
          targets.removeAt(0)
        }
      }
    }
    if (playing) {
      playing = false
      finished = true
      targets.clear()
      lastPurrsEarned = onFinish(score)
    }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = Color(0x44000000)),
    border = BorderStroke(1.5.dp, Color(0xFFF48FB1).copy(alpha = 0.55f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(Icons.Rounded.SportsEsports, contentDescription = null, tint = Color(0xFFFF80AB))
          Text(
            text = "Cranberry Harvest Catch",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        }
        Surface(color = Color(0x55FF80AB), shape = CircleShape) {
          Text(
            text = if (playing) "${secondsLeft}s • $score pts" else "Goal: 85+ pts = +2 🦃",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Text(
        text = "Tap Ruby Cranberries (+10) & Golden Berries (+25)! Avoid Spoiled Acorns (-12).",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFFFCE4EC),
      )

      BoxWithConstraints(
        modifier =
          Modifier.fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
              Brush.verticalGradient(listOf(Color(0xFF4A0E24), Color(0xFF7B1F3A), Color(0xFF331118)))
            )
            .testTag("cranberry_play_field"),
      ) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val btnPx = with(density) { 56.dp.toPx() }.roundToInt()

        if (!playing && !finished) {
          Column(
            modifier = Modifier.align(Alignment.Center).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              text = "Meadow Harvest Ready!",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
            Button(
              onClick = { playing = true },
              modifier = Modifier.height(48.dp).testTag("cranberry_start_button"),
            ) {
              Text("Start 16s Harvest", fontWeight = FontWeight.Bold)
            }
          }
        } else if (finished) {
          Column(
            modifier = Modifier.align(Alignment.Center).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "Harvest Score: $score pts!",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFFD54F),
            )
            Text(
              text =
                "Earned ${GameCatalog.formatCompactPurrs(lastPurrsEarned)} Purrs" +
                  if (score >= 85) " & +2 🦃 Turkeys!" else if (score >= 30) " & +1 🦃 Turkey!" else "!",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White,
              textAlign = TextAlign.Center,
            )
            Button(
              onClick = { playing = true },
              modifier = Modifier.height(48.dp).testTag("cranberry_replay_button"),
            ) {
              Text("Play Again", fontWeight = FontWeight.Bold)
            }
          }
        } else {
          targets.forEach { target ->
            val tx =
              ((target.xFraction * wPx).roundToInt() - btnPx / 2).coerceIn(
                4,
                (wPx.roundToInt() - btnPx - 4).coerceAtLeast(4),
              )
            val ty =
              ((target.yFraction * hPx).roundToInt() - btnPx / 2).coerceIn(
                4,
                (hPx.roundToInt() - btnPx - 4).coerceAtLeast(4),
              )
            val (bg, label, pts) =
              when (target.kind) {
                1 -> Triple(Color(0xFFFFB300), "🌟\n+25", 25)
                2 -> Triple(Color(0xFF4E342E), "🌰\n-12", -12)
                else -> Triple(Color(0xFFD81B60), "🍒\n+10", 10)
              }
            Surface(
              onClick = {
                score = (score + pts).coerceAtLeast(0)
                targets.removeAll { it.id == target.id }
              },
              shape = CircleShape,
              color = bg,
              border = BorderStroke(2.dp, Color.White),
              shadowElevation = 6.dp,
              modifier =
                Modifier.offset { IntOffset(tx, ty) }
                  .size(56.dp)
                  .testTag("cranberry_target_${target.id}"),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = label,
                  color = Color.White,
                  fontSize = 11.sp,
                  lineHeight = 12.sp,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center,
                )
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpicedPieBakeryZone(
  onFinish: (ordersCompleted: Int) -> Long,
) {
  val pantry = remember { listOf("Pumpkin", "Cinnamon", "Whipped Cream", "Brown Sugar", "Pecan") }
  var playing by remember { mutableStateOf(false) }
  var finished by remember { mutableStateOf(false) }
  var orderIndex by remember { mutableIntStateOf(0) }
  var stepIndex by remember { mutableIntStateOf(0) }
  var ovenTaps by remember { mutableIntStateOf(0) }
  var lastPurrsEarned by remember { mutableLongStateOf(0L) }
  var recipe by remember { mutableStateOf(listOf("Pumpkin", "Cinnamon", "Whipped Cream")) }
  var statusNote by remember { mutableStateOf("Match the 3 ingredients in order, then smash Bake!") }

  fun rollRecipe() {
    recipe = pantry.shuffled().take(3)
    stepIndex = 0
    ovenTaps = 0
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = Color(0x44000000)),
    border = BorderStroke(1.5.dp, Color(0xFFFFB74D).copy(alpha = 0.6f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Spiced Pie Order Dash",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White,
        )
        Surface(color = Color(0x55FFB300), shape = CircleShape) {
          Text(
            text = if (playing) "Order ${orderIndex + 1} / 3" else "3 Orders = +2 🦃",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      if (!playing && !finished) {
        Text(
          text = "Assemble 3 custom Harvest Pie recipes in order and fire up the stone oven!",
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFFFFE0B2),
        )
        Button(
          onClick = {
            orderIndex = 0
            finished = false
            playing = true
            rollRecipe()
            statusNote = "Tap ingredient #1!"
          },
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("bakery_start_button"),
        ) {
          Text("Open Bakery Oven", fontWeight = FontWeight.Bold)
        }
      } else if (finished) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = "3 / 3 Pies Baked to Perfection!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F),
            textAlign = TextAlign.Center,
          )
          Text(
            text = "Rewarded ${GameCatalog.formatCompactPurrs(lastPurrsEarned)} Purrs & +2 🦃 Turkeys!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
          )
          Button(
            onClick = {
              orderIndex = 0
              finished = false
              playing = true
              rollRecipe()
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("bakery_replay_button"),
          ) {
            Text("Bake Another Batch", fontWeight = FontWeight.Bold)
          }
        }
      } else {
        Surface(
          color = Color(0xFF4E2300),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, Color(0xFFFFD54F)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "Customer Recipe Ticket:",
              style = MaterialTheme.typography.labelMedium,
              color = Color(0xFFFFE082),
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              recipe.forEachIndexed { idx, ing ->
                val done = idx < stepIndex
                val active = idx == stepIndex
                Surface(
                  color =
                    when {
                      done -> Color(0xFF2E7D32)
                      active -> Color(0xFFE65100)
                      else -> Color(0xFF3E2723)
                    },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Text(
                    text = if (done) "✓ $ing" else "${idx + 1}. $ing",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                  )
                }
              }
            }
          }
        }

        Text(
          text = statusNote,
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFFFE0B2),
        )

        if (stepIndex < recipe.size) {
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            pantry.forEach { ing ->
              Button(
                onClick = {
                  if (ing == recipe[stepIndex]) {
                    stepIndex++
                    statusNote =
                      if (stepIndex >= recipe.size) "Mixed! Tap Bake in Oven 4 times!"
                      else "Great! Now add ${recipe[stepIndex]}."
                  } else {
                    statusNote = "Oops! Recipe needs ${recipe[stepIndex]} next."
                  }
                },
                modifier = Modifier.height(48.dp).testTag("bakery_ing_$ing"),
              ) {
                Text(ing, fontWeight = FontWeight.Bold)
              }
            }
          }
        } else {
          LinearProgressIndicator(
            progress = { ovenTaps / 4f },
            color = Color(0xFFFFB300),
            trackColor = Color(0x33FFFFFF),
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
          )
          Button(
            onClick = {
              ovenTaps++
              if (ovenTaps >= 4) {
                if (orderIndex + 1 >= 3) {
                  playing = false
                  finished = true
                  lastPurrsEarned = onFinish(3)
                } else {
                  orderIndex++
                  rollRecipe()
                  statusNote = "Order #$orderIndex served! Next pie up!"
                }
              }
            },
            colors =
              ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6D00),
                contentColor = Color.White,
              ),
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("bakery_bake_button"),
          ) {
            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("BAKE IN OVEN! ($ovenTaps/4)", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

private enum class FishStage {
  IDLE,
  WAITING,
  BITE,
  REELING,
  RESULT,
}

@Composable
private fun GoldenGravyFishingZone(
  onCatchReward: (title: String, basePurrs: Long, turkeys: Long, gems: Long) -> Long,
) {
  var stage by remember { mutableStateOf(FishStage.IDLE) }
  var catchProgress by remember { mutableFloatStateOf(0.25f) }
  var tensionMarker by remember { mutableFloatStateOf(0.50f) }
  var fishResultTitle by remember { mutableStateOf("") }
  var fishResultReward by remember { mutableStateOf("") }
  var tipMessage by remember { mutableStateOf("Cast your line into the Golden Gravy Falls pool!") }

  val infinite = rememberInfiniteTransition(label = "gravyFishing")
  val ripple by
    infinite.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
      label = "ripple",
    )

  LaunchedEffect(stage) {
    when (stage) {
      FishStage.WAITING -> {
        tipMessage = "Watch the bobber closely..."
        delay(Random.nextLong(1400L, 2600L))
        if (stage == FishStage.WAITING) {
          stage = FishStage.BITE
          tipMessage = "BITE! Tap HOOK NOW before it swims away!"
        }
      }
      FishStage.BITE -> {
        delay(2200L)
        if (stage == FishStage.BITE) {
          stage = FishStage.IDLE
          tipMessage = "Oh no, the Gravy Fish slipped away! Try casting again."
        }
      }
      FishStage.REELING -> {
        catchProgress = 0.25f
        tensionMarker = 0.45f
        var ticks = 0
        while (stage == FishStage.REELING) {
          delay(120L)
          ticks++
          // Tension drifts slowly downward unless tapped; inside [0.25..0.78] accelerates catch
          tensionMarker = (tensionMarker - 0.032f).coerceIn(0.05f, 0.95f)
          if (tensionMarker in 0.22f..0.82f) {
            catchProgress = (catchProgress + 0.055f).coerceAtMost(1f)
          } else {
            catchProgress = (catchProgress - 0.025f).coerceAtLeast(0f)
          }
          if (catchProgress >= 1f) {
            val roll = Random.nextInt(100)
            val (name, baseP, turk, gem) =
              when {
                roll < 15 -> listOf("Sanctuary Gravy Pearl", 12_000L, 2L, 1L)
                roll < 50 -> listOf("Golden Gravy Koi", 9_000L, 2L, 0L)
                else -> listOf("Amber Harvest Carp", 4_500L, 1L, 0L)
              }
            val title = name as String
            val scaledPurrs =
              onCatchReward(title, baseP as Long, turk as Long, gem as Long)
            fishResultTitle = title
            fishResultReward =
              "+${GameCatalog.formatCompactPurrs(scaledPurrs)} Purrs • +$turk 🦃" +
                if ((gem as Long) > 0L) " • +$gem 💎!" else ""
            stage = FishStage.RESULT
            break
          }
          if (ticks > 140 || catchProgress <= 0f) {
            stage = FishStage.IDLE
            tipMessage = "Line snapped! Keep the reel inside the amber zone."
            break
          }
        }
      }
      else -> Unit
    }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = Color(0x44000000)),
    border = BorderStroke(1.5.dp, Color(0xFFFFCA28).copy(alpha = 0.65f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(Icons.Rounded.Phishing, contentDescription = null, tint = Color(0xFFFFD54F))
        Text(
          text = "Golden Gravy Falls Fishing Pier",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White,
        )
      }

      // River & Bobber Canvas Stage
      Canvas(
        modifier =
          Modifier.fillMaxWidth()
            .height(175.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
              Brush.verticalGradient(listOf(Color(0xFF42210B), Color(0xFFD9822B), Color(0xFF9E5111)))
            )
            .testTag("fishing_river_canvas"),
      ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.60f

        // Shimmering waterfall streams at top-left
        for (i in 0..3) {
          val sx = w * (0.12f + i * 0.06f)
          drawLine(
            color = Color(0x88FFE082),
            start = Offset(sx, 0f),
            end = Offset(sx, h * 0.55f),
            strokeWidth = 6f,
          )
        }

        // Expanding ripples around bobber
        if (stage == FishStage.WAITING || stage == FishStage.BITE || stage == FishStage.REELING) {
          drawCircle(
            color = Color(0x88FFF8E1).copy(alpha = 1f - ripple),
            radius = 18f + ripple * (if (stage == FishStage.BITE) 55f else 32f),
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f),
          )
          // Fishing line from corner pier down to bobber
          drawLine(
            color = Color(0xFFE0E0E0),
            start = Offset(w * 0.85f, h * 0.08f),
            end = Offset(cx, cy),
            strokeWidth = 2.5f,
          )
          // Bobber
          val bobOffsetY = if (stage == FishStage.BITE) (ripple * 14f - 7f) else 0f
          drawCircle(
            color = if (stage == FishStage.BITE) Color(0xFFFF1744) else Color(0xFFE53935),
            radius = 14f,
            center = Offset(cx, cy + bobOffsetY),
          )
          drawCircle(
            color = Color.White,
            radius = 6f,
            center = Offset(cx - 3f, cy - 3f + bobOffsetY),
          )
        }
      }

      Text(
        text =
          when (stage) {
            FishStage.RESULT -> "Caught a $fishResultTitle!"
            else -> tipMessage
          },
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFFFE082),
      )

      if (stage == FishStage.REELING) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Catch Progress: ${(catchProgress * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
          )
          LinearProgressIndicator(
            progress = { catchProgress },
            color = Color(0xFF66BB6A),
            trackColor = Color(0x33FFFFFF),
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
          )
          Text(
            text = "Line Tension (keep in amber middle!):",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFFFE0B2),
          )
          Box(
            modifier =
              Modifier.fillMaxWidth()
                .height(22.dp)
                .clip(CircleShape)
                .background(Color(0x55000000)),
          ) {
            Box(
              modifier =
                Modifier.fillMaxWidth(0.56f)
                  .height(22.dp)
                  .align(Alignment.Center)
                  .background(Color(0x88FFB300)),
            )
            Box(
              modifier =
                Modifier.fillMaxWidth(tensionMarker.coerceIn(0.04f, 0.96f))
                  .height(22.dp),
            ) {
              Box(
                modifier =
                  Modifier.align(Alignment.CenterEnd)
                    .size(width = 8.dp, height = 22.dp)
                    .background(Color.White, CircleShape),
              )
            }
          }
        }
      }

      if (stage == FishStage.RESULT) {
        Surface(
          color = Color(0x332E7D32),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, Color(0xFF81C784)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = fishResultTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
            Text(
              text = fishResultReward,
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFC8E6C9),
            )
          }
        }
      }

      when (stage) {
        FishStage.IDLE, FishStage.RESULT -> {
          Button(
            onClick = { stage = FishStage.WAITING },
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("fishing_cast_button"),
          ) {
            Text("Cast Fishing Line", fontWeight = FontWeight.Bold)
          }
        }
        FishStage.WAITING -> {
          OutlinedButton(
            onClick = {
              stage = FishStage.IDLE
              tipMessage = "Pulled line too early — wait for a bite!"
            },
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("fishing_wait_button"),
          ) {
            Text("Waiting For Bite...", color = Color.White)
          }
        }
        FishStage.BITE -> {
          Button(
            onClick = { stage = FishStage.REELING },
            colors =
              ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF1744),
                contentColor = Color.White,
              ),
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("fishing_hook_button"),
          ) {
            Text("FISH ON! HOOK NOW!", fontWeight = FontWeight.ExtraBold)
          }
        }
        FishStage.REELING -> {
          Button(
            onClick = {
              tensionMarker = (tensionMarker + 0.16f).coerceAtMost(0.95f)
            },
            colors =
              ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB300),
                contentColor = Color(0xFF3E1C00),
              ),
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("fishing_reel_button"),
          ) {
            Text("REEL IN! (Tap Steadily)", fontWeight = FontWeight.ExtraBold)
          }
        }
      }
    }
  }
}

@Composable
private fun TurkeyFeastCookingZone(
  onServeFeast: (stars: Int, basePurrs: Long, turkeys: Long) -> Long,
) {
  // Stages: 0 = Ready, 1 = Prep/Seasoning (6 taps), 2 = Sizzle Sear Timing Bar, 3 = Gravy Glaze (5 taps), 4 = Served
  var cookStage by remember { mutableIntStateOf(0) }
  var seasonTaps by remember { mutableIntStateOf(0) }
  var glazeTaps by remember { mutableIntStateOf(0) }
  var qualityScore by remember { mutableIntStateOf(0) }
  var heatNeedle by remember { mutableFloatStateOf(0.15f) }
  var heatForward by remember { mutableStateOf(true) }
  var searLabel by remember { mutableStateOf("") }
  var lastPurrsEarned by remember { mutableLongStateOf(0L) }
  var finalStars by remember { mutableIntStateOf(5) }
  var finalTurkeys by remember { mutableLongStateOf(2L) }

  LaunchedEffect(cookStage) {
    if (cookStage == 2) {
      while (cookStage == 2) {
        delay(28L)
        if (heatForward) {
          heatNeedle += 0.032f
          if (heatNeedle >= 0.98f) heatForward = false
        } else {
          heatNeedle -= 0.032f
          if (heatNeedle <= 0.02f) heatForward = true
        }
      }
    }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = Color(0x44000000)),
    border = BorderStroke(1.5.dp, Color(0xFFFFAB00).copy(alpha = 0.65f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = Color(0xFFFFD54F))
        Text(
          text = "Turkey Feast Cooking Kitchen",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White,
        )
      }

      when (cookStage) {
        0 -> {
          Text(
            text = "Cook Turkey Gato's 3-Course Thanksgiving Roast: Season Herbs, Sear on the Sweet-Spot Flame, and Pour Golden Gravy!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFFFE0B2),
          )
          Button(
            onClick = {
              seasonTaps = 0
              glazeTaps = 0
              qualityScore = 40
              cookStage = 1
            },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("cooking_start_button"),
          ) {
            Text("Start Cooking Feast", fontWeight = FontWeight.Bold)
          }
        }

        1 -> {
          Text(
            text = "Step 1/3: Season & Baste With Sage Butter ($seasonTaps / 6)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F),
          )
          LinearProgressIndicator(
            progress = { seasonTaps / 6f },
            color = Color(0xFFFFCA28),
            trackColor = Color(0x33FFFFFF),
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
          )
          Button(
            onClick = {
              seasonTaps++
              if (seasonTaps >= 6) {
                qualityScore += 20
                cookStage = 2
              }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("cooking_season_button"),
          ) {
            Text("Brush Herbs & Cranberry Butter!", fontWeight = FontWeight.Bold)
          }
        }

        2 -> {
          Text(
            text = "Step 2/3: Lock Flame In Golden Sweet-Spot!",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F),
          )
          Box(
            modifier =
              Modifier.fillMaxWidth()
                .height(28.dp)
                .clip(CircleShape)
                .background(Color(0x66000000)),
          ) {
            Box(
              modifier =
                Modifier.fillMaxWidth(0.28f)
                  .height(28.dp)
                  .align(Alignment.Center)
                  .background(Color(0xBB2E7D32)),
            )
            Box(modifier = Modifier.fillMaxWidth(heatNeedle.coerceIn(0.03f, 0.97f)).height(28.dp)) {
              Box(
                modifier =
                  Modifier.align(Alignment.CenterEnd)
                    .size(width = 10.dp, height = 28.dp)
                    .background(Color(0xFFFFD54F), CircleShape),
              )
            }
          }
          Button(
            onClick = {
              val dist = abs(heatNeedle - 0.5f)
              val bonus =
                when {
                  dist <= 0.14f -> {
                    searLabel = "PERFECT GOLDEN SEAR! (+40 Quality)"
                    40
                  }
                  dist <= 0.28f -> {
                    searLabel = "Nice Crisp Roast! (+25 Quality)"
                    25
                  }
                  else -> {
                    searLabel = "Slightly Scorched! (+10 Quality)"
                    10
                  }
                }
              qualityScore += bonus
              glazeTaps = 0
              cookStage = 3
            },
            colors =
              ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE65100),
                contentColor = Color.White,
              ),
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("cooking_sear_button"),
          ) {
            Text("STOP FLAME NOW!", fontWeight = FontWeight.ExtraBold)
          }
        }

        3 -> {
          Text(
            text = "$searLabel\nStep 3/3: Pour Molten Golden Gravy ($glazeTaps / 5)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFE082),
          )
          LinearProgressIndicator(
            progress = { glazeTaps / 5f },
            color = Color(0xFFFFAB00),
            trackColor = Color(0x33FFFFFF),
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
          )
          Button(
            onClick = {
              glazeTaps++
              if (glazeTaps >= 5) {
                finalStars =
                  when {
                    qualityScore >= 95 -> 5
                    qualityScore >= 80 -> 4
                    else -> 3
                  }
                finalTurkeys = if (finalStars >= 5) 3L else if (finalStars == 4) 2L else 1L
                lastPurrsEarned = onServeFeast(finalStars, finalStars * 2_800L, finalTurkeys)
                cookStage = 4
              }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("cooking_glaze_button"),
          ) {
            Text("Pour Golden Gravy Glaze!", fontWeight = FontWeight.Bold)
          }
        }

        else -> {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              repeat(finalStars) {
                Icon(
                  imageVector = Icons.Rounded.Star,
                  contentDescription = null,
                  tint = Color(0xFFFFD54F),
                  modifier = Modifier.size(28.dp),
                )
              }
            }
            Text(
              text = "$finalStars-Star Royal Turkey Feast Served!",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              textAlign = TextAlign.Center,
            )
            Text(
              text =
                "+${GameCatalog.formatCompactPurrs(lastPurrsEarned)} Purrs & +$finalTurkeys 🦃 Turkeys!",
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFFC8E6C9),
            )
            Button(
              onClick = { cookStage = 0 },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("cooking_replay_button"),
            ) {
              Text("Cook Another Feast", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CornucopiaCitadelBossZone(
  uiState: PurrGameUiState,
  onTriggerBossCutscene: () -> Unit,
  onExitZone: () -> Unit,
) {
  val maxBossHp = 1000
  val maxPlayerHp = 100
  val bossCutsceneThreshold = 180

  var bossHp by remember { mutableIntStateOf(maxBossHp) }
  var playerHp by remember { mutableIntStateOf(maxPlayerHp) }
  var superCharge by remember { mutableIntStateOf(0) }
  var healCharges by remember { mutableIntStateOf(3) }
  var turnCounter by remember { mutableIntStateOf(0) }
  var bossTelegraphing by remember { mutableStateOf(false) }
  var bossCutscenePending by remember { mutableStateOf(false) }
  var hitShake by remember { mutableFloatStateOf(0f) }
  var battleLog by remember {
    mutableStateOf("Mecha-Gobbler Prime guards Cornucopia Citadel! Push it near defeat!")
  }

  LaunchedEffect(bossCutscenePending) {
    if (bossCutscenePending) {
      battleLog = "WARNING: Boss HP critical! Boss initiating Overdrive Termination!"
      delay(650L)
      onTriggerBossCutscene()
    }
  }

  LaunchedEffect(bossTelegraphing) {
    if (bossTelegraphing && !bossCutscenePending && playerHp > 0) {
      delay(2200L)
      if (bossTelegraphing && !bossCutscenePending && playerHp > 0) {
        bossTelegraphing = false
        val dmg = 24
        playerHp = (playerHp - dmg).coerceAtLeast(0)
        battleLog =
          if (playerHp > 0) "Crimson Cornucopia Laser hit for -$dmg HP!"
          else "${uiState.profile.selectedCatName} fainted! Tap Revive to try again."
      }
    }
  }

  fun dealBossDamage(amount: Int, actionLabel: String, chargeGain: Int) {
    if (bossCutscenePending || playerHp <= 0) return
    val nextHp = (bossHp - amount).coerceAtLeast(bossCutsceneThreshold - 30)
    bossHp = nextHp
    superCharge = (superCharge + chargeGain).coerceAtMost(100)
    hitShake = if (hitShake >= 0f) -12f else 12f
    turnCounter++

    if (nextHp <= bossCutsceneThreshold) {
      bossTelegraphing = false
      bossCutscenePending = true
      return
    }

    if (!bossTelegraphing && turnCounter % 2 == 0) {
      bossTelegraphing = true
      battleLog = "$actionLabel dealt $amount dmg! TELEGRAPH: Laser incoming — Tap SHIELD!"
    } else if (!bossTelegraphing) {
      val chipDmg = 8
      playerHp = (playerHp - chipDmg).coerceAtLeast(1)
      battleLog = "$actionLabel hit for $amount dmg! Boss grazed you (-$chipDmg HP)."
    } else {
      battleLog = "$actionLabel hit for $amount dmg! Watch out — tap SHIELD!"
    }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = Color(0x55000000)),
    border = BorderStroke(1.5.dp, Color(0xFFFF5252).copy(alpha = 0.75f)),
    modifier = Modifier.fillMaxWidth().testTag("citadel_boss_arena_card"),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Boss HP Bar
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "MECHA-GOBBLER PRIME",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFF8A80),
          )
          Text(
            text = "$bossHp / $maxBossHp HP",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        }
        LinearProgressIndicator(
          progress = { bossHp.toFloat() / maxBossHp.toFloat() },
          color = Color(0xFFFF1744),
          trackColor = Color(0x44FFFFFF),
          modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
        )
      }

      // Boss Stage Visual + Player Cat Champion
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(185.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
              Brush.radialGradient(listOf(Color(0xFF5D1318), Color(0xFF28080D)))
            ),
      ) {
        MechaGobblerBossCanvas(
          glitchOffset = if (bossCutscenePending) hitShake else hitShake * 0.4f,
          overdrive = bossTelegraphing || bossCutscenePending,
          modifier = Modifier.size(145.dp).align(Alignment.Center),
        )

        Surface(
          color = Color(0xBB1E0A0E),
          shape = CircleShape,
          border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
          modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            CatAvatarMini(
              patternId = uiState.profile.selectedPatternId,
              accessoryId = uiState.profile.selectedAccessoryId,
              modifier = Modifier.size(34.dp),
            )
            Text(
              text = uiState.profile.selectedCatName,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
          }
        }
      }

      // Player HP & Super
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Your Cat HP: $playerHp / $maxPlayerHp",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFC8E6C9),
            fontWeight = FontWeight.Bold,
          )
          LinearProgressIndicator(
            progress = { playerHp.toFloat() / maxPlayerHp.toFloat() },
            color = Color(0xFF66BB6A),
            trackColor = Color(0x33FFFFFF),
            modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
          )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Cranberry Surge: $superCharge%",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFFFE082),
            fontWeight = FontWeight.Bold,
          )
          LinearProgressIndicator(
            progress = { superCharge / 100f },
            color = Color(0xFFFFCA28),
            trackColor = Color(0x33FFFFFF),
            modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
          )
        }
      }

      Surface(
        color = if (bossTelegraphing) Color(0x99B71C1C) else Color(0x55000000),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (bossTelegraphing) Color(0xFFFFD54F) else Color(0x44FFFFFF)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = battleLog,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = if (bossTelegraphing) FontWeight.Bold else FontWeight.Normal,
          color = Color.White,
          modifier = Modifier.padding(12.dp),
        )
      }

      if (playerHp <= 0) {
        Button(
          onClick = {
            bossHp = maxBossHp
            playerHp = maxPlayerHp
            superCharge = 0
            healCharges = 3
            bossTelegraphing = false
            battleLog = "Back on your paws! Challenge Mecha-Gobbler Prime again!"
          },
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("boss_revive_button"),
        ) {
          Text("Revive & Retry Boss Battle", fontWeight = FontWeight.Bold)
        }
      } else {
        if (bossTelegraphing) {
          Button(
            onClick = {
              bossTelegraphing = false
              val counterDmg = 65
              val nextHp = (bossHp - counterDmg).coerceAtLeast(bossCutsceneThreshold - 30)
              bossHp = nextHp
              superCharge = (superCharge + 25).coerceAtMost(100)
              battleLog = "PARRIED Laser Beam! Counter-hit for $counterDmg dmg!"
              if (nextHp <= bossCutsceneThreshold) {
                bossCutscenePending = true
              }
            },
            colors =
              ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00E5FF),
                contentColor = Color(0xFF002029),
              ),
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("boss_shield_button"),
          ) {
            Icon(Icons.Rounded.Shield, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("SHIELD & COUNTER LASER!", fontWeight = FontWeight.ExtraBold)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Button(
            onClick = {
              val dmg = Random.nextInt(95, 130)
              dealBossDamage(dmg, "Paw Strike", 35)
            },
            colors =
              ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE65100),
                contentColor = Color.White,
              ),
            modifier = Modifier.weight(1f).height(50.dp).testTag("boss_attack_button"),
          ) {
            Text("Paw Strike", fontWeight = FontWeight.Bold, maxLines = 1)
          }

          Button(
            onClick = {
              if (superCharge >= 100) {
                superCharge = 0
                dealBossDamage(250, "CRANBERRY SURGE", 0)
              }
            },
            enabled = superCharge >= 100 && !bossCutscenePending,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFCA28),
                contentColor = Color(0xFF3E1C00),
              ),
            modifier = Modifier.weight(1f).height(50.dp).testTag("boss_surge_button"),
          ) {
            Icon(Icons.Rounded.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(4.dp))
            Text("Surge", fontWeight = FontWeight.Bold, maxLines = 1)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = {
              if (healCharges > 0 && !bossCutscenePending) {
                healCharges--
                playerHp = (playerHp + 35).coerceAtMost(maxPlayerHp)
                battleLog = "Catnip Treat restored +35 HP ($healCharges heals left)!"
              }
            },
            enabled = healCharges > 0 && !bossCutscenePending,
            border = BorderStroke(1.dp, Color(0xFF81C784)),
            modifier = Modifier.weight(1f).height(48.dp).testTag("boss_heal_button"),
          ) {
            Icon(
              Icons.Rounded.Favorite,
              contentDescription = null,
              tint = Color(0xFF81C784),
              modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(4.dp))
            Text("Heal ($healCharges)", color = Color.White, maxLines = 1)
          }

          OutlinedButton(
            onClick = onExitZone,
            border = BorderStroke(1.dp, Color(0x88FFFFFF)),
            modifier = Modifier.weight(1f).height(48.dp),
          ) {
            Text("Retreat", color = Color.White, maxLines = 1)
          }
        }
      }
    }
  }
}
