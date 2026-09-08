package com.mohandesomar.nourlv1.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohandesomar.nourlv1.data.model.ProjectItem
import com.mohandesomar.nourlv1.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: (
        name: String,
        age: Int,
        stage: String,
        grade: String,
        secondaryInterests: String,
        hasTechField: Boolean,
        initialProjects: List<ProjectItem>
    ) -> Unit
) {
    // Current step:
    // 0: Nour's Personal Spoken Intro
    // 1: Name
    // 2: Age
    // 3: Educational Stage & Grade
    // 4: Secondary Learning Fields
    // 5: Tech Projects (if tech selected)
    // 6: Personalized VIP Celebration
    var currentStep by remember { mutableIntStateOf(0) }

    var studentName by remember { mutableStateOf("") }
    var studentAge by remember { mutableStateOf("17") }
    var selectedStage by remember { mutableStateOf("ثانوي عام") }
    var selectedGrade by remember { mutableStateOf("تالتة ثانوي") }
    var customStageText by remember { mutableStateOf("") }

    val secondaryInterests = remember { mutableStateListOf<String>() }
    var customInterestText by remember { mutableStateOf("") }

    // Projects added during onboarding
    val initialProjects = remember { mutableStateListOf<ProjectItem>() }
    var projectTitle by remember { mutableStateOf("") }
    var projectUrl by remember { mutableStateOf("") }
    var projectStatus by remember { mutableStateOf("IN_PROGRESS") }

    val hasTechField = secondaryInterests.any {
        it.contains("برمجة") || it.contains("ذكاء اصطناعي") || it.contains("أمن") || it.contains("Tech")
    }

    val totalSteps = if (hasTechField) 6 else 5

    Scaffold(
        containerColor = ObsidianBlack,
        topBar = {
            if (currentStep in 1..totalSteps) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "خطوة $currentStep من $totalSteps",
                            style = MaterialTheme.typography.labelMedium,
                            color = NourCyan
                        )
                        Text(
                            text = "نور بتتعرف عليك 💜",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { currentStep.toFloat() / totalSteps.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NourCyan,
                        trackColor = ObsidianCardElevated,
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("onboarding_screen")
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(350)) + slideInHorizontally { width -> width / 2 })
                        .togetherWith(fadeOut(animationSpec = tween(250)) + slideOutHorizontally { width -> -width / 2 })
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    0 -> NourPersonalIntroStep(
                        onNext = { currentStep = 1 }
                    )
                    1 -> NourNameStep(
                        name = studentName,
                        onNameChange = { studentName = it },
                        onNext = { if (studentName.isNotBlank()) currentStep = 2 }
                    )
                    2 -> NourAgeStep(
                        studentName = studentName,
                        age = studentAge,
                        onAgeChange = { studentAge = it },
                        onNext = { currentStep = 3 }
                    )
                    3 -> NourStageStep(
                        studentName = studentName,
                        selectedStage = selectedStage,
                        selectedGrade = selectedGrade,
                        customStageText = customStageText,
                        onStageChange = { selectedStage = it },
                        onGradeChange = { selectedGrade = it },
                        onCustomStageChange = { customStageText = it },
                        onNext = { currentStep = 4 }
                    )
                    4 -> NourSecondaryInterestsStep(
                        studentName = studentName,
                        selectedInterests = secondaryInterests,
                        customText = customInterestText,
                        onCustomTextChange = { customInterestText = it },
                        onNext = {
                            if (hasTechField) {
                                currentStep = 5
                            } else {
                                currentStep = 6
                            }
                        }
                    )
                    5 -> NourTechProjectsStep(
                        studentName = studentName,
                        projectTitle = projectTitle,
                        projectUrl = projectUrl,
                        projectStatus = projectStatus,
                        projects = initialProjects,
                        onTitleChange = { projectTitle = it },
                        onUrlChange = { projectUrl = it },
                        onStatusChange = { projectStatus = it },
                        onAddProject = {
                            if (projectTitle.isNotBlank()) {
                                initialProjects.add(
                                    ProjectItem(
                                        title = projectTitle.trim(),
                                        projectUrl = projectUrl.trim(),
                                        status = projectStatus,
                                        techStack = "برمجة"
                                    )
                                )
                                projectTitle = ""
                                projectUrl = ""
                            }
                        },
                        onNext = { currentStep = 6 }
                    )
                    else -> NourCelebrationStep(
                        studentName = studentName,
                        grade = if (selectedStage == "مرحلة تانية") customStageText.ifBlank { "طالب متميز" } else selectedGrade,
                        secondaryField = secondaryInterests.joinToString("، "),
                        onFinish = {
                            val finalGrade = if (selectedStage == "مرحلة تانية") customStageText.ifBlank { "طالب" } else selectedGrade
                            val interestsStr = secondaryInterests.joinToString(", ")
                            onOnboardingFinished(
                                studentName.trim(),
                                studentAge.toIntOrNull() ?: 17,
                                selectedStage,
                                finalGrade,
                                interestsStr,
                                hasTechField,
                                initialProjects.toList()
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * الخطوة 0: مقدمة نور الشخصية الكاملة
 * تشمل صراحة جملة المهندس عمر الإلزامية غير القابلة للحذف
 */
@Composable
private fun NourPersonalIntroStep(
    onNext: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Glowing Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(NourGoldLight, NourCyan, NourViolet, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
                    .border(2.5.dp, NourCyan, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "نور بترحب بيك! 💜✨",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NourGold
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Nour's warm Egyptian introduction card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = NourPrimaryGradient),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "« أهلاً بيك يا بطل! حابة أعرفك بنفسي الأول... »",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "أنا هكون معاك طول رحلتك من أول يوم، وموجودة أساعدك في أي حاجة تحتاجها عشان متشلش هم المذاكرة وتنظيم وقتك خالص.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark,
                        lineHeight = 22.sp
                    )

                    // MANDATORY SENTENCE
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(NourViolet, NourCyan))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "المهندس عمر هو اللي صممني وبرمجني عشان أسهّل على الطلبة حياتهم.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = NourCyan,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Text(
                        text = "وفضولية جداً أتعرف عليك عشان أظبطلك كل خطة وعادة على مقاسك بالظبط... جاهز نبدأ سوا؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("start_onboarding_button")
        ) {
            Text(
                text = "يلا نتعرف يا نور! 🚀",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * الخطوة 1: الاسم
 */
@Composable
private fun NourNameStep(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))

            NourSpeechBubble(text = "قولي بقى يا بطل.. حابب أناديلك بإيه؟ اسمك إيه؟ ✨")

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("اسمك الكريم") },
                placeholder = { Text("مثال: عمر، أحمد، سارة، مريم...") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NourViolet,
                    unfocusedBorderColor = ObsidianCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_name_input")
            )
        }

        Button(
            onClick = onNext,
            enabled = name.isNotBlank(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("next_name_button")
        ) {
            Text("التالي ➡️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * الخطوة 2: العمر
 */
@Composable
private fun NourAgeStep(
    studentName: String,
    age: String,
    onAgeChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))

            NourSpeechBubble(text = "عاش يا $studentName! عندك كام سنة عشان أحسب إيقاع طاقتك؟ 🎂")

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) onAgeChange(it) },
                label = { Text("عمرك بالسنين") },
                placeholder = { Text("17") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NourViolet,
                    unfocusedBorderColor = ObsidianCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_age_input")
            )
        }

        Button(
            onClick = onNext,
            enabled = age.isNotBlank(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("next_age_button")
        ) {
            Text("تمام يا نور، التالي ➡️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * الخطوة 3: المرحلة والصف بالتفصيل
 */
@Composable
private fun NourStageStep(
    studentName: String,
    selectedStage: String,
    selectedGrade: String,
    customStageText: String,
    onStageChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onCustomStageChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val stages = listOf(
        "ثانوي عام" to listOf("أولى ثانوي", "تانية ثانوي", "تالتة ثانوي"),
        "إعدادي" to listOf("أولى إعدادي", "تانية إعدادي", "تالتة إعدادي"),
        "بكالوريا (النظام الجديد)" to listOf("السنة الأولى", "السنة الثانية", "السنة الثالثة"),
        "ابتدائي" to listOf("الصف الأول", "الصف الثاني", "الصف الثالث", "الصف الرابع", "الصف الخامس", "الصف السادس"),
        "مرحلة تانية" to emptyList()
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        item {
            NourSpeechBubble(text = "في أي مرحلة تعليمية يا $studentName؟ ده بيخليني أظبط خطط المذاكرة على امتحاناتك بالمللي! 📚")
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "اختر المرحلة:",
                style = MaterialTheme.typography.labelLarge,
                color = NourGold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stages.forEach { (stageKey, _) ->
                    val isSelected = selectedStage == stageKey
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NourViolet else ObsidianCardElevated,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = if (isSelected) NourPrimaryGradient else Brush.linearGradient(listOf(ObsidianCardBorder, ObsidianCardBorder))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onStageChange(stageKey)
                                val availableGrades = stages.firstOrNull { it.first == stageKey }?.second.orEmpty()
                                if (availableGrades.isNotEmpty()) {
                                    onGradeChange(availableGrades.first())
                                }
                            }
                    ) {
                        Text(
                            text = stageKey,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedStage == "مرحلة تانية") {
                OutlinedTextField(
                    value = customStageText,
                    onValueChange = onCustomStageChange,
                    label = { Text("اكتب مرحلتك (جامعة، أزهر، دبلوم...)") },
                    placeholder = { Text("مثال: هندسة القاهرة سنة تالتة، دبلوم فني...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NourViolet,
                        unfocusedBorderColor = ObsidianCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                val availableGrades = stages.firstOrNull { it.first == selectedStage }?.second.orEmpty()
                if (availableGrades.isNotEmpty()) {
                    Text(
                        text = "اختر الصف:",
                        style = MaterialTheme.typography.labelLarge,
                        color = NourCyan,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        availableGrades.forEach { grade ->
                            val isGradeSelected = selectedGrade == grade
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isGradeSelected) NourCyan.copy(alpha = 0.2f) else ObsidianCard,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        listOf(if (isGradeSelected) NourCyan else ObsidianCardBorder, if (isGradeSelected) NourCyan else ObsidianCardBorder)
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onGradeChange(grade) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isGradeSelected,
                                        onClick = { onGradeChange(grade) },
                                        colors = RadioButtonDefaults.colors(selectedColor = NourCyan)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = grade,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isGradeSelected) Color.White else TextSecondaryDark,
                                        fontWeight = if (isGradeSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("next_stage_button")
            ) {
                Text("المجالات الإضافية ➡️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * الخطوة 4: سؤال تفاعلي: بتتعلم حاجة تانية غير المدرسة/الكلية؟
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NourSecondaryInterestsStep(
    studentName: String,
    selectedInterests: MutableList<String>,
    customText: String,
    onCustomTextChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val presets = listOf(
        "برمجة 💻",
        "ذكاء اصطناعي (AI) 🤖",
        "أمن سيبراني 🛡️",
        "تصميم وجرافيك 🎨",
        "لغات أجنبية 🌐",
        "تسويق وبيزنس 📈"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(10.dp))

            NourSpeechBubble(text = "بتتعلم حاجة تانية غير المدرسة أو الكلية يا $studentName؟ ده اللي بيصنع الفرق الحقيقي! 🚀")

            Spacer(modifier = Modifier.height(24.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.forEach { item ->
                    val isSelected = selectedInterests.contains(item)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NourViolet else ObsidianCardElevated,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = if (isSelected) NourPrimaryGradient else Brush.linearGradient(listOf(ObsidianCardBorder, ObsidianCardBorder))
                        ),
                        modifier = Modifier.clickable {
                            if (isSelected) selectedInterests.remove(item) else selectedInterests.add(item)
                        }
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color.White else TextSecondaryDark,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = customText,
                onValueChange = {
                    onCustomTextChange(it)
                    if (it.isNotBlank() && !selectedInterests.contains(it)) {
                        selectedInterests.add(it)
                    }
                },
                label = { Text("مهارة أو مجال تاني؟") },
                placeholder = { Text("مثال: مونتاج، رسم، كتابة محتوى...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NourViolet,
                    unfocusedBorderColor = ObsidianCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("next_interests_button")
        ) {
            Text("التالي ➡️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * الخطوة 5: وريني شغلك! (لو اختار مجال تقني كالبرمجة)
 */
@Composable
private fun NourTechProjectsStep(
    studentName: String,
    projectTitle: String,
    projectUrl: String,
    projectStatus: String,
    projects: List<ProjectItem>,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onAddProject: () -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        item {
            NourSpeechBubble(text = "عاش يا بطل التقنية! 💻✨ وريني شغلك! ضيف مشاريعك عشان توثقها في قسم دائم خاص بيك.")
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = projectTitle,
                onValueChange = onTitleChange,
                label = { Text("اسم المشروع") },
                placeholder = { Text("مثال: بوت تليجرام أو تطبيق Android") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NourCyan,
                    unfocusedBorderColor = ObsidianCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = projectUrl,
                onValueChange = onUrlChange,
                label = { Text("رابط GitHub أو المعاينة") },
                placeholder = { Text("https://github.com/...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NourCyan,
                    unfocusedBorderColor = ObsidianCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                val statuses = listOf(
                    "IN_PROGRESS" to "شغال عليه ⏳",
                    "COMPLETED" to "مكتمل ✅"
                )
                statuses.forEach { (st, label) ->
                    val isSelected = projectStatus == st
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NourCyan.copy(alpha = 0.3f) else ObsidianCard,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(if (isSelected) NourCyan else ObsidianCardBorder, if (isSelected) NourCyan else ObsidianCardBorder)
                            )
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onStatusChange(st) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) NourCyan else TextSecondaryDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAddProject,
                enabled = projectTitle.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NourCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إضافة المشروع للكروت 🚀", color = ObsidianBlack, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Display added projects
            if (projects.isNotEmpty()) {
                Text(
                    text = "مشاريعك الحالية:",
                    style = MaterialTheme.typography.labelLarge,
                    color = NourGold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                projects.forEach { prj ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(prj.title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                if (prj.projectUrl.isNotBlank()) {
                                    Text(prj.projectUrl, style = MaterialTheme.typography.bodySmall, color = NourCyan)
                                }
                            }
                            Text(
                                text = if (prj.status == "COMPLETED") "✅ مكتمل" else "⏳ جاري",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (prj.status == "COMPLETED") SuccessMint else NourCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NourViolet),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("finish_projects_step_button")
            ) {
                Text("جاهز للاحتفال والانطلاق! 🎉", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * الخطوة 6: احتفال بصري فوري وشخصي + إحساس VIP
 */
@Composable
private fun NourCelebrationStep(
    studentName: String,
    grade: String,
    secondaryField: String,
    onFinish: () -> Unit
) {
    val isCrucialYear = grade.contains("تالتة ثانوي") || grade.contains("الثالثة بكالوريا") || grade.contains("ثانوية") || grade.contains("تالتة إعدادي")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Confetti & Trophy Aura
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(NourGoldLight.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, NourGold, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = NourGold,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "مرحباً بيك في نادي النخبة! 👑",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NourGold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "تم قبول دعوتك وتخصيص تجربة Nour-lv1 بالكامل باسمك يا $studentName!",
                style = MaterialTheme.typography.bodyMedium,
                color = NourCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tailored motivational card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = NourPrimaryGradient),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "رسالة خاصة من نور لمرحلة $grade 💜",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (isCrucialYear) {
                        Text(
                            text = "أنا عارفة إن $grade سنة مش سهلة ومحتاجة نفس طويل.. بس صدقني، هي كمان سنة مليانة فرص وتحدي جميل. طول ما إحنا بنقسّم المنهج بهدوء وعاداتك ثابتة، مفيش حاجة هتقف في طريقك!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryDark,
                            lineHeight = 22.sp
                        )
                    } else {
                        Text(
                            text = "سنة $grade فرصة ذهبية تبني فيها عادات استثنائية تسبق بيها زمايلك بمراحل. كل يوم تلتزم فيه هو خطوة عملاقة نحو هدفك!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryDark,
                            lineHeight = 22.sp
                        )
                    }

                    if (secondaryField.isNotBlank()) {
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✨ وشغفك الإضافي في ($secondaryField) هيخليك طالب فريد من نوعك في سوق العمل الحقيقي!",
                                style = MaterialTheme.typography.bodySmall,
                                color = NourGoldLight,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onFinish,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NourGold),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("enter_dashboard_button")
        ) {
            Text(
                text = "دخول لوحة التحكم والبدء من الصفر 🚀",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ObsidianBlack
            )
        }
    }
}

/**
 * فقاعة كلام نور المصرية الودودة
 */
@Composable
private fun NourSpeechBubble(text: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCardElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = NourPrimaryGradient),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(NourViolet.copy(alpha = 0.3f), CircleShape)
                    .border(1.dp, NourCyan, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NourCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                lineHeight = 24.sp
            )
        }
    }
}
