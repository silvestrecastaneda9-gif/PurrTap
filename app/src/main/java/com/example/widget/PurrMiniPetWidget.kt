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

class PurrMiniPetWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = PurrMiniPetWidget()
}

class PurrMiniPetWidget : GlanceAppWidget() {
  override val sizeMode: SizeMode = SizeMode.Responsive(RESPONSIVE_SIZES)

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val snap = LocalGameStorage(context).loadWidgetSnapshot()
    provideContent {
      GlanceTheme(
        colors =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors
          else PurrWidgetGlanceTheme.colors
      ) {
        MiniPetWidgetContent(snap)
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
        setTextViewText(android.R.id.text1, "PurrTap Mini Cat")
      }
    AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, fallback)
  }

  companion object {
    private val COMPACT = DpSize(110.dp, 110.dp)
    private val STANDARD = DpSize(170.dp, 140.dp)
    private val RESPONSIVE_SIZES = setOf(COMPACT, STANDARD)
  }
}

@Composable
private fun MiniPetWidgetContent(snap: WidgetSnapshotData) {
  val size = LocalSize.current
  val isRoomy = size.width >= 150.dp

  Column(
    modifier =
      GlanceModifier.fillMaxSize()
        .background(GlanceTheme.colors.widgetBackground)
        .cornerRadius(20.dp)
        .clickable(actionStartActivity<MainActivity>())
        .padding(10.dp),
    verticalAlignment = Alignment.Vertical.CenterVertically,
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
  ) {
    Row(
      modifier = GlanceModifier.fillMaxWidth(),
      verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
      Box(
        modifier =
          GlanceModifier.size(28.dp)
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(14.dp),
        contentAlignment = Alignment.Center,
      ) {
        Image(
          provider = ImageProvider(R.drawable.ic_widget_cat),
          contentDescription = snap.catName,
          colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
          modifier = GlanceModifier.size(18.dp),
        )
      }
      Spacer(modifier = GlanceModifier.width(6.dp))
      Text(
        text = snap.catName,
        style =
          TextStyle(
            color = GlanceTheme.colors.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
          ),
        maxLines = 1,
        modifier = GlanceModifier.defaultWeight(),
      )
    }

    Spacer(modifier = GlanceModifier.height(6.dp))

    Text(
      text = "${GameCatalog.formatCompactPurrs(snap.activeBalance)} ${if (isRoomy) snap.currencyShortLabel else ""}".trim(),
      style =
        TextStyle(
          color = GlanceTheme.colors.primary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
        ),
      maxLines = 1,
    )
    Text(
      text = "+${GameCatalog.formatCompactPurrs(snap.clickPower)}/tap",
      style =
        TextStyle(
          color = GlanceTheme.colors.onSurfaceVariant,
          fontSize = 11.sp,
        ),
      maxLines = 1,
    )

    Spacer(modifier = GlanceModifier.defaultWeight())

    Button(
      text = "Pet Cat",
      onClick = actionRunCallback<PetCatWidgetAction>(),
      colors =
        ButtonDefaults.buttonColors(
          backgroundColor = GlanceTheme.colors.primary,
          contentColor = GlanceTheme.colors.onPrimary,
        ),
      modifier = GlanceModifier.fillMaxWidth().height(34.dp),
    )
  }
}
