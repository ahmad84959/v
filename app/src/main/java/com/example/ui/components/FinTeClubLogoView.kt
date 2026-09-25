package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FinTeClubLogoView(
  modifier: Modifier = Modifier,
  size: Dp = 180.dp,
  animateGlow: Boolean = true
) {
  val infiniteTransition = rememberInfiniteTransition(label = "logoGlow")
  val glowAlpha by if (animateGlow) {
    infiniteTransition.animateFloat(
      initialValue = 0.6f,
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(
        animation = tween(1500, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "glowPulse"
    )
  } else {
    androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
  }

  Box(
    modifier = modifier
      .size(size)
      .testTag("finteclub_logo_view"),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = this.size.width
      val h = this.size.height
      val cx = w / 2f
      val cy = h / 2f
      val scale = w / 300f

      // 1. Draw outer diamond rhombus with electric blue border
      val diamondRadius = 135f * scale
      val outerDiamond = Path().apply {
        moveTo(cx, cy - diamondRadius)
        lineTo(cx + diamondRadius, cy)
        lineTo(cx, cy + diamondRadius)
        lineTo(cx - diamondRadius, cy)
        close()
      }

      // Background fill of diamond
      drawPath(
        path = outerDiamond,
        color = Color(0xFF070C18)
      )

      // Outer glow border
      drawPath(
        path = outerDiamond,
        color = Color(0xFF0088FF).copy(alpha = 0.4f * glowAlpha),
        style = Stroke(width = 8f * scale, join = StrokeJoin.Round)
      )
      // Crisp electric cyan line
      drawPath(
        path = outerDiamond,
        color = Color(0xFF00D2FF),
        style = Stroke(width = 3.5f * scale, join = StrokeJoin.Round)
      )
      // Inner thin blue line
      val innerRadius = 128f * scale
      val innerDiamond = Path().apply {
        moveTo(cx, cy - innerRadius)
        lineTo(cx + innerRadius, cy)
        lineTo(cx, cy + innerRadius)
        lineTo(cx - innerRadius, cy)
        close()
      }
      drawPath(
        path = innerDiamond,
        color = Color(0xFF0066CC).copy(alpha = 0.7f),
        style = Stroke(width = 1.5f * scale, join = StrokeJoin.Round)
      )

      // 2. Candlestick Chart (Top half inside diamond)
      // Candle 1 (Left: Gray/Silver)
      val c1x = cx - 28f * scale
      val c1y = cy - 65f * scale
      drawLine(
        color = Color(0xFFA0B4C8),
        start = Offset(c1x + 6f * scale, c1y - 12f * scale),
        end = Offset(c1x + 6f * scale, c1y + 35f * scale),
        strokeWidth = 2f * scale
      )
      drawRoundRect(
        color = Color(0xFFA0B4C8),
        topLeft = Offset(c1x, c1y),
        size = Size(12f * scale, 24f * scale),
        cornerRadius = CornerRadius(2f * scale)
      )

      // Candle 2 (Center: Electric Blue)
      val c2x = cx - 7f * scale
      val c2y = cy - 80f * scale
      drawLine(
        color = Color(0xFF0077FE),
        start = Offset(c2x + 7f * scale, c2y - 16f * scale),
        end = Offset(c2x + 7f * scale, c2y + 45f * scale),
        strokeWidth = 2.2f * scale
      )
      drawRoundRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF00D2FF), Color(0xFF0066EE))
        ),
        topLeft = Offset(c2x, c2y),
        size = Size(14f * scale, 32f * scale),
        cornerRadius = CornerRadius(2f * scale)
      )

      // Candle 3 (Right Center: Silver high)
      val c3x = cx + 15f * scale
      val c3y = cy - 75f * scale
      drawLine(
        color = Color(0xFFA0B4C8),
        start = Offset(c3x + 6f * scale, c3y - 14f * scale),
        end = Offset(c3x + 6f * scale, c3y + 32f * scale),
        strokeWidth = 2f * scale
      )
      drawRoundRect(
        color = Color(0xFFA0B4C8),
        topLeft = Offset(c3x, c3y),
        size = Size(12f * scale, 22f * scale),
        cornerRadius = CornerRadius(2f * scale)
      )

      // Candle 4 (Far Right: Tall Bullish Cyan)
      val c4x = cx + 37f * scale
      val c4y = cy - 90f * scale
      drawLine(
        color = Color(0xFF00D2FF),
        start = Offset(c4x + 6f * scale, c4y - 16f * scale),
        end = Offset(c4x + 6f * scale, c4y + 48f * scale),
        strokeWidth = 2.2f * scale
      )
      drawRoundRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF00FFFF), Color(0xFF00A3FF))
        ),
        topLeft = Offset(c4x, c4y),
        size = Size(12f * scale, 34f * scale),
        cornerRadius = CornerRadius(2f * scale)
      )

      // 3. Circuit board traces connecting to candles
      drawCircuitTraces(cx, cy, scale)

      // 4. University header text & "FinTeClub" typography
      drawLogoText(cx, cy, scale)

      // 5. Lower Circuit traces and terminals
      drawLowerCircuit(cx, cy, scale)
    }
  }
}

