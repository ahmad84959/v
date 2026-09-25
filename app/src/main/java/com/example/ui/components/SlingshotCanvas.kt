package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.audio.SoundManager
import com.example.model.BearEnemy
import com.example.model.Bird
import com.example.model.BirdType
import com.example.model.Block
import com.example.model.BlockType
import com.example.model.FloatingText
import com.example.model.LevelData
import com.example.model.Particle
import com.example.model.Vector2D
import com.example.physics.PhysicsEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ImpactRing(
  val x: Float,
  val y: Float,
  val color: Long,
  var radius: Float = 5f,
  val maxRadius: Float = 60f,
  var alpha: Float = 1.0f,
  var strokeWidth: Float = 4f
)

@Composable
fun SlingshotCanvas(
  levelData: LevelData,
  soundManager: SoundManager?,
  onScoreAdded: (Int) -> Unit,
  onLevelCompleted: (stars: Int, score: Int) -> Unit,
  onLevelFailed: () -> Unit,
  modifier: Modifier = Modifier
) {
  val coroutineScope = rememberCoroutineScope()
  val physicsEngine = remember { PhysicsEngine() }

  // Game state
  val blocks = remember(levelData) {
    mutableStateListOf<Block>().apply {
      addAll(levelData.blocks.map { it.copy() })
    }
  }

  val bears = remember(levelData) {
    mutableStateListOf<BearEnemy>().apply {
      addAll(levelData.bears.map { it.copy() })
    }
  }

  val birdQueue = remember(levelData) {
    mutableStateListOf<BirdType>().apply {
      addAll(levelData.birds)
    }
  }

  val activeBirds = remember { mutableStateListOf<Bird>() }
  val particles = remember { mutableStateListOf<Particle>() }
  val floatingTexts = remember { mutableStateListOf<FloatingText>() }
  val impactRings = remember { mutableStateListOf<ImpactRing>() }

  var currentBird by remember { mutableStateOf<Bird?>(null) }
  var isDraggingSlingshot by remember { mutableStateOf(false) }
  var dragOffset by remember { mutableStateOf(Vector2D(0f, 0f)) }

  // Screen shake animation for hit particle impact
  var screenShakeX by remember { mutableFloatStateOf(0f) }
  var screenShakeY by remember { mutableFloatStateOf(0f) }

  // Slingshot anchor positions in virtual coordinates (1000 x 600)
  val slingOrigin = physicsEngine.slingshotOrigin
  val slingLeftProng = Vector2D(slingOrigin.x - 14f, slingOrigin.y - 18f)
  val slingRightProng = Vector2D(slingOrigin.x + 14f, slingOrigin.y - 18f)

  // Load next bird when ready
  fun prepareNextBird() {
    if (currentBird == null && birdQueue.isNotEmpty()) {
      val nextType = birdQueue.removeAt(0)
      currentBird = Bird(
        id = System.currentTimeMillis().toInt(),
        type = nextType,
        position = Vector2D(slingOrigin.x, slingOrigin.y),
        velocity = Vector2D(0f, 0f),
        radius = nextType.baseRadius
      )
    }
  }

  LaunchedEffect(levelData) {
    prepareNextBird()
  }

  // Trigger impact particle effect with shockwave and screen shake
  fun triggerHitFeedback(x: Float, y: Float, color: Long, intensity: Float) {
    impactRings.add(
      ImpactRing(
        x = x,
        y = y,
        color = color,
        maxRadius = 30f + intensity * 35f,
        strokeWidth = 3f + intensity * 2f
      )
    )
    // Trigger screen shake
    screenShakeX = (Random.nextFloat() * 2f - 1f) * intensity * 8f
    screenShakeY = (Random.nextFloat() * 2f - 1f) * intensity * 8f
  }

  // Real-time Game Loop
  LaunchedEffect(levelData) {
    var lastFrameTime = System.nanoTime()
    var gameOverCheckDelay = 0f

    while (true) {
      withFrameNanos { now ->
        val dt = ((now - lastFrameTime) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
        lastFrameTime = now

        // Dampen screen shake
        screenShakeX *= 0.85f
        screenShakeY *= 0.85f

        // Update physics
        physicsEngine.update(
          dt = dt,
          activeBirds = activeBirds,
          blocks = blocks,
          bears = bears,
          particles = particles,
          floatingTexts = floatingTexts,
          soundManager = soundManager
        ) { addedScore ->
          onScoreAdded(addedScore)
        }

        // Update Impact Rings
        val deadRings = mutableListOf<ImpactRing>()
        for (ring in impactRings) {
          ring.radius += 180f * dt
          ring.alpha = (1.0f - (ring.radius / ring.maxRadius)).coerceIn(0f, 1f)
          if (ring.radius >= ring.maxRadius) {
            deadRings.add(ring)
          }
        }
        impactRings.removeAll(deadRings)

        // Check active birds state: remove dead birds
        activeBirds.removeAll { it.isDead }

        // Check if slingshot needs the next bird
        if (currentBird == null && activeBirds.isEmpty()) {
          if (birdQueue.isNotEmpty()) {
            prepareNextBird()
          } else {
            // No more birds and none in flight, check victory or failure
            gameOverCheckDelay += dt
            if (gameOverCheckDelay > 2.0f) {
              val allBearsDefeated = bears.all { it.isDefeated }
              if (allBearsDefeated) {
                // VICTORY!
                soundManager?.playWinSound()
                val remainingBirds = birdQueue.size + (if (currentBird != null) 1 else 0)
                val stars = when {
                  remainingBirds >= 2 -> 3
                  remainingBirds == 1 -> 2
                  else -> 1
                }
                onLevelCompleted(stars, 1000)
              } else {
                // FAILED!
                soundManager?.playLoseSound()
                onLevelFailed()
              }
            }
          }
        }

        // Check instant victory if all bears defeated
        if (bears.isNotEmpty() && bears.all { it.isDefeated }) {
          gameOverCheckDelay += dt
          if (gameOverCheckDelay > 1.2f) {
            soundManager?.playWinSound()
            val remainingBirds = birdQueue.size + (if (currentBird != null) 1 else 0)
            val stars = when {
              remainingBirds >= 2 -> 3
              remainingBirds == 1 -> 2
              else -> 1
            }
            onLevelCompleted(stars, 1000)
          }
        }
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("slingshot_canvas_box")
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(levelData) {
          // Touch gestures: dragging slingshot & tapping for special ability
          detectDragGestures(
            onDragStart = { offset ->
              val scaleX = size.width / 1000f
              val scaleY = size.height / 600f
              val touchVirtual = Vector2D(offset.x / scaleX, offset.y / scaleY)

              // Check if touch is near slingshot origin
              val distToSling = touchVirtual.distance(slingOrigin)
              if (distToSling < 70f && currentBird != null) {
                isDraggingSlingshot = true
                val dragVec = touchVirtual - slingOrigin
                val pullDist = dragVec.length().coerceAtMost(physicsEngine.maxPullDistance)
                dragOffset = if (dragVec.length() > 0.001f) dragVec.normalized() * pullDist else Vector2D()
                soundManager?.playPullSound()
              }
            },
            onDrag = { change, _ ->
              if (isDraggingSlingshot && currentBird != null) {
                change.consume()
                val scaleX = size.width / 1000f
                val scaleY = size.height / 600f
                val touchVirtual = Vector2D(change.position.x / scaleX, change.position.y / scaleY)
                val dragVec = touchVirtual - slingOrigin
                val pullDist = dragVec.length().coerceAtMost(physicsEngine.maxPullDistance)
                dragOffset = if (dragVec.length() > 0.001f) dragVec.normalized() * pullDist else Vector2D()
                currentBird?.position = slingOrigin + dragOffset
              }
            },
            onDragEnd = {
              if (isDraggingSlingshot && currentBird != null) {
                isDraggingSlingshot = false
                val pullDist = dragOffset.length()
                if (pullDist > 15f) {
                  // Launch!
                  val bird = currentBird!!
                  val dir = dragOffset.normalized()
                  bird.velocity = dir * (-pullDist * physicsEngine.launchMultiplier)
                  bird.isLaunched = true
                  bird.isFlying = true
                  activeBirds.add(bird)
                  currentBird = null
                  soundManager?.playLaunchSound()

                  // Spawn launch spark particles
                  for (i in 0 until 14) {
                    val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                    val speed = Random.nextFloat() * 140f + 30f
                    particles.add(
                      Particle(
                        x = slingOrigin.x,
                        y = slingOrigin.y,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        color = 0xFF00D2FF,
                        size = Random.nextFloat() * 5f + 2f,
                        life = 0f,
                        maxLife = 0.4f
                      )
                    )
                  }
                } else {
                  // Cancel pull
                  currentBird?.position = Vector2D(slingOrigin.x, slingOrigin.y)
                }
                dragOffset = Vector2D(0f, 0f)
              }
            },
            onDragCancel = {
              isDraggingSlingshot = false
              dragOffset = Vector2D(0f, 0f)
              currentBird?.position = Vector2D(slingOrigin.x, slingOrigin.y)
            }
          )
        }
        .pointerInput(levelData) {
          detectTapGestures {
            // Tap during flight activates the bird's special ability!
            for (bird in activeBirds) {
              if (bird.isFlying && !bird.abilityUsed) {
                physicsEngine.triggerSpecialAbility(
                  bird = bird,
                  activeBirds = activeBirds,
                  blocks = blocks,
                  bears = bears,
                  particles = particles,
                  floatingTexts = floatingTexts,
                  soundManager = soundManager,
                  onScoreAdded = onScoreAdded
                )
                triggerHitFeedback(bird.position.x, bird.position.y, bird.type.colorHex, 1.2f)
                break
              }
            }
          }
        }
    ) {
      val scaleX = size.width / 1000f
      val scaleY = size.height / 600f

      // Apply screen shake offset
      val shakeX = screenShakeX * scaleX
      val shakeY = screenShakeY * scaleY

      // Transform virtual coordinate (1000x600) to actual canvas pixel
      fun toPx(v: Vector2D): Offset = Offset(v.x * scaleX + shakeX, v.y * scaleY + shakeY)
      fun toPx(x: Float, y: Float): Offset = Offset(x * scaleX + shakeX, y * scaleY + shakeY)

      // 1. Draw Cyber Fintech Horizon & Grid Background
      drawBackground(scaleX, scaleY, shakeX, shakeY)

      // 2. Draw Trajectory Preview Dots when pulling
      if (isDraggingSlingshot && dragOffset.length() > 15f) {
        val trajectoryPoints = physicsEngine.calculateTrajectory(dragOffset)
        for (i in 1 until trajectoryPoints.size) {
          val pt = trajectoryPoints[i]
          val ptPx = toPx(pt)
          val alpha = (1.0f - (i.toFloat() / trajectoryPoints.size)).coerceIn(0.2f, 0.9f)
          val dotRadius = (5f - i * 0.18f).coerceAtLeast(2f) * scaleX
          drawCircle(
            color = Color(0xFF00D2FF).copy(alpha = alpha),
            radius = dotRadius,
            center = ptPx
          )
          // Glowing halo on dots
          drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = alpha * 0.4f),
            radius = dotRadius * 1.8f,
            center = ptPx
          )
        }
      }

      // 3. Draw Back Slingshot Elastic Band & Fork Back
      drawSlingshotBack(scaleX, scaleY, slingOrigin, slingLeftProng, currentBird, dragOffset, isDraggingSlingshot)

      // 4. Draw Destructible Blocks (Candlesticks, Bricks, TNT, Springs)
      for (block in blocks) {
        if (!block.isDestroyed) {
          drawBlock(block, scaleX, scaleY, shakeX, shakeY)
        }
      }

      // 5. Draw Bear Enemies
      for (bear in bears) {
        if (!bear.isDefeated) {
          drawBear(bear, scaleX, scaleY, shakeX, shakeY)
        }
      }

      // 6. Draw Active Flying Birds & their trails
      for (bird in activeBirds) {
        drawBird(bird, scaleX, scaleY, shakeX, shakeY)
      }

      // 7. Draw Current Bird on Slingshot
      currentBird?.let { bird ->
        drawBird(bird, scaleX, scaleY, shakeX, shakeY)
      }

      // 8. Draw Front Slingshot Elastic Band & Fork Front
      drawSlingshotFront(scaleX, scaleY, slingOrigin, slingRightProng, currentBird, dragOffset, isDraggingSlingshot)

      // 9. Draw Impact Rings (Shockwaves on hit)
      for (ring in impactRings) {
        drawCircle(
          color = Color(ring.color).copy(alpha = ring.alpha),
          radius = ring.radius * scaleX,
          center = toPx(ring.x, ring.y),
          style = Stroke(width = ring.strokeWidth * scaleX)
        )
      }

      // 10. Draw Particles (Debris shards, glowing circuit sparks, fire embers)
      for (p in particles) {
        val pCenter = toPx(p.x, p.y)
        if (p.isCircuitTrack) {
          // Draw digital circuit line spark
          val tailEnd = Offset(pCenter.x - p.vx * 0.035f * scaleX, pCenter.y - p.vy * 0.035f * scaleY)
          drawLine(
            color = Color(p.color).copy(alpha = p.alpha),
            start = pCenter,
            end = tailEnd,
            strokeWidth = p.size * scaleX * 0.7f,
            cap = StrokeCap.Round
          )
        } else {
          drawCircle(
            color = Color(p.color).copy(alpha = p.alpha),
            radius = p.size * scaleX * 0.6f,
            center = pCenter
          )
        }
      }

      // 11. Draw Floating Score & Combo Texts
      for (ft in floatingTexts) {
        drawFloatingText(ft, scaleX, scaleY, shakeX, shakeY)
      }

      // 12. Draw Queue of Remaining Birds near Slingshot ground
      for (i in birdQueue.indices) {
        val qType = birdQueue[i]
        val qX = (80f - i * 32f) * scaleX + shakeX
        val qY = 515f * scaleY + shakeY
        drawQueueBird(qType, Offset(qX, qY), scaleX)
      }
    }
  }
}

