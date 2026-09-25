package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.LevelProgress

class GamePreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("finteclub_slingshot_prefs", Context.MODE_PRIVATE)

  fun isLevelUnlocked(levelNumber: Int): Boolean {
    if (levelNumber == 1) return true
    return prefs.getBoolean("level_${levelNumber}_unlocked", false)
  }

  fun unlockLevel(levelNumber: Int) {
    prefs.edit().putBoolean("level_${levelNumber}_unlocked", true).apply()
  }

  fun getStars(levelNumber: Int): Int {
    return prefs.getInt("level_${levelNumber}_stars", 0)
  }

  fun saveLevelResult(levelNumber: Int, score: Int, stars: Int) {
    val currentHigh = prefs.getInt("level_${levelNumber}_high_score", 0)
    val currentStars = prefs.getInt("level_${levelNumber}_stars", 0)

    val editor = prefs.edit()
    if (score > currentHigh) {
      editor.putInt("level_${levelNumber}_high_score", score)
    }
    if (stars > currentStars) {
      editor.putInt("level_${levelNumber}_stars", stars)
    }
    // Unlock next level if levelNumber < 10
    if (levelNumber < 10) {
      editor.putBoolean("level_${levelNumber + 1}_unlocked", true)
    }
    // If level 10 is completed, record game completion
    if (levelNumber == 10 && stars > 0) {
      editor.putBoolean("game_completed_all_10", true)
    }
    editor.apply()
  }

  fun getHighScore(levelNumber: Int): Int {
    return prefs.getInt("level_${levelNumber}_high_score", 0)
  }

  fun getTotalStars(): Int {
    var total = 0
    for (i in 1..10) {
      total += getStars(i)
    }
    return total
  }

  fun hasCompletedAll10Levels(): Boolean {
    return prefs.getBoolean("game_completed_all_10", false) || getStars(10) > 0
  }

  fun unlockAllLevelsCheat() {
    val editor = prefs.edit()
    for (i in 1..10) {
      editor.putBoolean("level_${i}_unlocked", true)
    }
    editor.apply()
  }

  fun getAllLevelsProgress(): List<LevelProgress> {
    return (1..10).map { lvl ->
      LevelProgress(
        levelNumber = lvl,
        isUnlocked = isLevelUnlocked(lvl),
        stars = getStars(lvl),
        highScore = getHighScore(lvl)
      )
    }
  }

  var isSoundEnabled: Boolean
    get() = prefs.getBoolean("sound_enabled", true)
    set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

  var isHapticsEnabled: Boolean
    get() = prefs.getBoolean("haptics_enabled", true)
    set(value) = prefs.edit().putBoolean("haptics_enabled", value).apply()

  var isClubMember: Boolean
    get() = prefs.getBoolean("is_club_member", false)
    set(value) = prefs.edit().putBoolean("is_club_member", value).apply()

  var memberName: String
    get() = prefs.getString("member_name", "") ?: ""
    set(value) = prefs.edit().putString("member_name", value).apply()
}
