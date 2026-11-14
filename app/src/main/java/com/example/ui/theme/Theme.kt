package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val PurrShapes =
  Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
  )

@Composable
fun PurrTapTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  colorTheme: AppColorTheme = AppColorTheme.TERRACOTTA,
  content: @Composable () -> Unit,
) {
  val darkTheme =
    when (themeMode) {
      ThemeMode.SYSTEM -> isSystemInDarkTheme()
      ThemeMode.LIGHT -> false
      ThemeMode.DARK -> true
    }

  val colorScheme = if (darkTheme) colorTheme.darkScheme else colorTheme.lightScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = PurrShapes,
    content = content,
  )
}
