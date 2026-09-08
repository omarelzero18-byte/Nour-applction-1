package com.mohandesomar.nourlv1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohandesomar.nourlv1.data.model.HabitItem
import com.mohandesomar.nourlv1.data.model.SubjectExam
import com.mohandesomar.nourlv1.data.model.SuccessPathDayPoint
import com.mohandesomar.nourlv1.data.model.TaskItem
import com.mohandesomar.nourlv1.sound.SoundManager
import com.mohandesomar.nourlv1.ui.theme.*
import com.mohandesomar.nourlv1.viewmodel.NourViewModel
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessPathScreen(
    viewModel: NourViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val subjectExams by viewModel.allSubjectExams.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allHabits by viewModel.allHabits.collectAsState()

    var selectedExamIndex by remember { mutableIntStateOf(0) }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var selectedDayPoint by remember { mutableStateOf<SuccessPathDayPoint?>(null) }

    // Ensure selectedExamIndex stays valid
    val currentExam = if (subjectExams.isNotEmpty()) {
        subjectExams.getOrNull(selectedExamIndex) ?: subjectExams.first()
    } else null

    val dayPoints = remember(currentExam, allTasks, allHabits) {
        if (currentExam != null) {
            viewModel.calculateSuccessPathPoints(currentExam, allTasks, allHabits)
        } else emptyList()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = strings.successPathTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = strings.successPathSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            SoundManager.playButtonTap()
                            showAddExamDialog = true
                        },
                        modifier = Modifier
                            .testTag("add_exam_button")
                            .clip(CircleShape)
                            .background(NourViolet.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = strings.addSubjectExam,
                            tint = NourCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Subjects Tabs / Selector
            item {
                if (subjectExams.isNotEmpty()) {
                    Text(
                        text = "المواد والامتحانات المسجلة:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(subjectExams) { index, exam ->
                            val isSelected = index == selectedExamIndex
                            SubjectChip(
                                exam = exam,
                                isSelected = isSelected,
                                onClick = {
                                    SoundManager.playButtonTap()
                                    selectedExamIndex = index
                                    selectedDayPoint = null
                                }
                            )
                        }
                    }
                }
            }

            // 2. If no exams yet, provide high-craft Empty State
            if (currentExam == null) {
                item {
                    EmptySuccessPathCard(
                        strings = strings,
                        onAddExamClick = {
                            SoundManager.playButtonTap()
                            showAddExamDialog = true
                        }
                    )
                }
            } else {
                // 3. Countdown Banner & Curriculum Units Progress
                item {
                    ExamCountdownHeroCard(
                        exam = currentExam,
                        strings = strings,
                        onIncrementUnit = {
                            viewModel.incrementExamUnit(currentExam)
                        },
                        onDeleteExam = {
                            viewModel.deleteSubjectExam(currentExam)
                            selectedExamIndex = 0
                        }
                    )
                }

                // 4. Daily Achievement Metric Card (Combined Tasks + Streak)
                item {
                    val todayPoint = dayPoints.find { it.isToday }
                    val currentAchievement = todayPoint?.achievementPercentage ?: 70
                    DailyAchievementCard(
                        strings = strings,
                        achievementPercentage = currentAchievement,
                        completedTasks = todayPoint?.completedSubjectTasks ?: 0,
                        totalTasks = todayPoint?.totalSubjectTasks ?: 1,
                        streakActive = todayPoint?.studyStreakActive ?: true
                    )
                }

                // 5. Stock-Market Interactive Line Chart
                item {
                    StockMarketPerformanceCard(
                        points = dayPoints,
                        strings = strings,
                        onPointSelected = { point ->
                            SoundManager.playButtonTap()
                            selectedDayPoint = point
                        }
                    )
                }

                // 6. Inspected Day Point Detail
                if (selectedDayPoint != null) {
                    item {
                        DayDetailCard(
                            point = selectedDayPoint!!,
                            strings = strings,
                            onClose = { selectedDayPoint = null }
                        )
                    }
                }

                // 7. Exam Countdown Calendar Grid (with automated glowing "X" marks)
                item {
                    ExamCountdownCalendarCard(
                        points = dayPoints,
                        strings = strings,
                        onDayClick = { point ->
                            SoundManager.playButtonTap()
                            selectedDayPoint = point
                        }
                    )
                }
            }
        }
    }

    if (showAddExamDialog) {
        AddSubjectExamDialog(
            strings = strings,
            onDismiss = { showAddExamDialog = false },
            onConfirm = { name, title, days, units, color ->
                viewModel.addSubjectExam(
                    subjectName = name,
                    examTitle = title,
                    daysFromNow = days,
                    totalUnits = units,
                    colorHex = color
                )
                showAddExamDialog = false
            }
        )
    }
}

