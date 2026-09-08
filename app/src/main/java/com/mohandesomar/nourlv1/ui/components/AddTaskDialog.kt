package com.mohandesomar.nourlv1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.mohandesomar.nourlv1.data.model.TaskItem
import com.mohandesomar.nourlv1.speech.SpeechHelper
import com.mohandesomar.nourlv1.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    speechHelper: SpeechHelper,
    onDismiss: () -> Unit,
    onSave: (TaskItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var taskType by remember { mutableStateOf("TASK") } // "TASK", "EXAM", "DELIVERABLE"
    var priority by remember { mutableStateOf("MEDIUM") } // "HIGH", "MEDIUM", "LOW"

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
                        text = "إضافة مهمة جديدة ✨",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title with Voice Mic
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان المهمة أو الدرس") },
                        placeholder = { Text("مثال: حل شيت فيزياء الفصل الثاني") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NourViolet,
                            unfocusedBorderColor = ObsidianCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("task_title_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    VoiceInputButton(
                        speechHelper = speechHelper,
                        onSpeechResult = { recognized ->
                            title = if (title.isBlank()) recognized else "$title $recognized"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subject
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("المادة أو المجال") },
                    placeholder = { Text("مثال: رياضيات، برمجة، لغة عربية") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NourViolet,
                        unfocusedBorderColor = ObsidianCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_subject_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Task Type Selector
                Text(
                    text = "نوع العنصر:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(
                        "TASK" to "مهمة 📝",
                        "EXAM" to "امتحان 🎯",
                        "DELIVERABLE" to "تسليم 🚀"
                    )
                    types.forEach { (typeKey, label) ->
                        val selected = taskType == typeKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) NourViolet else ObsidianCard,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = if (selected) NourPrimaryGradient else Brush.linearGradient(listOf(ObsidianCardBorder, ObsidianCardBorder))
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { taskType = typeKey }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else TextSecondaryDark,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Selector
                Text(
                    text = "الأولوية:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val priorities = listOf(
                        "LOW" to ("عادية" to NourCyan),
                        "MEDIUM" to ("متوسطة" to NourGold),
                        "HIGH" to ("قصوى 🔥" to DangerCoral)
                    )
                    priorities.forEach { (pKey, pair) ->
                        val (label, tint) = pair
                        val selected = priority == pKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) tint.copy(alpha = 0.25f) else ObsidianCard,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(if (selected) tint else ObsidianCardBorder, if (selected) tint else ObsidianCardBorder))
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = pKey }
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

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                TaskItem(
                                    title = title.trim(),
                                    subject = subject.trim().ifBlank { "عام" },
                                    type = taskType,
                                    priority = priority,
                                    dueDateMillis = System.currentTimeMillis() + (86400000L * 2)
                                )
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_task_button")
                ) {
                    Text("حفظ المهمة (+15 نقطة) ⚡", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
