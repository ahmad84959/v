package com.example.data

import com.example.model.BearEnemy
import com.example.model.BirdType
import com.example.model.Block
import com.example.model.BlockType
import com.example.model.LevelData

object LevelRepository {

  val TOTAL_LEVELS = 10

  fun getLevel(levelNumber: Int): LevelData {
    return when (levelNumber) {
      1 -> createLevel1()
      2 -> createLevel2()
      3 -> createLevel3()
      4 -> createLevel4()
      5 -> createLevel5()
      6 -> createLevel6()
      7 -> createLevel7()
      8 -> createLevel8()
      9 -> createLevel9()
      10 -> createLevel10()
      else -> createLevel1()
    }
  }

  // Ground is at y = 520f. Slingshot is at x = 160f, y = 430f.
  private fun createLevel1(): LevelData {
    val blocks = mutableListOf(
      // Left vertical pillar
      Block(1, BlockType.CANDLE_BLUE, x = 680f, y = 420f, width = 24f, height = 100f),
      // Right vertical pillar
      Block(2, BlockType.CANDLE_BLUE, x = 760f, y = 420f, width = 24f, height = 100f),
      // Cross beam
      Block(3, BlockType.CANDLE_GREEN, x = 670f, y = 400f, width = 124f, height = 20f),
      // Top little pillar
      Block(4, BlockType.CANDLE_BLUE, x = 720f, y = 330f, width = 24f, height = 70f)
    )

    val bears = mutableListOf(
      BearEnemy(1, x = 732f, y = 490f, radius = 22f, health = 70f, maxHealth = 70f, name = "Bear #1")
    )

    return LevelData(
      levelNumber = 1,
      titleAr = "دخول السوق (Market Entry)",
      titleEn = "Market Entry",
      subtitleAr = "اضرب برج الشموع وأسقط دب الهبوط الأول!",
      birds = listOf(BirdType.BULL_COIN, BirdType.BULL_COIN, BirdType.BULL_COIN),
      blocks = blocks,
      bears = bears,
      targetScore3Stars = 1200,
      targetScore2Stars = 700
    )
  }

  private fun createLevel2(): LevelData {
    val blocks = mutableListOf(
      // Tower 1
      Block(1, BlockType.CANDLE_GREEN, x = 640f, y = 430f, width = 22f, height = 90f),
      Block(2, BlockType.CANDLE_GREEN, x = 700f, y = 430f, width = 22f, height = 90f),
      Block(3, BlockType.CANDLE_BLUE, x = 630f, y = 410f, width = 102f, height = 20f),

      // Tower 2
      Block(4, BlockType.CANDLE_GREEN, x = 780f, y = 430f, width = 22f, height = 90f),
      Block(5, BlockType.CANDLE_GREEN, x = 840f, y = 430f, width = 22f, height = 90f),
      Block(6, BlockType.CANDLE_BLUE, x = 770f, y = 410f, width = 102f, height = 20f),

      // Bridge connecting both
      Block(7, BlockType.CIRCUIT_STEEL, x = 690f, y = 390f, width = 120f, height = 16f)
    )

    val bears = mutableListOf(
      BearEnemy(1, x = 680f, y = 490f, radius = 20f, health = 80f, maxHealth = 80f),
      BearEnemy(2, x = 820f, y = 490f, radius = 20f, health = 80f, maxHealth = 80f)
    )

    return LevelData(
      levelNumber = 2,
      titleAr = "اختراق صاعد (Bullish Breakout)",
      titleEn = "Bullish Breakout",
      subtitleAr = "برجان للشموع يحميان اثنين من الدببة، اسحق الأساس!",
      birds = listOf(BirdType.BULL_COIN, BirdType.BULL_COIN, BirdType.CANDLE_ROCKET),
      blocks = blocks,
      bears = bears,
      targetScore3Stars = 2200,
      targetScore2Stars = 1400
    )
  }

