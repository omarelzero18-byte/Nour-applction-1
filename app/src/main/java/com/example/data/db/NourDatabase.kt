package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NourDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET points = points + :addPoints, focusReserve = focusReserve + :addFocus WHERE id = 1")
    suspend fun addPointsAndFocus(addPoints: Int, addFocus: Int)

    @Query("UPDATE user_profile SET completedActionsCount = completedActionsCount + 1 WHERE id = 1")
    suspend fun incrementActionsCount()

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY dueDateMillis ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDateMillis ASC")
    fun getPendingTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAtMillis DESC")
    fun getCompletedTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // Habits
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitItem): Long

    @Update
    suspend fun updateHabit(habit: HabitItem)

    @Delete
    suspend fun deleteHabit(habit: HabitItem)

    // Projects
    @Query("SELECT * FROM projects ORDER BY createdAtMillis DESC")
    fun getAllProjects(): Flow<List<ProjectItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectItem): Long

    @Update
    suspend fun updateProject(project: ProjectItem)

    @Delete
    suspend fun deleteProject(project: ProjectItem)

    // Chat
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM (SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit) ORDER BY timestamp ASC")
    suspend fun getRecentChatMessages(limit: Int = 6): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()

    // Emergency Plans
    @Query("SELECT * FROM emergency_plans WHERE isActive = 1 ORDER BY createdAtMillis DESC LIMIT 1")
    fun getActiveEmergencyPlan(): Flow<EmergencyPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyPlan(plan: EmergencyPlan): Long

    @Query("UPDATE emergency_plans SET isActive = 0")
    suspend fun deactivateAllEmergencyPlans()

    // Subject Exams (Success Path)
    @Query("SELECT * FROM subject_exams ORDER BY targetDateMillis ASC")
    fun getAllSubjectExams(): Flow<List<SubjectExam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjectExam(exam: SubjectExam): Long

    @Update
    suspend fun updateSubjectExam(exam: SubjectExam)

    @Delete
    suspend fun deleteSubjectExam(exam: SubjectExam)

    @Query("DELETE FROM subject_exams")
    suspend fun clearAllSubjectExams()

    // Study Reviews (Vision Summaries & Quizzes)
    @Query("SELECT * FROM study_reviews ORDER BY timestamp DESC")
    fun getAllStudyReviews(): Flow<List<StudyReviewItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyReview(review: StudyReviewItem): Long

    @Delete
    suspend fun deleteStudyReview(review: StudyReviewItem)

    @Query("DELETE FROM study_reviews")
    suspend fun clearAllStudyReviews()

    // Clear everything for clean reset / testing zero state
    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    @Query("DELETE FROM habits")
    suspend fun clearAllHabits()

    @Query("DELETE FROM projects")
    suspend fun clearAllProjects()

    @Query("DELETE FROM emergency_plans")
    suspend fun clearAllEmergencyPlans()

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()
}

val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `subject_exams` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `subjectName` TEXT NOT NULL,
                `examTitle` TEXT NOT NULL,
                `targetDateMillis` INTEGER NOT NULL,
                `totalUnits` INTEGER NOT NULL DEFAULT 5,
                `completedUnits` INTEGER NOT NULL DEFAULT 0,
                `colorHex` TEXT NOT NULL DEFAULT '#00C6FF',
                `createdAtMillis` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `study_reviews` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `subject` TEXT NOT NULL DEFAULT '',
                `questionsJson` TEXT NOT NULL DEFAULT '',
                `timestamp` INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

@Database(
    entities = [
        UserProfile::class,
        TaskItem::class,
        HabitItem::class,
        ProjectItem::class,
        ChatMessage::class,
        EmergencyPlan::class,
        SubjectExam::class,
        StudyReviewItem::class
    ],
    version = 3,
    exportSchema = true
)
abstract class NourDatabase : RoomDatabase() {
    abstract fun nourDao(): NourDao

    companion object {
        @Volatile
        private var INSTANCE: NourDatabase? = null

        fun getDatabase(context: android.content.Context): NourDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    NourDatabase::class.java,
                    "nour_lv1_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

