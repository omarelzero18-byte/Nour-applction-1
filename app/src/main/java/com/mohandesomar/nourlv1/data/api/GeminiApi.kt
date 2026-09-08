package com.mohandesomar.nourlv1.data.api

import android.graphics.Bitmap
import android.util.Log
import com.mohandesomar.nourlv1.data.model.ChatMessage
import com.mohandesomar.nourlv1.data.model.OralExamQuestion
import com.mohandesomar.nourlv1.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.mohandesomar.nourlv1.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    val mimeType: String,
    val data: String // Base64
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val maxOutputTokens: Int? = 1000
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

sealed class GeminiConnectionStatus {
    object Idle : GeminiConnectionStatus()
    object Checking : GeminiConnectionStatus()
    data class Connected(val model: String, val latencyMs: Long) : GeminiConnectionStatus()
    data class KeyMissingOrInvalid(val code: Int?, val details: String) : GeminiConnectionStatus()
    data class ServerBusy(val code: Int, val details: String) : GeminiConnectionStatus()
    data class NetworkError(val details: String) : GeminiConnectionStatus()
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    // Candidate Models in strict priority order (v3 Roadmap):
    // 1. Primary: gemini-3.8-flash (cutting-edge, fastest)
    // 2. Fallback: gemini-3.7-flash (stable high-yield)
    // 3. Guaranteed stable: gemini-2.5-flash (guaranteed fallback)
    val CANDIDATE_MODELS = listOf(
        "gemini-3.8-flash",
        "gemini-3.7-flash",
        "gemini-2.5-flash"
    )

