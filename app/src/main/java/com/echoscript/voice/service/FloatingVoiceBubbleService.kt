package com.echoscript.voice.service

import android.app.Notification
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.os.VibrationEffect
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.echoscript.voice.EchoScriptApplication
import com.echoscript.voice.R
import com.echoscript.voice.data.GeminiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Native Android Persistent Center Floating Assistive Hub Window
 * Floats permanently on top of all applications (Telegram, WhatsApp, ChatGPT, Chrome, Instagram, etc.)
 * Provides an expandable multi-action hub for Voice-to-Text, Translation, Prompt Engineering, and Polishing.
 */
class FloatingVoiceBubbleService : Service() {

    companion object {
        var isRunning = false
        const val NOTIFICATION_ID = 1001
    }

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var speechManager: AndroidSpeechManager
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private val geminiRepo = GeminiRepository()
    private var isMenuExpanded = false
    private var currentExtractedText = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        speechManager = AndroidSpeechManager(this)
        startForeground(NOTIFICATION_ID, createNotification())
        initFloatingHub()
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, EchoScriptApplication.CHANNEL_ID)
            .setContentTitle("EchoScript Voice Hub")
            .setContentText("دکمه شناور دسترسی سریع و تبدیل گفتار به متن بر روی صفحه فعال است")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun initFloatingHub() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Center on screen initially
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutParamsType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (screenWidth / 2) - 40 // Centered horizontally
            y = (screenHeight / 2) - 100 // Centered vertically
        }

        val inflater = LayoutInflater.from(this)
        floatingView = inflater.inflate(R.layout.layout_floating_bubble, null)

        // UI references
        val centerHubButton = floatingView?.findViewById<ImageView>(R.id.img_center_hub)
        val expandedMenuLayout = floatingView?.findViewById<LinearLayout>(R.id.layout_radial_menu)
        val textPreview = floatingView?.findViewById<TextView>(R.id.txt_bubble_preview)
        val pulseRing = floatingView?.findViewById<View>(R.id.view_pulse_ring)

        // Action Hub Buttons
        val btnActionMic = floatingView?.findViewById<ImageView>(R.id.btn_action_mic)
        val btnActionTranslate = floatingView?.findViewById<ImageView>(R.id.btn_action_translate)
        val btnActionPrompt = floatingView?.findViewById<ImageView>(R.id.btn_action_prompt)
        val btnActionPolish = floatingView?.findViewById<ImageView>(R.id.btn_action_polish)
        val btnActionCopy = floatingView?.findViewById<ImageView>(R.id.btn_action_copy)
        val btnActionClose = floatingView?.findViewById<ImageView>(R.id.btn_action_close)

        // Touch & Drag Handling for the Persistent Floating Button
        centerHubButton?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDrag = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDrag = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 12 || Math.abs(dy) > 12) {
                            isDrag = true
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        try {
                            windowManager.updateViewLayout(floatingView, params)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDrag) {
                            // Single tap: Toggle multi-action hub menu
                            vibrateBriefly()
                            toggleHubMenu(expandedMenuLayout, textPreview)
                        }
                        return true
                    }
                }
                return false
            }
        })

        // Action 1: Voice Dictation (Mic)
        btnActionMic?.setOnClickListener {
            vibrateBriefly()
            toggleSpeechRecognition(textPreview, pulseRing)
        }

        // Action 2: 1-Click Smart Translation (Persian <-> English)
        btnActionTranslate?.setOnClickListener {
            vibrateBriefly()
            val textToProcess = currentExtractedText.ifBlank { textPreview?.text?.toString() ?: "" }
            if (textToProcess.isNotBlank()) {
                textPreview?.visibility = View.VISIBLE
                textPreview?.text = "در حال ترجمه هوشمند با هوش مصنوعی..."
                serviceScope.launch {
                    try {
                        val translated = withContext(Dispatchers.IO) {
                            geminiRepo.translateText(textToProcess)
                        }
                        currentExtractedText = translated
                        textPreview?.text = "🌐 ترجمه: $translated"
                        copyToClipboard(translated, "متن ترجمه‌شده کپی شد")
                    } catch (e: Exception) {
                        textPreview?.text = "خطا در ترجمه: ${e.localizedMessage}"
                    }
                }
            } else {
                Toast.makeText(this, "ابتدا دکمه میکروفون را بزنید و صحبت کنید", Toast.LENGTH_SHORT).show()
            }
        }

        // Action 3: AI Prompt Generator (Make Professional Prompt)
        btnActionPrompt?.setOnClickListener {
            vibrateBriefly()
            val textToProcess = currentExtractedText.ifBlank { textPreview?.text?.toString() ?: "" }
            if (textToProcess.isNotBlank()) {
                textPreview?.visibility = View.VISIBLE
                textPreview?.text = "در حال ساخت پرامپت مهندسی شده..."
                serviceScope.launch {
                    try {
                        val enhanced = withContext(Dispatchers.IO) {
                            geminiRepo.enhancePrompt(textToProcess, "general")
                        }
                        currentExtractedText = enhanced
                        textPreview?.text = "✨ پرامپت: $enhanced"
                        copyToClipboard(enhanced, "پرامپت هوشمند در کلیپ‌بورد کپی شد")
                    } catch (e: Exception) {
                        textPreview?.text = "خطا در ارتقای پرامپت: ${e.localizedMessage}"
                    }
                }
            } else {
                Toast.makeText(this, "ابتدا متن یا ایده خود را ضبط کنید", Toast.LENGTH_SHORT).show()
            }
        }

        // Action 4: Polish Tone / Grammar & Formality
        btnActionPolish?.setOnClickListener {
            vibrateBriefly()
            val textToProcess = currentExtractedText.ifBlank { textPreview?.text?.toString() ?: "" }
            if (textToProcess.isNotBlank()) {
                textPreview?.visibility = View.VISIBLE
                textPreview?.text = "در حال ویرایش و اصلاح متن..."
                serviceScope.launch {
                    try {
                        val polished = withContext(Dispatchers.IO) {
                            geminiRepo.polishText(textToProcess)
                        }
                        currentExtractedText = polished
                        textPreview?.text = "🪄 ویرایش شده: $polished"
                        copyToClipboard(polished, "متن ویرایش‌شده کپی شد")
                    } catch (e: Exception) {
                        textPreview?.text = "خطا در ویرایش متن: ${e.localizedMessage}"
                    }
                }
            } else {
                Toast.makeText(this, "متنی برای اصلاح وجود ندارد", Toast.LENGTH_SHORT).show()
            }
        }

        // Action 5: 1-Tap Copy to Clipboard
        btnActionCopy?.setOnClickListener {
            vibrateBriefly()
            val textToCopy = currentExtractedText.ifBlank { textPreview?.text?.toString() ?: "" }
            if (textToCopy.isNotBlank()) {
                copyToClipboard(textToCopy, "متن در کلیپ‌بورد کپی شد 📋")
            } else {
                Toast.makeText(this, "متنی برای کپی وجود ندارد", Toast.LENGTH_SHORT).show()
            }
        }

        // Action 6: Minimize / Close Menu
        btnActionClose?.setOnClickListener {
            vibrateBriefly()
            isMenuExpanded = false
            expandedMenuLayout?.visibility = View.GONE
            textPreview?.visibility = View.GONE
        }

        windowManager.addView(floatingView, params)
    }

    private fun toggleHubMenu(menuLayout: LinearLayout?, textPreview: TextView?) {
        isMenuExpanded = !isMenuExpanded
        if (isMenuExpanded) {
            menuLayout?.visibility = View.VISIBLE
            if (currentExtractedText.isNotBlank()) {
                textPreview?.visibility = View.VISIBLE
            }
        } else {
            menuLayout?.visibility = View.GONE
            textPreview?.visibility = View.GONE
        }
    }

    private fun toggleSpeechRecognition(textView: TextView?, pulseRing: View?) {
        if (speechManager.isListening.value) {
            speechManager.stopListening()
            pulseRing?.visibility = View.GONE
            Toast.makeText(this, "ضبط متوقف شد", Toast.LENGTH_SHORT).show()
        } else {
            speechManager.clearText()
            currentExtractedText = ""
            textView?.visibility = View.VISIBLE
            textView?.text = "در حال شنیدن صدای شما... صحبت کنید..."
            pulseRing?.visibility = View.VISIBLE
            speechManager.startListening()

            serviceScope.launch {
                speechManager.partialText.collect { partial ->
                    if (partial.isNotBlank()) {
                        textView?.text = partial
                    }
                }
            }

            serviceScope.launch {
                speechManager.finalText.collect { final ->
                    if (final.isNotBlank()) {
                        currentExtractedText = final
                        textView?.text = final
                        copyToClipboard(final, "گفتار به متن تبدیل و کپی شد 📋")
                    }
                }
            }
        }
    }

    private fun copyToClipboard(text: String, successToast: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("EchoScript Voice Hub", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, successToast, Toast.LENGTH_SHORT).show()
    }

    private fun vibrateBriefly() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (e: Exception) {
            // Ignore if vibration unavailable
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        speechManager.stopListening()
        floatingView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
