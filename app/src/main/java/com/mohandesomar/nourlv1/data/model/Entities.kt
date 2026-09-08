package com.mohandesomar.nourlv1.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val age: Int = 0,
    val stage: String = "", // e.g. "ثانوي عام", "إعدادي", "ابتدائي", "بكالوريا", "جامعي/أخرى"
    val grade: String = "", // e.g. "تالتة ثانوي", "أولى إعدادي", "السنة الثانية بكالوريا", etc.
    val secondaryInterests: String = "", // Comma-separated: "برمجة, ذكاء اصطناعي"
    val hasTechField: Boolean = false,
    val points: Int = 0, // Zero Seed Data! Starts strictly at 0
    val level: Int = 1, // Level 1
    val focusReserve: Int = 0, // Starts at 0
    val onboardingCompleted: Boolean = false,
    val completedActionsCount: Int = 0, // Used for variable reward trigger
    val joinedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val subject: String = "",
    val type: String = "TASK", // "TASK", "EXAM", "DELIVERABLE"
    val dueDateMillis: Long = System.currentTimeMillis(),
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false,
    val completedAtMillis: Long = 0
)

@Entity(tableName = "habits")
data class HabitItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "مذاكرة", // "مذاكرة", "صحة", "قراءة", "تركيز"
    val currentStreak: Int = 0, // Zero Seed Data! Starts strictly at 0
    val bestStreak: Int = 0,
    val lastCompletedDate: String = "", // "YYYY-MM-DD"
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val projectUrl: String = "",
    val status: String = "IN_PROGRESS", // "IN_PROGRESS", "COMPLETED"
    val techStack: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "NOUR"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "emergency_plans")
data class EmergencyPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examName: String,
    val examDateMillis: Long,
    val remainingTopics: String,
    val planBreakdown: String,
    val isActive: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "subject_exams")
data class SubjectExam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val examTitle: String,
    val targetDateMillis: Long,
    val totalUnits: Int = 5,
    val completedUnits: Int = 0,
    val colorHex: String = "#00C6FF",
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class SuccessPathDayPoint(
    val dayIndex: Int,
    val dateString: String,
    val displayDate: String,
    val achievementPercentage: Int, // 0 to 100
    val stockMarketIndex: Float, // Stock index score (starts at 50, fluctuates up/down)
    val totalSubjectTasks: Int,
    val completedSubjectTasks: Int,
    val studyStreakActive: Boolean,
    val isPastDay: Boolean,
    val isToday: Boolean,
    val isFutureDay: Boolean
)

data class VariableReward(
    val title: String,
    val description: String,
    val bonusPoints: Int,
    val badgeTitle: String? = null,
    val nourMessage: String
)

@Entity(tableName = "study_reviews")
data class StudyReviewItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // "SUMMARY" or "QUIZ"
    val content: String,
    val subject: String = "",
    val questionsJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class SquadMember(
    val id: String,
    val name: String,
    val streakDays: Int,
    val avatarEmoji: String,
    val isCurrentUser: Boolean = false
)

data class WeeklyReport(
    val studentName: String,
    val weekRange: String,
    val activeDays: Int,
    val completedTasks: Int,
    val totalFocusMinutes: Int,
    val topHabitOrSubject: String,
    val streakDays: Int,
    val nourPrideMessage: String
)

data class OralExamQuestion(
    val id: Int,
    val questionText: String,
    val idealAnswerPoints: String,
    val subject: String
)