  private fun createLevel3(): LevelData {
    val blocks = mutableListOf(
      // High defensive wall
      Block(1, BlockType.BEAR_BRICK, x = 660f, y = 430f, width = 36f, height = 90f),
      Block(2, BlockType.BEAR_BRICK, x = 660f, y = 340f, width = 36f, height = 90f),
      Block(3, BlockType.CANDLE_BLUE, x = 750f, y = 430f, width = 24f, height = 90f),
      Block(4, BlockType.CANDLE_BLUE, x = 810f, y = 430f, width = 24f, height = 90f),
      Block(5, BlockType.CANDLE_GREEN, x = 740f, y = 410f, width = 104f, height = 20f),
      Block(6, BlockType.CIRCUIT_STEEL, x = 770f, y = 330f, width = 24f, height = 80f)
    )

    val bears = mutableListOf(
      BearEnemy(1, x = 780f, y = 490f, radius = 22f, health = 90f, maxHealth = 90f),
      BearEnemy(2, x = 780f, y = 375f, radius = 20f, health = 90f, maxHealth = 90f)
    )

    return LevelData(
      levelNumber = 3,
      titleAr = "موجة التضخم (Inflation Spike)",
      titleEn = "Inflation Spike",
      subtitleAr = "جدار التضخم الأحمر سميك، استعن بصاروخ الشمعة الثاقب!",
      birds = listOf(BirdType.CANDLE_ROCKET, BirdType.BULL_COIN, BirdType.BULL_COIN),
      blocks = blocks,
      bears = bears,
      targetScore3Stars = 2800,
      targetScore2Stars = 1800
    )
  }

  private fun createLevel4(): LevelData {
    val blocks = mutableListOf(
      // TNT crate at bottom
      Block(1, BlockType.TNT_CRATE, x = 720f, y = 480f, width = 40f, height = 40f),
      Block(2, BlockType.CANDLE_BLUE, x = 660f, y = 430f, width = 22f, height = 90f),
      Block(3, BlockType.CANDLE_BLUE, x = 780f, y = 430f, width = 22f, height = 90f),
      Block(4, BlockType.CIRCUIT_STEEL, x = 650f, y = 410f, width = 160f, height = 20f),

      // Second floor with another TNT
      Block(5, BlockType.TNT_CRATE, x = 720f, y = 370f, width = 40f, height = 40f),
      Block(6, BlockType.CANDLE_GREEN, x = 680f, y = 310f, width = 20f, height = 100f),
      Block(7, BlockType.CANDLE_GREEN, x = 760f, y = 310f, width = 20f, height = 100f),
      Block(8, BlockType.CANDLE_BLUE, x = 670f, y = 290f, width = 120f, height = 20f)
    )

    val bears = mutableListOf(
      BearEnemy(1, x = 690f, y = 490f, radius = 20f, health = 80f, maxHealth = 80f),
      BearEnemy(2, x = 750f, y = 490f, radius = 20f, health = 80f, maxHealth = 80f),
      BearEnemy(3, x = 725f, y = 260f, radius = 20f, health = 80f, maxHealth = 80f)
    )

    return LevelData(
      levelNumber = 4,
      titleAr = "تقلبات الكريبتو (Crypto Volatility)",
      titleEn = "Crypto Volatility",
      subtitleAr = "صناديق التصفية المتفجرة (TNT)! أصِب صندوق واحد لتحدث انفجاراً متسلسلاً!",
      birds = listOf(BirdType.CRYPTO_BOMB, BirdType.BULL_COIN, BirdType.CANDLE_ROCKET),
      blocks = blocks,
      bears = bears,
      targetScore3Stars = 3500,
      targetScore2Stars = 2200
    )
  }

