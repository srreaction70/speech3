package com.echoscript.voice.service

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Base64
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Continuous High-Quality Voice Recorder & Speech Manager for Android.
 * Solves the issue of premature cut-offs and sudden stopping during pauses.
 * Records audio continuously until the user explicitly taps "Stop Recording",
 * then processes the full captured audio with Gemini 3.8 Flash AI.
 */
class AndroidSpeechManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Recording States
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _isProcessingAI = MutableStateFlow(false)
    val isProcessingAI: StateFlow<Boolean> = _isProcessingAI

    private val _durationSeconds = MutableStateFlow(0)
    val durationSeconds: StateFlow<Int> = _durationSeconds

    private val _durationFormatted = MutableStateFlow("00:00")
    val durationFormatted: StateFlow<String> = _durationFormatted

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb

    private val _finalText = MutableStateFlow("")
    val finalText: StateFlow<String> = _finalText

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText

    private val _statusMessage = MutableStateFlow("آماده برای ضبط صدا")
    val statusMessage: StateFlow<String> = _statusMessage

    // Backwards compatibility alias
    val isListening: StateFlow<Boolean> = _isRecording

    /**
     * Starts continuous audio recording. Does NOT cut off on user pauses.
     * The recording stays active until stopRecording() is explicitly called by the user.
     */
    fun startListening(languageCode: String = "fa-IR"): Boolean {
        if (_isRecording.value) return true

        return try {
            val audioDir = File(context.cacheDir, "recordings").apply { mkdirs() }
            val outputFile = File(audioDir, "voice_${System.currentTimeMillis()}.m4a")
            currentAudioFile = outputFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _isRecording.value = true
            _durationSeconds.value = 0
            _durationFormatted.value = "00:00"
            _statusMessage.value = "در حال ضبط پیوسته... صحبت کنید (توقف خودکار غیرفعال است)"

            // Start live ticker & audio amplitude wave tracker
            timerJob?.cancel()
            timerJob = scope.launch {
                var sec = 0
                while (isActive && _isRecording.value) {
                    delay(1000)
                    sec++
                    _durationSeconds.value = sec
                    val m = sec / 60
                    val s = sec % 60
                    _durationFormatted.value = String.format("%02d:%02d", m, s)

                    try {
                        val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                        _rmsDb.value = (maxAmp / 32767f).coerceIn(0f, 1f)
                    } catch (e: Exception) {
                        _rmsDb.value = 0.4f
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _statusMessage.value = "خطا در شروع ضبط میکروفون: ${e.localizedMessage ?: "دسترسی ناموفق"}"
            stopListening()
            false
        }
    }

    /**
     * Stops continuous recording and returns the completed audio file for AI processing.
     */
    fun stopListening(): File? {
        timerJob?.cancel()
        timerJob = null
        _isRecording.value = false
        _rmsDb.value = 0f

        return try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                release()
            }
            mediaRecorder = null
            _statusMessage.value = "ضبط پایان یافت. در حال آماده‌سازی برای پردازش هوش مصنوعی..."
            currentAudioFile
        } catch (e: Exception) {
            e.printStackTrace()
            mediaRecorder = null
            null
        }
    }

    /**
     * Reads the recorded audio file into a Base64 string for Gemini API.
     */
    fun getRecordedAudioBase64(): String? {
        val file = currentAudioFile ?: return null
        if (!file.exists() || file.length() == 0L) return null
        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun setProcessingState(processing: Boolean, message: String = "") {
        _isProcessingAI.value = processing
        if (message.isNotBlank()) {
            _statusMessage.value = message
        }
    }

    fun clearText() {
        _finalText.value = ""
        _partialText.value = ""
        _statusMessage.value = "آماده برای ضبط صدا"
    }

    fun setText(text: String) {
        _finalText.value = text
        _partialText.value = ""
        _statusMessage.value = "متن آماده است ✨"
    }

    fun cleanup() {
        try {
            currentAudioFile?.delete()
        } catch (e: Exception) {}
        currentAudioFile = null
    }
}