private fun DrawScope.drawBackground(scaleX: Float, scaleY: Float, shakeX: Float, shakeY: Float) {
  // Deep cyber gradient
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFF060913), Color(0xFF0C1427), Color(0xFF0F1C36))
    )
  )

  // Neon Candlestick Skyline Silhouette in background
  val skyCandles = listOf(
    Pair(300f, 320f), Pair(360f, 280f), Pair(420f, 350f), Pair(480f, 260f),
    Pair(540f, 310f), Pair(600f, 240f), Pair(680f, 290f), Pair(750f, 220f),
    Pair(820f, 270f), Pair(890f, 200f), Pair(950f, 250f)
  )
  for ((cx, topY) in skyCandles) {
    val xPx = cx * scaleX + shakeX
    val topYPx = topY * scaleY + shakeY
    val groundYPx = 520f * scaleY + shakeY

    // Wick line
    drawLine(
      color = Color(0xFF162744),
      start = Offset(xPx, topYPx - 25f * scaleY),
      end = Offset(xPx, groundYPx),
      strokeWidth = 2f * scaleX
    )
    // Candle body
    drawRoundRect(
      color = Color(0xFF101F38),
      topLeft = Offset(xPx - 14f * scaleX, topYPx),
      size = Size(28f * scaleX, (groundYPx - topYPx) * 0.65f),
      cornerRadius = CornerRadius(3f * scaleX)
    )
  }

  // Perspective Digital Grid on Ground
  val groundYPx = 520f * scaleY + shakeY
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFF0A1526), Color(0xFF050B14)),
      startY = groundYPx,
      endY = size.height
    ),
    topLeft = Offset(0f, groundYPx),
    size = Size(size.width, size.height - groundYPx)
  )

  // Glowing neon floor line
  drawLine(
    brush = Brush.horizontalGradient(
      colors = listOf(Color(0xFF0088FF), Color(0xFF00D2FF), Color(0xFF00E676), Color(0xFF0088FF))
    ),
    start = Offset(0f, groundYPx),
    end = Offset(size.width, groundYPx),
    strokeWidth = 3f * scaleY
  )

  // Grid vertical perspective lines
  for (i in 0..12) {
    val gx = (i * 90f) * scaleX + shakeX
    drawLine(
      color = Color(0xFF142B4D).copy(alpha = 0.5f),
      start = Offset(gx, groundYPx),
      end = Offset(gx + (gx - size.width / 2f) * 0.4f, size.height),
      strokeWidth = 1.2f * scaleX
    )
  }
}

