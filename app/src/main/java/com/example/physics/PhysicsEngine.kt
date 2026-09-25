package com.example.physics

import com.example.audio.SoundManager
import com.example.model.BearEnemy
import com.example.model.Bird
import com.example.model.BirdType
import com.example.model.Block
import com.example.model.BlockType
import com.example.model.FloatingText
import com.example.model.Particle
import com.example.model.Vector2D
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class PhysicsEngine(
  val slingshotOrigin: Vector2D = Vector2D(160f, 430f),
  val groundY: Float = 520f,
  val worldWidth: Float = 1000f,
  val worldHeight: Float = 600f
) {
  val gravity: Float = 980f
  val maxPullDistance: Float = 85f
  val launchMultiplier: Float = 14.5f

  fun calculateTrajectory(dragOffset: Vector2D, count: Int = 16): List<Vector2D> {
    val pullDist = dragOffset.length().coerceAtMost(maxPullDistance)
    val dir = if (pullDist > 0.001f) dragOffset.normalized() else Vector2D()
    val initialVel = dir * (-pullDist * launchMultiplier)

    val points = mutableListOf<Vector2D>()
    var pos = slingshotOrigin
    var vel = initialVel
    val dt = 0.045f

    for (i in 0 until count) {
      points.add(Vector2D(pos.x, pos.y))
      pos = pos + (vel * dt)
      vel.y += gravity * dt
      if (pos.y >= groundY) {
        points.add(Vector2D(pos.x, groundY))
        break
      }
    }
    return points
  }

  fun update(
    dt: Float,
    activeBirds: MutableList<Bird>,
    blocks: MutableList<Block>,
    bears: MutableList<BearEnemy>,
    particles: MutableList<Particle>,
    floatingTexts: MutableList<FloatingText>,
    soundManager: SoundManager?,
    onScoreAdded: (Int) -> Unit
  ) {
    val safeDt = dt.coerceIn(0.001f, 0.033f)

    // 1. Update birds
    val birdsToRemove = mutableListOf<Bird>()
    val newBirdsToAdd = mutableListOf<Bird>()

    for (bird in activeBirds) {
      if (!bird.isFlying || bird.isDead) continue

      bird.lifeTime += safeDt
      // Apply gravity
      bird.velocity.y += gravity * safeDt
      bird.position = bird.position + (bird.velocity * safeDt)

      // Trail
      if (bird.trail.isEmpty() || bird.trail.last().distance(bird.position) > 18f) {
        bird.trail.add(Vector2D(bird.position.x, bird.position.y))
        if (bird.trail.size > 25) {
          bird.trail.removeAt(0)
        }
      }

      // Ground collision
      if (bird.position.y + bird.radius >= groundY) {
        bird.position.y = groundY - bird.radius
        bird.velocity.y = -bird.velocity.y * 0.35f
        bird.velocity.x *= 0.82f
        bird.bounceCount++
        if (abs(bird.velocity.y) < 30f && abs(bird.velocity.x) < 20f) {
          bird.velocity.x = 0f
          bird.velocity.y = 0f
        }
        soundManager?.playHitSound()
      }

      // Left / Right / Top bounds
      if (bird.position.x - bird.radius < 0f) {
        bird.position.x = bird.radius
        bird.velocity.x = -bird.velocity.x * 0.5f
      } else if (bird.position.x + bird.radius > worldWidth) {
        bird.position.x = worldWidth - bird.radius
        bird.velocity.x = -bird.velocity.x * 0.5f
      }
      if (bird.position.y - bird.radius < -100f) {
        bird.position.y = -100f + bird.radius
        bird.velocity.y = abs(bird.velocity.y) * 0.5f
      }

      // Collisions with blocks
      for (block in blocks) {
        if (block.isDestroyed) continue
        if (checkCircleAABBCollision(bird.position, bird.radius, block)) {
          resolveBirdBlockCollision(
            bird,
            block,
            blocks,
            bears,
            particles,
            floatingTexts,
            soundManager,
            onScoreAdded
          )
        }
      }

      // Collisions with bears
      for (bear in bears) {
        if (bear.isDefeated) continue
        val dist = bird.position.distance(Vector2D(bear.x, bear.y))
        if (dist <= bird.radius + bear.radius) {
          resolveBirdBearCollision(
            bird,
            bear,
            particles,
            floatingTexts,
            soundManager,
            onScoreAdded
          )
        }
      }

      // Check if bird is stopped or out of bounds
      val speed = bird.velocity.length()
      if ((speed < 12f && bird.bounceCount > 1) || bird.lifeTime > 8.0f || bird.position.x > worldWidth + 80f) {
        bird.isFlying = false
        bird.isDead = true
        // Poof particles when bird vanishes
        spawnPoofParticles(bird.position.x, bird.position.y, bird.type.colorHex, particles)
      }
    }

    activeBirds.addAll(newBirdsToAdd)

    // 2. Update Blocks (Physics & Falling)
    for (block in blocks) {
      if (block.isDestroyed) continue

      // If block has speed or is in air, apply gravity
      if (block.vy != 0f || block.vx != 0f || block.y + block.height < groundY - 1f) {
        block.vy += gravity * safeDt * 0.85f
        block.x += block.vx * safeDt
        block.y += block.vy * safeDt
        block.vx *= 0.94f

        // Ground hit
        if (block.y + block.height >= groundY) {
          block.y = groundY - block.height
          if (abs(block.vy) > 120f) {
            block.health -= abs(block.vy) * 0.15f
            soundManager?.playHitSound()
          }
          block.vy = -block.vy * 0.2f
          if (abs(block.vy) < 25f) block.vy = 0f
        }

        // Damage bears if falling on them
        for (bear in bears) {
          if (bear.isDefeated) continue
          if (block.contains(Vector2D(bear.x, bear.y)) ||
            (bear.x >= block.x && bear.x <= block.x + block.width &&
              abs((block.y + block.height) - bear.y) < bear.radius + 6f && abs(block.vy) > 60f)
          ) {
            val impactDamage = abs(block.vy) * 0.4f
            bear.health -= impactDamage
            bear.hitEffectTimer = 0.3f
            if (bear.health <= 0f) {
              defeatBear(bear, particles, floatingTexts, soundManager, onScoreAdded)
            }
          }
        }

        if (block.health <= 0f) {
          destroyBlock(block, blocks, bears, particles, floatingTexts, soundManager, onScoreAdded)
        }
      }
    }

    // 3. Update Bears
    for (bear in bears) {
      if (bear.isDefeated) continue
      if (bear.hitEffectTimer > 0f) {
        bear.hitEffectTimer -= safeDt
      }
      if (bear.vy != 0f || bear.vx != 0f || bear.y + bear.radius < groundY - 1f) {
        bear.vy += gravity * safeDt
        bear.x += bear.vx * safeDt
        bear.y += bear.vy * safeDt
        bear.vx *= 0.92f

        if (bear.y + bear.radius >= groundY) {
          bear.y = groundY - bear.radius
          bear.vy = -bear.vy * 0.25f
          if (abs(bear.vy) < 20f) bear.vy = 0f
        }
      }
    }

    // 4. Update Particles
    val deadParticles = mutableListOf<Particle>()
    for (p in particles) {
      p.life += safeDt
      if (p.life >= p.maxLife) {
        deadParticles.add(p)
      } else {
        p.x += p.vx * safeDt
        p.y += p.vy * safeDt
        p.vy += gravity * 0.4f * safeDt
        p.alpha = (1.0f - (p.life / p.maxLife)).coerceIn(0f, 1f)
      }
    }
    particles.removeAll(deadParticles)

    // 5. Update Floating Texts
    val deadTexts = mutableListOf<FloatingText>()
    for (ft in floatingTexts) {
      ft.life -= safeDt * 1.3f
      ft.y -= 45f * safeDt
      ft.alpha = ft.life.coerceIn(0f, 1f)
      ft.scale = 1.0f + (1f - ft.life) * 0.3f
      if (ft.life <= 0f) {
        deadTexts.add(ft)
      }
    }
    floatingTexts.removeAll(deadTexts)
  }

  fun triggerSpecialAbility(
    bird: Bird,
    activeBirds: MutableList<Bird>,
    blocks: MutableList<Block>,
    bears: MutableList<BearEnemy>,
    particles: MutableList<Particle>,
    floatingTexts: MutableList<FloatingText>,
    soundManager: SoundManager?,
    onScoreAdded: (Int) -> Unit
  ) {
    if (!bird.isFlying || bird.isDead || bird.abilityUsed) return
    bird.abilityUsed = true
    soundManager?.playSpecialAbilitySound()

    when (bird.type) {
      BirdType.BULL_COIN -> {
        // Bull Boost: Sudden massive surge forward
        val dir = if (bird.velocity.length() > 1f) bird.velocity.normalized() else Vector2D(1f, 0f)
        bird.velocity = dir * 850f
        spawnAbilityParticles(bird.position.x, bird.position.y, 0xFF00D2FF, particles, 18)
        floatingTexts.add(
          FloatingText(
            id = System.nanoTime(),
            text = "🚀 BULL BOOST!",
            x = bird.position.x,
            y = bird.position.y - 25f,
            color = 0xFF00D2FF
          )
        )
      }
      BirdType.CANDLE_ROCKET -> {
        // Candlestick Piercer: Laser dive forward/down with piercing speed
        val vx = (bird.velocity.x * 1.5f).coerceAtLeast(350f)
        val vy = bird.velocity.y + 220f
        bird.velocity = Vector2D(vx, vy)
        bird.radius = 18f
        spawnAbilityParticles(bird.position.x, bird.position.y, 0xFF00E676, particles, 22)
        floatingTexts.add(
          FloatingText(
            id = System.nanoTime(),
            text = "⚡ LASER PIERCE!",
            x = bird.position.x,
            y = bird.position.y - 25f,
            color = 0xFF00E676
          )
        )
      }
      BirdType.CRYPTO_BOMB -> {
        // Crypto Bomb: Detonate immediately!
        explode(
          bird.position.x,
          bird.position.y,
          radius = 180f,
          damage = 250f,
          blocks = blocks,
          bears = bears,
          particles = particles,
          floatingTexts = floatingTexts,
          soundManager = soundManager,
          onScoreAdded = onScoreAdded
        )
        bird.isFlying = false
        bird.isDead = true
      }
      BirdType.TRIPLE_SPLIT -> {
        // Dividend Split: Spawn two cloned birds at +20 and -20 degree angle
        floatingTexts.add(
          FloatingText(
            id = System.nanoTime(),
            text = "✨ 3X SPLIT!",
            x = bird.position.x,
            y = bird.position.y - 25f,
            color = 0xFF9D4EDD
          )
        )
        val speed = bird.velocity.length().coerceAtLeast(200f)
        val baseAngle = Math.atan2(bird.velocity.y.toDouble(), bird.velocity.x.toDouble())

        // Child 1 (+18 degrees)
        val angle1 = baseAngle + Math.toRadians(18.0)
        val bird1 = Bird(
          id = bird.id + 100,
          type = BirdType.TRIPLE_SPLIT,
          position = Vector2D(bird.position.x, bird.position.y - 10f),
          velocity = Vector2D((cos(angle1) * speed).toFloat(), (sin(angle1) * speed).toFloat()),
          radius = bird.radius * 0.85f,
          isLaunched = true,
          isFlying = true,
          abilityUsed = true
        )

        // Child 2 (-18 degrees)
        val angle2 = baseAngle - Math.toRadians(18.0)
        val bird2 = Bird(
          id = bird.id + 200,
          type = BirdType.TRIPLE_SPLIT,
          position = Vector2D(bird.position.x, bird.position.y + 10f),
          velocity = Vector2D((cos(angle2) * speed).toFloat(), (sin(angle2) * speed).toFloat()),
          radius = bird.radius * 0.85f,
          isLaunched = true,
          isFlying = true,
          abilityUsed = true
        )

        activeBirds.add(bird1)
        activeBirds.add(bird2)
        spawnAbilityParticles(bird.position.x, bird.position.y, 0xFF9D4EDD, particles, 16)
      }
    }
  }

  private fun checkCircleAABBCollision(circlePos: Vector2D, radius: Float, block: Block): Boolean {
    val closestX = circlePos.x.coerceIn(block.x, block.x + block.width)
    val closestY = circlePos.y.coerceIn(block.y, block.y + block.height)
    val dx = circlePos.x - closestX
    val dy = circlePos.y - closestY
    return (dx * dx + dy * dy) <= (radius * radius)
  }

  private fun resolveBirdBlockCollision(
    bird: Bird,
    block: Block,
    blocks: MutableList<Block>,
    bears: MutableList<BearEnemy>,
    particles: MutableList<Particle>,
    floatingTexts: MutableList<FloatingText>,
    soundManager: SoundManager?,
    onScoreAdded: (Int) -> Unit
  ) {
    val speed = bird.velocity.length()

    // Bouncy pad check
    if (block.type.isBouncy) {
      soundManager?.playHitSound()
      bird.velocity.y = -abs(bird.velocity.y).coerceAtLeast(300f) * 1.35f
      bird.velocity.x = bird.velocity.x * 1.15f
      spawnSparks(bird.position.x, bird.position.y, 0xFF00E5FF, particles, 12)
      return
    }

    // Damage to block
    val baseDamage = speed * bird.type.mass * 0.55f
    val damage = if (bird.type == BirdType.CANDLE_ROCKET) baseDamage * 1.8f else baseDamage
    block.health -= damage
    soundManager?.playHitSound()

    // Impart impulse to block
    block.vx += bird.velocity.x * 0.22f * bird.type.mass
    block.vy += bird.velocity.y * 0.18f * bird.type.mass

    // Rebound bird unless piercing
    if (bird.type == BirdType.CANDLE_ROCKET && bird.abilityUsed) {
      bird.velocity = bird.velocity * 0.78f // Slow slightly but pierce through!
    } else {
      bird.velocity.x = -bird.velocity.x * 0.45f
      bird.velocity.y = bird.velocity.y * 0.5f
    }

    spawnSparks(bird.position.x, bird.position.y, block.type.defaultColor, particles, 8)

    // Check if block destroyed
    if (block.health <= 0f) {
      destroyBlock(block, blocks, bears, particles, floatingTexts, soundManager, onScoreAdded)
    }

    // If bird is crypto bomb, explode on hit
    if (bird.type == BirdType.CRYPTO_BOMB) {
      explode(
        bird.position.x,
        bird.position.y,
        radius = 180f,
        damage = 220f,
        blocks = blocks,
        bears = bears,
        particles = particles,
        floatingTexts = floatingTexts,
        soundManager = soundManager,
        onScoreAdded = onScoreAdded
      )
      bird.isFlying = false
      bird.isDead = true
    }
  }

  private fun resolveBirdBearCollision(
    bird: Bird,
    bear: BearEnemy,
    particles: MutableList<Particle>,
    floatingTexts: MutableList<FloatingText>,
    soundManager: SoundManager?,
    onScoreAdded: (Int) -> Unit
  ) {
    val speed = bird.velocity.length()
    val damage = (speed * bird.type.mass * 0.8f).coerceAtLeast(35f)
    bear.health -= damage
    bear.hitEffectTimer = 0.35f
    soundManager?.playHitSound()

    // Impart push
    bear.vx += bird.velocity.x * 0.35f
    bear.vy += (bird.velocity.y * 0.35f) - 60f

    // Bird bounce
    bird.velocity.x = -bird.velocity.x * 0.4f

    spawnSparks(bear.x, bear.y, 0xFFFF334B, particles, 14)

    if (bear.health <= 0f) {
      defeatBear(bear, particles, floatingTexts, soundManager, onScoreAdded)
    }
  }

  private fun destroyBlock(
    block: Block,
    blocks: MutableList<Block>,
    bears: MutableList<BearEnemy>,
    particles: MutableList<Particle>,
    floatingTexts: MutableList<FloatingText>,
    soundManager: SoundManager?,
    onScoreAdded: (Int) -> Unit
  ) {
    block.isDestroyed = true
    onScoreAdded(block.type.scoreValue)

    floatingTexts.add(
      FloatingText(
        id = System.nanoTime(),
        text = "+${block.type.scoreValue}",
        x = block.x + block.width / 2f,
        y = block.y,
        color = block.type.defaultColor
      )
    )

    // Explosive TNT check
    if (block.type.isExplosive) {
      explode(
        cx = block.x + block.width / 2f,
        cy = block.y + block.height / 2f,
        radius = 210f,
        damage = 300f,
        blocks = blocks,
        bears = bears,
        particles = particles,
        floatingTexts = floatingTexts,
        soundManager = soundManager,
        onScoreAdded = onScoreAdded
      )
    } else {
      spawnBlockShards(block, particles)
    }
  }

  fun explode(
    cx: Float,
    cy: Float,
    radius: Float,
    damage: Float,
    blocks: MutableList<Block>,
    bears: MutableList<BearEnemy>,
    particles: MutableList<Particle>,
    floatingTexts: MutableList<FloatingText>,
    soundManager: SoundManager?,
    onScoreAdded: (Int) -> Unit
  ) {
    soundManager?.playExplosionSound()

    // Big explosion floating text
    floatingTexts.add(
      FloatingText(
        id = System.nanoTime(),
        text = "💥 BOOM!",
        x = cx,
        y = cy - 35f,
        color = 0xFFFF8800
      )
    )

    // Damage blocks
    for (block in blocks) {
      if (block.isDestroyed) continue
      val bx = block.x + block.width / 2f
      val by = block.y + block.height / 2f
      val dx = bx - cx
      val dy = by - cy
      val dist = sqrt(dx * dx + dy * dy)
      if (dist <= radius) {
        val factor = 1.0f - (dist / radius)
        block.health -= damage * factor
        val force = 380f * factor
        block.vx += if (dist > 0.01f) (dx / dist) * force else (Random.nextFloat() - 0.5f) * force
        block.vy += if (dist > 0.01f) (dy / dist) * force - 80f else -force

        if (block.health <= 0f) {
          destroyBlock(block, blocks, bears, particles, floatingTexts, soundManager, onScoreAdded)
        }
      }
    }

    // Damage bears
    for (bear in bears) {
      if (bear.isDefeated) continue
      val dx = bear.x - cx
      val dy = bear.y - cy
      val dist = sqrt(dx * dx + dy * dy)
      if (dist <= radius) {
        val factor = 1.0f - (dist / radius)
        bear.health -= damage * factor
        val force = 450f * factor
        bear.vx += if (dist > 0.01f) (dx / dist) * force else (Random.nextFloat() - 0.5f) * force
        bear.vy += if (dist > 0.01f) (dy / dist) * force - 120f else -force
        bear.hitEffectTimer = 0.4f

        if (bear.health <= 0f) {
          defeatBear(bear, particles, floatingTexts, soundManager, onScoreAdded)
        }
      }
    }

    // Explosion particles
    spawnExplosionParticles(cx, cy, particles)
  }

  private fun defeatBear(
    bear: BearEnemy,
    particles: MutableList<Particle>,
    floatingTexts: MutableList<FloatingText>,
    soundManager: SoundManager?,
    onScoreAdded: (Int) -> Unit
  ) {
    bear.isDefeated = true
    val points = if (bear.isBoss) 3000 else 1000
    onScoreAdded(points)
    soundManager?.playPigPopSound()

    floatingTexts.add(
      FloatingText(
        id = System.nanoTime(),
        text = if (bear.isBoss) "👑 BOSS CRUSHED! +$points" else "📉 BEAR MARKET DEFEATED! +$points",
        x = bear.x,
        y = bear.y - 30f,
        color = 0xFF00E676
      )
    )

    // Bear poof particles
    for (i in 0 until 24) {
      val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
      val speed = Random.nextFloat() * 260f + 60f
      particles.add(
        Particle(
          x = bear.x,
          y = bear.y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed - 60f,
          color = if (bear.isBoss) 0xFFFFD700 else 0xFFFF334B,
          size = Random.nextFloat() * 9f + 5f,
          life = 0f,
          maxLife = Random.nextFloat() * 0.6f + 0.4f
        )
      )
    }
  }

  private fun spawnBlockShards(block: Block, particles: MutableList<Particle>) {
    val count = 12
    for (i in 0 until count) {
      val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
      val speed = Random.nextFloat() * 190f + 40f
      particles.add(
        Particle(
          x = block.x + Random.nextFloat() * block.width,
          y = block.y + Random.nextFloat() * block.height,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed - 50f,
          color = block.type.defaultColor,
          size = Random.nextFloat() * 6f + 3f,
          life = 0f,
          maxLife = Random.nextFloat() * 0.5f + 0.3f,
          isCircuitTrack = Random.nextBoolean()
        )
      )
    }
  }

  private fun spawnSparks(x: Float, y: Float, color: Long, particles: MutableList<Particle>, count: Int) {
    for (i in 0 until count) {
      val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
      val speed = Random.nextFloat() * 220f + 40f
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed - 40f,
          color = color,
          size = Random.nextFloat() * 4f + 2f,
          life = 0f,
          maxLife = Random.nextFloat() * 0.4f + 0.2f
        )
      )
    }
  }

  private fun spawnAbilityParticles(x: Float, y: Float, color: Long, particles: MutableList<Particle>, count: Int) {
    for (i in 0 until count) {
      val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
      val speed = Random.nextFloat() * 280f + 60f
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed,
          color = color,
          size = Random.nextFloat() * 7f + 3f,
          life = 0f,
          maxLife = 0.5f,
          isCircuitTrack = true
        )
      )
    }
  }

  private fun spawnExplosionParticles(x: Float, y: Float, particles: MutableList<Particle>) {
    val colors = listOf(0xFFFF334B, 0xFFFF8800, 0xFFFFD700, 0xFF00D2FF, 0xFFFFFFFF)
    for (i in 0 until 35) {
      val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
      val speed = Random.nextFloat() * 380f + 50f
      val color = colors[Random.nextInt(colors.size)]
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed - 90f,
          color = color,
          size = Random.nextFloat() * 10f + 4f,
          life = 0f,
          maxLife = Random.nextFloat() * 0.7f + 0.4f,
          isCircuitTrack = Random.nextBoolean()
        )
      )
    }
  }

  private fun spawnPoofParticles(x: Float, y: Float, color: Long, particles: MutableList<Particle>) {
    for (i in 0 until 14) {
      val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
      val speed = Random.nextFloat() * 130f + 30f
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed - 40f,
          color = color,
          size = Random.nextFloat() * 5f + 3f,
          life = 0f,
          maxLife = 0.4f
        )
      )
    }
  }
}
