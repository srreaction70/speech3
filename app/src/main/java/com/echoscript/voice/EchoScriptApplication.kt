package com.echoscript.voice

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class EchoScriptApplication : Application() {
    companion object {
        const val CHANNEL_ID = "echoscript_floating_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "سرویس حباب شناور صوتی",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "نمایش حباب شناور تبدیل گفتار به متن بر روی سایر برنامه‌ها"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
