package com.mohandesomar.nourlv1.ui.components

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
import com.mohandesomar.nourlv1.data.model.HabitItem
import com.mohandesomar.nourlv1.speech.SpeechHelper
import com.mohandesomar.nourlv1.ui.theme.*

@Composable
fun AddHabitDialog(
    speechHelper: SpeechHelper,
    onDismiss: () -> Unit,
    onSave: (HabitItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("مذاكرة") }

    val categories = listOf("مذاكرة 📚", "تركيز 🧠", "صحة ورياضة 🏃‍♂️", "قراءة 📖", "تنظيم ⏰")

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
                        text = "بناء عادة يومية جديدة 🔥",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("اسم العادة اليومية") },
                        placeholder = { Text("مثال: جلسة بومودورو 45 دقيقة") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NourViolet,
                            unfocusedBorderColor = ObsidianCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("habit_title_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    VoiceInputButton(
                        speechHelper = speechHelper,
                        onSpeechResult = { recognized ->
                            title = if (title.isBlank()) recognized else "$title $recognized"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "التصنيف:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.chunked(2).forEach { rowList ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowList.forEach { cat ->
                                val selected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) NourViolet else ObsidianCard,
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = if (selected) NourPrimaryGradient else Brush.linearGradient(listOf(ObsidianCardBorder, ObsidianCardBorder))
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedCategory = cat }
                                ) {
                                    Text(
                                        text = cat,
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
                            if (rowList.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                HabitItem(
                                    title = title.trim(),
                                    category = selectedCategory,
                                    currentStreak = 0, // Zero Seed Data!
                                    bestStreak = 0
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
                        .testTag("save_habit_button")
                ) {
                    Text("بدء العادة (Streak 0 يوم) 🚀", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
