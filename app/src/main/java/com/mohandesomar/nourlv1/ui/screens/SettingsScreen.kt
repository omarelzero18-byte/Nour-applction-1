package com.mohandesomar.nourlv1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.mohandesomar.nourlv1.data.api.GeminiConnectionStatus
import com.mohandesomar.nourlv1.sound.SoundManager
import com.mohandesomar.nourlv1.ui.theme.*
import com.mohandesomar.nourlv1.viewmodel.NourViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NourViewModel,
    onResetToOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val appSettings by viewModel.appSettings.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val settingsTestStatus by viewModel.settingsTestStatus.collectAsState()

    var showConfirmResetDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    val studentName = profile?.name?.ifBlank { "طالب" } ?: "طالب"
    val studentGrade = profile?.grade?.ifBlank { "غير محدد" } ?: "غير محدد"
    val studentAge = profile?.age ?: 17
    val secondaryInterests = profile?.secondaryInterests?.ifBlank { "لا توجد مهارات إضافية مسجلة" } ?: "لا توجد مهارات إضافية مسجلة"
    val studentPoints = profile?.points ?: 0
    val studentLevel = profile?.level ?: 1
    val studentFocus = profile?.focusReserve ?: 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = strings.settingsTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = strings.settingsSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                .padding(horizontal = 18.dp)
                .testTag("settings_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Student Fintech Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(NourViolet, NourCyan)),
                            RoundedCornerShape(22.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(listOf(NourViolet, NourCyan))
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = studentName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "$studentGrade • $studentAge سنة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NourCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                                .padding(vertical = 10.dp, horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStatItem(title = "المستوى", value = "Lv.$studentLevel", tint = NourGold)
                            ProfileStatItem(title = "النقاط", value = "$studentPoints XP", tint = NourCyan)
                            ProfileStatItem(title = "التركيز", value = "$studentFocus%", tint = NourEmerald)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "الاهتمامات والمهارات: $secondaryInterests",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Appearance & Theme Section (MANDATORY FUNCTIONAL TOGGLE)
            item {
                SectionHeader(title = strings.appearanceSection)
            }

            item {
                SettingsCard {
                    // Dark / Light Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (appSettings.darkTheme) NourViolet.copy(alpha = 0.2f) else NourGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (appSettings.darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (appSettings.darkTheme) NourCyan else NourGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.themeModeTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = strings.themeModeDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = appSettings.darkTheme,
                            onCheckedChange = { checked ->
                                SoundManager.playButtonTap()
                                viewModel.setDarkTheme(checked)
                            },
                            modifier = Modifier.testTag("theme_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NourViolet,
                                uncheckedThumbColor = NourGold,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )

                    // Font Size Segmented Control
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatSize, contentDescription = null, tint = NourCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = strings.fontSizeTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = strings.fontSizeDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FontSizeOption(
                                label = strings.fontSmall,
                                isSelected = appSettings.fontScale < 0.95f,
                                onClick = {
                                    SoundManager.playButtonTap()
                                    viewModel.setFontScale(0.85f)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FontSizeOption(
                                label = strings.fontMedium,
                                isSelected = appSettings.fontScale in 0.95f..1.05f,
                                onClick = {
                                    SoundManager.playButtonTap()
                                    viewModel.setFontScale(1.0f)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FontSizeOption(
                                label = strings.fontLarge,
                                isSelected = appSettings.fontScale > 1.05f,
                                onClick = {
                                    SoundManager.playButtonTap()
                                    viewModel.setFontScale(1.15f)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )

                    // Language Selector (Arabic RTL vs English LTR)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = NourViolet, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = strings.languageTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = strings.languageDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LanguageOption(
                                label = strings.langArabic,
                                isSelected = appSettings.language == "ar",
                                onClick = {
                                    SoundManager.playButtonTap()
                                    viewModel.setLanguage("ar")
                                },
                                modifier = Modifier.weight(1f)
                            )
                            LanguageOption(
                                label = strings.langEnglish,
                                isSelected = appSettings.language == "en",
                                onClick = {
                                    SoundManager.playButtonTap()
                                    viewModel.setLanguage("en")
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Sound Design & Audio Effects Section
            item {
                SectionHeader(title = strings.soundSection)
            }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (appSettings.soundEnabled) NourCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (appSettings.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = null,
                                    tint = if (appSettings.soundEnabled) NourCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.soundEffectsTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = strings.soundEffectsDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = appSettings.soundEnabled,
                            onCheckedChange = { checked ->
                                viewModel.setSoundEnabled(checked)
                                if (checked) {
                                    viewModel.playPreviewSound()
                                }
                            },
                            modifier = Modifier.testTag("sound_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NourCyan,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preview Audio Chime Button
                    OutlinedButton(
                        onClick = {
                            viewModel.playPreviewSound()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_chime_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = appSettings.soundEnabled,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NourCyan.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NourCyan)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.soundTestButton,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. Gemini API Connection & Real Diagnostics
            item {
                SectionHeader(title = strings.apiConnectionSection)
            }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(NourViolet.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = NourCyan, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.apiConnectionTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = strings.apiConnectionDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Diagnostic status result card
                    when (val testStatus = settingsTestStatus) {
                        is GeminiConnectionStatus.Idle -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = NourCyan, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "اضغط الزر بالأسفل لإرسال طلب فحص حقيقي للخوادم والتأكد من سلامة المفتاح والسرعة.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        is GeminiConnectionStatus.Checking -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NourCyan.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NourCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = NourCyan
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = strings.apiTesting,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NourCyan
                                    )
                                }
                            }
                        }
                        is GeminiConnectionStatus.Connected -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SuccessMint.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessMint.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessMint, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "الاتصال ناجح 100% بنور ✅",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessMint
                                        )
                                        Text(
                                            text = "الموديل المتصل: ${testStatus.model} • سرعة الاستجابة: ${testStatus.latencyMs}ms",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                        is GeminiConnectionStatus.KeyMissingOrInvalid -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DangerCoral.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DangerCoral.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = DangerCoral, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "فشل التحقق: مشكلة في مفتاح API (Secrets) ⚠️",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DangerCoral
                                        )
                                        Text(
                                            text = testStatus.details,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                        is GeminiConnectionStatus.ServerBusy -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NourGold.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NourGold.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = NourGold, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "ازدحام مؤقت في السيرفر (HTTP ${testStatus.code}) 🔄",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = NourGold
                                        )
                                        Text(
                                            text = testStatus.details,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                        is GeminiConnectionStatus.NetworkError -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondaryDark.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "خطأ في الشبكة أو الاتصال 🌐",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = testStatus.details,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.testApiConnection() },
                        enabled = settingsTestStatus !is GeminiConnectionStatus.Checking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_api_connection_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NourViolet,
                            contentColor = Color.White
                        )
                    ) {
                        if (settingsTestStatus is GeminiConnectionStatus.Checking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.apiTesting)
                        } else {
                            Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.apiTestButton,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 5. About & Credits Section (Eng. Omar & Egypt Badge)
            item {
                SectionHeader(title = strings.aboutSection)
            }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(NourViolet.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = NourCyan, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = strings.madeInEgypt,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = strings.engineerOmarCredit,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NourCyan
                            )
                            Text(
                                text = strings.appVersion,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Privacy & Security Policy
            item {
                SectionHeader(title = "الخصوصية والأمان 🛡️")
            }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(NourEmerald.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = NourEmerald, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "سياسة حماية البيانات والخصوصية",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "بياناتك مشفرة ومحفوظة محلياً 100%، ولا يتم بيع أو مشاركة أي بيانات شخصية.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showPrivacyPolicyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NourEmerald.copy(alpha = 0.2f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NourEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NourEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("قراءة وثيقة الخصوصية الكاملة 📜", color = NourEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 5. Zero Seed Data Reset Section
            item {
                SectionHeader(title = strings.dataSection)
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NourCoral.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = NourCoral, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.resetAccountTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NourCoral
                            )
                        }
                        Text(
                            text = strings.resetAccountDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Button(
                            onClick = { showConfirmResetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NourCoral.copy(alpha = 0.2f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NourCoral),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_account_button")
                        ) {
                            Text(strings.resetAccountButton, color = NourCoral, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showConfirmResetDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmResetDialog = false },
            title = {
                Text(
                    text = strings.confirmResetTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = strings.confirmResetDesc,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllDataForTesting()
                        showConfirmResetDialog = false
                        onResetToOnboarding()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NourCoral)
                ) {
                    Text(strings.confirmResetAction, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmResetDialog = false }) {
                    Text(strings.cancelAction, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = NourEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "وثيقة الخصوصية والأمان",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "نحن في تطبيق نور (Nour AI) نضع خصوصية الطلاب في المقام الأول وفقاً لأعلى معايير الأمان العالمية:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        Text(
                            text = "1. التخزين المحلي:\nكافة الجداول، المهام، المشاريع، والعادات مخزنة داخل جهازك في قاعدة بيانات مشفرة (Room SQLite) ولا تنتقل لأي خادم خارجي.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                    item {
                        Text(
                            text = "2. الذكاء الاصطناعي (Firebase AI / Gemini):\nالاستفسارات والملخصات تعامل عبر اتصال آمن ومشفر بأحدث نماذج Google Gemini. لا يتم تدريب النماذج على بياناتك الشخصية.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                    item {
                        Text(
                            text = "3. الحماية ومكافحة التزييف (App Check):\nالتطبيق محمي بواسطة Firebase App Check و Play Integrity لضمان عدم التلاعب بطلبات الشات وتأمين حسابات الطلاب.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                    item {
                        Text(
                            text = "4. السكواد والأصدقاء:\nيتم إنشاء معرف عشوائي مجهول الهوية (Anonymous Auth) لحفظ الستريك بين الأصدقاء فقط، دون الحاجة لطلب رقم هاتف أو بريد إلكتروني.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NourEmerald)
                ) {
                    Text("فهمت وموافق ✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
private fun ProfileStatItem(title: String, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = tint)
    }
}

@Composable
private fun FontSizeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NourViolet else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NourCyan.copy(alpha = 0.25f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) NourCyan else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NourCyan else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