private fun DrawScope.drawCircuitTraces(cx: Float, cy: Float, scale: Float) {
  val circuitColor = Color(0xFF0088FF)
  val strokeWidth = 1.8f * scale

  // Left trace
  val pLeft = Path().apply {
    moveTo(cx - 75f * scale, cy - 35f * scale)
    lineTo(cx - 40f * scale, cy - 35f * scale)
    lineTo(cx - 28f * scale, cy - 42f * scale)
    lineTo(cx - 28f * scale, cy - 30f * scale)
  }
  drawPath(pLeft, circuitColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

  // Right trace
  val pRight = Path().apply {
    moveTo(cx + 15f * scale, cy - 40f * scale)
    lineTo(cx + 30f * scale, cy - 40f * scale)
    lineTo(cx + 50f * scale, cy - 55f * scale)
    lineTo(cx + 80f * scale, cy - 55f * scale)
  }
  drawPath(pRight, circuitColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

  // Terminals
  drawCircle(circuitColor, radius = 2.8f * scale, center = Offset(cx - 75f * scale, cy - 35f * scale))
  drawCircle(circuitColor, radius = 2.8f * scale, center = Offset(cx + 80f * scale, cy - 55f * scale))
}

private fun DrawScope.drawLogoText(cx: Float, cy: Float, scale: Float) {
  val paint = android.graphics.Paint().apply {
    isAntiAlias = true
    textAlign = android.graphics.Paint.Align.CENTER
  }

  // University Header: "ANKARA YILDIRIM BEYAZIT ÜNİVERSİTESİ"
  paint.color = android.graphics.Color.WHITE
  paint.textSize = 8.5f * scale
  paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
  drawContext.canvas.nativeCanvas.drawText(
    "ANKARA YILDIRIM BEYAZIT ÜNİVERSİTESİ",
    cx,
    cy - 12f * scale,
    paint
  )

  // Main Brand: "FinTeClub"
  // "Fin" in White, "Te" in Bright Cyan, "Club" in White
  val brandSize = 34f * scale
  paint.textSize = brandSize
  paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)

  // Measure segments to position accurately
  paint.color = android.graphics.Color.WHITE
  val widthFin = paint.measureText("Fin")

  paint.color = android.graphics.Color.parseColor("#00D2FF")
  val widthTe = paint.measureText("Te")

  paint.color = android.graphics.Color.WHITE
  val widthClub = paint.measureText("Club")

  val totalWidth = widthFin + widthTe + widthClub
  var startX = cx - (totalWidth / 2f)
  val textY = cy + 22f * scale

  // Draw "Fin"
  paint.color = android.graphics.Color.WHITE
  paint.textAlign = android.graphics.Paint.Align.LEFT
  drawContext.canvas.nativeCanvas.drawText("Fin", startX, textY, paint)
  startX += widthFin

  // Draw "Te" (Cyan)
  paint.color = android.graphics.Color.parseColor("#00D2FF")
  drawContext.canvas.nativeCanvas.drawText("Te", startX, textY, paint)
  startX += widthTe

  // Draw "Club"
  paint.color = android.graphics.Color.WHITE
  drawContext.canvas.nativeCanvas.drawText("Club", startX, textY, paint)
}

private fun DrawScope.drawLowerCircuit(cx: Float, cy: Float, scale: Float) {
  val circuitColor = Color(0xFF0077FE)
  val cyanNode = Color(0xFF00D2FF)
  val strokeWidth = 1.8f * scale

  // Horizontal separator under FinTeClub
  drawLine(
    brush = Brush.horizontalGradient(
      colors = listOf(Color.Transparent, Color(0xFF00D2FF), Color(0xFF0077FE), Color.Transparent)
    ),
    start = Offset(cx - 90f * scale, cy + 30f * scale),
    end = Offset(cx + 90f * scale, cy + 30f * scale),
    strokeWidth = 2f * scale
  )

  // Branch 1 (Center-Left)
  val b1 = Path().apply {
    moveTo(cx - 60f * scale, cy + 36f * scale)
    lineTo(cx - 60f * scale, cy + 44f * scale)
    lineTo(cx - 40f * scale, cy + 64f * scale)
    lineTo(cx - 40f * scale, cy + 76f * scale)
  }
  drawPath(b1, circuitColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
  drawCircle(cyanNode, radius = 3f * scale, center = Offset(cx - 40f * scale, cy + 76f * scale))

  // Branch 2 (Left)
  val b2 = Path().apply {
    moveTo(cx - 65f * scale, cy + 45f * scale)
    lineTo(cx - 65f * scale, cy + 52f * scale)
  }
  drawPath(b2, circuitColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
  drawCircle(cyanNode, radius = 2.5f * scale, center = Offset(cx - 65f * scale, cy + 52f * scale))

  // Branch 3 (Center)
  val b3 = Path().apply {
    moveTo(cx, cy + 32f * scale)
    lineTo(cx, cy + 52f * scale)
    lineTo(cx - 8f * scale, cy + 60f * scale)
    lineTo(cx - 8f * scale, cy + 85f * scale)
  }
  drawPath(b3, circuitColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
  drawCircle(cyanNode, radius = 3.2f * scale, center = Offset(cx - 8f * scale, cy + 85f * scale))

  // Branch 4 (Center-Right)
  val b4 = Path().apply {
    moveTo(cx + 15f * scale, cy + 32f * scale)
    lineTo(cx + 15f * scale, cy + 48f * scale)
    lineTo(cx + 35f * scale, cy + 68f * scale)
    lineTo(cx + 48f * scale, cy + 68f * scale)
  }
  drawPath(b4, circuitColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
  drawCircle(cyanNode, radius = 3f * scale, center = Offset(cx + 48f * scale, cy + 68f * scale))

  // Branch 5 (Far Right)
  val b5 = Path().apply {
    moveTo(cx + 45f * scale, cy + 34f * scale)
    lineTo(cx + 45f * scale, cy + 42f * scale)
    lineTo(cx + 60f * scale, cy + 50f * scale)
  }
  drawPath(b5, circuitColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
  drawCircle(cyanNode, radius = 2.5f * scale, center = Offset(cx + 60f * scale, cy + 50f * scale))
}
