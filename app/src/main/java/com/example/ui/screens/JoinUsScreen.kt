package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GamePreferences
import com.example.ui.components.FinTeClubLogoView
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NeonCyan

@Composable
fun JoinUsScreen(
  gamePreferences: GamePreferences,
  onNavigateBack: () -> Unit,
  onReplayGame: () -> Unit
) {
  BackHandler { onNavigateBack() }

  val context = LocalContext.current
  val instagramUrl = "https://www.instagram.com/aybufinteclub"

  var nameInput by remember { mutableStateOf(gamePreferences.memberName) }
  var universityInput by remember { mutableStateOf("Ankara Yıldırım Beyazıt Üniversitesi") }
  var departmentInput by remember { mutableStateOf("") }
  var emailInput by remember { mutableStateOf("") }
  var selectedInterest by remember { mutableStateOf("التحليل والتداول (Trading)") }
  var isSubmitted by remember { mutableStateOf(gamePreferences.isClubMember) }

  fun openInstagram() {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl))
      context.startActivity(intent)
    } catch (_: Exception) {
      Toast.makeText(context, "الرابط: $instagramUrl", Toast.LENGTH_LONG).show()
    }
  }

  fun shareClub() {
    try {
      val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
          Intent.EXTRA_TEXT,
          "🚀 لقد أتممت جميع مراحل لعبة FinTeClub Sling! تابع نادي التكنولوجيا المالية لجامعة أنقرة يلدريم بيازيد على انستغرام: $instagramUrl"
        )
        type = "text/plain"
      }
      context.startActivity(Intent.createChooser(sendIntent, "شارك FinTeClub"))
    } catch (_: Exception) {
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberNavy)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("join_us_screen")
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onNavigateBack,
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(DeepNavy)
          .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
          .testTag("btn_back_join_us")
      ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
      }

      Text(
        text = "انضم إلينا | Join FinTeClub",
        color = NeonCyan,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )

      IconButton(
        onClick = { shareClub() },
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(DeepNavy)
          .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
          .testTag("btn_share_join_us")
      ) {
        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
      }
    }

    // Hero Section: Glowing Animated FinTeClub Logo
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      FinTeClubLogoView(size = 200.dp, animateGlow = true)
    }

    // Congratulations & Club Title Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = CardNavy),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Stars, contentDescription = null, tint = GoldCoin, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "تهانينا! لقد سحقت جميع دببة السوق!",
            color = GoldCoin,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "ANKARA YILDIRIM BEYAZIT ÜNİVERSİTESİ",
          color = Color(0xFFA0B4C8),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 1.sp
        )

        Text(
          text = "FinTeClub - نادي التكنولوجيا المالية",
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "النادي الطلابي الرائد في جامعة أنقرة يلدريم بيازيد المتخصص في مجالات الفنتك (FinTech)، التحليل الفني، التداول الخوارزمي، تقنيات البلوكشين والذكاء الاصطناعي في الأسواق المالية.",
          color = Color(0xFFB0C0D4),
          fontSize = 13.sp,
          lineHeight = 20.sp,
          textAlign = TextAlign.Center
        )
      }
    }

    // PRIMARY INSTAGRAM CALL TO ACTION BUTTON
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 18.dp)
        .clickable { openInstagram() }
        .testTag("btn_instagram_card"),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.horizontalGradient(
              colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
            )
          )
          .padding(vertical = 16.dp, horizontal = 20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Tag,
                contentDescription = "Instagram",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = "تابعنا على انستغرام الرسمي",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "@aybufinteclub",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Icon(
            Icons.Default.OpenInNew,
            contentDescription = "Open Link",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }

    // Application Membership Form / Badge
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 18.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = DeepNavy),
      border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
    ) {
      Column(
        modifier = Modifier.padding(18.dp)
      ) {
        if (!isSubmitted) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CardMembership, contentDescription = null, tint = NeonCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "استمارة الانضمام الفوري للنادي",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("الاسم الكامل (Full Name)") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_member_name"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = Color.DarkGray,
              focusedLabelColor = NeonCyan,
              unfocusedLabelColor = Color.LightGray,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = departmentInput,
            onValueChange = { departmentInput = it },
            label = { Text("القسم أو الكلية (Department / Faculty)") },
            placeholder = { Text("مثال: هندسة حاسوب / إدارة أعمال", color = Color.Gray) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_department"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = Color.DarkGray,
              focusedLabelColor = NeonCyan,
              unfocusedLabelColor = Color.LightGray,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("البريد الإلكتروني (Email)") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_email"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = Color.DarkGray,
              focusedLabelColor = NeonCyan,
              unfocusedLabelColor = Color.LightGray,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "مجال الاهتمام الرئيسي:",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )

          Spacer(modifier = Modifier.height(6.dp))

          val interests = listOf(
            "التحليل الفني والتداول (Trading & Technical Analysis)",
            "البلوكشين والعملات المشفرة (Web3 & Crypto)",
            "الذكاء الاصطناعي في المالية (AI in Finance)",
            "ريادة الأعمال والهاكاثونات (Hackathons & Startups)"
          )

          for (item in interests) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedInterest == item) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                .border(
                  1.dp,
                  if (selectedInterest == item) NeonCyan else Color.DarkGray,
                  RoundedCornerShape(8.dp)
                )
                .clickable { selectedInterest = item }
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = item,
                color = if (selectedInterest == item) NeonCyan else Color.LightGray,
                fontSize = 12.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              if (nameInput.trim().isNotEmpty()) {
                gamePreferences.isClubMember = true
                gamePreferences.memberName = nameInput
                isSubmitted = true
                Toast.makeText(context, "تم تسجيل طلبك بنجاح! أهلاً بك في FinTeClub 🎉", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, "الرجاء كتابة الاسم الكامل", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_submit_membership"),
            colors = ButtonDefaults.buttonColors(containerColor = BullGreen),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(Icons.Default.Send, contentDescription = null, tint = CyberNavy)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "إرسال طلب العضوية",
              color = CyberNavy,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          }
        } else {
          // Member ID Badge
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              Icons.Default.CheckCircle,
              contentDescription = null,
              tint = BullGreen,
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "بطاقة عضو FinTeClub معتمدة!",
              color = BullGreen,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "العضو: ${gamePreferences.memberName}",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.ExtraBold
            )
            Text(
              text = "جامعة أنقرة يلدريم بيازيد | AYBU FinTech Club",
              color = NeonCyan,
              fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
              onClick = { isSubmitted = false },
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("تعديل البيانات", color = Color.LightGray, fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Club Perks and Activities Section
    Text(
      text = "ماذا ينتظرك في FinTeClub؟",
      color = Color.White,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    ClubPerkItem(
      icon = Icons.Default.AutoGraph,
      title = "ورش عمل التداول والتحليل المالي",
      desc = "تعلم استراتيجيات التداول، قراءة الشموع اليابانية، وإدارة المخاطر في الأسواق العالمية وبورصة إسطنبول."
    )

    ClubPerkItem(
      icon = Icons.Default.Security,
      title = "تقنيات البلوكشين والويب 3",
      desc = "بناء العقود الذكية، استكشاف التمويل اللامركزي (DeFi)، وتطبيقات سلاسل الكتل المستقبلية."
    )

    ClubPerkItem(
      icon = Icons.Default.School,
      title = "هاكاثونات ومشاريع تخرج فنتك",
      desc = "المشاركة في هاكاثونات التكنولوجيا المالية الوطنية والدولية، وحاضنات الأفكار الريادية."
    )

    ClubPerkItem(
      icon = Icons.Default.Group,
      title = "شبكة علاقات مع رواد القطاع المالي",
      desc = "لقاءات حصرية مع خبراء البنوك، شركات التكنولوجيا المالية، والمستثمرين لفتح فرص تدريب وتوظيف."
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Replay / Levels Buttons
    Row(modifier = Modifier.fillMaxWidth()) {
      Button(
        onClick = onReplayGame,
        modifier = Modifier
          .weight(1f)
          .height(46.dp)
          .testTag("btn_replay_from_join"),
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("العودة للعب", color = CyberNavy, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.width(10.dp))

      OutlinedButton(
        onClick = { openInstagram() },
        modifier = Modifier
          .weight(1f)
          .height(46.dp)
          .testTag("btn_instagram_secondary"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Instagram", color = Color.White)
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
private fun ClubPerkItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  desc: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = CardNavy),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3250))
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(DeepNavy),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column {
        Text(
          text = title,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = desc,
          color = Color(0xFFA0B4C8),
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
      }
    }
  }
}