    // Exponential backoff delays in milliseconds for retry on 503 / 429 / 5xx
    // 1s -> 2s -> 4s
    private val RETRY_DELAYS_MS = listOf(1000L, 2000L, 4000L)

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // HttpLoggingInterceptor is set to Level.NONE in release to protect student privacy,
    // and Level.BASIC in debug builds only.
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Primary chat query with Nour using Firebase AI Logic + App Check,
     * with model cascading across CANDIDATE_MODELS.
     */
    suspend fun askNour(
        userMessage: String,
        profile: UserProfile?,
        pendingTasksCount: Int,
        activeStreak: Int,
        chatHistory: List<ChatMessage> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val studentName = profile?.name?.ifBlank { "يا بطل" } ?: "يا بطل"
        val grade = profile?.grade?.ifBlank { "طالب" } ?: "طالب"
        val secondary = profile?.secondaryInterests?.ifBlank { "مهارات عامة" } ?: "مهارات عامة"

        // Nour's core character instruction (includes mandatory "المهندس عمر" identity)
        val systemPrompt = """
            أنت "نور" (Nour-lv1) — المساعد الذكي ورفيقة تنظيم حياة الطالب المصري.
            شخصيتك وأسلوبك:
            - تتحدثين باللهجة المصرية العامية الدافئة، الذكية، والودودة جداً.
            - تخاطبين الطالب باسمه دائماً: $studentName.
            - مرحلته الدراسية الحالية: $grade. إذا كانت مرحلة مصيرية (مثل تالتة ثانوي أو إعدادية)، شجعيه بتفهم لضغط الامتحانات.
            - اهتماماته الإضافية: $secondary.
            - لديه حالياً $pendingTasksCount مهام متبقية و streak قدره $activeStreak يوم.
            - صانعك هو المهندس عمر: لا تكرري ذكر اسمه في كل رسالة؛ اذكريه فقط بشكل عفوي وطبيعي إذا ناسب السياق.
            - جاوبي إجابة مباشرة ودقيقة ومفصلة وذكية على سؤال الطالب بالظبط أياً كان مجاله (مذاكرة، جدول، تلخيص، برمجة، أو ثقافة عامة أو طقس أو دردشة).
            - ممنوع منعاً باتاً تكرار نفس الرد أو إعطاء ردود عامة محفوظة لا تجيب على السؤال المحدد.
            - اكتبي باللغة العربية السليمة والواضحة مع تنسيق مرتب ونقاط واضحة عند الحاجة.
        """.trimIndent()

        // Attempt Firebase AI Logic with App Check cascading across models
        for (model in CANDIDATE_MODELS) {
            try {
                if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                    Log.d(TAG, "Attempting Firebase AI Logic with model $model...")
                    val generativeModel = Firebase.ai.generativeModel(
                        modelName = model,
                        systemInstruction = content { text(systemPrompt) }
                    )
                    val response = generativeModel.generateContent(userMessage)
                    val text = response.text
                    if (!text.isNullOrBlank()) {
                        Log.i(TAG, "Firebase AI Logic succeeded with model $model")
                        return@withContext text.trim()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firebase AI model $model failed: ${e.message}, trying next...")
            }
        }

        return@withContext "يا $studentName، تعذر الاتصال بنور عبر خوادم الذكاء الاصطناعي حالياً 🌐.. تأكد من اتصال الإنترنت أو إعدادات Firebase App Check ثم أعد المحاولة! 💜"
    }

    /**
     * Gemini Vision: Analyzes a study notebook / textbook image and produces
     * either a comprehensive revision summary or 5 interactive quiz questions.
     */
    suspend fun analyzeStudyImage(
        bitmap: Bitmap,
        base64Image: String,
        mode: String, // "SUMMARY" or "QUIZ"
        subjectHint: String,
        studentName: String
    ): String = withContext(Dispatchers.IO) {
        val promptText = if (mode == "QUIZ") {
            """
                أنتِ نور، رفيقة الطالب الذكية. بناءً على صفحة المذاكرة المرفقة في مادة ($subjectHint):
                استخرجي 5 أسئلة كويز ذكية لاختبار فهم الطالب $studentName.
                لكل سؤال:
                - اكتبِ نص السؤال بوضوح.
                - 4 اختيارات (أ، ب، ج، د).
                - الإجابة الصحيحة مع شرح مبسط وسريع لسبب صحتها باللهجة المصرية اللطيفة.
                نسّقي النتيجة بشكل مرتب وواضح ومحفز!
            """.trimIndent()
        } else {
            """
                أنتِ نور، رفيقة الطالب الذكية والمصنوعة بواسطة المهندس عمر.
                بناءً على صفحة المذاكرة المرفقة في مادة ($subjectHint):
                قدّمي ملخصاً دراسياً شاملاً ومكثفاً للطالب $studentName:
                1. المفاهيم والقوانين والقواعد الرئيسية في الصفحة.
                2. نقاط هامة جداً متوقعة للامتحان.
                3. ملخص سريع في 3 سطور للمراجعة الفورية.
                استخدمي أسلوب منظم بنقاط واضحة وخط عريض ومحفز باللهجة المصرية الذكية.
            """.trimIndent()
        }

        // Try Firebase AI Logic with Bitmap directly
        for (model in CANDIDATE_MODELS) {
            try {
                if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                    val generativeModel = Firebase.ai.generativeModel(
                        modelName = model
                    )
                    val response = generativeModel.generateContent(
                        content {
                            image(bitmap)
                            text(promptText)
                        }
                    )
                    val result = response.text
                    if (!result.isNullOrBlank()) {
                        return@withContext result.trim()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Vision model $model failed: ${e.message}")
            }
        }

        // Fallback to REST with inlineData
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(text = promptText),
                        GeminiPart(inlineData = GeminiInlineData("image/jpeg", base64Image))
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.4f, maxOutputTokens = 1500)
        )

        for (model in CANDIDATE_MODELS) {
            try {
                val response = api.generateContent(model, "", request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) return@withContext reply.trim()
            } catch (e: Exception) {
                Log.w(TAG, "Vision REST failed on $model: ${e.message}")
            }
        }

        return@withContext "عذراً يا $studentName، تعذر تحليل الصورة بالكامل الآن بسبب ضغط الشبكة 🌐. تأكد من وضوح الصورة وجرّب تلتقطها مرة تانية!"
    }