private fun DrawScope.drawSlingshotBack(
  scaleX: Float,
  scaleY: Float,
  origin: Vector2D,
  leftProng: Vector2D,
  bird: Bird?,
  dragOffset: Vector2D,
  isDragging: Boolean
) {
  val basePx = Offset(origin.x * scaleX, (origin.y + 70f) * scaleY)
  val leftProngPx = Offset(leftProng.x * scaleX, leftProng.y * scaleY)

  // Left Fork Arm
  drawLine(
    color = Color(0xFF3E2723),
    start = Offset(origin.x * scaleX, origin.y * scaleY),
    end = leftProngPx,
    strokeWidth = 9f * scaleX,
    cap = StrokeCap.Round
  )
  // Cyber glowing stripe on fork
  drawLine(
    color = Color(0xFF00D2FF),
    start = Offset(origin.x * scaleX, origin.y * scaleY),
    end = leftProngPx,
    strokeWidth = 2.5f * scaleX,
    cap = StrokeCap.Round
  )

  // Back Elastic Band
  if (bird != null) {
    val birdPos = if (isDragging) origin + dragOffset else bird.position
    val birdPx = Offset(birdPos.x * scaleX, birdPos.y * scaleY)
    val tension = (dragOffset.length() / 85f).coerceIn(0f, 1f)
    val bandColor = if (tension > 0.7f) Color(0xFFFF5252) else Color(0xFF00D2FF)

    drawLine(
      color = bandColor,
      start = leftProngPx,
      end = Offset(birdPx.x - 10f * scaleX, birdPx.y),
      strokeWidth = (5f - tension * 1.5f) * scaleX,
      cap = StrokeCap.Round
    )
  }
}