@Composable
private fun SubjectChip(
    exam: SubjectExam,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val chipColor = try {
        Color(android.graphics.Color.parseColor(exam.colorHex))
    } catch (_: Throwable) {
        NourCyan
    }

    val backgroundColor = if (isSelected) chipColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isSelected) chipColor else MaterialTheme.colorScheme.outline

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(chipColor)
            )
            Text(
                text = exam.subjectName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExamCountdownHeroCard(
    exam: SubjectExam,
    strings: AppStrings,
    onIncrementUnit: () -> Unit,
    onDeleteExam: () -> Unit
) {
    val now = System.currentTimeMillis()
    val diffMillis = exam.targetDateMillis - now
    val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis).coerceAtLeast(0)

    val subjectColor = try {
        Color(android.graphics.Color.parseColor(exam.colorHex))
    } catch (_: Throwable) {
        NourCyan
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(subjectColor.copy(alpha = 0.6f), MaterialTheme.colorScheme.outline)),
                RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = exam.subjectName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = subjectColor
                    )
                    Text(
                        text = exam.examTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDeleteExam,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = strings.deleteExamAction,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Big Countdown Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                subjectColor.copy(alpha = 0.18f),
                                NourVioletDark.copy(alpha = 0.25f)
                            )
                        )
                    )
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.countdownTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$daysRemaining",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = if (daysRemaining <= 3) NourCoral else subjectColor
                        )
                        Text(
                            text = strings.daysRemainingWord,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            // Curriculum Units Progress
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.unitsProgressTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${exam.completedUnits} / ${exam.totalUnits} فصول",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = subjectColor
                    )
                }

                val unitProgress = if (exam.totalUnits > 0) {
                    (exam.completedUnits.toFloat() / exam.totalUnits).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { unitProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = subjectColor,
                    trackColor = MaterialTheme.colorScheme.background
                )

                if (exam.completedUnits < exam.totalUnits) {
                    OutlinedButton(
                        onClick = onIncrementUnit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = subjectColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, subjectColor.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.markUnitCompleted, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyAchievementCard(
    strings: AppStrings,
    achievementPercentage: Int,
    completedTasks: Int,
    totalTasks: Int,
    streakActive: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.dailyAchievementTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (streakActive) Icons.Default.LocalFireDepartment else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (streakActive) NourGold else NourCoral,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (streakActive) strings.streakMaintained else strings.streakBroken,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (streakActive) NourGoldLight else NourCoral
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completedTasks من $totalTasks مهام تم إنجازها اليوم",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(NourViolet.copy(alpha = 0.35f), MaterialTheme.colorScheme.surfaceVariant)
                        )
                    )
                    .border(2.dp, NourCyan, CircleShape)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$achievementPercentage%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NourCyan
                    )
                    Text(
                        text = "إنجاز",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Stock-Market Performance Line Chart ("مؤشر البورصة")
 * Custom drawn with glowing bezier curve and interactive tap detection.
 */
@Composable
private fun StockMarketPerformanceCard(
    points: List<SuccessPathDayPoint>,
    strings: AppStrings,
    onPointSelected: (SuccessPathDayPoint) -> Unit
) {
    val nonFuturePoints = points.filter { !it.isFutureDay }
    val lastIndex = nonFuturePoints.lastOrNull()?.stockMarketIndex ?: 50f
    val firstIndex = nonFuturePoints.firstOrNull()?.stockMarketIndex ?: 50f
    val isOverallRising = lastIndex >= firstIndex

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = if (isOverallRising) NourEmerald else NourCoral,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.stockChartTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = strings.stockChartSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Current Index Score
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isOverallRising) NourEmerald.copy(alpha = 0.2f) else NourCoral.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "%.1f pts".format(lastIndex),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverallRising) NourEmerald else NourCoral
                    )
                }
            }

            // Interactive Stock Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
            ) {
                if (nonFuturePoints.isNotEmpty()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .pointerInput(nonFuturePoints) {
                                detectTapGestures { offset ->
                                    val stepX = size.width / (nonFuturePoints.size.coerceAtLeast(2) - 1).toFloat()
                                    val tappedIndex = ((offset.x + (stepX / 2)) / stepX).toInt()
                                        .coerceIn(0, nonFuturePoints.size - 1)
                                    onPointSelected(nonFuturePoints[tappedIndex])
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height

                        val minVal = 0f
                        val maxVal = 100f
                        val range = (maxVal - minVal).coerceAtLeast(1f)

                        val coords = nonFuturePoints.mapIndexed { idx, point ->
                            val x = if (nonFuturePoints.size > 1) {
                                (idx.toFloat() / (nonFuturePoints.size - 1)) * w
                            } else w / 2f
                            val normY = (point.stockMarketIndex - minVal) / range
                            val y = h - (normY * h)
                            Offset(x, y)
                        }

                        // Draw background horizontal grid lines
                        val gridLines = 4
                        for (g in 0..gridLines) {
                            val gy = (g.toFloat() / gridLines) * h
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(0f, gy),
                                end = Offset(w, gy),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Draw filled gradient under line
                        val fillPath = Path().apply {
                            moveTo(coords.first().x, h)
                            coords.forEach { lineTo(it.x, it.y) }
                            lineTo(coords.last().x, h)
                            close()
                        }

                        val fillBrush = Brush.verticalGradient(
                            colors = listOf(
                                (if (isOverallRising) NourCyan else NourGold).copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = h
                        )
                        drawPath(fillPath, brush = fillBrush)

                        // Draw smooth line
                        val linePath = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            for (i in 1 until coords.size) {
                                val prev = coords[i - 1]
                                val cur = coords[i]
                                val cx = (prev.x + cur.x) / 2f
                                cubicTo(cx, prev.y, cx, cur.y, cur.x, cur.y)
                            }
                        }

                        drawPath(
                            path = linePath,
                            brush = Brush.horizontalGradient(
                                listOf(NourViolet, NourCyan, if (isOverallRising) NourEmerald else NourGold)
                            ),
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Draw points on curve
                        coords.forEachIndexed { idx, offset ->
                            val point = nonFuturePoints[idx]
                            val isToday = point.isToday
                            val pointColor = if (isToday) NourGold else NourCyan

                            if (isToday) {
                                // Pulsing outer glow for today
                                drawCircle(
                                    color = NourGold.copy(alpha = 0.35f),
                                    radius = 10.dp.toPx(),
                                    center = offset
                                )
                            }

                            drawCircle(
                                color = pointColor,
                                radius = if (isToday) 6.dp.toPx() else 4.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }
            }

            Text(
                text = if (isOverallRising) strings.marketTrendRising else strings.marketTrendFalling,
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverallRising) NourEmerald else NourGoldLight,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Inspected Day Card - Shows exact metrics when a chart or calendar point is tapped
 */
@Composable
private fun DayDetailCard(
    point: SuccessPathDayPoint,
    strings: AppStrings,
    onClose: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NourCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = NourCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${strings.dayDetailTitle} (${point.displayDate})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = strings.closeAction, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DetailMetricBox(
                    title = "نسبة الإنجاز",
                    value = "${point.achievementPercentage}%",
                    tint = NourCyan
                )
                DetailMetricBox(
                    title = "مؤشر البورصة",
                    value = "%.1f".format(point.stockMarketIndex),
                    tint = NourVioletLight
                )
                DetailMetricBox(
                    title = "المهام",
                    value = "${point.completedSubjectTasks}/${point.totalSubjectTasks}",
                    tint = NourEmerald
                )
                DetailMetricBox(
                    title = "الـ Streak",
                    value = if (point.studyStreakActive) "مستمر 🔥" else "منقطع ⚠️",
                    tint = if (point.studyStreakActive) NourGold else NourCoral
                )
            }
        }
    }
}

@Composable
private fun DetailMetricBox(title: String, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = tint)
    }
}

/**
 * Exam Countdown Calendar Card
 * Renders grid of days up to exam date.
 * Automatically marks all passed days with a glowing visual 'X'.
 */
@Composable
private fun ExamCountdownCalendarCard(
    points: List<SuccessPathDayPoint>,
    strings: AppStrings,
    onDayClick: (SuccessPathDayPoint) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = NourViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.calendarTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = strings.calendarSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Calendar Grid: 7 columns (days of week)
            val chunkedDays = remember(points) { points.chunked(7) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chunkedDays.forEach { rowDays ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowDays.forEach { point ->
                            CalendarDayCell(
                                point = point,
                                modifier = Modifier.weight(1f),
                                onClick = { onDayClick(point) }
                            )
                        }
                        // Fill empty cells if last row has fewer than 7 days
                        val remaining = 7 - rowDays.size
                        repeat(remaining) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    point: SuccessPathDayPoint,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cellBackground = when {
        point.isToday -> NourGold.copy(alpha = 0.25f)
        point.isPastDay -> MaterialTheme.colorScheme.background
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    val cellBorder = when {
        point.isToday -> NourGold
        point.isPastDay -> MaterialTheme.colorScheme.outline
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(cellBackground)
            .border(1.dp, cellBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (point.isPastDay) {
            // Visual Glowing 'X' mark for past completed days
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val strokeW = 2.5.dp.toPx()
                val xMarkColor = NourCoral.copy(alpha = 0.8f)
                drawLine(
                    color = xMarkColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = xMarkColor,
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }

        // Day Number
        Text(
            text = "${point.dayIndex}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (point.isToday) FontWeight.Black else FontWeight.Bold,
            color = when {
                point.isToday -> NourGold
                point.isPastDay -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.onBackground
            }
        )
    }
}

@Composable
private fun EmptySuccessPathCard(
    strings: AppStrings,
    onAddExamClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(NourViolet.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Timeline,
                    contentDescription = null,
                    tint = NourCyan,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = strings.noExamsYet,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = strings.addFirstExamPrompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onAddExamClick,
                colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.addSubjectExam, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Dialog to add a new subject / exam to Success Path
 */
@Composable
private fun AddSubjectExamDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (name: String, title: String, days: Int, units: Int, colorHex: String) -> Unit
) {
    var subjectName by remember { mutableStateOf("") }
    var examTitle by remember { mutableStateOf("") }
    var daysRemaining by remember { mutableIntStateOf(14) }
    var totalUnits by remember { mutableIntStateOf(5) }
    var selectedColor by remember { mutableStateOf("#00C6FF") }

    val colorOptions = listOf("#00C6FF", "#8A2BE2", "#FFD700", "#00F5D4", "#FF477E")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.addSubjectExam,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text(strings.subjectNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text(strings.examTitleLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column {
                    Text(
                        text = "عدد الأيام المتبقية: $daysRemaining يوماً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = daysRemaining.toFloat(),
                        onValueChange = { daysRemaining = it.roundToInt() },
                        valueRange = 1f..60f,
                        colors = SliderDefaults.colors(thumbColor = NourCyan, activeTrackColor = NourViolet)
                    )
                }

                Column {
                    Text(
                        text = "إجمالي الفصول / الوحدات: $totalUnits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = totalUnits.toFloat(),
                        onValueChange = { totalUnits = it.roundToInt() },
                        valueRange = 1f..20f,
                        colors = SliderDefaults.colors(thumbColor = NourGold, activeTrackColor = NourCyan)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val col = Color(android.graphics.Color.parseColor(hex))
                        val isPicked = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (isPicked) 3.dp else 1.dp,
                                    color = if (isPicked) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subjectName.isNotBlank()) {
                        onConfirm(
                            subjectName.trim(),
                            if (examTitle.isBlank()) "امتحان مادة $subjectName" else examTitle.trim(),
                            daysRemaining,
                            totalUnits,
                            selectedColor
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
                enabled = subjectName.isNotBlank()
            ) {
                Text(strings.saveExamAction, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelAction, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    )
}
