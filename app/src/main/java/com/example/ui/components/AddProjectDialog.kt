package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProjectItem
import com.example.ui.theme.*

@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onSave: (ProjectItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var projectUrl by remember { mutableStateOf("") }
    var techStack by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("IN_PROGRESS") } // "IN_PROGRESS", "COMPLETED"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
            border = CardDefaults.outlinedCardBorder().copy(brush = NourPrimaryGradient),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إضافة مشروع جديد 💻",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم المشروع") },
                    placeholder = { Text("مثال: تطبيق مهام بـ Kotlin أو بوت تليجرام") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NourViolet,
                        unfocusedBorderColor = ObsidianCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = projectUrl,
                    onValueChange = { projectUrl = it },
                    label = { Text("رابط المشروع / GitHub (اختياري)") },
                    placeholder = { Text("https://github.com/...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NourViolet,
                        unfocusedBorderColor = ObsidianCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = techStack,
                    onValueChange = { techStack = it },
                    label = { Text("التقنيات المستخدمة") },
                    placeholder = { Text("مثال: Kotlin, Jetpack Compose, Gemini API") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NourViolet,
                        unfocusedBorderColor = ObsidianCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "حالة المشروع:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statuses = listOf(
                        "IN_PROGRESS" to ("لسه شغال عليه ⏳" to NourCyan),
                        "COMPLETED" to ("مكتمل وجاهز ✅" to SuccessMint)
                    )
                    statuses.forEach { (stKey, pair) ->
                        val (label, tint) = pair
                        val selected = status == stKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) tint.copy(alpha = 0.25f) else ObsidianCard,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(if (selected) tint else ObsidianCardBorder, if (selected) tint else ObsidianCardBorder)
                                )
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { status = stKey }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) tint else TextSecondaryDark,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                ProjectItem(
                                    title = title.trim(),
                                    description = description.trim(),
                                    projectUrl = projectUrl.trim(),
                                    techStack = techStack.trim().ifBlank { "عام" },
                                    status = status
                                )
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NourCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_project_button")
                ) {
                    Text(
                        text = "إضافة لبورتفوليو المشاريع 🚀",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianBlack
                    )
                }
            }
        }
    }
}