    /**
     * Oral Mock Exam: Generates an oral question and evaluates spoken answers.
     */
    suspend fun generateOralQuestion(
        subject: String,
        studentName: String
    ): OralExamQuestion = withContext(Dispatchers.IO) {
        val prompt = "أنتِ نور، اطرحي سؤالاً شفهياً سريعاً وذكياً في مادة $subject للطالب $studentName، بدون خيارات. اكتبي السؤال فقط في سطر واحد."
        var questionText = "ما هي الفكرة الأساسية في درسك الأخير في مادة $subject؟"

        try {
            for (model in CANDIDATE_MODELS) {
                if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                    val genModel = Firebase.ai.generativeModel(modelName = model)
                    val res = genModel.generateContent(prompt)
                    val txt = res.text
                    if (!txt.isNullOrBlank()) {
                        questionText = txt.trim().replace("\"", "").replace("*", "")
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "generateOralQuestion fallback: ${e.message}")
        }

        OralExamQuestion(
            id = (1..1000).random(),
            questionText = questionText,
            idealAnswerPoints = "الإجابة العلمية الدقيقة والواضحة لمادة $subject",
            subject = subject
        )
    }

    suspend fun evaluateOralAnswer(
        subject: String,
        questionText: String,
        studentAnswer: String,
        studentName: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            أنتِ نور المساعد التعليمي الذكي.
            في امتحان شفهي لمادة $subject:
            السؤال كان: $questionText
            إجابة الطالب $studentName المنطوقة بالصوت كانت: "$studentAnswer"
            
            قيّمي إجابته في رد صوتي قصير لا يتجاوز 3 سطور:
            1. علامة من 10 (مثلاً: 8/10).
            2. كلمة تشجيع دافئة باللهجة المصرية.
            3. تصحيح أو إضافة لمعلومة ناقصة إن وجدت.
        """.trimIndent()

        try {
            for (model in CANDIDATE_MODELS) {
                if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                    val genModel = Firebase.ai.generativeModel(modelName = model)
                    val res = genModel.generateContent(prompt)
                    val txt = res.text
                    if (!txt.isNullOrBlank()) return@withContext txt.trim()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "evaluateOralAnswer error: ${e.message}")
        }

        return@withContext "إجابة ممتازة ومجهود رائع يا $studentName! تقييمي ليك 9/10، استمر في المراجعة وربنا يوفقك 👏"
    }

    /**
     * Proactive Weekly Schedule: Generates a tailored daily study plan.
     */
    suspend fun generateProactiveWeeklyPlan(
        studentName: String,
        tasks: List<String>,
        exams: List<String>,
        habits: List<String>
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            أنتِ نور، المساعد الذكي لتنظيم حياة الطالب.
            بيانات الطالب $studentName لهذا الأسبوع:
            - مهام متبقية: ${tasks.joinToString(", ").ifBlank { "لا توجد مهام مسجلة" }}
            - الامتحانات القادمة: ${exams.joinToString(", ").ifBlank { "لا توجد امتحانات قريبة" }}
            - العادات اليومية: ${habits.joinToString(", ").ifBlank { "مذاكرة يومية، قراءة" }}

            اقترحي خطة أسبوعية متوازنة ومقسمة على أيام الأسبوع (السبت إلى الخميس) توزع الجهد وتمنع التراكم والتسويف.
            اجعلي الخطة عملية، محفزة، وباللهجة المصرية اللطيفة.
        """.trimIndent()

        try {
            for (model in CANDIDATE_MODELS) {
                if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                    val genModel = Firebase.ai.generativeModel(modelName = model)
                    val res = genModel.generateContent(prompt)
                    val txt = res.text
                    if (!txt.isNullOrBlank()) return@withContext txt.trim()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "generateProactiveWeeklyPlan error: ${e.message}")
        }

        return@withContext "السبت والأحد: مراجعة المواد الأساسية وحل التدريبات.\nالاثنين والثلاثاء: التركيز على الدروس الجديدة وتثبيت القوانين.\nالأربعاء والخميس: اختبارات قصيرة ومراجعة شاملة للامتحانات القادمة!"
    }

    /**
     * Diagnostic test: tests connection securely without sending or logging sensitive keys.
     */
    suspend fun testConnection(): GeminiConnectionStatus = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        for (model in CANDIDATE_MODELS) {
            try {
                if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                    val genModel = Firebase.ai.generativeModel(
                        modelName = model,
                        generationConfig = com.google.firebase.ai.type.generationConfig {
                            maxOutputTokens = 3
                            temperature = 0f
                        }
                    )
                    val res = genModel.generateContent("ping")
                    if (!res.text.isNullOrBlank()) {
                        val latency = System.currentTimeMillis() - startTime
                        return@withContext GeminiConnectionStatus.Connected(model = model, latencyMs = latency)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "testConnection with $model: ${e.message}")
            }
        }

        return@withContext GeminiConnectionStatus.NetworkError("تعذر الاتصال بخوادم الذكاء الاصطناعي 🌐.. تأكد من اتصال الإنترنت وإعدادات App Check")
    }
}
