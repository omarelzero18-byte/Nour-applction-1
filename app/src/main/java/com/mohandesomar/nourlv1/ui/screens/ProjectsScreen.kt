package com.mohandesomar.nourlv1.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohandesomar.nourlv1.data.model.ProjectItem
import com.mohandesomar.nourlv1.ui.components.AddProjectDialog
import com.mohandesomar.nourlv1.ui.components.EmptyStateCard
import com.mohandesomar.nourlv1.ui.theme.*
import com.mohandesomar.nourlv1.viewmodel.NourViewModel

@Composable
fun ProjectsScreen(
    viewModel: NourViewModel
) {
    val context = LocalContext.current
    val projects by viewModel.allProjects.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ObsidianBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NourCyan,
                contentColor = ObsidianBlack,
                shape = CircleShape,
                modifier = Modifier.testTag("projects_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مشروع")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .testTag("projects_screen")
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "بورتفوليو المشاريع 💻",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "قسم دائم لتوثيق شغلك ومشاريعك التقنية والإبداعية جنب دراستك!",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (projects.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Default.Code,
                    title = "لا توجد مشاريع مسجلة بعد",
                    message = "القسم فاضي وجاهز لإبداعك.. وثق مشاريعك البرمجية أو التصميمية هنا، واكسب +50 نقطة XP فورية لكل مشروع!",
                    actionButtonText = "إضافة أول مشروع 🚀",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(projects) { project ->
                        ProjectItemCard(
                            project = project,
                            onToggleStatus = {
                                val newStatus = if (project.status == "COMPLETED") "IN_PROGRESS" else "COMPLETED"
                                viewModel.updateProject(project.copy(status = newStatus))
                            },
                            onDelete = { viewModel.deleteProject(project) },
                            onOpenLink = {
                                if (project.projectUrl.isNotBlank()) {
                                    try {
                                        val url = if (!project.projectUrl.startsWith("http://") && !project.projectUrl.startsWith("https://")) {
                                            "https://${project.projectUrl}"
                                        } else project.projectUrl
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            }
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
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onSave = {
                viewModel.addProject(it)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProjectItemCard(
    project: ProjectItem,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onOpenLink: () -> Unit
) {
    val isCompleted = project.status == "COMPLETED"

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(if (isCompleted) SuccessMint.copy(alpha = 0.5f) else NourCyan.copy(alpha = 0.4f), Color.Transparent)
            )
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
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCompleted) SuccessMint.copy(alpha = 0.2f) else NourCyan.copy(alpha = 0.2f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(if (isCompleted) SuccessMint else NourCyan, if (isCompleted) SuccessMint else NourCyan)
                        )
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = if (isCompleted) "مكتمل ✅" else "شغال عليه ⏳",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) SuccessMint else NourCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (project.techStack.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = NourGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = project.techStack,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (project.projectUrl.isNotBlank()) {
                    TextButton(
                        onClick = onOpenLink,
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = NourCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "فتح الرابط", color = NourCyan, style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onToggleStatus) {
                        Text(
                            text = if (isCompleted) "جعله قيد التنفيذ" else "تحديد كمكتمل ✅",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف المشروع", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
