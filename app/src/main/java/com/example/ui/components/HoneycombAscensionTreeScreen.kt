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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Hexagon
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AscensionUpgradeDef
import com.example.data.model.GameCatalog
import com.example.ui.viewmodel.PurrGameUiState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private data class HexAxialCoord(val q: Int, val r: Int)

private val FlatTopHexShape = GenericShape { size, _ ->
  val cx = size.width / 2f
  val cy = size.height / 2f
  val rx = size.width / 2f
  val ry = size.height / 2f
  for (i in 0 until 6) {
    val angle = (PI / 3.0) * i
    val px = cx + (rx * cos(angle)).toFloat()
    val py = cy + (ry * sin(angle)).toFloat()
    if (i == 0) moveTo(px, py) else lineTo(px, py)
  }
  close()
}

private fun ascensionNodeCoord(nodeId: String): HexAxialCoord =
  when (nodeId) {
    "asc_golden_whiskers" -> HexAxialCoord(0, -1)
    "asc_bargain_paws" -> HexAxialCoord(-1, 0)
    "asc_frenzy_aura" -> HexAxialCoord(1, -1)
    "asc_lucky_catnip" -> HexAxialCoord(-1, 1)
    "asc_lunar_harmony" -> HexAxialCoord(1, 0)
    "asc_rebirth_echo" -> HexAxialCoord(0, 1)
    "asc_gem_seeker" -> HexAxialCoord(0, -2)
    "asc_offline_mastery" -> HexAxialCoord(-2, 0)
    "asc_pass_scholar" -> HexAxialCoord(2, -2)
    "asc_click_supernova" -> HexAxialCoord(-2, 1)
    "asc_passive_symphony" -> HexAxialCoord(2, -1)
    "asc_cosmic_sovereign" -> HexAxialCoord(0, 2)
    else -> HexAxialCoord(0, 0)
  }

private fun ascensionNodeIcon(nodeId: String): ImageVector =
  when (nodeId) {
    "asc_golden_whiskers" -> Icons.Rounded.AutoAwesome
    "asc_bargain_paws" -> Icons.Rounded.LocalOffer
    "asc_frenzy_aura" -> Icons.Rounded.Bolt
    "asc_lucky_catnip" -> Icons.Rounded.Pets
    "asc_lunar_harmony" -> Icons.Rounded.NightsStay
    "asc_rebirth_echo" -> Icons.Rounded.RestartAlt
    "asc_gem_seeker" -> Icons.Rounded.Diamond
    "asc_offline_mastery" -> Icons.Rounded.Bedtime
    "asc_pass_scholar" -> Icons.Rounded.WorkspacePremium
    "asc_click_supernova" -> Icons.Rounded.TouchApp
    "asc_passive_symphony" -> Icons.Rounded.MusicNote
    "asc_cosmic_sovereign" -> Icons.Rounded.Stars
    else -> Icons.Rounded.Hexagon
  }