private fun DrawScope.drawSlingshotFront(
  scaleX: Float,
  scaleY: Float,
  origin: Vector2D,
  rightProng: Vector2D,
  bird: Bird?,
  dragOffset: Vector2D,
  isDragging: Boolean
) {
  val basePx = Offset(origin.x * scaleX, (origin.y + 70f) * scaleY)
  val rightProngPx = Offset(rightProng.x * scaleX, rightProng.y * scaleY)
  val centerForkPx = Offset(origin.x * scaleX, origin.y * scaleY)

  // Slingshot Stem
  drawLine(
    color = Color(0xFF4E342E),
    start = basePx,
    end = centerForkPx,
    strokeWidth = 12f * scaleX,
    cap = StrokeCap.Round
  )
  // Neon cyber core line
  drawLine(
    color = Color(0xFF0088FF),
    start = basePx,
    end = centerForkPx,
    strokeWidth = 3f * scaleX,
    cap = StrokeCap.Round
  )

  // Right Fork Arm
  drawLine(
    color = Color(0xFF5D4037),
    start = centerForkPx,
    end = rightProngPx,
    strokeWidth = 9f * scaleX,
    cap = StrokeCap.Round
  )
  drawLine(
    color = Color(0xFF00D2FF),
    start = centerForkPx,
    end = rightProngPx,
    strokeWidth = 2.5f * scaleX,
    cap = StrokeCap.Round
  )

  // Front Elastic Band & Leather Pouch
  if (bird != null) {
    val birdPos = if (isDragging) origin + dragOffset else bird.position
    val birdPx = Offset(birdPos.x * scaleX, birdPos.y * scaleY)
    val tension = (dragOffset.length() / 85f).coerceIn(0f, 1f)
    val bandColor = if (tension > 0.7f) Color(0xFFFF5252) else Color(0xFF00D2FF)

    drawLine(
      color = bandColor,
      start = rightProngPx,
      end = Offset(birdPx.x + 10f * scaleX, birdPx.y),
      strokeWidth = (5.5f - tension * 1.5f) * scaleX,
      cap = StrokeCap.Round
    )

    // Leather / Digital Pouch behind bird
    drawCircle(
      color = Color(0xFF1E2A3A),
      radius = (bird.radius * 0.9f) * scaleX,
      center = birdPx,
      style = Stroke(width = 2.5f * scaleX)
    )
  }
}

