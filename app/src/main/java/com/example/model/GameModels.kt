package com.example.model

import kotlin.math.sqrt

data class Vector2D(var x: Float = 0f, var y: Float = 0f) {
  operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
  operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
  operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
  operator fun div(scalar: Float) = if (scalar != 0f) Vector2D(x / scalar, y / scalar) else Vector2D()

  fun length(): Float = sqrt(x * x + y * y)
  fun distance(other: Vector2D): Float = sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
  fun normalized(): Vector2D {
    val len = length()
    return if (len > 0.0001f) Vector2D(x / len, y / len) else Vector2D()
  }
}

enum class BirdType(
  val displayNameEn: String,
  val displayNameAr: String,
  val abilityDescAr: String,
  val abilityDescEn: String,
  val baseRadius: Float,
  val mass: Float,
  val colorHex: Long
) {
  BULL_COIN(
    displayNameEn = "Bullish Token",
    displayNameAr = "رمز الصعود (Bull Token)",
    abilityDescAr = "انقر لتفعيل الاندفاع الصاعد السريع!",
    abilityDescEn = "Tap to trigger a powerful Bull Boost!",
    baseRadius = 18f,
    mass = 1.0f,
    colorHex = 0xFF00D2FF
  ),
  CANDLE_ROCKET(
    displayNameEn = "Candlestick Piercer",
    displayNameAr = "صاروخ الشمعة اليابانية",
    abilityDescAr = "انقر لاختراق الأعمدة والتحصينات!",
    abilityDescEn = "Tap to pierce through vertical pillars!",
    baseRadius = 15f,
    mass = 1.3f,
    colorHex = 0xFF00E676
  ),
  CRYPTO_BOMB(
    displayNameEn = "Crypto Bomb",
    displayNameAr = "قنبلة التصفية (Crypto Bomb)",
    abilityDescAr = "انقر لتفجير الصدمة وهدم الحصن بأكمله!",
    abilityDescEn = "Tap to detonate an explosive market shockwave!",
    baseRadius = 22f,
    mass = 1.8f,
    colorHex = 0xFFFFD700
  ),
  TRIPLE_SPLIT(
    displayNameEn = "Dividend Split",
    displayNameAr = "تجزئة الأسهم (Split 3x)",
    abilityDescAr = "انقر لتجزئة القذيفة إلى 3 عملات تكتسح الهدف!",
    abilityDescEn = "Tap to split into 3 spreading projectiles!",
    baseRadius = 16f,
    mass = 0.8f,
    colorHex = 0xFF9D4EDD
  )
}

data class Bird(
  val id: Int,
  val type: BirdType,
  var position: Vector2D = Vector2D(),
  var velocity: Vector2D = Vector2D(),
  var radius: Float = type.baseRadius,
  var isLaunched: Boolean = false,
  var isFlying: Boolean = false,
  var isDead: Boolean = false,
  var abilityUsed: Boolean = false,
  var bounceCount: Int = 0,
  var trail: MutableList<Vector2D> = mutableListOf(),
  var lifeTime: Float = 0f
)

enum class BlockType(
  val displayName: String,
  val baseHealth: Float,
  val defaultColor: Long,
  val isExplosive: Boolean = false,
  val isBouncy: Boolean = false,
  val scoreValue: Int = 100
) {
  CANDLE_BLUE("Cyan Candlestick", 70f, 0xFF00A3FF, scoreValue = 150),
  CANDLE_GREEN("Bull Candle", 50f, 0xFF00E676, scoreValue = 100),
  BEAR_BRICK("Red Bear Barrier", 130f, 0xFFFF334B, scoreValue = 250),
  CIRCUIT_STEEL("Reinforced Circuit", 220f, 0xFF546E7A, scoreValue = 300),
  TNT_CRATE("Liquidation TNT", 25f, 0xFFFF8800, isExplosive = true, scoreValue = 500),
  BOUNCE_PAD("Market Spring", 500f, 0xFF00E5FF, isBouncy = true, scoreValue = 50)
}

data class Block(
  val id: Int,
  val type: BlockType,
  var x: Float,
  var y: Float,
  var width: Float,
  var height: Float,
  var health: Float = type.baseHealth,
  val maxHealth: Float = type.baseHealth,
  var vx: Float = 0f,
  var vy: Float = 0f,
  var rotation: Float = 0f,
  var isDestroyed: Boolean = false
) {
  fun contains(point: Vector2D): Boolean {
    return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height
  }
}

data class BearEnemy(
  val id: Int,
  var x: Float,
  var y: Float,
  var radius: Float = 22f,
  var health: Float = 100f,
  val maxHealth: Float = 100f,
  var vx: Float = 0f,
  var vy: Float = 0f,
  val isBoss: Boolean = false,
  var isDefeated: Boolean = false,
  val name: String = "Bearish Crash",
  var hitEffectTimer: Float = 0f
)

data class Particle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  val color: Long,
  var alpha: Float = 1.0f,
  var size: Float,
  var life: Float,
  val maxLife: Float,
  val isCircuitTrack: Boolean = false
)

data class FloatingText(
  val id: Long,
  val text: String,
  var x: Float,
  var y: Float,
  val color: Long,
  var alpha: Float = 1.0f,
  var scale: Float = 1.0f,
  var life: Float = 1.0f
)

data class LevelData(
  val levelNumber: Int,
  val titleAr: String,
  val titleEn: String,
  val subtitleAr: String,
  val birds: List<BirdType>,
  val blocks: List<Block>,
  val bears: List<BearEnemy>,
  val targetScore3Stars: Int,
  val targetScore2Stars: Int
)

data class LevelProgress(
  val levelNumber: Int,
  val isUnlocked: Boolean,
  val stars: Int,
  val highScore: Int
)
