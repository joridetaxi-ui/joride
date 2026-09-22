package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = VeloAmber,
    onPrimary = Color(0xFF000000),
    primaryContainer = VeloAmberDark,
    onPrimaryContainer = Color.White,
    secondary = VeloEmerald,
    onSecondary = Color(0xFF000000),
    secondaryContainer = VeloEmeraldDark,
    onSecondaryContainer = Color.White,
    tertiary = VeloTeal,
    background = VeloDarkSlate,
    onBackground = Color(0xFFF8FAFC),
    surface = VeloSurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = VeloCardDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = VeloAmberDark,
    onPrimary = Color.White,
    primaryContainer = VeloAmberLight,
    onPrimaryContainer = Color(0xFF78350F),
    secondary = VeloEmeraldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = VeloTeal,
    background = VeloBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = VeloSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = VeloCardLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = VeloGrayLight
  )

@Composable
fun VeloGoTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  VeloGoTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