private fun DrawScope.drawBlock(
  block: Block,
  scaleX: Float,
  scaleY: Float,
  shakeX: Float,
  shakeY: Float
) {
  val bPx = Offset(block.x * scaleX + shakeX, block.y * scaleY + shakeY)
  val wPx = block.width * scaleX
  val hPx = block.height * scaleY
  val healthRatio = (block.health / block.maxHealth).coerceIn(0f, 1f)

  when (block.type) {
    BlockType.CANDLE_BLUE, BlockType.CANDLE_GREEN -> {
      // Candlestick Pillar with glowing top & bottom wick!
      val isCyan = block.type == BlockType.CANDLE_BLUE
      val candleColor = if (isCyan) Color(0xFF00A3FF) else Color(0xFF00E676)
      val glowColor = if (isCyan) Color(0xFF00E5FF) else Color(0xFF69F0AE)

      // Center wick vertical line
      drawLine(
        color = candleColor.copy(alpha = 0.85f),
        start = Offset(bPx.x + wPx / 2f, bPx.y - 12f * scaleY),
        end = Offset(bPx.x + wPx / 2f, bPx.y + hPx + 12f * scaleY),
        strokeWidth = 2.5f * scaleX
      )

      // Candlestick Body
      drawRoundRect(
        brush = Brush.verticalGradient(
          colors = listOf(glowColor, candleColor, candleColor.copy(alpha = 0.8f))
        ),
        topLeft = bPx,
        size = Size(wPx, hPx),
        cornerRadius = CornerRadius(4f * scaleX)
      )

      // Health / damage cracks
      if (healthRatio < 0.65f) {
        drawLine(
          color = Color.White.copy(alpha = 0.7f),
          start = Offset(bPx.x + wPx * 0.2f, bPx.y + hPx * 0.3f),
          end = Offset(bPx.x + wPx * 0.7f, bPx.y + hPx * 0.5f),
          strokeWidth = 1.8f * scaleX
        )
      }
      if (healthRatio < 0.35f) {
        drawLine(
          color = Color.White.copy(alpha = 0.8f),
          start = Offset(bPx.x + wPx * 0.6f, bPx.y + hPx * 0.45f),
          end = Offset(bPx.x + wPx * 0.3f, bPx.y + hPx * 0.8f),
          strokeWidth = 2f * scaleX
        )
      }
    }

    BlockType.BEAR_BRICK -> {
      // Red Bearish Heavy Brick
      drawRoundRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F), Color(0xFF8B0000))
        ),
        topLeft = bPx,
        size = Size(wPx, hPx),
        cornerRadius = CornerRadius(4f * scaleX)
      )
      // Brick border
      drawRoundRect(
        color = Color(0xFFFF8A80),
        topLeft = bPx,
        size = Size(wPx, hPx),
        cornerRadius = CornerRadius(4f * scaleX),
        style = Stroke(width = 1.5f * scaleX)
      )
      // Bear icon watermark
      drawLine(
        color = Color(0x66FFFFFF),
        start = Offset(bPx.x + wPx * 0.3f, bPx.y + hPx * 0.4f),
        end = Offset(bPx.x + wPx * 0.7f, bPx.y + hPx * 0.6f),
        strokeWidth = 2f * scaleX
      )
    }

    BlockType.CIRCUIT_STEEL -> {
      // Reinforced Circuit Beam
      drawRoundRect(
        brush = Brush.horizontalGradient(
          colors = listOf(Color(0xFF37474F), Color(0xFF546E7A), Color(0xFF37474F))
        ),
        topLeft = bPx,
        size = Size(wPx, hPx),
        cornerRadius = CornerRadius(3f * scaleX)
      )
      // Circuit neon line
      drawLine(
        color = Color(0xFF00D2FF).copy(alpha = 0.75f),
        start = Offset(bPx.x + 4f * scaleX, bPx.y + hPx / 2f),
        end = Offset(bPx.x + wPx - 4f * scaleX, bPx.y + hPx / 2f),
        strokeWidth = 2f * scaleX
      )
    }

    BlockType.TNT_CRATE -> {
      // Explosive TNT / Liquidation Box
      drawRoundRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFFFF9100), Color(0xFFFF3D00))
        ),
        topLeft = bPx,
        size = Size(wPx, hPx),
        cornerRadius = CornerRadius(4f * scaleX)
      )
      // Warning stripes
      drawRoundRect(
        color = Color(0xFF212121),
        topLeft = Offset(bPx.x + 3f * scaleX, bPx.y + hPx * 0.3f),
        size = Size(wPx - 6f * scaleX, hPx * 0.4f),
        cornerRadius = CornerRadius(2f * scaleX)
      )

      val paint = Paint().apply {
        color = android.graphics.Color.YELLOW
        textSize = 11f * scaleX
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
      }
      drawContext.canvas.nativeCanvas.drawText("TNT", bPx.x + wPx / 2f, bPx.y + hPx * 0.6f, paint)
    }

    BlockType.BOUNCE_PAD -> {
      // Spring market trampoline
      drawRoundRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF00E5FF), Color(0xFF0077FE))
        ),
        topLeft = bPx,
        size = Size(wPx, hPx),
        cornerRadius = CornerRadius(5f * scaleX)
      )
      // Coiled spring lines
      val coils = 4
      for (i in 0 until coils) {
        val cx = bPx.x + (i + 0.5f) * (wPx / coils)
        drawCircle(
          color = Color.White,
          radius = 3f * scaleX,
          center = Offset(cx, bPx.y + hPx / 2f)
        )
      }
    }
  }
}

