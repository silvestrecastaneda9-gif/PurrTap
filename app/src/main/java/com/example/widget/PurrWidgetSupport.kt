package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import androidx.glance.material3.ColorProviders
import com.example.MainActivity
import com.example.data.local.LocalGameStorage
import com.example.ui.theme.AppColorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object PurrWidgetGlanceTheme {
  val colors =
    ColorProviders(
      light = AppColorTheme.TERRACOTTA.lightScheme,
      dark = AppColorTheme.TERRACOTTA.darkScheme,
    )
}

object PurrWidgetUpdater {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  fun requestUpdate(context: Context) {
    val appCtx = context.applicationContext
    scope.launch {
      updateAllWidgets(appCtx)
    }
  }

  suspend fun updateAllWidgets(context: Context) {
    runCatching { PurrSanctuaryWidget().updateAll(context) }
      .onFailure { Log.w("PurrWidgetUpdater", "Sanctuary widget update skipped: ${it.message}") }
    runCatching { PurrMiniPetWidget().updateAll(context) }
      .onFailure { Log.w("PurrWidgetUpdater", "Mini widget update skipped: ${it.message}") }
  }

  fun canRequestPinWidget(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    val manager = AppWidgetManager.getInstance(context) ?: return false
    return runCatching { manager.isRequestPinAppWidgetSupported }.getOrDefault(false)
  }

  fun requestPinWidget(context: Context, mini: Boolean = false): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    val manager = AppWidgetManager.getInstance(context) ?: return false
    if (!manager.isRequestPinAppWidgetSupported) return false
    val cls =
      if (mini) PurrMiniPetWidgetReceiver::class.java
      else PurrSanctuaryWidgetReceiver::class.java
    val provider = ComponentName(context, cls)
    val callbackIntent = Intent(context, MainActivity::class.java)
    val successCallback =
      PendingIntent.getActivity(
        context,
        if (mini) 402 else 401,
        callbackIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
    return runCatching { manager.requestPinAppWidget(provider, null, successCallback) }
      .getOrDefault(false)
  }
}

class PetCatWidgetAction : ActionCallback {
  override suspend fun onAction(
    context: Context,
    glanceId: GlanceId,
    parameters: ActionParameters,
  ) {
    val appCtx = context.applicationContext
    LocalGameStorage(appCtx).recordWidgetPetTap()
    PurrWidgetUpdater.updateAllWidgets(appCtx)
  }
}
