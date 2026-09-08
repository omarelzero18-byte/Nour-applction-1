package com.example.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural Sound Design Engine for Nour
 * Generates warm, clean, royalty-free digital chimes, bells, and tones
 * using 16-bit PCM AudioTrack synthesis.
 */
object SoundManager {

    @Volatile
    var isSoundEnabled: Boolean = true

    private const val SAMPLE_RATE = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    private fun playPcm(samples: ShortArray) {
        if (!isSoundEnabled || samples.isEmpty()) return
        scope.launch {
            var audioTrack: AudioTrack? = null
            try {
                val bufferSize = samples.size * 2
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                val durationMs = (samples.size * 1000L) / SAMPLE_RATE
                delay(durationMs + 30)
            } catch (_: Throwable) {
                // Graceful fallback if device audio service is busy
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Throwable) {}
            }
        }
    }

    // ==========================================
    // 1) أصوات الإنترو (Intro Sequence Sounds)
    // ==========================================

    /**
     * ولادة النور: نغمة ناعمة صاعدة الطبقة (soft rising tone)
     * Glides smoothly from 240Hz to 520Hz over 0.9s with gentle envelope.
     */
    fun playIntroBirthOfLight() {
        val durationSec = 0.9
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        val startFreq = 240.0
        val endFreq = 520.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / durationSec
            val currentFreq = startFreq + (endFreq - startFreq) * progress

            // Gentle fade-in and smooth fade-out envelope
            val envelope = when {
                progress < 0.2 -> progress / 0.2
                progress > 0.7 -> (1.0 - progress) / 0.3
                else -> 1.0
            }

            val sample = sin(2.0 * PI * currentFreq * t) * envelope * 0.7
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * الكرة المتحوّلة: صوت تموّج ونبض هادئ (soft swell/whoosh)
     * Warm 180Hz swell with smooth sinusoidal amplitude curve.
     */
    fun playIntroMorphingSphere() {
        val durationSec = 1.0
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / durationSec

            // Swell envelope: sin(PI * progress)
            val envelope = sin(PI * progress)
            // Harmonics: 180Hz + subtle 360Hz
            val sample = (0.75 * sin(2.0 * PI * 180.0 * t) + 0.25 * sin(2.0 * PI * 360.0 * t)) * envelope * 0.65
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * الجسيمات المتجمّعة: نغمة عنقودية زجاجية سريعة (crystalline chime cluster)
     * Cascading crystalline pentatonic chime cluster (0.8s).
     */
    fun playIntroParticleCluster() {
        val durationSec = 0.8
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        val frequencies = doubleArrayOf(1046.5, 1318.5, 1567.9, 1975.5, 2349.3) // C6, E6, G6, B6, D7
        val noteOffsets = doubleArrayOf(0.0, 0.08, 0.16, 0.24, 0.32)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var combinedSample = 0.0

            for (n in frequencies.indices) {
                val noteStart = noteOffsets[n]
                if (t >= noteStart) {
                    val noteT = t - noteStart
                    val decay = exp(-noteT * 6.0)
                    combinedSample += sin(2.0 * PI * frequencies[n] * noteT) * decay * 0.18
                }
            }

            buffer[i] = (combinedSample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * صحوة نور: نغمة دفء رنانة هادئة (warm awakening bell)
     * 440Hz + 880Hz + 1320Hz warm resonant chime bell (1.2s).
     */
    fun playIntroAwakening() {
        val durationSec = 1.2
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        val fundamental = 440.0 // A4 warm frequency

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val attack = (t / 0.03).coerceAtMost(1.0)
            val decay = exp(-t * 2.8)
            val envelope = attack * decay

            val sample = (
                0.60 * sin(2.0 * PI * fundamental * t) +
                0.28 * sin(2.0 * PI * (fundamental * 2.0) * t) +
                0.12 * sin(2.0 * PI * (fundamental * 3.0) * t)
            ) * envelope * 0.8

            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    // ==========================================
    // 2) الأصوات التفاعلية داخل التطبيق (In-App Sounds)
    // ==========================================

    /**
     * إكمال مهمة: صوت قصير إيجابي وواضح (crisp positive ping)
     * High clean ping: 784Hz -> 1046Hz (0.22s).
     */
    fun playTaskComplete() {
        val durationSec = 0.22
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = if (t < 0.06) 784.0 else 1046.5
            val localT = if (t < 0.06) t else (t - 0.06)
            val decay = exp(-localT * 12.0)
            val sample = sin(2.0 * PI * freq * t) * decay * 0.75
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * الحفاظ على Streak / زيادته: أربيجيو صاعد احتفالي (short ascending arpeggio)
     * C5 -> E5 -> G5 -> C6 (0.38s).
     */
    fun playStreakIncrease() {
        val durationSec = 0.40
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val stepDuration = 0.09

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noteIndex = (t / stepDuration).toInt().coerceAtMost(notes.size - 1)
            val noteT = t - (noteIndex * stepDuration)
            val decay = exp(-noteT * 7.0)

            val sample = sin(2.0 * PI * notes[noteIndex] * t) * decay * 0.7
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * مكافأة مفاجئة (Variable Reward): نغمة ساحرة مميزة ونادرة (sparkle/magic chime)
     * Cascading shimmer of bell harmonics (0.65s).
     */
    fun playVariableReward() {
        val durationSec = 0.65
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        val freqs = doubleArrayOf(880.0, 1108.7, 1318.5, 1661.2, 2093.0) // A5 pentatonic
        val offsets = doubleArrayOf(0.0, 0.06, 0.12, 0.18, 0.24)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var mixed = 0.0
            for (n in freqs.indices) {
                if (t >= offsets[n]) {
                    val nt = t - offsets[n]
                    val decay = exp(-nt * 5.0)
                    mixed += sin(2.0 * PI * freqs[n] * nt) * decay * 0.20
                }
            }
            buffer[i] = (mixed.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * رفع مستوى (Level Up / Badge): لحن قصير احتفالي هادئ (soft short fanfare)
     * Triumphant chord + high resolving tone (0.7s).
     */
    fun playLevelUp() {
        val durationSec = 0.75
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val sample = when {
                t < 0.2 -> {
                    val decay = exp(-t * 4.0)
                    (sin(2.0 * PI * 523.25 * t) + sin(2.0 * PI * 659.25 * t)) * decay * 0.4
                }
                t < 0.4 -> {
                    val dt = t - 0.2
                    val decay = exp(-dt * 4.0)
                    (sin(2.0 * PI * 659.25 * t) + sin(2.0 * PI * 783.99 * t)) * decay * 0.4
                }
                else -> {
                    val dt = t - 0.4
                    val decay = exp(-dt * 3.5)
                    (sin(2.0 * PI * 1046.50 * t) + sin(2.0 * PI * 1318.51 * t)) * decay * 0.45
                }
            }
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * تنبيه خطر انقطاع الـ Streak: نغمة ودّية خفيفة ومحايدة (friendly neutral reminder)
     * Soft descending reminder (440Hz -> 392Hz, 0.3s).
     */
    fun playStreakWarning() {
        val durationSec = 0.30
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = if (t < 0.15) 440.0 else 392.0
            val decay = exp(-(t % 0.15) * 8.0)
            val sample = sin(2.0 * PI * freq * t) * decay * 0.5
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * فتح مسار النجاح أو الملخص: لحن قصير مبهج (screen open chime)
     * Upward chord: 587Hz -> 740Hz -> 880Hz (0.45s).
     */
    fun playScreenOpen() {
        val durationSec = 0.45
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        val notes = doubleArrayOf(587.33, 739.99, 880.0) // D5, F#5, A5
        val step = 0.10

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val idx = (t / step).toInt().coerceAtMost(notes.size - 1)
            val nt = t - (idx * step)
            val decay = exp(-nt * 6.0)
            val sample = sin(2.0 * PI * notes[idx] * t) * decay * 0.6
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * إرسال / استقبال رسالة من نور: صوت pop ناعم ودافئ
     * Soft warm bubble drop: 460Hz -> 230Hz (0.07s).
     */
    fun playMessagePop() {
        val durationSec = 0.07
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / durationSec
            val freq = 460.0 - (230.0 * progress)
            val envelope = sin(PI * progress)
            val sample = sin(2.0 * PI * freq * t) * envelope * 0.7
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * تفعيل المايك (Speech-to-Text): نغمة صاعدة خفيفة عند بدء الاستماع
     * 440Hz -> 880Hz (0.12s).
     */
    fun playMicStart() {
        val durationSec = 0.12
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / durationSec
            val freq = 440.0 + (440.0 * progress)
            val envelope = sin(PI * progress)
            val sample = sin(2.0 * PI * freq * t) * envelope * 0.65
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * إيقاف المايك (Speech-to-Text): نغمة هابطة خفيفة عند انتهاء الاستماع
     * 880Hz -> 440Hz (0.12s).
     */
    fun playMicStop() {
        val durationSec = 0.12
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / durationSec
            val freq = 880.0 - (440.0 * progress)
            val envelope = sin(PI * progress)
            val sample = sin(2.0 * PI * freq * t) * envelope * 0.65
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * ضغطات الأزرار العامة: طقة خفيفة جدًا (subtle button tap)
     * 600Hz quick pulse (0.025s).
     */
    fun playButtonTap() {
        val durationSec = 0.025
        val numSamples = (durationSec * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 60.0)
            val sample = sin(2.0 * PI * 600.0 * t) * decay * 0.4
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playPcm(buffer)
    }
}
