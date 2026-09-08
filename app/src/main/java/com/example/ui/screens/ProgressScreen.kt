package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.theme.*
import com.example.viewmodel.NourViewModel

@Composable
fun ProgressScreen(
    viewModel: NourViewModel
) {
    val profile by viewModel.userProfile.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val projects by viewModel.allProjects.collectAsState()

    val studentName = profile?.name?.ifBlank { "يا بطل" } ?: "يا بطل"
    val points = profile?.points ?: 0 // Zero Seed Data! Starts at 0
    val level = profile?.level ?: 1
    val completedTasksCount = tasks.count { it.isCompleted }
    val habitsCount = habits.size
    val highestStreak = habits.maxOfOrNull { it.currentStreak } ?: 0

    val nextLevelXP = 200
    val progress = (points.toFloat() / nextLevelXP.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        containerColor = ObsidianBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .testTag("progress_screen"),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "المستوى والإنجازات 🏆",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "كل مهمة وعادة بتكسبك خبرة وبتطور مستواك الحقيقي!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }

            // Level & XP Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = NourPrimaryGradient),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "المستوى $level",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NourGold
                                )
                                Text(
                                    text = "طالب ناشئ مستمر",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondaryDark
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(
                                        brush = Brush.radialGradient(listOf(NourGoldLight, NourGoldDark)),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(30.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = NourGold,
                            trackColor = ObsidianBlack
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$points XP حالياً",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                            Text(
                                text = "$nextLevelXP XP للمستوى التالي",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            }

            // Spotify-Wrapped Style Weekly Summary Card (الملخص الأسبوعي)
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140C2C)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(NourViolet, NourCyan, NourGold)
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = NourCyan, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الملخص الأسبوعي • Wrapped 🎧",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NourVioletDark
                            ) {
                                Text(
                                    text = "الأسبوع الحالي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NourCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "يا $studentName، دي نظرة عامة على أدائك منذ بداية الحساب:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SummaryMetricBox(
                                title = "المهام المنجزة",
                                value = "$completedTasksCount",
                                icon = Icons.Default.CheckCircle,
                                tint = SuccessMint,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryMetricBox(
                                title = "أعلى Streak",
                                value = "$highestStreak يوم",
                                icon = Icons.Default.LocalFireDepartment,
                                tint = NourGold,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryMetricBox(
                                title = "المشاريع",
                                value = "${projects.size}",
                                icon = Icons.Default.Code,
                                tint = NourCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (completedTasksCount == 0 && highestStreak == 0)
                                    "✨ 'البداية هي نصف كل شيء' — حسابك لسه في يومه الأول، وكل خطوة جاية هتغير أرقامك دي للأعلى!"
                                else
                                    "🔥 استمرارية ممتازة يا $studentName! عاداتك بتتثبت ونور فخورة بتركيزك اليومي.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NourGoldLight,
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Badges Section
            item {
                Text(
                    text = "شارات التميز 🏅",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))

                val badges = listOf(
                    Triple("شارة البداية الذهبية", "تفعيل أول خطة وعادات في التطبيق", points >= 50),
                    Triple("بطل الـ Streak", "المحافظة على عادة يومية لعدة أيام", highestStreak >= 3),
                    Triple("محارب الطوارئ", "تفعيل خطة إنقاذ امتحانات بنجاح", viewModel.activeEmergencyPlan.collectAsState().value != null),
                    Triple("صانع المشاريع", "توثيق مشروع برمجي أو تقني", projects.isNotEmpty())
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    badges.forEach { (name, desc, unlocked) ->
                        BadgeRowItem(name = name, description = desc, isUnlocked = unlocked)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SummaryMetricBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ObsidianBlack,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(ObsidianCardBorder, ObsidianCardBorder))
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun BadgeRowItem(
    name: String,
    description: String,
    isUnlocked: Boolean
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isUnlocked) ObsidianCardElevated else ObsidianCard.copy(alpha = 0.5f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(if (isUnlocked) NourGold.copy(alpha = 0.5f) else ObsidianCardBorder, Color.Transparent)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isUnlocked) NourGold.copy(alpha = 0.2f) else ObsidianCard,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.MilitaryTech else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) NourGold else TextTertiaryDark,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color.White else TextTertiaryDark
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }
        }
    }
}
