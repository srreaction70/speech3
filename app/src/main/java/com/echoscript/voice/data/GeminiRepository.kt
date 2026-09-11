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
data class Part(val text: String)

@Serializable
data class GeminiResponse(val candidates: List<Candidate>? = null)

@Serializable
data class Candidate(val content: Content? = null)

class GeminiRepository(private val apiKey: String = "") {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun professionalizePrompt(rawIdea: String, style: String = "general"): String {
        val systemInstruction = """
            تبدیل ایده صوتی به پرامپت مهندسی‌شده و استاندارد هوش مصنوعی.
            خروجی را به صورت ساختاریافته شامل: نقش (Role)، وظیفه و دستورالعمل‌ها بنویس.
        """.trimIndent()

        val prompt = "$systemInstruction\nایده خام: $rawIdea"
        return callGemini(prompt)
    }

    suspend fun translateText(text: String): String {
        val prompt = """
            If the following text is in Persian, translate it to clear natural English.
            If the text is in English, translate it to fluent Persian.
            Output ONLY the translated text without markdown or quotes.
            
            Text: $text
        """.trimIndent()
        return callGemini(prompt)
    }

    suspend fun polishText(text: String): String {
        val prompt = """
            متن گفتاری زیر را ویرایش کن، غلط‌های املایی را اصلاح کرده و علائم نگارشی را اضافه کن.
            تنها متن تمیزشده نهایی را برگردان:
            $text
        """.trimIndent()
        return callGemini(prompt)
    }

    private suspend fun callGemini(prompt: String): String {
        val key = apiKey.ifEmpty { System.getenv("GEMINI_API_KEY") ?: "" }
        if (key.isEmpty()) {
            return prompt // Fallback if API key is not yet set
        }

        return try {
            val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$key") {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(listOf(Content(listOf(Part(prompt))))))
            }.body()

            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: prompt
        } catch (e: Exception) {
            e.printStackTrace()
            prompt
        }
    }
}
