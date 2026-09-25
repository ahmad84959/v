package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.GamePreferences
import com.example.data.LevelRepository
import com.example.model.LevelProgress
import com.example.ui.components.FinTeClubLogoView
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NeonCyan

@Composable
fun LevelSelectScreen(
  gamePreferences: GamePreferences,
  onSelectLevel: (Int) -> Unit,
  onNavigateBack: () -> Unit,
  onNavigateToJoinUs: () -> Unit
) {
  BackHandler { onNavigateBack() }

  var refreshTrigger by remember { mutableStateOf(0) }
  val levelsProgress = remember(refreshTrigger) {
    gamePreferences.getAllLevelsProgress()
  }
  val totalStars = remember(refreshTrigger) {
    gamePreferences.getTotalStars()
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberNavy)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("level_select_screen")
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(DeepNavy)
            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
            .testTag("btn_back_levels")
        ) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(
            text = "مراحل السوق المالي (10 مراحل)",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "FinTeClub Challenge",
            color = NeonCyan,
            fontSize = 12.sp
          )
        }
      }

      // Total Stars Chip
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldCoin.copy(alpha = 0.6f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = GoldCoin,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "$totalStars / 30",
            color = GoldCoin,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Join Us Banner shortcut
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)
        .clickable { onNavigateToJoinUs() }
        .testTag("banner_join_us"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = DeepNavy),
      border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          FinTeClubLogoView(size = 46.dp, animateGlow = false)
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "نادي FinTeClub - انضم إلينا!",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = "جامعة أنقرة يلدريم بيازيد | تداول، بلوكشين وفنتك",
              color = NeonCyan,
              fontSize = 11.sp
            )
          }
        }
        Icon(
          Icons.Default.Celebration,
          contentDescription = null,
          tint = NeonCyan,
          modifier = Modifier.size(24.dp)
        )
      }
    }

    // 10 Levels Grid
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      contentPadding = PaddingValues(bottom = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(levelsProgress) { progress ->
        val levelData = LevelRepository.getLevel(progress.levelNumber)
        LevelCard(
          progress = progress,
          titleAr = levelData.titleAr,
          onClick = {
            if (progress.isUnlocked) {
              onSelectLevel(progress.levelNumber)
            }
          }
        )
      }
    }

    // Bottom Dev / Quick Unlock All Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedButton(
        onClick = {
          gamePreferences.unlockAllLevelsCheat()
          refreshTrigger++
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("btn_unlock_all")
      ) {
        Icon(Icons.Default.LockOpen, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("فتح جميع المراحل (تجربة)", fontSize = 12.sp, color = NeonCyan)
      }

      Button(
        onClick = onNavigateToJoinUs,
        colors = ButtonDefaults.buttonColors(containerColor = BullGreen),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("btn_join_us_footer")
      ) {
        Text("انضم للنادي", color = CyberNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      }
    }
  }
}

@Composable
private fun LevelCard(
  progress: LevelProgress,
  titleAr: String,
  onClick: () -> Unit
) {
  val isUnlocked = progress.isUnlocked
  val isBossLevel = progress.levelNumber == 10

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(130.dp)
      .clickable(enabled = isUnlocked) { onClick() }
      .testTag("level_card_${progress.levelNumber}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isUnlocked) (if (isBossLevel) Color(0xFF1E1430) else CardNavy) else DeepNavy.copy(alpha = 0.5f)
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = if (isBossLevel) 1.5.dp else 1.dp,
      color = if (isBossLevel) GoldCoin else (if (isUnlocked) NeonCyan.copy(alpha = 0.5f) else Color.DarkGray)
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Card Top Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(if (isUnlocked) (if (isBossLevel) GoldCoin else NeonCyan) else Color.Gray),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${progress.levelNumber}",
              color = CyberNavy,
              fontSize = 14.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }

          if (!isUnlocked) {
            Icon(
              Icons.Default.Lock,
              contentDescription = "Locked",
              tint = Color.Gray,
              modifier = Modifier.size(20.dp)
            )
          } else {
            // Stars
            Row {
              for (s in 1..3) {
                Icon(
                  Icons.Default.Star,
                  contentDescription = null,
                  tint = if (s <= progress.stars) GoldCoin else Color.DarkGray,
                  modifier = Modifier.size(15.dp)
                )
              }
            }
          }
        }

        // Title
        Text(
          text = if (isBossLevel) "👑 $titleAr" else titleAr,
          color = if (isUnlocked) Color.White else Color.Gray,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 2
        )

        // Card Bottom
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (isUnlocked) {
            Text(
              text = if (progress.highScore > 0) "أعلى نتيجة: ${progress.highScore}" else "جاهز للعب",
              color = if (progress.highScore > 0) GoldCoin else Color.LightGray,
              fontSize = 10.sp
            )
            Icon(
              Icons.Default.PlayArrow,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(18.dp)
            )
          } else {
            Text(
              text = "مقفل",
              color = Color.Gray,
              fontSize = 11.sp
            )
          }
        }
      }
    }
  }
}