private fun DrawScope.drawBear(
  bear: BearEnemy,
  scaleX: Float,
  scaleY: Float,
  shakeX: Float,
  shakeY: Float
) {
  val bPx = Offset(bear.x * scaleX + shakeX, bear.y * scaleY + shakeY)
  val rPx = bear.radius * scaleX
  val isHit = bear.hitEffectTimer > 0f

  // Body Color: Red Bearish or Golden for Boss
  val bodyBrush = if (isHit) {
    Brush.radialGradient(colors = listOf(Color.White, Color(0xFFFF5252)))
  } else if (bear.isBoss) {
    Brush.radialGradient(
      colors = listOf(Color(0xFFFFD700), Color(0xFFFF8800), Color(0xFFBF360C)),
      center = bPx,
      radius = rPx
    )
  } else {
    Brush.radialGradient(
      colors = listOf(Color(0xFFFF5252), Color(0xFFD50000), Color(0xFF880000)),
      center = bPx,
      radius = rPx
    )
  }

  // 1. Bear Ears
  val earRadius = rPx * 0.35f
  drawCircle(
    color = if (bear.isBoss) Color(0xFFFF8800) else Color(0xFFD50000),
    radius = earRadius,
    center = Offset(bPx.x - rPx * 0.72f, bPx.y - rPx * 0.72f)
  )
  drawCircle(
    color = if (bear.isBoss) Color(0xFFFF8800) else Color(0xFFD50000),
    radius = earRadius,
    center = Offset(bPx.x + rPx * 0.72f, bPx.y - rPx * 0.72f)
  )
  drawCircle(
    color = Color(0xFF212121),
    radius = earRadius * 0.5f,
    center = Offset(bPx.x - rPx * 0.72f, bPx.y - rPx * 0.72f)
  )
  drawCircle(
    color = Color(0xFF212121),
    radius = earRadius * 0.5f,
    center = Offset(bPx.x + rPx * 0.72f, bPx.y - rPx * 0.72f)
  )

  // 2. Main Body
  drawCircle(
    brush = bodyBrush,
    radius = rPx,
    center = bPx
  )

  // 3. Boss Crown if Boss
  if (bear.isBoss) {
    val crownPath = Path().apply {
      moveTo(bPx.x - rPx * 0.6f, bPx.y - rPx * 0.85f)
      lineTo(bPx.x - rPx * 0.7f, bPx.y - rPx * 1.35f)
      lineTo(bPx.x - rPx * 0.2f, bPx.y - rPx * 1.05f)
      lineTo(bPx.x, bPx.y - rPx * 1.45f)
      lineTo(bPx.x + rPx * 0.2f, bPx.y - rPx * 1.05f)
      lineTo(bPx.x + rPx * 0.7f, bPx.y - rPx * 1.35f)
      lineTo(bPx.x + rPx * 0.6f, bPx.y - rPx * 0.85f)
      close()
    }
    drawPath(crownPath, Color(0xFFFFD700))
    drawPath(crownPath, Color(0xFFFFF176), style = Stroke(width = 1.5f * scaleX))
  }

  // 4. Snout
  drawOval(
    color = Color(0xFF263238),
    topLeft = Offset(bPx.x - rPx * 0.45f, bPx.y + rPx * 0.05f),
    size = Size(rPx * 0.9f, rPx * 0.65f)
  )
  // Nose
  drawCircle(
    color = Color.Black,
    radius = rPx * 0.16f,
    center = Offset(bPx.x, bPx.y + rPx * 0.22f)
  )

  // 5. Angry Eyes & Eyebrows
  val eyeRadius = rPx * 0.22f
  // Left eye
  drawCircle(Color.White, radius = eyeRadius, center = Offset(bPx.x - rPx * 0.35f, bPx.y - rPx * 0.2f))
  drawCircle(Color.Black, radius = eyeRadius * 0.5f, center = Offset(bPx.x - rPx * 0.3f, bPx.y - rPx * 0.18f))

  // Right eye
  drawCircle(Color.White, radius = eyeRadius, center = Offset(bPx.x + rPx * 0.35f, bPx.y - rPx * 0.2f))
  drawCircle(Color.Black, radius = eyeRadius * 0.5f, center = Offset(bPx.x + rPx * 0.4f, bPx.y - rPx * 0.18f))

  // Angry eyebrows
  drawLine(
    color = Color.Black,
    start = Offset(bPx.x - rPx * 0.6f, bPx.y - rPx * 0.45f),
    end = Offset(bPx.x - rPx * 0.15f, bPx.y - rPx * 0.28f),
    strokeWidth = 2.5f * scaleX
  )
  drawLine(
    color = Color.Black,
    start = Offset(bPx.x + rPx * 0.6f, bPx.y - rPx * 0.45f),
    end = Offset(bPx.x + rPx * 0.15f, bPx.y - rPx * 0.28f),
    strokeWidth = 2.5f * scaleX
  )

  // 6. Boss Health Bar
  if (bear.isBoss) {
    val barWidth = rPx * 2.2f
    val barHeight = 6f * scaleY
    val barX = bPx.x - barWidth / 2f
    val barY = bPx.y - rPx * 1.6f
    val healthRatio = (bear.health / bear.maxHealth).coerceIn(0f, 1f)

    drawRoundRect(
      color = Color(0x88000000),
      topLeft = Offset(barX, barY),
      size = Size(barWidth, barHeight),
      cornerRadius = CornerRadius(2f * scaleX)
    )
    drawRoundRect(
      color = if (healthRatio > 0.4f) Color(0xFFFFD700) else Color(0xFFFF334B),
      topLeft = Offset(barX, barY),
      size = Size(barWidth * healthRatio, barHeight),
      cornerRadius = CornerRadius(2f * scaleX)
    )
  }
}