  private fun createLevel5(): LevelData {
    val blocks = mutableListOf(
      // Steel Bunker
      Block(1, BlockType.CIRCUIT_STEEL, x = 680f, y = 420f, width = 26f, height = 100f),
      Block(2, BlockType.CIRCUIT_STEEL, x = 800f, y = 420f, width = 26f, height = 100f),
      Block(3, BlockType.CIRCUIT_STEEL, x = 670f, y = 398f, width = 166f, height = 22f),

      // Roof pillars
      Block(4, BlockType.CANDLE_GREEN, x = 710f, y = 310f, width = 22f, height = 88f),
      Block(5, BlockType.CANDLE_GREEN, x = 770f, y = 310f, width = 22f, height = 88f),
      Block(6, BlockType.BEAR_BRICK, x = 700f, y = 290f, width = 104f, height = 20f)
    )

    val bears = mutableListOf(
      BearEnemy(1, x = 740f, y = 490f, radius = 22f, health = 90f, maxHealth = 90f),
      BearEnemy(2, x = 740f, y = 360f, radius = 20f, health = 80f, maxHealth = 80f),
      BearEnemy(3, x = 860f, y = 495f, radius = 18f, health = 70f, maxHealth = 70f)
    )

    return LevelData(
      levelNumber = 5,
      titleAr = "المقاومة الخوارزمية (Algorithmic Resistance)",
      titleEn = "Algorithmic Resistance",
      subtitleAr = "استخدم تجزئة الأسهم (Split 3x) في الجو لاستهداف مناطق متعددة دفعة واحدة!",
      birds = listOf(BirdType.TRIPLE_SPLIT, BirdType.CANDLE_ROCKET, BirdType.CRYPTO_BOMB),
      blocks = blocks,
      bears = bears,
      targetScore3Stars = 3800,
      targetScore2Stars = 2500
    )
  }

  private fun createLevel6(): LevelData {
    val blocks = mutableListOf(
      // Stepped candlestick towers
      Block(1, BlockType.CANDLE_BLUE, x = 620f, y = 450f, width = 24f, height = 70f),
      Block(2, BlockType.CANDLE_GREEN, x = 610f, y = 430f, width = 60f, height = 20f),

      Block(3, BlockType.CANDLE_BLUE, x = 700f, y = 380f, width = 24f, height = 140f),
      Block(4, BlockType.CANDLE_GREEN, x = 690f, y = 360f, width = 60f, height = 20f),

      Block(5, BlockType.CANDLE_BLUE, x = 780f, y = 300f, width = 24f, height = 220f),
      Block(6, BlockType.CANDLE_GREEN, x = 770f, y = 280f, width = 60f, height = 20f),

      Block(7, BlockType.TNT_CRATE, x = 700f, y = 480f, width = 40f, height = 40f)
    )

    val bears = mutableListOf(
      BearEnemy(1, x = 635f, y = 400f, radius = 18f, health = 80f, maxHealth = 80f),
      BearEnemy(2, x = 720f, y = 330f, radius = 20f, health = 85f, maxHealth = 85f),
      BearEnemy(3, x = 800f, y = 250f, radius = 22f, health = 90f, maxHealth = 90f)
    )

    return LevelData(
      levelNumber = 6,
      titleAr = "عصر الدببة (The Short Squeeze)",
      titleEn = "The Short Squeeze",
      subtitleAr = "الدببة تصعد درجات الشموع الصاعدة، اعصرهم دفعة واحدة!",
      birds = listOf(BirdType.BULL_COIN, BirdType.TRIPLE_SPLIT, BirdType.CRYPTO_BOMB, BirdType.CANDLE_ROCKET),
      blocks = blocks,
      bears = bears,
      targetScore3Stars = 4200,
      targetScore2Stars = 2800
    )
  }

  private fun createLevel7(): LevelData {
    val blocks = mutableListOf(
      // Spring bounce pad at the bottom to ricochet over wall
      Block(1, BlockType.BOUNCE_PAD, x = 560f, y = 500f, width = 60f, height = 20f),

      // High wall
      Block(2, BlockType.CIRCUIT_STEEL, x = 660f, y = 330f, width = 26f, height = 190f),

      // Inner fortress
      Block(3, BlockType.CANDLE_BLUE, x = 740f, y = 430f, width = 22f, height = 90f),
      Block(4, BlockType.CANDLE_BLUE, x = 810f, y = 430f, width = 22f, height = 90f),
      Block(5, BlockType.BEAR_BRICK, x = 730f, y = 410f, width = 112f, height = 20f),
      Block(6, BlockType.TNT_CRATE, x = 765f, y = 480f, width = 40f, height = 40f),
      Block(7, BlockType.CANDLE_GREEN, x = 770f, y = 330f, width = 24f, height = 80f)
    )

    val bears = mutableListOf(
      BearEnemy(1, x = 750f, y = 485f, radius = 20f, health = 90f, maxHealth = 90f),
      BearEnemy(2, x = 800f, y = 485f, radius = 20f, health = 90f, maxHealth = 90f),
      BearEnemy(3, x = 780f, y = 380f, radius = 20f, health = 90f, maxHealth = 90f)
    )

    return LevelData(
      levelNumber = 7,
      titleAr = "تصدي الانهيار (Flash Crash Defense)",
      titleEn = "Flash Crash Defense",
      subtitleAr = "ارتد من لوحة السوق الزنبركية وتخطى الجدار العازل!",
      birds = listOf(BirdType.CANDLE_ROCKET, BirdType.CRYPTO_BOMB, BirdType.TRIPLE_SPLIT, BirdType.BULL_COIN),
      blocks = blocks,
      bears = bears,
      targetScore3Stars = 4500,
      targetScore2Stars = 3000
    )
  }

