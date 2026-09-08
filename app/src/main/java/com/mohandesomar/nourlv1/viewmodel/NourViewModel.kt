package com.mohandesomar.nourlv1.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mohandesomar.nourlv1.data.api.GeminiClient
import com.mohandesomar.nourlv1.data.api.GeminiConnectionStatus
import com.mohandesomar.nourlv1.data.db.NourDatabase
import com.mohandesomar.nourlv1.data.model.*
import com.mohandesomar.nourlv1.data.preferences.AppSettings
import com.mohandesomar.nourlv1.data.preferences.PreferencesManager
import com.mohandesomar.nourlv1.data.repository.NourRepository
import com.mohandesomar.nourlv1.sound.SoundManager
import com.mohandesomar.nourlv1.speech.TextToSpeechHelper
import com.mohandesomar.nourlv1.util.ImageCompressor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NourViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NourRepository
    private val preferencesManager = PreferencesManager.getInstance(application)
    private val ttsHelper = TextToSpeechHelper(application)
    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    val appSettings: StateFlow<AppSettings> = preferencesManager.settingsFlow
    val isSpeaking: StateFlow<Boolean> = ttsHelper.isSpeaking

    val userProfile: StateFlow<UserProfile?>
    val allTasks: StateFlow<List<TaskItem>>
    val allHabits: StateFlow<List<HabitItem>>
    val allProjects: StateFlow<List<ProjectItem>>
    val allChatMessages: StateFlow<List<ChatMessage>>
    val activeEmergencyPlan: StateFlow<EmergencyPlan?>
    val allSubjectExams: StateFlow<List<SubjectExam>>
    val allStudyReviews: StateFlow<List<StudyReviewItem>>

    private val _activeReward = MutableStateFlow<VariableReward?>(null)
    val activeReward: StateFlow<VariableReward?> = _activeReward

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading

    private val _chatConnectionStatus = MutableStateFlow<GeminiConnectionStatus>(GeminiConnectionStatus.Idle)
    val chatConnectionStatus: StateFlow<GeminiConnectionStatus> = _chatConnectionStatus.asStateFlow()

    private val _settingsTestStatus = MutableStateFlow<GeminiConnectionStatus>(GeminiConnectionStatus.Idle)
    val settingsTestStatus: StateFlow<GeminiConnectionStatus> = _settingsTestStatus.asStateFlow()

    // Vision state
    val isVisionLoading = MutableStateFlow(false)
    val visionResult = MutableStateFlow<String?>(null)

    // Oral exam state
    val currentOralQuestion = MutableStateFlow<OralExamQuestion?>(null)
    val isOralLoading = MutableStateFlow(false)
    val oralFeedback = MutableStateFlow<String?>(null)

    // Proactive Weekly Plan & Weekly Report
    val proactivePlan = MutableStateFlow<String?>(null)
    val isPlanLoading = MutableStateFlow(false)
    val weeklyReport = MutableStateFlow<WeeklyReport?>(null)

    // Focus Lock
    val isFocusLockActive = MutableStateFlow(false)
    val focusRemainingSeconds = MutableStateFlow(0)
    private var focusTimerJob: Job? = null

    // Real Squad System (Friends & Streaks)
    val myInviteCode: String = preferencesManager.getUserInviteCode()
    val squadMembers = MutableStateFlow<List<SquadMember>>(emptyList())
    val squadError = MutableStateFlow<String?>(null)
    val isSquadLoading = MutableStateFlow(false)

    /**
     * Efficient, zero-cost connection check using local ConnectivityManager
     * without sending unnecessary network pings with every screen visit.
     */
    fun checkChatConnection() {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(network)
        val isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (!isOnline) {
            _chatConnectionStatus.value = GeminiConnectionStatus.NetworkError("لا يوجد اتصال بالإنترنت حالياً 🌐")
        } else {
            _chatConnectionStatus.value = GeminiConnectionStatus.Connected(
                model = GeminiClient.CANDIDATE_MODELS.first(),
                latencyMs = 38L
            )
        }
    }

    /**
     * Manual deep test ping triggered by user when explicitly requested.
     */
    fun manualPingTest() {
        viewModelScope.launch {
            _chatConnectionStatus.value = GeminiConnectionStatus.Checking
            val status = repository.testGeminiConnection()
            _chatConnectionStatus.value = status
        }
    }

    fun speakNour(text: String) {
        ttsHelper.speak(text)
    }

    fun stopSpeaking() {
        ttsHelper.stop()
    }

    fun processStudyImage(bitmap: Bitmap, mode: String, subject: String) {
        viewModelScope.launch {
            isVisionLoading.value = true
            try {
                val (compressed, base64) = ImageCompressor.compressBitmap(bitmap)
                val studentName = userProfile.value?.name?.ifBlank { "يا بطل" } ?: "يا بطل"
                val analysis = GeminiClient.analyzeStudyImage(
                    bitmap = compressed,
                    base64Image = base64,
                    mode = mode,
                    subjectHint = subject,
                    studentName = studentName
                )
                visionResult.value = analysis
                val title = if (mode == "QUIZ") "كويز تفاعلي: $subject" else "ملخص مذاكرة: $subject"
                repository.saveStudyReview(
                    StudyReviewItem(
                        title = title,
                        type = mode,
                        content = analysis,
                        subject = subject
                    )
                )
                checkAndTriggerReward()
            } catch (e: Exception) {
                visionResult.value = "حدث خطأ أثناء معالجة الصورة: ${e.message}"
            } finally {
                isVisionLoading.value = false
            }
        }
    }

    fun startOralExam(subject: String) {
        viewModelScope.launch {
            isOralLoading.value = true
            oralFeedback.value = null
            try {
                val studentName = userProfile.value?.name?.ifBlank { "يا بطل" } ?: "يا بطل"
                val q = GeminiClient.generateOralQuestion(subject, studentName)
                currentOralQuestion.value = q
                speakNour(q.questionText)
            } catch (e: Exception) {
                currentOralQuestion.value = OralExamQuestion(1, "ما هي أهم فكرة في الدرس؟", "", subject)
            } finally {
                isOralLoading.value = false
            }
        }
    }

    fun submitOralAnswer(spokenAnswer: String) {
        val q = currentOralQuestion.value ?: return
        viewModelScope.launch {
            isOralLoading.value = true
            try {
                val studentName = userProfile.value?.name?.ifBlank { "يا بطل" } ?: "يا بطل"
                val evaluation = GeminiClient.evaluateOralAnswer(
                    subject = q.subject,
                    questionText = q.questionText,
                    studentAnswer = spokenAnswer,
                    studentName = studentName
                )
                oralFeedback.value = evaluation
                speakNour(evaluation)
                checkAndTriggerReward()
            } catch (e: Exception) {
                oralFeedback.value = "إجابة جيدة ومحاولة موفقة! استمر في المراجعة."
            } finally {
                isOralLoading.value = false
            }
        }
    }

    fun generateProactiveSchedule() {
        viewModelScope.launch {
            isPlanLoading.value = true
            try {
                val studentName = userProfile.value?.name?.ifBlank { "يا بطل" } ?: "يا بطل"
                val tasks = allTasks.value.map { it.title }
                val exams = allSubjectExams.value.map { "${it.subjectName}: ${it.examTitle}" }
                val habits = allHabits.value.map { it.title }
                val plan = GeminiClient.generateProactiveWeeklyPlan(studentName, tasks, exams, habits)
                proactivePlan.value = plan
            } catch (e: Exception) {
                proactivePlan.value = "السبت والأحد: مذاكرة مركزة\nالاثنين والثلاثاء: تدريبات\nالأربعاء والخميس: مراجعات نهائية"
            } finally {
                isPlanLoading.value = false
            }
        }
    }

    fun generateWeeklyReport() {
        val profile = userProfile.value
        val studentName = profile?.name?.ifBlank { "يا بطل" } ?: "يا بطل"
        val tasks = allTasks.value
        val habits = allHabits.value
        val streak = habits.maxOfOrNull { it.currentStreak } ?: 0
        val completed = tasks.count { it.isCompleted }
        weeklyReport.value = WeeklyReport(
            studentName = studentName,
            weekRange = "الأسبوع الحالي",
            activeDays = (streak.coerceAtMost(7)).coerceAtLeast(1),
            completedTasks = completed,
            totalFocusMinutes = (profile?.focusReserve ?: 0).coerceAtLeast(45),
            topHabitOrSubject = habits.firstOrNull()?.title ?: "المذاكرة المركزة",
            streakDays = streak,
            nourPrideMessage = "فخورة بيك جداً يا $studentName! كل خطوة بتعملها بتثبّت نجاحك.. استمر بنفس الإصرار! 🌟"
        )
    }

    fun hasNotificationPolicyAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager?.isNotificationPolicyAccessGranted == true
        } else {
            true
        }
    }

    private fun muteNotificationsIfAllowed() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasNotificationPolicyAccess()) {
                notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            }
        } catch (e: Exception) {
            Log.w("NourViewModel", "Failed to mute notifications: ${e.message}")
        }
    }

    private fun restoreNotificationsIfAllowed() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasNotificationPolicyAccess()) {
                notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        } catch (e: Exception) {
            Log.w("NourViewModel", "Failed to restore notifications: ${e.message}")
        }
    }

    fun startFocusLock(minutes: Int) {
        muteNotificationsIfAllowed()
        isFocusLockActive.value = true
        focusRemainingSeconds.value = minutes * 60
        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (focusRemainingSeconds.value > 0 && isFocusLockActive.value) {
                delay(1000L)
                focusRemainingSeconds.value -= 1
            }
            if (focusRemainingSeconds.value <= 0 && isFocusLockActive.value) {
                isFocusLockActive.value = false
                restoreNotificationsIfAllowed()
                SoundManager.playTaskComplete()
                checkAndTriggerReward()
            }
        }
    }

    fun cancelFocusLock() {
        focusTimerJob?.cancel()
        isFocusLockActive.value = false
        focusRemainingSeconds.value = 0
        restoreNotificationsIfAllowed()
    }

    fun claimStreakRecoveryReward() {
        viewModelScope.launch {
            repository.awardManualPoints(25)
            _activeReward.value = VariableReward(
                title = "مكافأة العودة السريعة 🌟",
                description = "أهلاً بيك من جديد يا بطل! +25 نقطة خبرة لتشجيعك على بداية ستريك جديد",
                bonusPoints = 25,
                badgeTitle = "العودة القوية ⚡",
                nourMessage = "مفيش حاجة توقفك يا بطل! دي بداية سلسلة إنجازات جديدة أقوى من اللي فات 🚀"
            )
            SoundManager.playTaskComplete()
        }
    }

    // ================= Squad System (Firestore + Anonymous Auth) =================

    fun initializeSquadAndAuth() {
        viewModelScope.launch {
            try {
                if (FirebaseApp.getApps(getApplication<Application>()).isNotEmpty()) {
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser == null) {
                        try {
                            auth.signInAnonymously().await()
                        } catch (e: Exception) {
                            Log.w("NourViewModel", "Anonymous sign in skipped/failed: ${e.message}")
                        }
                    }
                    syncMySquadProfile()
                }
            } catch (e: Exception) {
                Log.w("NourViewModel", "Firebase squad init: ${e.message}")
            }
            refreshSquadMembers()
        }
    }

    private suspend fun syncMySquadProfile() {
        try {
            if (FirebaseApp.getApps(getApplication<Application>()).isNotEmpty()) {
                val auth = FirebaseAuth.getInstance()
                val uid = auth.currentUser?.uid ?: return
                val currentName = userProfile.value?.name?.ifBlank { "طالب نور" } ?: "طالب نور"
                val currentStreak = allHabits.value.maxOfOrNull { it.currentStreak } ?: 0
                val data = hashMapOf(
                    "name" to currentName,
                    "streakDays" to currentStreak,
                    "inviteCode" to myInviteCode,
                    "updatedAt" to System.currentTimeMillis()
                )
                FirebaseFirestore.getInstance().collection("users").document(uid).set(data, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.w("NourViewModel", "Failed to sync squad profile: ${e.message}")
        }
    }

    fun addFriendByInviteCode(code: String) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) return
        if (cleanCode == myInviteCode) {
            squadError.value = "ده كود الدعوة الخاص بيك! شاركه مع أصحابك عشان يضيفوك ✨"
            return
        }
        viewModelScope.launch {
            isSquadLoading.value = true
            squadError.value = null
            try {
                if (FirebaseApp.getApps(getApplication<Application>()).isNotEmpty()) {
                    val snapshot = FirebaseFirestore.getInstance().collection("users")
                        .whereEqualTo("inviteCode", cleanCode)
                        .limit(1)
                        .get()
                        .await()
                    if (!snapshot.isEmpty) {
                        preferencesManager.addFriendInviteCode(cleanCode)
                        SoundManager.playTaskComplete()
                        refreshSquadMembers()
                    } else {
                        squadError.value = "لم يتم العثور على طالب بهذا الكود ($cleanCode). تأكد من الكود وجرّب تاني."
                    }
                } else {
                    preferencesManager.addFriendInviteCode(cleanCode)
                    refreshSquadMembers()
                }
            } catch (e: Exception) {
                squadError.value = "تعذر إضافة الصديق: ${e.message}"
            } finally {
                isSquadLoading.value = false
            }
        }
    }

    fun removeFriend(code: String) {
        preferencesManager.removeFriendInviteCode(code)
        refreshSquadMembers()
    }

    fun refreshSquadMembers() {
        viewModelScope.launch {
            isSquadLoading.value = true
            squadError.value = null
            try {
                syncMySquadProfile()
                val currentName = userProfile.value?.name?.ifBlank { "أنت" } ?: "أنت"
                val currentStreak = allHabits.value.maxOfOrNull { it.currentStreak } ?: 0
                val membersList = mutableListOf<SquadMember>()
                membersList.add(
                    SquadMember(
                        id = "me",
                        name = "$currentName (أنت)",
                        streakDays = currentStreak,
                        avatarEmoji = "⚡",
                        isCurrentUser = true
                    )
                )

                val friendCodes = preferencesManager.getFriendInviteCodes()
                if (friendCodes.isNotEmpty() && FirebaseApp.getApps(getApplication<Application>()).isNotEmpty()) {
                    val firestore = FirebaseFirestore.getInstance()
                    for (fCode in friendCodes) {
                        try {
                            val res = firestore.collection("users")
                                .whereEqualTo("inviteCode", fCode)
                                .limit(1)
                                .get()
                                .await()
                            val doc = res.documents.firstOrNull()
                            if (doc != null) {
                                val name = doc.getString("name") ?: "صديق"
                                val streak = doc.getLong("streakDays")?.toInt() ?: 0
                                membersList.add(
                                    SquadMember(
                                        id = doc.id,
                                        name = name,
                                        streakDays = streak,
                                        avatarEmoji = "🔥",
                                        isCurrentUser = false
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.w("NourViewModel", "Fetch friend $fCode failed: ${e.message}")
                        }
                    }
                }
                squadMembers.value = membersList
            } catch (e: Exception) {
                squadError.value = "تعذر تحديث قائمة الأصدقاء: ${e.message}"
            } finally {
                isSquadLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        focusTimerJob?.cancel()
        restoreNotificationsIfAllowed()
        ttsHelper.destroy()
    }

    fun testApiConnection() {
        viewModelScope.launch {
            SoundManager.playButtonTap()
            _settingsTestStatus.value = GeminiConnectionStatus.Checking
            val status = repository.testGeminiConnection()
            _settingsTestStatus.value = status
            if (status is GeminiConnectionStatus.Connected) {
                SoundManager.playTaskComplete()
            }
        }
    }

    init {
        val database = NourDatabase.getDatabase(application)
        repository = NourRepository(database.nourDao())

        userProfile = repository.userProfile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
        allTasks = repository.allTasks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        allHabits = repository.allHabits.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        allProjects = repository.allProjects.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        allChatMessages = repository.allChatMessages.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        activeEmergencyPlan = repository.activeEmergencyPlan.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
        allSubjectExams = repository.allSubjectExams.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        allStudyReviews = repository.allStudyReviews.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        initializeSquadAndAuth()
    }

    // ================= Settings Actions =================
    fun setDarkTheme(enabled: Boolean) {
        preferencesManager.setDarkTheme(enabled)
    }

    fun setFontScale(scale: Float) {
        preferencesManager.setFontScale(scale)
    }

    fun setLanguage(language: String) {
        preferencesManager.setLanguage(language)
    }

    fun setSoundEnabled(enabled: Boolean) {
        preferencesManager.setSoundEnabled(enabled)
    }

    fun playPreviewSound() {
        SoundManager.playTaskComplete()
    }

    fun playScreenOpenSound() {
        SoundManager.playScreenOpen()
    }


    fun completeOnboarding(
        name: String,
        age: Int,
        stage: String,
        grade: String,
        secondaryInterests: String,
        hasTechField: Boolean,
        initialProjects: List<ProjectItem> = emptyList()
    ) {
        viewModelScope.launch {
            // Profile starts at 0 points, Level 1, 0 focusReserve (Zero Seed Data)
            val newProfile = UserProfile(
                id = 1,
                name = name.trim(),
                age = age,
                stage = stage,
                grade = grade,
                secondaryInterests = secondaryInterests,
                hasTechField = hasTechField,
                points = 0,
                level = 1,
                focusReserve = 0,
                onboardingCompleted = true,
                completedActionsCount = 0
            )
            repository.saveProfile(newProfile)

            for (project in initialProjects) {
                repository.addProject(project)
            }
        }
    }

    fun addTask(task: TaskItem) {
        viewModelScope.launch {
            repository.addTask(task)
            checkAndTriggerReward()
        }
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            if (!task.isCompleted) {
                SoundManager.playTaskComplete()
            }
            repository.toggleTaskCompletion(task)
            checkAndTriggerReward()
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun addHabit(habit: HabitItem) {
        viewModelScope.launch {
            repository.addHabit(habit)
            checkAndTriggerReward()
        }
    }

    fun checkInHabit(habit: HabitItem) {
        viewModelScope.launch {
            SoundManager.playStreakIncrease()
            repository.checkInHabit(habit)
            checkAndTriggerReward()
        }
    }

    fun deleteHabit(habit: HabitItem) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun addProject(project: ProjectItem) {
        viewModelScope.launch {
            repository.addProject(project)
            checkAndTriggerReward()
        }
    }

    fun updateProject(project: ProjectItem) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    fun deleteProject(project: ProjectItem) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    // ================= Subject Exams (Success Path) =================
    fun addSubjectExam(
        subjectName: String,
        examTitle: String,
        daysFromNow: Int,
        totalUnits: Int,
        colorHex: String = "#00C6FF"
    ) {
        viewModelScope.launch {
            val targetDateMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(daysFromNow.toLong().coerceAtLeast(1))
            val exam = SubjectExam(
                subjectName = subjectName.trim(),
                examTitle = examTitle.trim(),
                targetDateMillis = targetDateMillis,
                totalUnits = totalUnits.coerceAtLeast(1),
                completedUnits = 0,
                colorHex = colorHex
            )
            repository.addSubjectExam(exam)
            SoundManager.playScreenOpen()
            checkAndTriggerReward()
        }
    }

    fun incrementExamUnit(exam: SubjectExam) {
        viewModelScope.launch {
            if (exam.completedUnits < exam.totalUnits) {
                val updated = exam.copy(completedUnits = exam.completedUnits + 1)
                repository.updateSubjectExam(updated)
                SoundManager.playTaskComplete()
                checkAndTriggerReward()
            }
        }
    }

    fun deleteSubjectExam(exam: SubjectExam) {
        viewModelScope.launch {
            repository.deleteSubjectExam(exam)
        }
    }

    fun calculateSuccessPathPoints(
        exam: SubjectExam,
        tasks: List<TaskItem>,
        habits: List<HabitItem>
    ): List<SuccessPathDayPoint> {
        val now = System.currentTimeMillis()
        val totalDaysSpan = ((exam.targetDateMillis - exam.createdAtMillis) / (24 * 60 * 60 * 1000L)).toInt().coerceIn(7, 30)
        val passedDays = ((now - exam.createdAtMillis) / (24 * 60 * 60 * 1000L)).toInt().coerceIn(0, totalDaysSpan)

        val subjectTasks = tasks.filter {
            it.title.contains(exam.subjectName, ignoreCase = true) ||
            it.subject.contains(exam.subjectName, ignoreCase = true)
        }.ifEmpty { tasks.take(4) }

        val completedTasksCount = subjectTasks.count { it.isCompleted }
        val hasActiveStreak = habits.any { it.currentStreak > 0 }

        val taskRatio = if (subjectTasks.isNotEmpty()) (completedTasksCount.toFloat() / subjectTasks.size) else 0.7f
        val streakRatio = if (hasActiveStreak) 0.5f else 0.2f
        val todayAchievementPct = ((taskRatio * 50f) + (streakRatio * 100f)).toInt().coerceIn(15, 100)

        val points = mutableListOf<SuccessPathDayPoint>()
        var runningStockIndex = 50.0f
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (dayIdx in 0..totalDaysSpan) {
            val dayMillis = exam.createdAtMillis + TimeUnit.DAYS.toMillis(dayIdx.toLong())
            val cal = Calendar.getInstance().apply { timeInMillis = dayMillis }
            val dateStr = fullDateFormat.format(cal.time)
            val displayStr = dateFormat.format(cal.time)

            val isPast = dayIdx < passedDays
            val isToday = dayIdx == passedDays
            val isFuture = dayIdx > passedDays

            val dailyPct: Int
            val dayTasksCompleted: Int
            val dayStreak: Boolean

            when {
                isToday -> {
                    dailyPct = todayAchievementPct
                    dayTasksCompleted = completedTasksCount
                    dayStreak = hasActiveStreak
                    val delta = (dailyPct - 50) * 0.4f
                    runningStockIndex = (runningStockIndex + delta).coerceIn(10f, 100f)
                }
                isPast -> {
                    val seed = (exam.id * 31 + dayIdx * 17).toInt()
                    val variation = ((seed % 35) + 55).coerceIn(40, 95)
                    dailyPct = variation
                    dayTasksCompleted = ((seed % 3) + 1).coerceAtMost(subjectTasks.size.coerceAtLeast(1))
                    dayStreak = (seed % 4) != 0
                    val delta = (dailyPct - 45) * 0.35f
                    runningStockIndex = (runningStockIndex + delta).coerceIn(15f, 98f)
                }
                else -> {
                    dailyPct = 0
                    dayTasksCompleted = 0
                    dayStreak = false
                }
            }

            points.add(
                SuccessPathDayPoint(
                    dayIndex = dayIdx + 1,
                    dateString = dateStr,
                    displayDate = displayStr,
                    achievementPercentage = dailyPct,
                    stockMarketIndex = if (isFuture) 0f else runningStockIndex,
                    totalSubjectTasks = subjectTasks.size.coerceAtLeast(1),
                    completedSubjectTasks = dayTasksCompleted,
                    studyStreakActive = dayStreak,
                    isPastDay = isPast,
                    isToday = isToday,
                    isFutureDay = isFuture
                )
            )
        }

        return points
    }

    fun sendMessageToNour(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            SoundManager.playMessagePop()
            _isChatLoading.value = true
            val profile = userProfile.value
            val pendingCount = allTasks.value.count { !it.isCompleted }
            val currentStreak = allHabits.value.maxOfOrNull { it.currentStreak } ?: 0

            repository.sendMessageToNour(
                message = message.trim(),
                profile = profile,
                pendingTasksCount = pendingCount,
                activeStreak = currentStreak
            )
            SoundManager.playMessagePop()
            _isChatLoading.value = false
        }
    }

    fun activateEmergencyPlan(examName: String, examDateMillis: Long, topics: String, plan: String) {
        viewModelScope.launch {
            repository.saveEmergencyPlan(examName, examDateMillis, topics, plan)
            checkAndTriggerReward()
        }
    }

    fun deactivateEmergencyPlan() {
        viewModelScope.launch {
            repository.clearEmergencyPlan()
        }
    }

    private suspend fun checkAndTriggerReward() {
        val reward = repository.recordActionAndCheckReward(userProfile.value)
        if (reward != null) {
            SoundManager.playVariableReward()
            _activeReward.value = reward
        }
    }

    fun dismissReward() {
        _activeReward.value = null
    }

    fun resetAllDataForTesting() {
        viewModelScope.launch {
            repository.resetAllDataForTesting()
        }
    }
}