private fun DrawScope.drawBird(
  bird: Bird,
  scaleX: Float,
  scaleY: Float,
  shakeX: Float,
  shakeY: Float
) {
  val bPx = Offset(bird.position.x * scaleX + shakeX, bird.position.y * scaleY + shakeY)
  val rPx = bird.radius * scaleX

  // 1. Bird Smoke/Spark Trail
  if (bird.trail.size > 1) {
    for (i in 0 until bird.trail.size - 1) {
      val p1 = bird.trail[i]
      val p2 = bird.trail[i + 1]
      val alpha = (i.toFloat() / bird.trail.size).coerceIn(0.1f, 0.7f)
      val width = (3.5f * (i.toFloat() / bird.trail.size)) * scaleX
      drawLine(
        color = Color(bird.type.colorHex).copy(alpha = alpha),
        start = Offset(p1.x * scaleX + shakeX, p1.y * scaleY + shakeY),
        end = Offset(p2.x * scaleX + shakeX, p2.y * scaleY + shakeY),
        strokeWidth = width,
        cap = StrokeCap.Round
      )
    }
  }

  // Calculate rotation from velocity
  val angle = if (bird.velocity.length() > 10f) {
    Math.toDegrees(atan2(bird.velocity.y.toDouble(), bird.velocity.x.toDouble())).toFloat()
  } else 0f

  rotate(degrees = angle, pivot = bPx) {
    when (bird.type) {
      BirdType.BULL_COIN -> {
        // FinTeClub Bullish Token (Glowing Cyan & Blue Diamond Coin)
        val diamondRadius = rPx * 1.15f
        val diamondPath = Path().apply {
          moveTo(bPx.x, bPx.y - diamondRadius)
          lineTo(bPx.x + diamondRadius, bPx.y)
          lineTo(bPx.x, bPx.y + diamondRadius)
          lineTo(bPx.x - diamondRadius, bPx.y)
          close()
        }
        drawPath(
          path = diamondPath,
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF00E5FF), Color(0xFF0088FF), Color(0xFF0044AA)),
            center = bPx,
            radius = diamondRadius
          )
        )
        drawPath(
          path = diamondPath,
          color = Color(0xFF00FFFF),
          style = Stroke(width = 2.2f * scaleX, join = StrokeJoin.Round)
        )

        // Bull horns emblem on token
        drawLine(
          color = Color.White,
          start = Offset(bPx.x - rPx * 0.4f, bPx.y - rPx * 0.3f),
          end = Offset(bPx.x + rPx * 0.4f, bPx.y - rPx * 0.3f),
          strokeWidth = 2f * scaleX,
          cap = StrokeCap.Round
        )
        drawLine(
          color = Color(0xFF00E5FF),
          start = Offset(bPx.x, bPx.y - rPx * 0.3f),
          end = Offset(bPx.x, bPx.y + rPx * 0.4f),
          strokeWidth = 2.5f * scaleX,
          cap = StrokeCap.Round
        )

        // Fierce Eye
        drawCircle(Color.White, radius = rPx * 0.28f, center = Offset(bPx.x + rPx * 0.35f, bPx.y - rPx * 0.15f))
        drawCircle(Color.Black, radius = rPx * 0.15f, center = Offset(bPx.x + rPx * 0.42f, bPx.y - rPx * 0.15f))
      }

      BirdType.CANDLE_ROCKET -> {
        // Candlestick Piercer (Sleek Green Rocket)
        val rocketPath = Path().apply {
          moveTo(bPx.x + rPx * 1.3f, bPx.y)
          lineTo(bPx.x - rPx * 0.9f, bPx.y - rPx * 0.7f)
          lineTo(bPx.x - rPx * 0.5f, bPx.y)
          lineTo(bPx.x - rPx * 0.9f, bPx.y + rPx * 0.7f)
          close()
        }
        drawPath(
          path = rocketPath,
          brush = Brush.linearGradient(
            colors = listOf(Color(0xFF69F0AE), Color(0xFF00E676), Color(0xFF00B0FF))
          )
        )
        drawPath(
          path = rocketPath,
          color = Color.White,
          style = Stroke(width = 1.8f * scaleX)
        )

        // Piercing nose wick
        drawLine(
          color = Color(0xFF00FFFF),
          start = Offset(bPx.x + rPx * 1.3f, bPx.y),
          end = Offset(bPx.x + rPx * 1.8f, bPx.y),
          strokeWidth = 2.5f * scaleX
        )

        // Focused Eye
        drawCircle(Color.White, radius = rPx * 0.22f, center = Offset(bPx.x + rPx * 0.4f, bPx.y - rPx * 0.2f))
        drawCircle(Color.Black, radius = rPx * 0.11f, center = Offset(bPx.x + rPx * 0.48f, bPx.y - rPx * 0.2f))
      }

      BirdType.CRYPTO_BOMB -> {
        // Crypto Bomb (Large Heavy Golden/Electric Sphere with Sparking Fuse)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFEA00), Color(0xFFFFB300), Color(0xFFFF6D00)),
            center = bPx,
            radius = rPx
          ),
          radius = rPx,
          center = bPx
        )
        drawCircle(
          color = Color(0xFFFFD700),
          radius = rPx,
          center = bPx,
          style = Stroke(width = 2.2f * scaleX)
        )

        // Fuse with electric spark
        drawLine(
          color = Color(0xFF424242),
          start = Offset(bPx.x - rPx * 0.6f, bPx.y - rPx * 0.6f),
          end = Offset(bPx.x - rPx * 0.9f, bPx.y - rPx * 0.9f),
          strokeWidth = 2.5f * scaleX
        )
        drawCircle(
          color = Color(0xFF00E5FF),
          radius = 3.5f * scaleX,
          center = Offset(bPx.x - rPx * 0.9f, bPx.y - rPx * 0.9f)
        )

        // Crypto Currency '₿' / '₮' symbol
        val paint = Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = 14f * scaleX
          typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          textAlign = Paint.Align.CENTER
        }
        drawContext.canvas.nativeCanvas.drawText("₿", bPx.x, bPx.y + 5f * scaleY, paint)

        // Bomb Eyes
        drawCircle(Color.White, radius = rPx * 0.24f, center = Offset(bPx.x + rPx * 0.45f, bPx.y - rPx * 0.15f))
        drawCircle(Color.Black, radius = rPx * 0.12f, center = Offset(bPx.x + rPx * 0.52f, bPx.y - rPx * 0.15f))
      }

      BirdType.TRIPLE_SPLIT -> {
        // Dividend Split (Purple Quantum Token)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE0AAFF), Color(0xFF9D4EDD), Color(0xFF5A189A)),
            center = bPx,
            radius = rPx
          ),
          radius = rPx,
          center = bPx
        )
        drawCircle(
          color = Color(0xFFC77DFF),
          radius = rPx,
          center = bPx,
          style = Stroke(width = 1.8f * scaleX)
        )

        // Split "3X" label
        val paint = Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = 10f * scaleX
          typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          textAlign = Paint.Align.CENTER
        }
        drawContext.canvas.nativeCanvas.drawText("3X", bPx.x, bPx.y + 4f * scaleY, paint)

        // Eye
        drawCircle(Color.White, radius = rPx * 0.25f, center = Offset(bPx.x + rPx * 0.4f, bPx.y - rPx * 0.15f))
        drawCircle(Color.Black, radius = rPx * 0.12f, center = Offset(bPx.x + rPx * 0.46f, bPx.y - rPx * 0.15f))
      }
    }
  }
}

