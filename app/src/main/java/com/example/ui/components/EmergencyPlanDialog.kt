package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun EmergencyPlanDialog(
    onDismiss: () -> Unit,
    onActivatePlan: (examName: String, examDateMillis: Long, topics: String, plan: String) -> Unit
) {
    var examName by remember { mutableStateOf("") }
    var daysRemaining by remember { mutableStateOf("3") }
    var topics by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
            border = CardDefaults.outlinedCardBorder().copy(brush = EmergencyGradient),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = DangerCoral,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تفعيل وضع الطوارئ 🚨",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = DangerCoral
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "نور هتحسبلك خطة إنقاذ مضغوطة تركز على الـ 20% الأكثر أهمية، وتثبت عداد تنازلي حرج في الرئيسية!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("اسم الامتحان / المادة") },
                    placeholder = { Text("مثال: فيزياء، كيمياء عضوية، إحصاء") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DangerCoral,
                        unfocusedBorderColor = ObsidianCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_exam_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = daysRemaining,
                    onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) daysRemaining = it },
                    label = { Text("باقي كام يوم على الامتحان؟") },
                    placeholder = { Text("مثال: 3") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DangerCoral,
                        unfocusedBorderColor = ObsidianCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_days_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = topics,
                    onValueChange = { topics = it },
                    label = { Text("المنهج أو الفصول المتبقية") },
                    placeholder = { Text("مثال: الفصل الثالث والرابع + حل نماذج سابقة") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DangerCoral,
                        unfocusedBorderColor = ObsidianCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_topics_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val days = daysRemaining.toLongOrNull() ?: 3L
                        val targetDate = System.currentTimeMillis() + (days * 86400000L)
                        val generatedPlan = "خطة طوارئ $examName المكثفة:\n" +
                                "• اليوم 1: مذاكرة سريعة لأهم قوانين $topics (جلسات بومودورو 50 دقيقة)\n" +
                                "• اليوم 2: حل امتحانات الوزارة والسنوات السابقة المباشرة\n" +
                                "• ليلة الامتحان: مراجعة الملخص النهائي والخرائط الذهنية"

                        onActivatePlan(
                            examName.trim().ifBlank { "امتحان عاجل" },
                            targetDate,
                            topics.trim().ifBlank { "المنهج كامل" },
                            generatedPlan
                        )
                    },
                    enabled = examName.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerCoral),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("activate_emergency_button")
                ) {
                    Text(
                        text = "بناء خطة الإنقاذ والبدء فوراً! 🔥",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
