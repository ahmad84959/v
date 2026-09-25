package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.window.Dialog
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.ui.components.FinTeClubLogoView
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NeonCyan

@Composable
fun MainMenuScreen(
  gamePreferences: GamePreferences,
  soundManager: SoundManager,
  onStartGame: () -> Unit,
  onOpenLevels: () -> Unit,
  onOpenJoinUs: () -> Unit
) {
  var showHowToPlay by remember { mutableStateOf(false) }
  var isSoundOn by remember { mutableStateOf(soundManager.isSoundEnabled) }
  val totalStars = remember { gamePreferences.getTotalStars() }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberNavy)
      .testTag("main_menu_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top row: Stars chip & Sound toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DeepNavy),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldCoin.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = GoldCoin, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "$totalStars / 30", color = GoldCoin, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }

        IconButton(
          onClick = {
            isSoundOn = !isSoundOn
            soundManager.isSoundEnabled = isSoundOn
            gamePreferences.isSoundEnabled = isSoundOn
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(DeepNavy)
            .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape)
            .testTag("btn_menu_sound")
        ) {
          Icon(
            imageVector = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
            contentDescription = "Sound Toggle",
            tint = if (isSoundOn) NeonCyan else Color.Gray
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Glowing AYBU FinTeClub Logo
      FinTeClubLogoView(size = 210.dp, animateGlow = true)

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "FinTeClub Sling",
        color = Color.White,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp
      )

      Text(
        text = "Ankara Yıldırım Beyazıt Üniversitesi",
        color = NeonCyan,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
      )

      Text(
        text = "محاكاة المقلاع المالي - اسحق دببة السوق!",
        color = Color(0xFFA0B4C8),
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Menu Buttons Column
      Column(
        modifier = Modifier.fillMaxWidth(0.88f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Start Game Button
        Button(
          onClick = onStartGame,
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("btn_start_game"),
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(16.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberNavy, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "ابدأ اللعب (Play Game)",
            color = CyberNavy,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Levels Grid Button
        Button(
          onClick = onOpenLevels,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("btn_levels_menu"),
          colors = ButtonDefaults.buttonColors(containerColor = CardNavy),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Icon(Icons.Default.List, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "المراحل (10 Stages)",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Join Us Club Portal Button
        Button(
          onClick = onOpenJoinUs,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("btn_join_us_menu"),
          colors = ButtonDefaults.buttonColors(containerColor = BullGreen),
          shape = RoundedCornerShape(16.dp)
        ) {
          Icon(Icons.Default.Celebration, contentDescription = null, tint = CyberNavy)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "انضم إلينا (Join FinTeClub)",
            color = CyberNavy,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // How To Play Button
        OutlinedButton(
          onClick = { showHowToPlay = true },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("btn_how_to_play"),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.LightGray)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "طريقة اللعب والقدرات الخارقة",
            color = Color.LightGray,
            fontSize = 13.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Footer
      Text(
        text = "aybufinteclub | Instagram: @aybufinteclub",
        color = Color(0xFF6B809E),
        fontSize = 11.sp
      )
    }

    // How to Play Modal Dialog
    if (showHowToPlay) {
      HowToPlayDialog(onDismiss = { showHowToPlay = false })
    }
  }
}

@Composable
private fun HowToPlayDialog(onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .testTag("how_to_play_dialog"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = CardNavy),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "🎯 طريقة اللعب وقوى FinTeClub",
          color = NeonCyan,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        HowToItem(
          title = "1. السحب والإطلاق (Slingshot)",
          desc = "اسحب القذيفة إلى الخلف لتحديد قوة وزاوية الإطلاق. خط النقاط المضيء يوضح مسار القذيفة المتوقع!"
        )

        HowToItem(
          title = "2. المهارة الخارقة في الجو (Special Ability)",
          desc = "انقر على الشاشة أثناء طيران القذيفة لتفعيل قدرتها الفريدة:\n• رمز الصعود (Bull Token): اندفاع صاعد فائق السرعة.\n• صاروخ الشمعة (Candle Piercer): اختراق الأعمدة والتحصينات.\n• قنبلة الكريبتو (Crypto Bomb): تفجير فوري يهز الحصن بأكمله.\n• تجزئة الأسهم (Split 3x): الانقسام إلى 3 قذائف تغطي مساحة واسعة!"
        )

        HowToItem(
          title = "3. إسقاط الدببة وهدم التحصينات",
          desc = "الهدف في كل مرحلة هو إسقاط جميع دببة الهبوط. أصِب صناديق TNT المتفجرة لتحدث انفجارات متسلسلة!"
        )

        HowToItem(
          title = "4. الـ 10 مراحل وبوابة الانضمام للنادي",
          desc = "تتدرج اللعبة عبر 10 مراحل صعبة وصولاً إلى مواجهة 'الزعيم الدب الأكبر'. عند إتمام المراحل ستفتح لك بوابة الانضمام إلى نادي FinTeClub!"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("فهمت، لنبدأ اللعب!", color = CyberNavy, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun HowToItem(title: String, desc: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 10.dp)
  ) {
    Text(
      text = title,
      color = GoldCoin,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = desc,
      color = Color(0xFFC0D0E0),
      fontSize = 12.sp,
      lineHeight = 17.sp
    )
  }
}
