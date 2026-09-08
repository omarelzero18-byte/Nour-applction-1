package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import java.util.UUID

class TextToSpeechHelper(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale("ar"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "Arabic TTS language not directly supported, falling back to default")
                        engine.setLanguage(Locale.getDefault())
                    }
                    engine.setSpeechRate(0.95f)
                    engine.setPitch(1.05f) // Warm, pleasant tone for Nour
                    isInitialized = true

                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                            _currentUtteranceId.value = utteranceId
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            _currentUtteranceId.value = null
                        }

                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            _currentUtteranceId.value = null
                        }
                    })
                }
            } else {
                Log.e(TAG, "TTS Initialization failed with status $status")
            }
        }
    }

    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (!isInitialized || tts == null) {
            initTts()
        }
        stop()
        // Strip markdown asterisks and hash symbols for clean audio pronunciation
        val cleanText = text
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("~", "")
            .trim()

        _isSpeaking.value = true
        _currentUtteranceId.value = utteranceId
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _currentUtteranceId.value = null
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "TextToSpeechHelper"
    }
}
