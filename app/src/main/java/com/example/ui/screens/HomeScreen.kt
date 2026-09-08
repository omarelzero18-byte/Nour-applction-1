package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitItem
import com.example.data.model.TaskItem
import com.example.speech.SpeechHelper
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.NourViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    viewModel: NourViewModel,
    speechHelper: SpeechHelper,
    onNavigateToTasks: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToSuccessPath: () -> Unit = {},
    onNavigateToSmartTools: () -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val emergencyPlan by viewModel.activeEmergencyPlan.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    val studentName = profile?.name?.ifBlank { "يا بطل" } ?: "يا بطل"
    val points = profile?.points ?: 0 // Zero Seed Data! Starts at 0
    val level = profile?.level ?: 1
    val focusReserve = profile?.focusReserve ?: 0

    val pendingTasks = tasks.filter { !it.isCompleted }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = NourViolet,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("home_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مهمة")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .testTag("home_screen"),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Top Header: Greeting, Level & Points
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "صباح الهمة يا $studentName! 🌟",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = profile?.grade?.ifBlank { "طالب متميز" } ?: "طالب متميز",
                            style = MaterialTheme.typography.bodySmall,
                            color = NourCyan
                        )
                    }

                    // Level & XP Capsule
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianCardElevated,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(NourViolet, NourGold))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = NourGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$points XP • Lv.$level",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Active Emergency Crunch Banner
            if (emergencyPlan != null && emergencyPlan?.isActive == true) {
                item {
                    val daysLeft = ((emergencyPlan!!.examDateMillis - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
                        border = CardDefaults.outlinedCardBorder().copy(brush = EmergencyGradient),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(DangerCoral, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "وضع الطوارئ نشط 🚨",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = DangerCoral,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "امتحان ${emergencyPlan!!.examName} (باقي $daysLeft يوم)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = emergencyPlan!!.remainingTopics,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { viewModel.deactivateEmergencyPlan() }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "إنهاء الطوارئ", tint = SuccessMint)
                            }
                        }
                    }
                }
            }

            // Focus Reserve (رصيد التركيز)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(NourViolet.copy(alpha = 0.5f), Color.Transparent))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = NourCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "رصيد التركيز (Focus Reserve)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "$focusReserve / 100",
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (focusReserve == 0) "الرصيد يبدأ من الصفر — أكمل المهام والعادات لتعبئة مخزون طاقتك الذهنية!" else "طاقة ذهنية نشطة! حافظ على تركيزك.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                }
            }

            // Quick Actions Hub
            item {
                Text(
                    text = "إجراءات سريعة ⚡",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "اسأل نور",
                        subtitle = "مساعدك الذكي",
                        icon = Icons.Default.AutoAwesome,
                        gradient = NourHeroGradient,
                        onClick = onNavigateToChat,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "وضع الطوارئ",
                        subtitle = "خطة مكثفة",
                        icon = Icons.Default.Warning,
                        gradient = EmergencyGradient,
                        onClick = { showEmergencyDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                QuickActionCard(
                    title = "أدوات نور الذكية (v3)",
                    subtitle = "كاميرا المذاكرة • امتحان شفهي • قفل التركيز • ستريك",
                    icon = Icons.Default.Psychology,
                    gradient = SuccessPathGradient,
                    onClick = onNavigateToSmartTools,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Success Path Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(NourCyan, NourViolet, NourGold))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSuccessPath() }
                        .testTag("home_success_path_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(listOf(NourCyan, NourViolet))
                                    )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "مسار النجاح والامتحانات 📈",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "مؤشر البورصة للأداء وعداد تنازلي لكل مادة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NourCyan
                                )
                            }
                        }

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = NourCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Today's Tasks Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مهام اليوم 📝",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = onNavigateToTasks) {
                        Text("عرض الكل", color = NourCyan)
                    }
                }

                if (pendingTasks.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Default.Checklist,
                        title = "لسه معندكش مهام مسجلة!",
                        message = "حسابك جديد وفاضي تماماً.. ابدأ يومك بإضافة أول مهمة دراسية واكسب نقاط XP فورية!",
                        actionButtonText = "أضف أول مهمة الآن ➕",
                        onActionClick = { showAddTaskDialog = true }
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        pendingTasks.take(3).forEach { task ->
                            TaskRowItem(
                                task = task,
                                onToggle = { viewModel.toggleTask(task) }
                            )
                        }
                    }
                }
            }

            // Habits & Streak Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عاداتك والـ Streak 🔥",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = onNavigateToHabits) {
                        Text("إدارة العادات", color = NourCyan)
                    }
                }

                if (habits.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Default.LocalFireDepartment,
                        title = "الـ Streak يبدأ من 0 يوم!",
                        message = "مفيش أي عادة مسجلة مسبقاً.. اختر عادة دراسية يومية وابدأ عداد الاستمرارية الحقيقي بنفسك!",
                        actionButtonText = "بناء أول عادة 🚀",
                        onActionClick = { showAddHabitDialog = true }
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(habits) { habit ->
                            HabitCardItem(
                                habit = habit,
                                onCheckIn = { viewModel.checkInHabit(habit) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            speechHelper = speechHelper,
            onDismiss = { showAddTaskDialog = false },
            onSave = {
                viewModel.addTask(it)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            speechHelper = speechHelper,
            onDismiss = { showAddHabitDialog = false },
            onSave = {
                viewModel.addHabit(it)
                showAddHabitDialog = false
            }
        )
    }

    if (showEmergencyDialog) {
        EmergencyPlanDialog(
            onDismiss = { showEmergencyDialog = false },
            onActivatePlan = { exam, date, topics, plan ->
                viewModel.activateEmergencyPlan(exam, date, topics, plan)
                showEmergencyDialog = false
            }
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianCardElevated,
        border = CardDefaults.outlinedCardBorder().copy(brush = gradient),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .background(gradient, CircleShape)
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
            }
        }
    }
}

@Composable
fun TaskRowItem(
    task: TaskItem,
    onToggle: () -> Unit
) {
    val priorityColor = when (task.priority) {
        "HIGH" -> DangerCoral
        "MEDIUM" -> NourGold
        else -> NourCyan
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ObsidianCard,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(ObsidianCardBorder, Color.Transparent))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = SuccessMint, uncheckedColor = TextSecondaryDark)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) TextTertiaryDark else Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.subject,
                        style = MaterialTheme.typography.labelSmall,
                        color = NourCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "•", color = TextSecondaryDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (task.priority == "HIGH") "أولوية قصوى 🔥" else if (task.priority == "MEDIUM") "متوسطة" else "عادية",
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor
                    )
                }
            }
        }
    }
}

@Composable
fun HabitCardItem(
    habit: HabitItem,
    onCheckIn: () -> Unit
) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isDoneToday = habit.lastCompletedDate == today

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(if (isDoneToday) SuccessMint else NourViolet, Color.Transparent)
            )
        ),
        modifier = Modifier.width(170.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = NourCyan
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = NourGold, modifier = Modifier.size(16.dp))
                    Text(
                        text = "${habit.currentStreak}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NourGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = habit.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onCheckIn,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDoneToday) SuccessMint.copy(alpha = 0.25f) else NourViolet
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isDoneToday) "تم اليوم ✅" else "تسجيل إنجاز 🔥",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDoneToday) SuccessMint else Color.White
                )
            }
        }
    }
}
