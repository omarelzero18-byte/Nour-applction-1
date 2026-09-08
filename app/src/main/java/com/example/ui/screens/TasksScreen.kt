package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskItem
import com.example.speech.SpeechHelper
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*
import com.example.viewmodel.NourViewModel

@Composable
fun TasksScreen(
    viewModel: NourViewModel,
    speechHelper: SpeechHelper
) {
    val tasks by viewModel.allTasks.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "TASK", "EXAM", "DELIVERABLE"
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredTasks = when (selectedFilter) {
        "TASK" -> tasks.filter { it.type == "TASK" }
        "EXAM" -> tasks.filter { it.type == "EXAM" }
        "DELIVERABLE" -> tasks.filter { it.type == "DELIVERABLE" }
        else -> tasks
    }

    Scaffold(
        containerColor = ObsidianBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NourViolet,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("tasks_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مهمة")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .testTag("tasks_screen")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "إدارة المهام والمذاكرة 📚",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "قسّم مذاكرتك لخطوات صغيرة، وحقق أهدافك اليومية!",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "الكل",
                    "TASK" to "مهام 📝",
                    "EXAM" to "امتحانات 🎯",
                    "DELIVERABLE" to "تسليمات 🚀"
                )
                filters.forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NourViolet else ObsidianCardElevated,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = if (isSelected) NourPrimaryGradient else Brush.linearGradient(listOf(ObsidianCardBorder, ObsidianCardBorder))
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = key }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else TextSecondaryDark,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (filteredTasks.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Default.AssignmentLate,
                    title = "لا توجد عناصر في هذا القسم",
                    message = "حسابك فاضي تماماً.. اضغط زر الإضافة لتسجيل أول درس أو امتحان في جدولك!",
                    actionButtonText = "إضافة عنصر جديد ➕",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks) { task ->
                        DetailedTaskCard(
                            task = task,
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            speechHelper = speechHelper,
            onDismiss = { showAddDialog = false },
            onSave = {
                viewModel.addTask(it)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DetailedTaskCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priority) {
        "HIGH" -> DangerCoral
        "MEDIUM" -> NourGold
        else -> NourCyan
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ObsidianCardElevated,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(if (task.isCompleted) SuccessMint.copy(alpha = 0.4f) else NourViolet.copy(alpha = 0.3f), Color.Transparent)
            )
        ),
        modifier = Modifier.fillMaxWidth()
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
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) TextTertiaryDark else Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
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
                        text = if (task.priority == "HIGH") "قصوى 🔥" else if (task.priority == "MEDIUM") "متوسطة" else "عادية",
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "•", color = TextSecondaryDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (task.type == "EXAM") "امتحان" else if (task.type == "DELIVERABLE") "تسليم" else "مهمة",
                        style = MaterialTheme.typography.labelSmall,
                        color = NourGold
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف المهمة", tint = TextSecondaryDark, modifier = Modifier.size(20.dp))
            }
        }
    }
}