private fun DrawScope.drawQueueBird(type: BirdType, center: Offset, scaleX: Float) {
  val r = 12f * scaleX
  drawCircle(
    color = Color(type.colorHex),
    radius = r,
    center = center
  )
  drawCircle(
    color = Color.White,
    radius = r,
    center = center,
    style = Stroke(width = 1.2f * scaleX)
  )
  // Mini eye
  drawCircle(Color.White, radius = r * 0.35f, center = Offset(center.x + r * 0.35f, center.y - r * 0.2f))
  drawCircle(Color.Black, radius = r * 0.18f, center = Offset(center.x + r * 0.42f, center.y - r * 0.2f))
}

private fun DrawScope.drawFloatingText(
  ft: FloatingText,
  scaleX: Float,
  scaleY: Float,
  shakeX: Float,
  shakeY: Float
) {
  val xPx = ft.x * scaleX + shakeX
  val yPx = ft.y * scaleY + shakeY

  val paint = Paint().apply {
    color = Color(ft.color).copy(alpha = ft.alpha).hashCode()
    textSize = (16f * ft.scale) * scaleX
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    textAlign = Paint.Align.CENTER
    setShadowLayer(8f * scaleX, 0f, 0f, android.graphics.Color.BLACK)
  }

  drawContext.canvas.nativeCanvas.drawText(ft.text, xPx, yPx, paint)
}
