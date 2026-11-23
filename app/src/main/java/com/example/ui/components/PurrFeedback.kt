package com.example.ui.components

import androidx.compose.runtime.staticCompositionLocalOf

data class PurrFeedbackController(
  val tap: () -> Unit = {},
  val confirm: () -> Unit = {},
  val slideTick: () -> Unit = {},
)

val LocalPurrFeedback = staticCompositionLocalOf { PurrFeedbackController() }
