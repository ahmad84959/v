package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.data.LevelRepository
import com.example.model.LevelData
import com.example.ui.components.SlingshotCanvas
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NeonCyan

@Composable
fun GameScreen(
  levelNumber: Int,
  soundManager: SoundManager,
  gamePreferences: GamePreferences,
  onNavigateBack: () -> Unit,
  onNextLevel: (Int) -> Unit,
  onNavigateToJoinUs: () -> Unit
) {
  BackHandler { onNavigateBack() }

  var currentScore by remember { mutableIntStateOf(0) }
  var isGameWon by remember { mutableStateOf(false) }
  var isGameLost by remember { mutableStateOf(false) }
  var isPaused by remember { mutableStateOf(false) }
  var starsEarned by remember { mutableIntStateOf(0) }
  var restartKey by remember { mutableIntStateOf(0) }
  var isSoundOn by remember { mutableStateOf(soundManager.isSoundEnabled) }

  val levelData = remember(levelNumber, restartKey) {
    LevelRepository.getLevel(levelNumber)
  }

  fun resetLevel() {
    currentScore = 0
    isGameWon = false
    isGameLost = false
    isPaused = false
    starsEarned = 0
    restartKey++
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberNavy)
      .testTag("game_screen")
  ) {
    // 1. Live Interactive Physics Slingshot Canvas
    key(levelNumber, restartKey) {
      SlingshotCanvas(
        levelData = levelData,
        soundManager = soundManager,
        onScoreAdded = { delta ->
          currentScore += delta
        },
        onLevelCompleted = { stars, bonus ->
          val finalScore = currentScore + bonus
          currentScore = finalScore
          starsEarned = stars
          isGameWon = true
          gamePreferences.saveLevelResult(levelNumber, finalScore, stars)
        },
        onLevelFailed = {
          isGameLost = true
        },
        modifier = Modifier.fillMaxSize()
      )
    }

    // 2. Top HUD Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left: Back button & Level Title
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(DeepNavy.copy(alpha = 0.85f))
            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
            .testTag("btn_back_game")
        ) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
          Text(
            text = "مرحلة $levelNumber / 10",
            color = NeonCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = levelData.titleAr,
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1
          )
        }
      }

      // Center: Score badge
      Card(
        colors = CardDefaults.cardColors(containerColor = DeepNavy.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "النقاط: ",
            color = Color.LightGray,
            fontSize = 12.sp
          )
          Text(
            text = "$currentScore",
            color = GoldCoin,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }

      // Right: Controls (Restart, Sound, Pause)
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = {
            isSoundOn = !isSoundOn
            soundManager.isSoundEnabled = isSoundOn
            gamePreferences.isSoundEnabled = isSoundOn
          },
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(DeepNavy.copy(alpha = 0.8f))
            .testTag("btn_sound_toggle")
        ) {
          Icon(
            imageVector = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
            contentDescription = "Sound",
            tint = if (isSoundOn) NeonCyan else Color.Gray,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
          onClick = { resetLevel() },
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(DeepNavy.copy(alpha = 0.8f))
            .testTag("btn_restart_game")
        ) {
          Icon(
            Icons.Default.Refresh,
            contentDescription = "Restart",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // 3. Ability Guide Bar at Bottom
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 8.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(DeepNavy.copy(alpha = 0.85f))
        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
      Text(
        text = "⚡ اضغط على الشاشة أثناء طيران القذيفة لتفعيل قدرة FinTeClub الخارقة!",
        color = NeonCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
    }

    // 4. VICTORY DIALOG
    if (isGameWon) {
      VictoryDialog(
        levelNumber = levelNumber,
        score = currentScore,
        stars = starsEarned,
        isFinalLevel = levelNumber == 10,
        onNextLevel = {
          if (levelNumber < 10) {
            onNextLevel(levelNumber + 1)
          } else {
            onNavigateToJoinUs()
          }
        },
        onReplay = { resetLevel() },
        onLevelsMenu = onNavigateBack,
        onJoinClub = onNavigateToJoinUs
      )
    }

    // 5. DEFEAT DIALOG
    if (isGameLost) {
      DefeatDialog(
        levelNumber = levelNumber,
        score = currentScore,
        onRetry = { resetLevel() },
        onLevelsMenu = onNavigateBack
      )
    }
  }
}

@Composable
private fun VictoryDialog(
  levelNumber: Int,
  score: Int,
  stars: Int,
  isFinalLevel: Boolean,
  onNextLevel: () -> Unit,
  onReplay: () -> Unit,
  onLevelsMenu: () -> Unit,
  onJoinClub: () -> Unit
) {
  Dialog(onDismissRequest = {}) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .testTag("victory_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CardNavy),
      border = androidx.compose.foundation.BorderStroke(2.dp, NeonCyan)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = if (isFinalLevel) "🎉 النصر الأكبر: انهيار الدببة! 🎉" else "📈 فوز صاعد ممتاز!",
          color = BullGreen,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stars Row
        Row(
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.padding(vertical = 8.dp)
        ) {
          for (i in 1..3) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = "Star",
              tint = if (i <= stars) GoldCoin else Color.DarkGray,
              modifier = Modifier.size(if (i == 2) 48.dp else 38.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "مجموع النقاط: $score",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.ExtraBold
        )

        if (isFinalLevel) {
          Spacer(modifier = Modifier.height(12.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(NeonCyan.copy(alpha = 0.15f))
              .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "👑 أتممت جميع مراحل السوق الـ 10 بنجاح!",
                color = GoldCoin,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = "أنت الآن مؤهل بالكامل للإنضمام إلى نادي FinTeClub!",
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        if (isFinalLevel) {
          Button(
            onClick = onJoinClub,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_join_us_victory"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(Icons.Default.Celebration, contentDescription = null, tint = CyberNavy)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "انضم إلينا الآن (Join FinTeClub)",
              color = CyberNavy,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          }
        } else {
          Button(
            onClick = onNextLevel,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_next_level"),
            colors = ButtonDefaults.buttonColors(containerColor = BullGreen),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text(
              text = "المرحلة التالية ($((levelNumber + 1)))",
              color = CyberNavy,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CyberNavy)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = onReplay,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("btn_replay_victory"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Text("إعادة", color = Color.White)
          }

          Spacer(modifier = Modifier.width(8.dp))

          OutlinedButton(
            onClick = onLevelsMenu,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("btn_menu_victory"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("المراحل", color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
private fun DefeatDialog(
  levelNumber: Int,
  score: Int,
  onRetry: () -> Unit,
  onLevelsMenu: () -> Unit
) {
  Dialog(onDismissRequest = {}) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .testTag("defeat_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = CardNavy),
      border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF334B))
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "📉 تراجع السوق!",
          color = Color(0xFFFF334B),
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "نفدت قذائف الرموز قبل القضاء على جميع الدببة. أعد ضبط زاوية الإطلاق والقوة وحاول مجدداً!",
          color = Color.LightGray,
          fontSize = 13.sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "النقاط المحرزة: $score",
          color = Color.White,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onRetry,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("btn_retry_defeat"),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF334B)),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "إعادة المحاولة",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = onLevelsMenu,
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("btn_menu_defeat"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("قائمة المراحل", color = Color.White)
        }
      }
    }
  }
}
