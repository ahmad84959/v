package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.ui.screens.GameScreen
import com.example.ui.screens.JoinUsScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
  MAIN_MENU,
  LEVEL_SELECT,
  GAME,
  JOIN_US
}

class MainActivity : ComponentActivity() {

  private lateinit var soundManager: SoundManager
  private lateinit var gamePreferences: GamePreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    soundManager = SoundManager(this)
    gamePreferences = GamePreferences(this)
    soundManager.isSoundEnabled = gamePreferences.isSoundEnabled
    soundManager.isHapticsEnabled = gamePreferences.isHapticsEnabled

    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
          AppNavigation(
            soundManager = soundManager,
            gamePreferences = gamePreferences
          )
        }
      }
    }
  }
}

@Composable
fun AppNavigation(
  soundManager: SoundManager,
  gamePreferences: GamePreferences
) {
  var currentScreen by remember { mutableStateOf(ScreenState.MAIN_MENU) }
  var currentLevelNumber by remember { mutableIntStateOf(1) }

  when (currentScreen) {
    ScreenState.MAIN_MENU -> {
      MainMenuScreen(
        gamePreferences = gamePreferences,
        soundManager = soundManager,
        onStartGame = {
          // Start from highest unlocked level or level 1
          val allProgress = gamePreferences.getAllLevelsProgress()
          val latestUnlocked = allProgress.lastOrNull { it.isUnlocked }?.levelNumber ?: 1
          currentLevelNumber = latestUnlocked
          currentScreen = ScreenState.GAME
        },
        onOpenLevels = {
          currentScreen = ScreenState.LEVEL_SELECT
        },
        onOpenJoinUs = {
          currentScreen = ScreenState.JOIN_US
        }
      )
    }

    ScreenState.LEVEL_SELECT -> {
      LevelSelectScreen(
        gamePreferences = gamePreferences,
        onSelectLevel = { level ->
          currentLevelNumber = level
          currentScreen = ScreenState.GAME
        },
        onNavigateBack = {
          currentScreen = ScreenState.MAIN_MENU
        },
        onNavigateToJoinUs = {
          currentScreen = ScreenState.JOIN_US
        }
      )
    }

    ScreenState.GAME -> {
      GameScreen(
        levelNumber = currentLevelNumber,
        soundManager = soundManager,
        gamePreferences = gamePreferences,
        onNavigateBack = {
          currentScreen = ScreenState.LEVEL_SELECT
        },
        onNextLevel = { nextLevel ->
          currentLevelNumber = nextLevel
          currentScreen = ScreenState.GAME
        },
        onNavigateToJoinUs = {
          currentScreen = ScreenState.JOIN_US
        }
      )
    }

    ScreenState.JOIN_US -> {
      JoinUsScreen(
        gamePreferences = gamePreferences,
        onNavigateBack = {
          currentScreen = ScreenState.MAIN_MENU
        },
        onReplayGame = {
          currentLevelNumber = 1
          currentScreen = ScreenState.GAME
        }
      )
    }
  }
}