  private fun createLevel8(): LevelData {
    val blocks = mutableListOf(
      // Left tower
      Block(1, BlockType.BEAR_BRICK, x = 640f, y = 410f, width = 26f, height = 110f),
      Block(2, BlockType.CANDLE_BLUE, x = 700f, y = 410f, width = 22f, height = 110f),
      Block(3, BlockType.CIRCUIT_STEEL, x = 630f, y = 390f, width = 100f, height = 20f),

      // Center TNT pile
      Block(4, BlockType.TNT_CRATE, x = 740f, y = 480f, width = 38f, height = 40f),
      Block(5, BlockType.TNT_CRATE, x = 740f, y = 440f, width = 38f, height = 40f),

      // Right tower
      Block(6, BlockType.CANDLE_BLUE, x = 800f, y = 410f, width = 22f, height = 110f),
      Block(7, BlockType.BEAR_BRICK, x = 860f, y = 410f, width = 26f, height = 110f),
      Block(8, BlockType.CIRCUIT_STEEL, x = 790f, y = 390f, width = 102f, height = 20f),

      // Grand bridge across all
      Block(9, BlockType.CANDLE_GREEN, x = 680f, y = 370f, width = 180f, height = 18f)
    )

    val bears = mutableListOf(
      BearEnemy(1, x = 670f, y = 490f, radius = 22f, health = 95f, maxHealth = 95f),
      BearEnemy(2, x = 830f, y = 490f, radius = 22f, health = 95f, maxHealth = 95f),
      BearEnemy(3, x = 760f, y = 340f, radius = 20f, health = 95f, maxHealth = 95f)
    )

    return LevelData(
      levelNumber = 8,
      titleAr = "فخ السيولة (Liquidity Trap)",
      titleEn = "Liquidity Trap",
      subtitleAr = "برجان حصينان يتوسطهما ركام متفجرات، احسب الزاوية بدقة!",
      birds = listOf(BirdType.CRYPTO_BOMB, BirdType.CANDLE_ROCKET, BirdType.TRIPLE_SPLIT, BirdType.BULL_COIN),
      blocks = blocks,
      bears = bears,
      targetScore3Stars = 4800,
      targetScore2Stars = 3200
    )
  }

