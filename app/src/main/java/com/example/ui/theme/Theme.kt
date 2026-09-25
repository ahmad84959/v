package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FinTeClubColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = CyberNavy,
  primaryContainer = ElectricBlue,
  onPrimaryContainer = TextWhite,
  secondary = BullGreen,
  onSecondary = CyberNavy,
  tertiary = GoldCoin,
  background = CyberNavy,
  onBackground = TextWhite,
  surface = DeepNavy,
  onSurface = TextWhite,
  surfaceVariant = CardNavy,
  onSurfaceVariant = TextMuted,
  error = BearRed,
  onError = TextWhite,
  outline = BorderNavy
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = CyberNavy.toArgb()
        window.navigationBarColor = CyberNavy.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = FinTeClubColorScheme,
    typography = Typography,
    content = content
  )
}