/**
 * Full-screen in-activity overlay (no Dialog window) so WindowInsets.safeDrawing keeps every
 * zoom, hex-jump, and unlock button comfortably reachable on mobile screens.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HoneycombAscensionTreeScreen(
  uiState: PurrGameUiState,
  onUnlockAscensionNode: (String) -> Unit,
  onPerformGatoAscension: () -> Unit,
  onDismiss: () -> Unit,
) {
  BackHandler { onDismiss() }

  val challenge = uiState.challengeState
  val nodes = GameCatalog.ascensionUpgrades

  // Null means the central Gato Sanctuary Core hex is focused
  var selectedNodeId by remember {
    mutableStateOf<String?>(
      nodes.firstOrNull { GameCatalog.canUnlockAscensionNode(it, challenge).first }?.id
        ?: nodes.firstOrNull { it.id !in challenge.unlockedAscensionIds }?.id
        ?: nodes.firstOrNull()?.id
    )
  }
  var panOffset by remember { mutableStateOf(Offset.Zero) }
  var zoomScale by remember { mutableFloatStateOf(0.78f) }

  val pulseTransition = rememberInfiniteTransition(label = "honeycombPulse")
  val pulseAlpha by
    pulseTransition.animateFloat(
      initialValue = 0.45f,
      targetValue = 1.0f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1500, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "hexGlow",
    )

  val goldAccent = Color(0xFFFFD54F)
  val cyanAccent = Color(0xFF4DD0E1)
  val bgTop = Color(0xFF120C21)
  val bgBottom = Color(0xFF090711)

  Surface(
    color = bgBottom,
    modifier =
      Modifier.fillMaxSize()
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = {},
        )
        .testTag("honeycomb_ascension_fullscreen"),
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .background(Brush.verticalGradient(listOf(bgTop, Color(0xFF19122B), bgBottom)))
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // Top HUD Header
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
          IconButton(
            onClick = onDismiss,
            colors =
              IconButtonDefaults.iconButtonColors(
                containerColor = Color(0xFF282040),
                contentColor = Color.White,
              ),
            modifier = Modifier.size(48.dp).testTag("close_honeycomb_tree_button"),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = "Exit Honeycomb Tree")
          }
          Column {
            Text(
              text = "Ascension Honeycomb",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text =
                "${challenge.unlockedAscensionIds.size}/${nodes.size} Hexes • Ascensión #${challenge.ascensionCount}",
              style = MaterialTheme.typography.labelMedium,
              color = Color(0xFFD0C4EC),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Surface(
          color = Color(0xFF2C2246),
          shape = CircleShape,
          border = BorderStroke(1.dp, goldAccent.copy(alpha = 0.65f)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Diamond,
              contentDescription = null,
              tint = goldAccent,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = "${challenge.gatoShards} Shards",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = goldAccent,
              maxLines = 1,
            )
          }
        }
      }

      // Dedicated Mobile-Friendly Canvas Toolbar (48dp Zoom & Recenter Controls cleanly outside canvas)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Drag hive or tap pills below",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFFD5CCEA),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f).padding(end = 6.dp),
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          FilledTonalIconButton(
            onClick = { zoomScale = (zoomScale - 0.15f).coerceAtLeast(0.55f) },
            colors =
              IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color(0xFF282040),
                contentColor = Color.White,
              ),
            modifier = Modifier.size(48.dp).testTag("honeycomb_zoom_out"),
          ) {
            Icon(Icons.Rounded.ZoomOut, contentDescription = "Zoom out", modifier = Modifier.size(20.dp))
          }
          FilledTonalIconButton(
            onClick = {
              panOffset = Offset.Zero
              zoomScale = 0.78f
            },
            colors =
              IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color(0xFF282040),
                contentColor = goldAccent,
              ),
            modifier = Modifier.size(48.dp).testTag("honeycomb_recenter"),
          ) {
            Icon(
              Icons.Rounded.CenterFocusStrong,
              contentDescription = "Center honeycomb",
              modifier = Modifier.size(20.dp),
            )
          }
          FilledTonalIconButton(
            onClick = { zoomScale = (zoomScale + 0.15f).coerceAtMost(1.45f) },
            colors =
              IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color(0xFF282040),
                contentColor = Color.White,
              ),
            modifier = Modifier.size(48.dp).testTag("honeycomb_zoom_in"),
          ) {
            Icon(Icons.Rounded.ZoomIn, contentDescription = "Zoom in", modifier = Modifier.size(20.dp))
          }
        }
      }

      // Interactive Honeycomb Canvas Stage
      BoxWithConstraints(
        modifier =
          Modifier.weight(1f)
            .fillMaxWidth()
            .heightIn(min = 170.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
              Brush.radialGradient(
                colors = listOf(Color(0xFF2B1E4A), Color(0xFF140E24), Color(0xFF0B0815))
              )
            )
            .pointerInput(Unit) {
              detectDragGestures { change, dragAmount ->
                change.consume()
                panOffset =
                  Offset(
                    x = (panOffset.x + dragAmount.x).coerceIn(-480f, 480f),
                    y = (panOffset.y + dragAmount.y).coerceIn(-480f, 480f),
                  )
              }
            }
            .testTag("honeycomb_canvas_stage")
      ) {
        val density = LocalDensity.current
        val stageW = constraints.maxWidth.toFloat()
        val stageH = constraints.maxHeight.toFloat()
        val minDimPx = minOf(stageW, stageH)
        val baseCellRadiusPx = (minDimPx / 7.8f) * zoomScale
        val hexNodeWidthDp = with(density) { (baseCellRadiusPx * 1.88f).toDp() }.coerceIn(58.dp, 128.dp)
        val hexNodeHeightDp = with(density) { (baseCellRadiusPx * 1.64f).toDp() }.coerceIn(50.dp, 112.dp)
        val nodeWpx = with(density) { hexNodeWidthDp.toPx() }
        val nodeHpx = with(density) { hexNodeHeightDp.toPx() }

        val originX = stageW / 2f + panOffset.x
        val originY = stageH / 2f + panOffset.y

        fun hexCenter(coord: HexAxialCoord): Offset {
          val step = baseCellRadiusPx * 1.15f
          val x = originX + step * 1.5f * coord.q
          val y = originY + step * sqrt(3f) * (coord.r + coord.q / 2f)
          return Offset(x, y)
        }

        val coreCenter = hexCenter(HexAxialCoord(0, 0))

        // Background Honeycomb Lattice + Glowing Energy Conduits
        Canvas(modifier = Modifier.fillMaxSize()) {
          val ambientRings =
            listOf(
              HexAxialCoord(1, -2),
              HexAxialCoord(2, 0),
              HexAxialCoord(1, 1),
              HexAxialCoord(-1, 2),
              HexAxialCoord(-2, 2),
              HexAxialCoord(-1, -1),
            )
          ambientRings.forEach { coord ->
            val c = hexCenter(coord)
            val r = baseCellRadiusPx * 0.92f
            val hexPath =
              Path().apply {
                for (i in 0 until 6) {
                  val angle = (PI / 3.0) * i
                  val px = c.x + (r * cos(angle)).toFloat()
                  val py = c.y + (r * 0.87f * sin(angle)).toFloat()
                  if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
              }
            drawPath(
              path = hexPath,
              color = Color(0xFF473A6B).copy(alpha = 0.25f),
              style = Stroke(width = 1.5.dp.toPx()),
            )
          }

          val t1Node = nodes.firstOrNull { it.id == "asc_golden_whiskers" }
          if (t1Node != null) {
            val t1Center = hexCenter(ascensionNodeCoord(t1Node.id))
            val t1Owned = t1Node.id in challenge.unlockedAscensionIds
            drawLine(
              color =
                if (t1Owned) goldAccent.copy(alpha = 0.9f)
                else cyanAccent.copy(alpha = 0.55f * pulseAlpha),
              start = coreCenter,
              end = t1Center,
              strokeWidth = if (t1Owned) 5.dp.toPx() else 3.dp.toPx(),
              cap = StrokeCap.Round,
            )
          }

          nodes.forEach { node ->
            val childCenter = hexCenter(ascensionNodeCoord(node.id))
            val childOwned = node.id in challenge.unlockedAscensionIds
            val (canUnlockChild, _) = GameCatalog.canUnlockAscensionNode(node, challenge)
            node.prerequisiteIds.forEach { prereqId ->
              val parentCenter = hexCenter(ascensionNodeCoord(prereqId))
              val parentOwned = prereqId in challenge.unlockedAscensionIds
              val lineColor =
                when {
                  parentOwned && childOwned -> goldAccent.copy(alpha = 0.92f)
                  parentOwned && canUnlockChild -> cyanAccent.copy(alpha = 0.85f * pulseAlpha)
                  parentOwned -> cyanAccent.copy(alpha = 0.45f)
                  else -> Color(0xFF534A75).copy(alpha = 0.55f)
                }
              val strokePx =
                when {
                  parentOwned && childOwned -> 5.dp.toPx()
                  parentOwned -> 3.5.dp.toPx()
                  else -> 2.dp.toPx()
                }
              val pathEffect =
                if (!parentOwned && !childOwned) {
                  PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                } else null
              drawLine(
                color = lineColor,
                start = parentCenter,
                end = childCenter,
                strokeWidth = strokePx,
                cap = StrokeCap.Round,
                pathEffect = pathEffect,
              )
            }
          }
        }

        // Center Sanctuary Hub Hex (0, 0)
        val coreSelected = selectedNodeId == null
        Surface(
          color = Color(0xFF291D0B),
          shape = FlatTopHexShape,
          border =
            BorderStroke(
              width = if (coreSelected) 3.dp else 2.dp,
              color = if (uiState.canAscend) goldAccent else goldAccent.copy(alpha = 0.72f),
            ),
          shadowElevation = 8.dp,
          modifier =
            Modifier.offset {
              IntOffset(
                x = (coreCenter.x - nodeWpx / 2f).roundToInt(),
                y = (coreCenter.y - nodeHpx / 2f).roundToInt(),
              )
            }
              .size(width = hexNodeWidthDp, height = hexNodeHeightDp)
              .clip(FlatTopHexShape)
              .clickable { selectedNodeId = null }
              .testTag("honeycomb_core_hex"),
        ) {
          Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Rounded.Hexagon,
              contentDescription = "Gato Core",
              tint = goldAccent,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "GATO CORE",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
              fontWeight = FontWeight.Bold,
              color = goldAccent,
              maxLines = 1,
            )
            Text(
              text =
                if (uiState.canAscend) "ASCEND!"
                else "${challenge.rebirthCount}/5 RB",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
              color = if (uiState.canAscend) cyanAccent else Color(0xFFF5E6C4),
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
            )
          }
        }

        // Upgrade Hex Nodes
        nodes.forEach { node ->
          val center = hexCenter(ascensionNodeCoord(node.id))
          val isOwned = node.id in challenge.unlockedAscensionIds
          val (canUnlock, _) = GameCatalog.canUnlockAscensionNode(node, challenge)
          val isSelected = selectedNodeId == node.id

          val hexBg =
            when {
              isOwned -> Color(0xFF3E2C0B)
              canUnlock -> Color(0xFF133645)
              else -> Color(0xFF1D1830)
            }
          val hexBorder =
            when {
              isSelected -> Color.White
              isOwned -> goldAccent
              canUnlock -> cyanAccent.copy(alpha = pulseAlpha)
              else -> Color(0xFF564D78)
            }
          val labelTint =
            when {
              isOwned -> goldAccent
              canUnlock -> Color(0xFFB2EBF2)
              else -> Color(0xFFB8B0D0)
            }

          Surface(
            color = hexBg,
            shape = FlatTopHexShape,
            border = BorderStroke(if (isSelected) 3.dp else 2.dp, hexBorder),
            shadowElevation = if (isOwned || canUnlock) 8.dp else 2.dp,
            modifier =
              Modifier.offset {
                IntOffset(
                  x = (center.x - nodeWpx / 2f).roundToInt(),
                  y = (center.y - nodeHpx / 2f).roundToInt(),
                )
              }
                .size(width = hexNodeWidthDp, height = hexNodeHeightDp)
                .clip(FlatTopHexShape)
                .clickable { selectedNodeId = node.id }
                .testTag("honeycomb_node_${node.id}"),
          ) {
            Column(
              modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Icon(
                imageVector =
                  if (isOwned) Icons.Rounded.CheckCircle else ascensionNodeIcon(node.id),
                contentDescription = node.name,
                tint = labelTint,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = node.name,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 10.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = if (isOwned) "OWNED" else "${node.shardCost} Sh",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = labelTint,
                maxLines = 1,
              )
            }
          }
        }
      }

      // 1-Tap Scrollable Hex Quick-Jump Strip so every Hex is reachable on mobile regardless of pan/zoom
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .testTag("honeycomb_quick_jump_strip"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val corePicked = selectedNodeId == null
        Surface(
          onClick = { selectedNodeId = null },
          color = if (corePicked) goldAccent else Color(0xFF282040),
          shape = CircleShape,
          border =
            BorderStroke(
              1.5.dp,
              if (corePicked) Color.White else goldAccent.copy(alpha = 0.6f),
            ),
          modifier = Modifier.heightIn(min = 48.dp).testTag("honeycomb_chip_core"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Hexagon,
              contentDescription = null,
              tint = if (corePicked) Color(0xFF281A00) else goldAccent,
              modifier = Modifier.size(15.dp),
            )
            Text(
              text = if (uiState.canAscend) "CORE • ASCEND!" else "CORE (${challenge.rebirthCount}/5)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (corePicked) Color(0xFF281A00) else Color.White,
              maxLines = 1,
            )
          }
        }

        nodes.forEach { node ->
          val owned = node.id in challenge.unlockedAscensionIds
          val (ready, _) = GameCatalog.canUnlockAscensionNode(node, challenge)
          val picked = selectedNodeId == node.id
          val bg =
            when {
              picked && owned -> goldAccent
              picked && ready -> cyanAccent
              picked -> Color(0xFF4C3F72)
              owned -> Color(0xFF3E2C0B)
              ready -> Color(0xFF133645)
              else -> Color(0xFF1E1731)
            }
          val borderCol =
            when {
              picked -> Color.White
              owned -> goldAccent
              ready -> cyanAccent
              else -> Color(0xFF564D78)
            }
          val fg =
            if (picked && (owned || ready)) Color(0xFF1A1000)
            else Color.White
          Surface(
            onClick = { selectedNodeId = node.id },
            color = bg,
            shape = CircleShape,
            border = BorderStroke(1.5.dp, borderCol),
            modifier = Modifier.heightIn(min = 48.dp).testTag("honeycomb_chip_${node.id}"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              Icon(
                imageVector = if (owned) Icons.Rounded.CheckCircle else ascensionNodeIcon(node.id),
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(15.dp),
              )
              Text(
                text = node.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = fg,
                maxLines = 1,
              )
            }
          }
        }
      }

      // Bottom Inspector Drawer (bounded height + verticalScroll so Unlock/Ascend button never clips)
      val selectedNode: AscensionUpgradeDef? = nodes.find { it.id == selectedNodeId }
      Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1631)),
        border =
          BorderStroke(
            1.5.dp,
            if (selectedNode != null && selectedNode.id in challenge.unlockedAscensionIds) {
              goldAccent.copy(alpha = 0.75f)
            } else {
              cyanAccent.copy(alpha = 0.5f)
            },
          ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier =
          Modifier.fillMaxWidth()
            .widthIn(max = 640.dp)
            .align(Alignment.CenterHorizontally)
            .testTag("honeycomb_inspector_card"),
      ) {
        Column(
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(max = 225.dp)
              .verticalScroll(rememberScrollState())
              .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (selectedNode == null) {
            Text(
              text = "Gato Sanctuary Core (Ascensión #${challenge.ascensionCount})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = goldAccent,
            )
            Text(
              text =
                "Reach 5 Rebirths (${challenge.rebirthCount}/5) to Ascend for +1 Gato Shard & +${GameCatalog.ASCENSION_GEM_REWARD} Rare Gems.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFD7CFF0),
            )
            Button(
              onClick = onPerformGatoAscension,
              enabled = uiState.canAscend,
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = goldAccent,
                  contentColor = Color(0xFF281A00),
                ),
              modifier =
                Modifier.fillMaxWidth().height(48.dp).testTag("honeycomb_core_ascend_button"),
            ) {
              Text(
                text =
                  if (uiState.canAscend) "Ascend Now (+1 Shard, +${GameCatalog.ASCENSION_GEM_REWARD} Gems)"
                  else "Need ${5L - challenge.rebirthCount.coerceAtMost(5L)} More Rebirths to Ascend",
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          } else {
            val isOwned = selectedNode.id in challenge.unlockedAscensionIds
            val (canUnlock, reason) = GameCatalog.canUnlockAscensionNode(selectedNode, challenge)
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = selectedNode.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
              )
              Surface(
                color = if (isOwned) goldAccent else Color(0xFF2D2547),
                shape = CircleShape,
              ) {
                Text(
                  text = if (isOwned) "TIER ${selectedNode.tier} • OWNED" else "TIER ${selectedNode.tier} • ${selectedNode.shardCost} SHARD(S)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isOwned) Color(0xFF281A00) else cyanAccent,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }

            Text(
              text = selectedNode.effectSummary,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = goldAccent,
            )
            Text(
              text = selectedNode.description,
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFD5CCEA),
            )

            if (selectedNode.prerequisiteIds.isNotEmpty()) {
              val prereqLabels =
                selectedNode.prerequisiteIds.mapNotNull { pid ->
                  val pNode = nodes.find { it.id == pid } ?: return@mapNotNull null
                  val pOwned = pid in challenge.unlockedAscensionIds
                  (if (pOwned) "✓ " else "○ ") + pNode.name
                }
              Text(
                text = "Links from: ${prereqLabels.joinToString("  •  ")}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFAEB5D6),
              )
            }

            Button(
              onClick = { onUnlockAscensionNode(selectedNode.id) },
              enabled = canUnlock,
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = if (canUnlock) cyanAccent else Color(0xFF332B4D),
                  contentColor = if (canUnlock) Color(0xFF002026) else Color(0xFFBEB7D8),
                ),
              contentPadding = PaddingValues(horizontal = 16.dp),
              modifier =
                Modifier.fillMaxWidth()
                  .height(48.dp)
                  .testTag("honeycomb_unlock_${selectedNode.id}"),
            ) {
              Icon(
                imageVector =
                  when {
                    isOwned -> Icons.Rounded.CheckCircle
                    canUnlock -> Icons.Rounded.AutoAwesome
                    else -> Icons.Rounded.Lock
                  },
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text =
                  when {
                    isOwned -> "Hex Node Active"
                    canUnlock -> "Unlock Hex (${selectedNode.shardCost} Gato Shard)"
                    else -> reason ?: "Need ${selectedNode.shardCost} Shard(s)"
                  },
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}