  private fun createLevel9(): LevelData {
    val blocks = mutableListOf(
      // Floor 1
      Block(1, BlockType.CIRCUIT_STEEL, x = 650f, y = 430f, width = 26f, height = 90f),
      Block(2, BlockType.BEAR_BRICK, x = 730f, y = 430f, width = 26f, height = 90f),
      Block(3, BlockType.CIRCUIT_STEEL, x = 810f, y = 430f, width = 26f, height = 90f),
      Block(4, BlockType.CANDLE_BLUE, x = 640f, y = 410f, width = 210f, height = 20f),

      // Floor 2
      Block(5, BlockType.CANDLE_GREEN, x = 680f, y = 320f, width = 22f, height = 90f),
      Block(6, BlockType.CANDLE_GREEN, x = 770f, y = 320f, width = 22f, height = 90f),
      Block(7, BlockType.CANDLE_BLUE, x = 670f, y = 300f, width = 132f, height = 20f),

      // Floor 3
      Block(8, BlockType.TNT_CRATE, x = 720f, y = 260f, width = 38f, height = 40f),
      Block(9, BlockType.BEAR_BRICK, x = 715f, y = 220f, width = 50f, height = 40f)
    )

    val bears = mutableListOf(
      BearEnemy(1, x = 690f, y = 490f, radius = 22f, health = 100f, maxHealth = 100f),
      BearEnemy(2, x = 770f, y = 490f, radius = 22f, health = 100f, maxHealth = 100f),
      BearEnemy(3, x = 730f, y = 370f, radius = 20f, health = 95f, maxHealth = 95f),
      BearEnemy(4, x = 740f, y = 185f, radius = 18f, health = 90f, maxHealth = 90f)
    )

    return LevelData(
      levelNumber = 9,
      titleAr = "قلعة الديون (Citadel of Debt)",
      titleEn = "Citadel of Debt",
      subtitleAr = "4 دببة متحصنة في 3 طوابق فولاذية، دمر الطابق الأوسط لهدم المبنى بأكمله!",
      birds = listOf(BirdType.CANDLE_ROCKET, BirdType.CRYPTO_BOMB, BirdType.TRIPLE_SPLIT, BirdType.BULL_COIN),
      blocks = blocks,
      bears = bears,
      targetScore3Stars = 5500,
      targetScore2Stars = 3800
    )
  }

  private fun createLevel10(): LevelData {
    val blocks = mutableListOf(
      // The Grand Wall Street Bunker
      Block(1, BlockType.CIRCUIT_STEEL, x = 650f, y = 410f, width = 30f, height = 110f),
      Block(2, BlockType.TNT_CRATE, x = 690f, y = 480f, width = 40f, height = 40f),
      Block(3, BlockType.BEAR_BRICK, x = 740f, y = 410f, width = 30f, height = 110f),
      Block(4, BlockType.TNT_CRATE, x = 780f, y = 480f, width = 40f, height = 40f),
      Block(5, BlockType.CIRCUIT_STEEL, x = 830f, y = 410f, width = 30f, height = 110f),

      // Roof beam
      Block(6, BlockType.CIRCUIT_STEEL, x = 640f, y = 390f, width = 230f, height = 22f),

      // Boss Throne Tower
      Block(7, BlockType.CANDLE_BLUE, x = 700f, y = 290f, width = 24f, height = 100f),
      Block(8, BlockType.CANDLE_BLUE, x = 780f, y = 290f, width = 24f, height = 100f),
      Block(9, BlockType.CANDLE_GREEN, x = 690f, y = 270f, width = 124f, height = 20f),
      Block(10, BlockType.TNT_CRATE, x = 738f, y = 230f, width = 38f, height = 40f)
    )

    val bears = mutableListOf(
      // Mini guards
      BearEnemy(1, x = 695f, y = 450f, radius = 18f, health = 80f, maxHealth = 80f, name = "Bear Guard 1"),
      BearEnemy(2, x = 795f, y = 450f, radius = 18f, health = 80f, maxHealth = 80f, name = "Bear Guard 2"),
      // THE MEGA BOSS BEAR!
      BearEnemy(
        id = 3,
        x = 742f,
        y = 345f,
        radius = 34f,
        health = 300f,
        maxHealth = 300f,
        isBoss = true,
        name = "الزعيم الدب الأكبر (Mega Bear)"
      )
    )

    return LevelData(
      levelNumber = 10,
      titleAr = "المعركة الكبرى: انهيار وول ستريت (Final Boss)",
      titleEn = "Final Wall Street Crash Boss",
      subtitleAr = "الزعيم الدب الأكبر ذو الـ 300 نقطة حياة! اهزمه لتنهي اللعبة وتفتح بوابة FinTeClub!",
      birds = listOf(
        BirdType.CRYPTO_BOMB,
        BirdType.CANDLE_ROCKET,
        BirdType.TRIPLE_SPLIT,
        BirdType.BULL_COIN,
        BirdType.CRYPTO_BOMB
      ),
      blocks = blocks,
      bears = bears,
      targetScore3Stars = 7000,
      targetScore2Stars = 5000
    )
  }
}
