package com.example.data.repository

import com.example.data.api.GeminiClient
import com.example.data.api.GeminiConnectionStatus
import com.example.data.db.NourDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class NourRepository(private val dao: NourDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allTasks: Flow<List<TaskItem>> = dao.getAllTasks()
    val allHabits: Flow<List<HabitItem>> = dao.getAllHabits()
    val allProjects: Flow<List<ProjectItem>> = dao.getAllProjects()
    val allChatMessages: Flow<List<ChatMessage>> = dao.getAllChatMessages()
    val activeEmergencyPlan: Flow<EmergencyPlan?> = dao.getActiveEmergencyPlan()
    val allSubjectExams: Flow<List<SubjectExam>> = dao.getAllSubjectExams()
    val allStudyReviews: Flow<List<StudyReviewItem>> = dao.getAllStudyReviews()

    suspend fun saveStudyReview(review: StudyReviewItem): Long {
        val id = dao.insertStudyReview(review)
        dao.addPointsAndFocus(addPoints = 25, addFocus = 15)
        return id
    }

    suspend fun deleteStudyReview(review: StudyReviewItem) {
        dao.deleteStudyReview(review)
    }

    suspend fun saveProfile(profile: UserProfile) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun recordActionAndCheckReward(currentProfile: UserProfile?): VariableReward? {
        if (currentProfile == null) return null
        val newActionCount = currentProfile.completedActionsCount + 1
        dao.incrementActionsCount()

        // Dopamine Variable Reward Logic:
        // Mandatory guarantee: After 2-3 actions (newActionCount == 2 or 3), trigger the first surprise reward
        if (newActionCount == 2 || newActionCount == 3) {
            val bonusXP = 50
            dao.addPointsAndFocus(addPoints = bonusXP, addFocus = 20)
            return VariableReward(
                title = "مكافأة غير متوقعة من نور! 🌟",
                description = "لأنك بدأت رحلتك الفعلية وبدأت تبني عاداتك بقوة، استحقيت مكافأة خاصة!",
                bonusPoints = bonusXP,
                badgeTitle = "شارة البداية الذهبية 🏆",
                nourMessage = "يا بطل! أول خطوة دايمًا هي الأصعب، وعشان كده حبيت أفاجئك بـ $bonusXP نقطة وشارة البداية الذهبية.. كمل بنفس الهمة!"
            )
        }

        // Subsequent variable rewards: random chance (~25%) on milestones
        if (newActionCount > 3 && Random.nextFloat() < 0.25f) {
            val bonusXP = (Random.nextInt(3) + 1) * 25
            dao.addPointsAndFocus(addPoints = bonusXP, addFocus = 15)
            val rewards = listOf(
                VariableReward(
                    title = "دفعة طاقة مفاجئة! ⚡",
                    description = "مكافأة عشوائية على استمرارك وتركيزك المستمر اليوم.",
                    bonusPoints = bonusXP,
                    badgeTitle = "صائد المكافآت 🎯",
                    nourMessage = "شاطر جداً! الاستمرارية دي ملهمة، ونور بتقدّر تعبك.. ضفت لك $bonusXP نقطة إضافية في رصيدك!"
                ),
                VariableReward(
                    title = "هدية تركيز VIP! 💎",
                    description = "رصيدك في تصاعد مستمر ومستواك بيتطور بسرعة ملحوظة.",
                    bonusPoints = bonusXP,
                    badgeTitle = "العقل المنظم 🧠",
                    nourMessage = "طاقة إيجابية رهيبة! أخدت $bonusXP نقطة ورصيد تركيزك زاد.. المهندس عمر كان دايمًا يقول النظام نص النجاح!"
                )
            )
            return rewards.random()
        }

        return null
    }

    suspend fun addTask(task: TaskItem): Long {
        val id = dao.insertTask(task)
        dao.addPointsAndFocus(addPoints = 15, addFocus = 10)
        return id
    }

    suspend fun toggleTaskCompletion(task: TaskItem) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            completedAtMillis = if (!task.isCompleted) System.currentTimeMillis() else 0
        )
        dao.updateTask(updated)
        if (updated.isCompleted) {
            dao.addPointsAndFocus(addPoints = 30, addFocus = 25)
        }
    }

    suspend fun deleteTask(task: TaskItem) {
        dao.deleteTask(task)
    }

    suspend fun addHabit(habit: HabitItem): Long {
        val id = dao.insertHabit(habit)
        dao.addPointsAndFocus(addPoints = 15, addFocus = 10)
        return id
    }

    suspend fun checkInHabit(habit: HabitItem) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (habit.lastCompletedDate == today) return // Already checked today

        val newStreak = habit.currentStreak + 1
        val newBest = maxOf(newStreak, habit.bestStreak)
        val updated = habit.copy(
            currentStreak = newStreak,
            bestStreak = newBest,
            lastCompletedDate = today
        )
        dao.updateHabit(updated)
        dao.addPointsAndFocus(addPoints = 25, addFocus = 30)
    }

    suspend fun deleteHabit(habit: HabitItem) {
        dao.deleteHabit(habit)
    }

    suspend fun addProject(project: ProjectItem): Long {
        val id = dao.insertProject(project)
        dao.addPointsAndFocus(addPoints = 50, addFocus = 25)
        return id
    }

    suspend fun updateProject(project: ProjectItem) {
        dao.updateProject(project)
    }

    suspend fun deleteProject(project: ProjectItem) {
        dao.deleteProject(project)
    }

    suspend fun sendMessageToNour(
        message: String,
        profile: UserProfile?,
        pendingTasksCount: Int,
        activeStreak: Int
    ): String {
        val priorHistory = dao.getRecentChatMessages(limit = 6)
        dao.insertChatMessage(ChatMessage(sender = "USER", message = message))
        val reply = GeminiClient.askNour(
            userMessage = message,
            profile = profile,
            pendingTasksCount = pendingTasksCount,
            activeStreak = activeStreak,
            chatHistory = priorHistory
        )
        dao.insertChatMessage(ChatMessage(sender = "NOUR", message = reply))
        return reply
    }

    suspend fun testGeminiConnection(): GeminiConnectionStatus {
        return GeminiClient.testConnection()
    }

    suspend fun saveEmergencyPlan(examName: String, examDateMillis: Long, topics: String, plan: String) {
        dao.deactivateAllEmergencyPlans()
        dao.insertEmergencyPlan(
            EmergencyPlan(
                examName = examName,
                examDateMillis = examDateMillis,
                remainingTopics = topics,
                planBreakdown = plan,
                isActive = true
            )
        )
        dao.addPointsAndFocus(addPoints = 40, addFocus = 50)
    }

    suspend fun clearEmergencyPlan() {
        dao.deactivateAllEmergencyPlans()
    }

    suspend fun addSubjectExam(exam: SubjectExam): Long {
        val id = dao.insertSubjectExam(exam)
        dao.addPointsAndFocus(addPoints = 30, addFocus = 20)
        return id
    }

    suspend fun updateSubjectExam(exam: SubjectExam) {
        dao.updateSubjectExam(exam)
    }

    suspend fun deleteSubjectExam(exam: SubjectExam) {
        dao.deleteSubjectExam(exam)
    }

    suspend fun resetAllDataForTesting() {
        dao.clearAllTasks()
        dao.clearAllHabits()
        dao.clearAllProjects()
        dao.clearAllEmergencyPlans()
        dao.clearAllSubjectExams()
        dao.clearChat()
        dao.clearUserProfile()
    }
}

