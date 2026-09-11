package com.echoscript.voice.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.echoscript.voice.data.GeminiRepository
import com.echoscript.voice.service.AndroidSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("echoscript_voice_prefs", Context.MODE_PRIVATE)
    private val speechManager = AndroidSpeechManager(application)
    private val geminiRepo = GeminiRepository(prefs.getString("gemini_api_key", "") ?: "")

    val isListening: StateFlow<Boolean> = speechManager.isListening
    val isRecording: StateFlow<Boolean> = speechManager.isRecording
    val durationFormatted: StateFlow<String> = speechManager.durationFormatted
    val durationSeconds: StateFlow<Int> = speechManager.durationSeconds
    val statusMessage: StateFlow<String> = speechManager.statusMessage
    val partialText: StateFlow<String> = speechManager.partialText
    val finalText: StateFlow<String> = speechManager.finalText
    val rmsDb: StateFlow<Float> = speechManager.rmsDb

    private val _isProcessingAI = MutableStateFlow(false)
    val isProcessingAI: StateFlow<Boolean> = _isProcessingAI

    private val _selectedLanguage = MutableStateFlow(prefs.getString("selected_lang", "fa-IR") ?: "fa-IR")
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    private val _promptEnhancerEnabled = MutableStateFlow(prefs.getBoolean("prompt_enhancer", false))
    val promptEnhancerEnabled: StateFlow<Boolean> = _promptEnhancerEnabled

    private val _apiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val apiKey: StateFlow<String> = _apiKey

    /**
     * Toggles continuous audio recording.
     * When stopping, immediately sends the recorded audio to Gemini 3.8 Flash for accurate transcription.
     */
    fun toggleRecording() {
        if (speechManager.isRecording.value) {
            stopAndTranscribe()
        } else {
            startSpeechRecognition()
        }
    }

    fun startSpeechRecognition() {
        speechManager.startListening(_selectedLanguage.value)
    }

    /**
     * Stops continuous recording and processes the entire captured audio without premature cut-offs.
     */
    fun stopAndTranscribe() {
        val audioFile = speechManager.stopListening()
        val base64Audio = speechManager.getRecordedAudioBase64()

        if (base64Audio.isNullOrEmpty()) {
            speechManager.setText("")
            return
        }

        viewModelScope.launch {
            _isProcessingAI.value = true
            speechManager.setProcessingState(true, "در حال تبدیل گفتار ضبط‌شده به متن با هوش مصنوعی (Gemini 3.8 Flash)...")

            try {
                val transcribedText = geminiRepo.transcribeAudio(
                    base64Audio = base64Audio,
                    mimeType = "audio/mp4",
                    targetLanguage = _selectedLanguage.value
                )

                if (transcribedText.isNotBlank()) {
                    speechManager.setText(transcribedText)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isProcessingAI.value = false
                speechManager.setProcessingState(false)
                speechManager.cleanup()
            }
        }
    }

    fun stopSpeechRecognition() {
        stopAndTranscribe()
    }

    fun clearText() {
        speechManager.clearText()
    }

    fun onRecordPermissionGranted() {
        toggleRecording()
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
        prefs.edit().putString("selected_lang", lang).apply()
    }

    fun togglePromptEnhancer(enabled: Boolean) {
        _promptEnhancerEnabled.value = enabled
        prefs.edit().putBoolean("prompt_enhancer", enabled).apply()
    }

    fun setApiKey(key: String) {
        _apiKey.value = key
        prefs.edit().putString("gemini_api_key", key).apply()
        geminiRepo.setApiKey(key)
    }

    fun translateCurrentText() {
        val current = finalText.value.trim()
        if (current.isEmpty()) return

        viewModelScope.launch {
            _isProcessingAI.value = true
            val translated = geminiRepo.translateText(current)
            speechManager.setText(translated)
            _isProcessingAI.value = false
        }
    }

    fun professionalizeCurrentText(style: String = "general") {
        val current = finalText.value.trim()
        if (current.isEmpty()) return

        viewModelScope.launch {
            _isProcessingAI.value = true
            val enhanced = geminiRepo.professionalizePrompt(current, style)
            speechManager.setText(enhanced)
            _isProcessingAI.value = false
        }
    }

    fun polishCurrentText() {
        val current = finalText.value.trim()
        if (current.isEmpty()) return

        viewModelScope.launch {
            _isProcessingAI.value = true
            val polished = geminiRepo.polishText(current)
            speechManager.setText(polished)
            _isProcessingAI.value = false
        }
    }
}
