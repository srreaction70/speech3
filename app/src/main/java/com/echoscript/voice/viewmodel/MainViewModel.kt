package com.echoscript.voice.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.echoscript.voice.data.GeminiRepository
import com.echoscript.voice.service.AndroidSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val speechManager = AndroidSpeechManager(application)
    private val geminiRepo = GeminiRepository()

    val isListening: StateFlow<Boolean> = speechManager.isListening
    val partialText: StateFlow<String> = speechManager.partialText
    val finalText: StateFlow<String> = speechManager.finalText
    val rmsDb: StateFlow<Float> = speechManager.rmsDb

    private val _isProcessingAI = MutableStateFlow(false)
    val isProcessingAI: StateFlow<Boolean> = _isProcessingAI

    private val _selectedLanguage = MutableStateFlow("fa-IR")
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    private val _promptEnhancerEnabled = MutableStateFlow(true)
    val promptEnhancerEnabled: StateFlow<Boolean> = _promptEnhancerEnabled

    fun startSpeechRecognition() {
        speechManager.startListening(_selectedLanguage.value)
    }

    fun stopSpeechRecognition() {
        speechManager.stopListening()
    }

    fun clearText() {
        speechManager.clearText()
    }

    fun onRecordPermissionGranted() {
        startSpeechRecognition()
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun togglePromptEnhancer(enabled: Boolean) {
        _promptEnhancerEnabled.value = enabled
    }

    fun translateCurrentText() {
        val current = (finalText.value + " " + partialText.value).trim()
        if (current.isEmpty()) return

        viewModelScope.launch {
            _isProcessingAI.value = true
            val translated = geminiRepo.translateText(current)
            speechManager.setText(translated)
            _isProcessingAI.value = false
        }
    }

    fun professionalizeCurrentText(style: String = "general") {
        val current = (finalText.value + " " + partialText.value).trim()
        if (current.isEmpty()) return

        viewModelScope.launch {
            _isProcessingAI.value = true
            val enhanced = geminiRepo.professionalizePrompt(current, style)
            speechManager.setText(enhanced)
            _isProcessingAI.value = false
        }
    }

    fun polishCurrentText() {
        val current = (finalText.value + " " + partialText.value).trim()
        if (current.isEmpty()) return

        viewModelScope.launch {
            _isProcessingAI.value = true
            val polished = geminiRepo.polishText(current)
            speechManager.setText(polished)
            _isProcessingAI.value = false
        }
    }
}
