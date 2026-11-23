package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.R
import com.example.data.local.LocalGameStorage
import com.example.data.local.WidgetSnapshotData
import com.example.data.model.GameCatalog

class PurrSanctuaryWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = PurrSanctuaryWidget()
}

class PurrSanctuaryWidget : GlanceAppWidget() {
  override val sizeMode: SizeMode = SizeMode.Responsive(RESPONSIVE_SIZES)

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val snap = LocalGameStorage(context).loadWidgetSnapshot()
    provideContent {
      GlanceTheme(
        colors =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors
          else PurrWidgetGlanceTheme.colors
      ) {
        SanctuaryWidgetContent(snap)
      }
    }
  }

  override fun onCompositionError(
    context: Context,
    glanceId: GlanceId,
    appWidgetId: Int,
    throwable: Throwable,
  ) {
    val fallback =
      RemoteViews(context.packageName, android.R.layout.simple_list_item_1).apply {
        setTextViewText(android.R.id.text1, "PurrTap Sanctuary • Tap to open")
      }
    AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, fallback)
  }

  companion object {
    private val COMPACT = DpSize(180.dp, 110.dp)
    private val MEDIUM = DpSize(250.dp, 110.dp)
    private val EXPANDED = DpSize(300.dp, 180.dp)
    private val RESPONSIVE_SIZES = setOf(COMPACT, MEDIUM, EXPANDED)
  }
}

@Composable
private fun SanctuaryWidgetContent(snap: WidgetSnapshotData) {
  val size = LocalSize.current
  val isTall = size.height >= 150.dp
  val isWide = size.width >= 230.dp

  Column(
    modifier =
      GlanceModifier.fillMaxSize()
        .background(GlanceTheme.colors.widgetBackground)
        .cornerRadius(20.dp)
        .clickable(actionStartActivity<MainActivity>())
        .padding(12.dp),
    verticalAlignment = Alignment.Vertical.Top,
  ) {
    // Top Header: Cat avatar badge + Name + Weather/Status
    Row(
      modifier = GlanceModifier.fillMaxWidth(),
      verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
      Box(
        modifier =
          GlanceModifier.size(32.dp)
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(16.dp),
        contentAlignment = Alignment.Center,
      ) {
        Image(
          provider = ImageProvider(R.drawable.ic_widget_cat),
          contentDescription = snap.catName,
          colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
          modifier = GlanceModifier.size(20.dp),
        )
      }
      Spacer(modifier = GlanceModifier.width(8.dp))
      Column(modifier = GlanceModifier.defaultWeight()) {
        Text(
          text = snap.catName,
          style =
            TextStyle(
              color = GlanceTheme.colors.onSurface,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
            ),
          maxLines = 1,
        )
        if (isWide) {
          Text(
            text = "${snap.coatName} • ${snap.weatherLabel} ${snap.weatherTempC}°C",
            style =
              TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 11.sp,
              ),
            maxLines = 1,
          )
        }
      }
      if (snap.canClaimDaily || snap.goldenRainActive) {
        Box(
          modifier =
            GlanceModifier.background(
              if (snap.goldenRainActive) GlanceTheme.colors.tertiary
              else GlanceTheme.colors.primary
            )
              .cornerRadius(10.dp)
              .padding(horizontal = 7.dp, vertical = 3.dp),
        ) {
          Text(
            text = if (snap.goldenRainActive) "4x GOLD" else "DAILY!",
            style =
              TextStyle(
                color =
                  if (snap.goldenRainActive) GlanceTheme.colors.onTertiary
                  else GlanceTheme.colors.onPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
              ),
          )
        }
      }
    }

    Spacer(modifier = GlanceModifier.height(6.dp))

    // Purrs Balance + Rates
    Row(
      modifier = GlanceModifier.fillMaxWidth(),
      verticalAlignment = Alignment.Vertical.Bottom,
    ) {
      Column(modifier = GlanceModifier.defaultWeight()) {
        Text(
          text = "${GameCatalog.formatCompactPurrs(snap.activeBalance)} ${snap.currencyShortLabel}",
          style =
            TextStyle(
              color =
                if (snap.isHardcore) GlanceTheme.colors.error
                else GlanceTheme.colors.primary,
              fontSize = if (isTall) 20.sp else 17.sp,
              fontWeight = FontWeight.Bold,
            ),
          maxLines = 1,
        )
        Text(
          text =
            "+${GameCatalog.formatCompactPurrs(snap.clickPower)}/tap • +${GameCatalog.formatCompactPurrs(snap.pps)}/s",
          style =
            TextStyle(
              color = GlanceTheme.colors.onSurfaceVariant,
              fontSize = 11.sp,
            ),
          maxLines = 1,
        )
      }
      if (isWide) {
        Column(horizontalAlignment = Alignment.Horizontal.End) {
          Text(
            text = "${snap.gems} Gems",
            style =
              TextStyle(
                color = GlanceTheme.colors.tertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
              ),
            maxLines = 1,
          )
          Text(
            text = "Streak ${snap.dailyStreak}d",
            style =
              TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 11.sp,
              ),
            maxLines = 1,
          )
        }
      }
    }

    if (isTall) {
      Spacer(modifier = GlanceModifier.height(6.dp))
      Text(
        text = "Lifetime Pets: ${GameCatalog.formatCompactPurrs(snap.totalPets)} • ${if (snap.gemFrenzyActive) "Gem Frenzy 3x Active!" else "Tap below to pet from Home Screen"}",
        style =
          TextStyle(
            color = GlanceTheme.colors.onSurfaceVariant,
            fontSize = 11.sp,
          ),
        maxLines = 1,
      )
    }

    Spacer(modifier = GlanceModifier.defaultWeight())

    // Action Row
    Row(
      modifier = GlanceModifier.fillMaxWidth(),
      verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
      Button(
        text = "Pet +${GameCatalog.formatCompactPurrs(snap.clickPower)}",
        onClick = actionRunCallback<PetCatWidgetAction>(),
        colors =
          ButtonDefaults.buttonColors(
            backgroundColor = GlanceTheme.colors.primary,
            contentColor = GlanceTheme.colors.onPrimary,
          ),
        modifier = GlanceModifier.defaultWeight().height(36.dp),
      )
      if (isWide) {
        Spacer(modifier = GlanceModifier.width(8.dp))
        Button(
          text = "Open",
          onClick = actionStartActivity<MainActivity>(),
          colors =
            ButtonDefaults.buttonColors(
              backgroundColor = GlanceTheme.colors.secondaryContainer,
              contentColor = GlanceTheme.colors.onSecondaryContainer,
            ),
          modifier = GlanceModifier.height(36.dp),
        )
      }
    }
  }
}
