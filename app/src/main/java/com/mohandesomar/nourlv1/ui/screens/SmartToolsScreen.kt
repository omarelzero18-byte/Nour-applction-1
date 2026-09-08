package com.mohandesomar.nourlv1.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohandesomar.nourlv1.speech.SpeechHelper
import com.mohandesomar.nourlv1.ui.components.VoiceInputButton
import com.mohandesomar.nourlv1.ui.theme.*
import com.mohandesomar.nourlv1.viewmodel.NourViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartToolsScreen(
    viewModel: NourViewModel,
    speechHelper: SpeechHelper
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val studentName = profile?.name?.ifBlank { "يا بطل" } ?: "يا بطل"

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("رؤية الكاميرا 📷", "امتحان شفهي 🎙️", "قفل التركيز 🔒", "الخطة والتقرير 📊", "ستريك الأصدقاء 👥")

    // Vision state
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var visionSubject by remember { mutableStateOf("فيزياء") }
    val isVisionLoading by viewModel.isVisionLoading.collectAsState()
    val visionResult by viewModel.visionResult.collectAsState()
    val savedReviews by viewModel.allStudyReviews.collectAsState()

    // Oral state
    var oralSubject by remember { mutableStateOf("تاريخ") }
    val oralQuestion by viewModel.currentOralQuestion.collectAsState()
    val isOralLoading by viewModel.isOralLoading.collectAsState()
    val oralFeedback by viewModel.oralFeedback.collectAsState()
    var spokenAnswerText by remember { mutableStateOf("") }

    // Focus Lock state
    val isFocusLockActive by viewModel.isFocusLockActive.collectAsState()
    val focusRemainingSeconds by viewModel.focusRemainingSeconds.collectAsState()

    // Proactive & Report state
    val proactivePlan by viewModel.proactivePlan.collectAsState()
    val isPlanLoading by viewModel.isPlanLoading.collectAsState()
    val weeklyReport by viewModel.weeklyReport.collectAsState()

    // Squad state
    val squadMembers by viewModel.squadMembers.collectAsState()
    val squadError by viewModel.squadError.collectAsState()
    val isSquadLoading by viewModel.isSquadLoading.collectAsState()
    var friendCodeInput by remember { mutableStateOf("") }
    val myInviteCode = viewModel.myInviteCode

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                selectedBitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
            } catch (e: Exception) {
                // Handled gracefully
            }
        }
    }

    val cameraCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            selectedImageUri = null
        }
    }

    Scaffold(
        containerColor = ObsidianBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "أدوات نور الذكية (v3)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "الذكاء الاصطناعي والصوت والرؤية لخدمة الطالب",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("smart_tools_screen")
        ) {
            // Horizontal navigation tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs.indices.toList()) { index ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = {
                            Text(
                                text = tabs[index],
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color.White else TextSecondaryDark
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NourViolet,
                            containerColor = ObsidianCard
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedTab == index,
                            borderColor = ObsidianCardBorder,
                            selectedBorderColor = NourCyan
                        )
                    )
                }
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // ================= 1. Gemini Vision =================
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ObsidianCard,
                                border = BorderStroke(1.dp, ObsidianCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(NourCyan.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NourCyan)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "مساعد الرؤية الذكي (Gemini Vision)",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "صوّر صفحة من كتابك أو كشكولك ونور هتلخصهالك أو تعملك كويز",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryDark
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Subject selection
                                    OutlinedTextField(
                                        value = visionSubject,
                                        onValueChange = { visionSubject = it },
                                        label = { Text("اسم المادة أو الدرس") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NourCyan,
                                            unfocusedBorderColor = ObsidianCardBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Photo selection buttons (Camera + Gallery)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { cameraCaptureLauncher.launch(null) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NourCyan),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("تصوير بالكاميرا 📷", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }

                                        Button(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    androidx.activity.result.PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianCardElevated),
                                            border = BorderStroke(1.dp, NourCyan),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = NourCyan)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("من المعرض 🖼️", color = Color.White, fontSize = 13.sp)
                                        }
                                    }

                                    if (selectedBitmap != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = NourEmerald.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, NourEmerald.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NourEmerald, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("تم تجهيز صورة صفحة المذاكرة بنجاح ✓", color = NourEmerald, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    if (selectedBitmap != null) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    selectedBitmap?.let {
                                                        viewModel.processStudyImage(it, "SUMMARY", visionSubject)
                                                    }
                                                },
                                                enabled = !isVisionLoading,
                                                colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("تلخيص مكثف 📝")
                                            }

                                            Button(
                                                onClick = {
                                                    selectedBitmap?.let {
                                                        viewModel.processStudyImage(it, "QUIZ", visionSubject)
                                                    }
                                                },
                                                enabled = !isVisionLoading,
                                                colors = ButtonDefaults.buttonColors(containerColor = NourCyan),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("كويز 5 أسئلة 🎯", color = Color.Black)
                                            }
                                        }
                                    }

                                    if (isVisionLoading) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(color = NourCyan, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("نور بتحلل محتوى الصفحة وتستخرج القوانين...", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    visionResult?.let { resultText ->
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = ObsidianCardElevated,
                                            border = BorderStroke(1.dp, NourCyan.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("نتيجة تحليل نور 💡", fontWeight = FontWeight.Bold, color = NourCyan)
                                                    IconButton(onClick = { viewModel.speakNour(resultText) }) {
                                                        Icon(Icons.Default.VolumeUp, contentDescription = "استماع", tint = NourCyan)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = resultText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White,
                                                    lineHeight = 20.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Previous Saved Reviews
                        if (savedReviews.isNotEmpty()) {
                            item {
                                Text("ملخصات وكويزات سابقة محفوظة 📚", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            items(savedReviews) { review ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = ObsidianCard,
                                    border = BorderStroke(1.dp, ObsidianCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(review.title, fontWeight = FontWeight.Bold, color = NourCyan, style = MaterialTheme.typography.labelLarge)
                                            IconButton(onClick = { viewModel.speakNour(review.content) }) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = "استماع", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Text(
                                            text = review.content.take(180) + "...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // ================= 2. Oral Mock Exam =================
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ObsidianCard,
                                border = BorderStroke(1.dp, ObsidianCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(NourViolet.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = NourViolet)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "الامتحان الشفهي الذكي مع نور",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "نور بتسألك بالصوت، وانت بتجاوب بصوتك وبتقيّمك فوراً",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondaryDark
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = oralSubject,
                                        onValueChange = { oralSubject = it },
                                        label = { Text("المادة التي ترغب في امتحانها") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NourViolet,
                                            unfocusedBorderColor = ObsidianCardBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { viewModel.startOralExam(oralSubject) },
                                        enabled = !isOralLoading,
                                        colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("اطرحي عليا سؤال شفهي يا نور 🗣️")
                                    }

                                    if (isOralLoading) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp).align(Alignment.CenterHorizontally), color = NourCyan)
                                    }

                                    oralQuestion?.let { q ->
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = ObsidianCardElevated,
                                            border = BorderStroke(1.dp, NourViolet.copy(alpha = 0.6f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("سؤال نور الشفهي:", fontWeight = FontWeight.Bold, color = NourViolet)
                                                    IconButton(onClick = { viewModel.speakNour(q.questionText) }) {
                                                        Icon(Icons.Default.VolumeUp, contentDescription = "استماع للسؤال", tint = NourCyan)
                                                    }
                                                }
                                                Text(q.questionText, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)

                                                Spacer(modifier = Modifier.height(14.dp))
                                                Text("إجابتك المنطوقة:", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = spokenAnswerText,
                                                        onValueChange = { spokenAnswerText = it },
                                                        placeholder = { Text("تكلّم أو اكتب إجابتك...") },
                                                        modifier = Modifier.weight(1f),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = Color.White,
                                                            unfocusedTextColor = Color.White
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    VoiceInputButton(
                                                        speechHelper = speechHelper,
                                                        onSpeechResult = { recognized ->
                                                            spokenAnswerText = if (spokenAnswerText.isBlank()) recognized else "$spokenAnswerText $recognized"
                                                        }
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Button(
                                                    onClick = {
                                                        if (spokenAnswerText.isNotBlank()) {
                                                            viewModel.submitOralAnswer(spokenAnswerText)
                                                        }
                                                    },
                                                    enabled = spokenAnswerText.isNotBlank() && !isOralLoading,
                                                    colors = ButtonDefaults.buttonColors(containerColor = NourCyan),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("إرسال الإجابة للتقييم الصوتي 🎯", color = Color.Black)
                                                }
                                            }
                                        }
                                    }

                                    oralFeedback?.let { feedback ->
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = ObsidianSurface,
                                            border = BorderStroke(1.dp, NourGold),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text("تقييم نور لإجابتك 🌟", fontWeight = FontWeight.Bold, color = NourGold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(feedback, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // ================= 3. Focus Lock =================
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ObsidianCard,
                                border = BorderStroke(1.dp, ObsidianCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(if (isFocusLockActive) DangerCoral.copy(alpha = 0.2f) else NourCyan.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (isFocusLockActive) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = null,
                                            tint = if (isFocusLockActive) DangerCoral else NourCyan,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (isFocusLockActive) "قفل التركيز نَشِط 🔒" else "قفل التركيز ومكافحة التشتت",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isFocusLockActive) "استمر في المذاكرة بدون مقاطعة.. نور مراقباك يا $studentName!" else "اختر مدة الجلسة واقفل كل المشتتات وابدأ مذاكرة عميقة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryDark,
                                        textAlign = TextAlign.Center
                                    )

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !viewModel.hasNotificationPolicyAccess()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = NourVioletDark.copy(alpha = 0.4f),
                                            border = BorderStroke(1.dp, NourCyan.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = NourCyan, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "لكتم إشعارات الهاتف تلقائياً أثناء الجلسة، امنح صلاحية عدم الإزعاج",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                TextButton(
                                                    onClick = {
                                                        try {
                                                            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {}
                                                    }
                                                ) {
                                                    Text("تفعيل", color = NourCyan, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    if (isFocusLockActive) {
                                        val minutes = focusRemainingSeconds / 60
                                        val seconds = focusRemainingSeconds % 60
                                        Text(
                                            text = String.format("%02d:%02d", minutes, seconds),
                                            style = MaterialTheme.typography.displayMedium,
                                            fontWeight = FontWeight.Black,
                                            color = NourCyan
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        OutlinedButton(
                                            onClick = { viewModel.cancelFocusLock() },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCoral),
                                            border = BorderStroke(1.dp, DangerCoral)
                                        ) {
                                            Text("إنهاء الجلسة مبكراً")
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.startFocusLock(25) },
                                                colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("25 دقيقة 🍅")
                                            }
                                            Button(
                                                onClick = { viewModel.startFocusLock(45) },
                                                colors = ButtonDefaults.buttonColors(containerColor = NourCyan),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("45 دقيقة ⚡", color = Color.Black)
                                            }
                                            Button(
                                                onClick = { viewModel.startFocusLock(60) },
                                                colors = ButtonDefaults.buttonColors(containerColor = NourGold),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("60 دقيقة 🏆", color = Color.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // ================= 4. Proactive Plan & Weekly Report =================
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ObsidianCard,
                                border = BorderStroke(1.dp, ObsidianCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("خطة أسبوعية استباقية من نور 📅", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("تحليل ذكي لمهامك وامتحاناتك وتوزيعها على أيام الأسبوع", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.generateProactiveSchedule() },
                                        enabled = !isPlanLoading,
                                        colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("توليد جدول الأسبوع الذكي ✨")
                                    }

                                    if (isPlanLoading) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp).align(Alignment.CenterHorizontally), color = NourCyan)
                                    }

                                    proactivePlan?.let { plan ->
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = ObsidianCardElevated,
                                            border = BorderStroke(1.dp, NourCyan.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = plan,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White,
                                                modifier = Modifier.padding(14.dp),
                                                lineHeight = 22.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))
                                    HorizontalDivider(color = ObsidianCardBorder)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text("تقرير الإنجاز الأسبوعي (Weekly Report) 🏆", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("ملخص أسبوعك مع نور بنبرة فخر واعتزاز بإنجازك", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.generateWeeklyReport() },
                                        colors = ButtonDefaults.buttonColors(containerColor = NourCyan),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("عرض تقرير هذا الأسبوع 📊", color = Color.Black)
                                    }

                                    weeklyReport?.let { rep ->
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = ObsidianSurface,
                                            border = BorderStroke(1.dp, NourGold),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text("بطل الأسبوع: ${rep.studentName} 🌟", fontWeight = FontWeight.Bold, color = NourGold)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("• أيام نشطة: ${rep.activeDays} أيام", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                                Text("• مهام منجزة: ${rep.completedTasks} مهمة", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                                Text("• دقائق تركيز: ${rep.totalFocusMinutes} دقيقة", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                                Text("• الستريك: ${rep.streakDays} يوم متتالي", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(rep.nourPrideMessage, style = MaterialTheme.typography.bodyMedium, color = NourCyan)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // ================= 5. Squad Streaks (Friends) =================
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ObsidianCard,
                                border = BorderStroke(1.dp, ObsidianCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("ستريك الأصدقاء (Friends Squad) 🔥", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("تنافس وشجع زمايلك في المذاكرة اليومية عشان الستريك ميفلتش!", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                                        }
                                        IconButton(
                                            onClick = { viewModel.refreshSquadMembers() },
                                            enabled = !isSquadLoading
                                        ) {
                                            if (isSquadLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NourCyan, strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = NourCyan)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // My Invite Code Card
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ObsidianSurface,
                                        border = BorderStroke(1.dp, NourCyan.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("كود الدعوة الخاص بيك:", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
                                                Text(myInviteCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = NourCyan, letterSpacing = 2.sp)
                                            }
                                            Button(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                                    val clip = android.content.ClipData.newPlainText("Nour Invite Code", myInviteCode)
                                                    clipboard?.setPrimaryClip(clip)
                                                    android.widget.Toast.makeText(context, "تم نسخ كود الدعوة بنجاح ✓", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NourCyan),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("نسخ", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Add Friend Input
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = friendCodeInput,
                                            onValueChange = { if (it.length <= 8) friendCodeInput = it.uppercase() },
                                            placeholder = { Text("أدخل كود الصديق") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NourCyan,
                                                unfocusedBorderColor = ObsidianCardBorder,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        Button(
                                            onClick = {
                                                if (friendCodeInput.isNotBlank()) {
                                                    viewModel.addFriendByInviteCode(friendCodeInput)
                                                    friendCodeInput = ""
                                                }
                                            },
                                            enabled = friendCodeInput.isNotBlank() && !isSquadLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("إضافة +", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    squadError?.let { err ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(err, color = DangerCoral, style = MaterialTheme.typography.bodySmall)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (squadMembers.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("جاري تحميل بيانات السكواد...", color = TextSecondaryDark)
                                        }
                                    } else {
                                        squadMembers.forEach { member ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (member.isCurrentUser) NourVioletDark.copy(alpha = 0.5f) else ObsidianCardElevated,
                                                border = BorderStroke(1.dp, if (member.isCurrentUser) NourCyan else ObsidianCardBorder),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(member.avatarEmoji, fontSize = 24.sp)
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(member.name, color = Color.White, fontWeight = FontWeight.Bold)
                                                        Text(if (member.isCurrentUser) "أنت" else "صديق في الفريق", color = TextSecondaryDark, style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = DangerCoral.copy(alpha = 0.2f),
                                                        border = BorderStroke(1.dp, DangerCoral.copy(alpha = 0.5f))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("🔥 ${member.streakDays} يوم", color = DangerCoral, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
