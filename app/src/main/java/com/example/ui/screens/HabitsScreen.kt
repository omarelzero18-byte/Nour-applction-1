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
import androidx.compose.ui.unit.dp
import com.example.data.model.HabitItem
import com.example.speech.SpeechHelper
import com.example.ui.components.AddHabitDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*
import com.example.viewmodel.NourViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitsScreen(
    viewModel: NourViewModel,
    speechHelper: SpeechHelper
) {
    val habits by viewModel.allHabits.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val uncompletedToday = habits.filter { it.lastCompletedDate != today }
    val focusReserve = profile?.focusReserve ?: 0

    Scaffold(
        containerColor = ObsidianBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NourViolet,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("habits_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "بناء عادة")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .testTag("habits_screen"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "العادات والـ Streak 🔥",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الاستمرارية هي سر التفوق الحقيقي.. Streak اليوم يبدأ من الصفر ويكبر معاك!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }

            // Streak Risk Warning (if user has habits but hasn't completed them today)
            if (habits.isNotEmpty() && uncompletedToday.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(WarningAmber, DangerCoral))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "تنبيه استمرارية الـ Streak ⚠️",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber
                                )
                                Text(
                                    text = "عندك ${uncompletedToday.size} عادات متبقية النهاردة! سجل إنجازك قبل نهاية اليوم عشان تحافظ على عدادك.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Focus Reserve
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(NourViolet.copy(alpha = 0.4f), Color.Transparent))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مخزون الطاقة الذهنية (Focus Reserve)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$focusReserve%",
                                style = MaterialTheme.typography.labelMedium,
                                color = NourCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (focusReserve / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NourCyan,
                            trackColor = ObsidianBlack
                        )
                    }
                }
            }

            // Habits List or Zero State
            if (habits.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.LocalFireDepartment,
                        title = "لا توجد عادات مسجلة حتى الآن",
                        message = "حسابك جديد وفاضي تماماً (0 أيام Streak).. ابدأ بتحديد عادة دراسية بسيطة زي 'مذاكرة 45 دقيقة' عشان تبني أول Streak!",
                        actionButtonText = "إضافة عادة جديدة الآن ➕",
                        onActionClick = { showAddDialog = true }
                    )
                }
            } else {
                items(habits) { habit ->
                    val isDone = habit.lastCompletedDate == today
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(if (isDone) SuccessMint else NourViolet, Color.Transparent)
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = habit.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = habit.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NourCyan
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "•", color = TextSecondaryDark)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "أفضل إنجاز: ${habit.bestStreak} يوم",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondaryDark
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Streak Badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (habit.currentStreak > 0) NourGold.copy(alpha = 0.2f) else ObsidianCard,
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(
                                            listOf(if (habit.currentStreak > 0) NourGold else ObsidianCardBorder, if (habit.currentStreak > 0) NourGold else ObsidianCardBorder)
                                        )
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = NourGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${habit.currentStreak} يوم",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = NourGold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Button(
                                    onClick = { viewModel.checkInHabit(habit) },
                                    enabled = !isDone,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDone) SuccessMint.copy(alpha = 0.2f) else NourViolet
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = if (isDone) "تم ✅" else "تسجيل 🔥",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDone) SuccessMint else Color.White
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteHabit(habit) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف العادة", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            speechHelper = speechHelper,
            onDismiss = { showAddDialog = false },
            onSave = {
                viewModel.addHabit(it)
                showAddDialog = false
            }
        )
    }
}
