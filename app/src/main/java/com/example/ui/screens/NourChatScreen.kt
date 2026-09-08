package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiConnectionStatus
import com.example.data.model.ChatMessage
import com.example.speech.SpeechHelper
import com.example.ui.components.VoiceInputButton
import com.example.ui.theme.*
import com.example.viewmodel.NourViewModel
import kotlinx.coroutines.launch

@Composable
fun NourChatScreen(
    viewModel: NourViewModel,
    speechHelper: SpeechHelper
) {
    val messages by viewModel.allChatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val connectionStatus by viewModel.chatConnectionStatus.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val studentName = profile?.name?.ifBlank { "يا بطل" } ?: "يا بطل"

    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val quickChips = listOf(
        "ساعديني أنظم يومي 📅",
        "عندي امتحان ومش لاحق 🚨",
        "حاسس بإحباط ومحتاج تشجيع 💜",
        "فكرة لمشروع برمجي 💻"
    )

    // Check Gemini API connection in real-time when chat screen is opened
    LaunchedEffect(Unit) {
        viewModel.checkChatConnection()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = ObsidianBlack,
        topBar = {
            Surface(
                color = ObsidianSurface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(NourViolet.copy(alpha = 0.5f), Color.Transparent))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                brush = Brush.radialGradient(listOf(NourCyan, NourViolet)),
                                shape = CircleShape
                            )
                            .border(1.5.dp, NourGold, CircleShape)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نور (Nour-lv1)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (val status = connectionStatus) {
                                is GeminiConnectionStatus.Checking -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        strokeWidth = 1.5.dp,
                                        color = NourCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "فحص الاتصال بـ Gemini...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NourCyan
                                    )
                                }
                                is GeminiConnectionStatus.Connected -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SuccessMint)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "نور متصلة ✅ (${status.latencyMs}ms)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessMint
                                    )
                                }
                                is GeminiConnectionStatus.KeyMissingOrInvalid -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(DangerCoral)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مشكلة في المفتاح ⚠️ (Secrets panel)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DangerCoral
                                    )
                                }
                                is GeminiConnectionStatus.ServerBusy -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NourGold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "السيرفر مزدحم (503) 🔄",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NourGold
                                    )
                                }
                                is GeminiConnectionStatus.NetworkError -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(TextSecondaryDark)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تعذر الاتصال بالشبكة 🌐",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondaryDark
                                    )
                                }
                                is GeminiConnectionStatus.Idle -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SuccessMint)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "متصلة الآن • رفيقتك الذكية",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessMint
                                    )
                                }
                            }
                        }
                    }

                    // Manual Connection Re-test button
                    IconButton(
                        onClick = { viewModel.checkChatConnection() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "إعادة فحص الاتصال",
                            tint = NourCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Quick Suggestion Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    items(quickChips) { chip ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ObsidianCardElevated,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(ObsidianCardBorder, ObsidianCardBorder))
                            ),
                            modifier = Modifier.clickable {
                                viewModel.sendMessageToNour(chip)
                            }
                        ) {
                            Text(
                                text = chip,
                                style = MaterialTheme.typography.labelSmall,
                                color = NourCyan,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Input Row with Voice Mic & Send
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = { Text("اسأل نور أي سؤال في دراستك...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NourViolet,
                            unfocusedBorderColor = ObsidianCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    VoiceInputButton(
                        speechHelper = speechHelper,
                        onSpeechResult = { recognized ->
                            inputQuery = if (inputQuery.isBlank()) recognized else "$inputQuery $recognized"
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputQuery.isNotBlank()) {
                                val text = inputQuery
                                inputQuery = ""
                                viewModel.sendMessageToNour(text)
                            }
                        },
                        enabled = inputQuery.isNotBlank() && !isChatLoading,
                        modifier = Modifier
                            .size(46.dp)
                            .background(if (inputQuery.isNotBlank()) NourViolet else ObsidianCard, CircleShape)
                            .testTag("send_chat_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال",
                            tint = if (inputQuery.isNotBlank()) Color.White else TextSecondaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("chat_screen")
        ) {
            // Live Connection Issue Banners
            when (val status = connectionStatus) {
                is GeminiConnectionStatus.KeyMissingOrInvalid -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DangerCoral.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, DangerCoral.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.KeyOff,
                                contentDescription = null,
                                tint = DangerCoral,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تنبيه نور: مفتاح الـ API مفقود أو غير صحيح ⚠️",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerCoral
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "يا $studentName، عشان أقدر أرد عليك محتاجين نتأكد من ضبط GEMINI_API_KEY في لوحة Secrets في إعدادات Google AI Studio!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = { viewModel.checkChatConnection() },
                                colors = ButtonDefaults.textButtonColors(contentColor = DangerCoral)
                            ) {
                                Text("إعادة فحص", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                is GeminiConnectionStatus.ServerBusy -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NourGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NourGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = NourGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ازدحام مؤقت في السيرفر (HTTP ${status.code}) 🔄",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NourGold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "المفتاح مضبوط بنجاح، لكن سيرفرات Google عليها ضغط لحظي. جرب تسألني كمان ثواني وهرد عليك فوراً!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = { viewModel.checkChatConnection() },
                                colors = ButtonDefaults.textButtonColors(contentColor = NourGold)
                            ) {
                                Text("تحديث", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                is GeminiConnectionStatus.NetworkError -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, TextSecondaryDark.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تعذر الاتصال بالشبكة 🌐",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = status.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = { viewModel.checkChatConnection() },
                                colors = ButtonDefaults.textButtonColors(contentColor = NourCyan)
                            ) {
                                Text("إعادة المحاولة", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                else -> { /* Idle or Connected - no warning banner needed */ }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.radialGradient(listOf(NourViolet.copy(alpha = 0.4f), Color.Transparent)),
                                    shape = CircleShape
                                )
                                .border(1.5.dp, NourCyan, CircleShape)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NourCyan, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "أهلاً بيك يا $studentName! 💜",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "أنا جاهزة أساعدك في أي وقت: تقسيم جدول مذاكرتك، خطط طوارئ للامتحانات، نصائح برمجة، أو حتى لو حاسس بإحباط وعايز طاقة إيجابية!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryDark,
                            lineHeight = 22.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(messages) { msg ->
                            ChatMessageBubble(
                                message = msg,
                                onSpeak = { text -> viewModel.speakNour(text) },
                                onStopSpeak = { viewModel.stopSpeaking() }
                            )
                        }

                        if (isChatLoading) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 12.dp, top = 6.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = NourCyan
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "نور بتفكر وتكتبلك... 💜",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onSpeak: (String) -> Unit = {},
    onStopSpeak: () -> Unit = {}
) {
    val isUser = message.sender == "USER"
    var isPlayingThis by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .background(NourVioletDark, CircleShape)
                    .border(1.dp, NourCyan, CircleShape)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NourCyan, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) NourViolet else ObsidianCardElevated,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    if (isUser) listOf(NourViolet, NourCyan) else listOf(ObsidianCardBorder, ObsidianCardBorder)
                )
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    lineHeight = 22.sp
                )
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPlayingThis) NourViolet else ObsidianCard,
                            border = BorderStroke(1.dp, if (isPlayingThis) NourCyan else ObsidianCardBorder),
                            modifier = Modifier.clickable {
                                if (isPlayingThis) {
                                    isPlayingThis = false
                                    onStopSpeak()
                                } else {
                                    isPlayingThis = true
                                    onSpeak(message.message)
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPlayingThis) "إيقاف الصوت ⏹" else "استمع لنور 🔊",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPlayingThis) Color.White else NourCyan,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
