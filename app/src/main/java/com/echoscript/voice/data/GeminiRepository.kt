package com.echoscript.voice.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GeminiRequest(val contents: List<Content>)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class InlineData(val mimeType: String, val data: String)

@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
data class GeminiResponse(val candidates: List<Candidate>? = null)

@Serializable
data class Candidate(val content: Content? = null)

class GeminiRepository(private var apiKey: String = "") {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                encodeDefaults = false
            })
        }
    }

    fun setApiKey(key: String) {
        this.apiKey = key
    }

    /**
     * Transcribes full audio recording with Gemini 3.8 Flash.
     * High accuracy with punctuation, grammar, and Persian half-spaces.
     */
    suspend fun transcribeAudio(
        base64Audio: String,
        mimeType: String = "audio/mp4",
        targetLanguage: String = "fa-IR"
    ): String {
        val prompt = if (targetLanguage.startsWith("fa")) {
            "Transcribe the spoken Persian (فارسی) words from this recorded audio accurately. Include proper grammar, punctuation (commas, periods, question marks), and half-spaces (نیم‌فاصله). Output ONLY the raw transcribed text without introductory speech, quotes, or markdown wrappers."
        } else {
            "Transcribe the spoken words from this audio accurately into standard English with capitalization and punctuation. Output ONLY the raw transcribed text."
        }

        val parts = listOf(
            Part(inlineData = InlineData(mimeType = mimeType, data = base64Audio)),
            Part(text = prompt)
        )

        return callGeminiWithParts(parts)
    }

    suspend fun professionalizePrompt(rawIdea: String, style: String = "general"): String {
        val systemInstruction = """
            تبدیل ایده صوتی به پرامپت مهندسی‌شده و استاندارد هوش مصنوعی.
            خروجی را به صورت ساختاریافته شامل: نقش (Role)، وظیفه و دستورالعمل‌ها بنویس.
        """.trimIndent()

        val prompt = "$systemInstruction\nایده خام: $rawIdea"
        return callGeminiWithParts(listOf(Part(text = prompt)))
    }

    suspend fun translateText(text: String): String {
        val prompt = """
            If the following text is in Persian, translate it to clear natural English.
            If the text is in English, translate it to fluent Persian.
            Output ONLY the translated text without markdown or quotes.
            
            Text: $text
        """.trimIndent()
        return callGeminiWithParts(listOf(Part(text = prompt)))
    }

    suspend fun polishText(text: String): String {
        val prompt = """
            متن گفتاری زیر را ویرایش کن، غلط‌های املایی را اصلاح کرده و علائم نگارشی را اضافه کن.
            تنها متن تمیزشده نهایی را برگردان:
            $text
        """.trimIndent()
        return callGeminiWithParts(listOf(Part(text = prompt)))
    }

    private suspend fun callGeminiWithParts(parts: List<Part>): String {
        val key = apiKey.ifEmpty { System.getenv("GEMINI_API_KEY") ?: "" }
        if (key.isEmpty()) {
            return "کلید API تعریف نشده است. لطفاً در منوی تنظیمات اپلیکیشن، کلید Gemini API Key را وارد نمایید."
        }

        return try {
            val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$key") {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(listOf(Content(parts))))
            }.body()

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text?.trim()
            text ?: "پاسخی از مدل هوش مصنوعی دریافت نشد."
        } catch (e: Exception) {
            e.printStackTrace()
            "خطا در پردازش هوش مصنوعی: ${e.localizedMessage ?: "عدم برقراری ارتباط اینترنت"}"
        }
    }
}
