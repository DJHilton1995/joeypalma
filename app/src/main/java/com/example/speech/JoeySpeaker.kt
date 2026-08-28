package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JoeySpeaker(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentlySpeakingMessageId = MutableStateFlow<Long?>(null)
    val currentlySpeakingMessageId: StateFlow<Long?> = _currentlySpeakingMessageId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w("JoeySpeaker", "US Language not supported or missing data")
                    } else {
                        engine.setPitch(1.05f) // Energetic Joey tone
                        engine.setSpeechRate(1.0f)
                        isInitialized = true
                    }
                }
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                _currentlySpeakingMessageId.value = null
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _currentlySpeakingMessageId.value = null
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                _currentlySpeakingMessageId.value = null
            }
        })
    }

    fun speak(messageId: Long, text: String) {
        if (!isInitialized || tts == null) return

        // If currently speaking this exact message, stop it
        if (_currentlySpeakingMessageId.value == messageId && _isSpeaking.value) {
            stop()
            return
        }

        // Clean markdown symbols from speech for natural audio
        val cleanSpeechText = text
            .replace(Regex("```[a-zA-Z]*\\n[\\s\\S]*?```"), "Code block omitted from speech.")
            .replace(Regex("[#*_`~]"), "")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .take(1500)

        _currentlySpeakingMessageId.value = messageId
        tts?.speak(cleanSpeechText, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_$messageId")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _currentlySpeakingMessageId.value = null
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("JoeySpeaker", "Error shutting down TTS: ${e.message}")
        }
    }
}
